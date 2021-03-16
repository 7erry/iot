/*
 * Copyright 2016 - 2020 Anton Tananaev (anton )
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hazelcast.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.config.Config;
import org.hazelcast.config.Keys;
import org.hazelcast.helper.DateUtil;
import org.hazelcast.model.Statistics;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsManager.class);

    private static final int SPLIT_MODE = Calendar.DAY_OF_MONTH;

    private final Config config;
    private final DataManager dataManager;
    private final Client client;
    private final ObjectMapper objectMapper;

    private final AtomicInteger lastUpdate = new AtomicInteger(Calendar.getInstance().get(SPLIT_MODE));

    private final Set<Long> users = new HashSet<>();
    private final Map<Long, String> deviceProtocols = new HashMap<>();

    private int requests;
    private int messagesReceived;
    private int messagesStored;
    private int mailSent;
    private int smsSent;
    private int geocoderRequests;
    private int geolocationRequests;

    @Inject
    public StatisticsManager(Config config, DataManager dataManager, Client client, ObjectMapper objectMapper) {
        this.config = config;
        this.dataManager = dataManager;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    private void checkSplit() {
        int currentUpdate = Calendar.getInstance().get(SPLIT_MODE);
        if (lastUpdate.getAndSet(currentUpdate) != currentUpdate) {
            Statistics statistics = new Statistics();

            synchronized (this) {
                statistics.setCaptureTime(new Date());
                statistics.setActiveUsers(users.size());
                statistics.setActiveDevices(deviceProtocols.size());
                statistics.setRequests(requests);
                statistics.setMessagesReceived(messagesReceived);
                statistics.setMessagesStored(messagesStored);
                statistics.setMailSent(mailSent);
                statistics.setSmsSent(smsSent);
                statistics.setGeocoderRequests(geocoderRequests);
                statistics.setGeolocationRequests(geolocationRequests);
                if (!deviceProtocols.isEmpty()) {
                    Map<String, Integer> protocols = new HashMap<>();
                    for (String protocol : deviceProtocols.values()) {
                        protocols.compute(protocol, (key, count) -> count != null ? count + 1 : 1);
                    }
                    statistics.setProtocols(protocols);
                }

                users.clear();
                deviceProtocols.clear();
                requests = 0;
                messagesReceived = 0;
                messagesStored = 0;
                mailSent = 0;
                smsSent = 0;
                geocoderRequests = 0;
                geolocationRequests = 0;
            }

            try {
                dataManager.addObject(statistics);
            } catch (SQLException e) {
                LOGGER.warn("Error saving statistics", e);
            }

            String url = config.getString(Keys.SERVER_STATISTICS);
            if (url != null) {
                String time = DateUtil.formatDate(statistics.getCaptureTime());

                Form form = new Form();
                form.param("version", getClass().getPackage().getImplementationVersion());
                form.param("captureTime", time);
                form.param("activeUsers", String.valueOf(statistics.getActiveUsers()));
                form.param("activeDevices", String.valueOf(statistics.getActiveDevices()));
                form.param("requests", String.valueOf(statistics.getRequests()));
                form.param("messagesReceived", String.valueOf(statistics.getMessagesReceived()));
                form.param("messagesStored", String.valueOf(statistics.getMessagesStored()));
                form.param("mailSent", String.valueOf(statistics.getMailSent()));
                form.param("smsSent", String.valueOf(statistics.getSmsSent()));
                form.param("geocoderRequests", String.valueOf(statistics.getGeocoderRequests()));
                form.param("geolocationRequests", String.valueOf(statistics.getGeolocationRequests()));
                if (statistics.getProtocols() != null) {
                    try {
                        form.param("protocols", objectMapper.writeValueAsString(statistics.getProtocols()));
                    } catch (JsonProcessingException e) {
                        LOGGER.warn("Failed to serialize protocols", e);
                    }
                }

                client.target(url).request().async().post(Entity.form(form));
            }
        }
    }

    public synchronized void registerRequest(long userId) {
        checkSplit();
        requests += 1;
        if (userId != 0) {
            users.add(userId);
        }
    }

    public synchronized void registerMessageReceived() {
        checkSplit();
        messagesReceived += 1;
    }

    public synchronized void registerMessageStored(long deviceId, String protocol) {
        checkSplit();
        messagesStored += 1;
        if (deviceId != 0) {
            deviceProtocols.put(deviceId, protocol);
        }
    }

    public synchronized void registerMail() {
        checkSplit();
        mailSent += 1;
    }

    public synchronized void registerSms() {
        checkSplit();
        smsSent += 1;
    }

    public synchronized void registerGeocoderRequest() {
        checkSplit();
        geocoderRequests += 1;
    }

    public synchronized void registerGeolocationRequest() {
        checkSplit();
        geolocationRequests += 1;
    }

}
