/*
 * Copyright 2015 - 2019 Anton Tananaev (anton@traccar.org)
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
package org.hazelcast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.config.Config;
import org.hazelcast.database.AttributesManager;
import org.hazelcast.database.BaseObjectManager;
import org.hazelcast.database.CalendarManager;
import org.hazelcast.database.CommandsManager;
import org.hazelcast.database.ConnectionManager;
import org.hazelcast.database.DataManager;
import org.hazelcast.database.DeviceManager;
import org.hazelcast.database.DriversManager;
import org.hazelcast.database.GeofenceManager;
import org.hazelcast.database.GroupsManager;
import org.hazelcast.database.IdentityManager;
import org.hazelcast.database.LdapProvider;
import org.hazelcast.database.MailManager;
import org.hazelcast.database.MaintenancesManager;
import org.hazelcast.database.MediaManager;
import org.hazelcast.database.NotificationManager;
import org.hazelcast.database.PermissionsManager;
import org.hazelcast.schedule.ScheduleManager;
import org.hazelcast.database.UsersManager;
import org.hazelcast.geocoder.Geocoder;
import org.hazelcast.helper.Log;
import org.hazelcast.helper.SanitizerModule;
import org.hazelcast.model.Attribute;
import org.hazelcast.model.BaseModel;
import org.hazelcast.model.Calendar;
import org.hazelcast.model.Command;
import org.hazelcast.model.Device;
import org.hazelcast.model.Driver;
import org.hazelcast.model.Geofence;
import org.hazelcast.model.Group;
import org.hazelcast.model.Maintenance;
import org.hazelcast.model.Notification;
import org.hazelcast.model.User;
import org.hazelcast.notification.EventForwarder;
import org.hazelcast.notification.JsonTypeEventForwarder;
import org.hazelcast.notification.NotificatorManager;
import org.hazelcast.reports.model.TripsConfig;
import org.hazelcast.sms.SmsManager;
import org.hazelcast.sms.smpp.SmppClient;
import org.hazelcast.web.WebServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.ContextResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public final class Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private Context() {
    }

    private static Config config;

    public static Config getConfig() {
        return config;
    }

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static IdentityManager identityManager;

    public static IdentityManager getIdentityManager() {
        return identityManager;
    }

    private static DataManager dataManager;

    public static DataManager getDataManager() {
        return dataManager;
    }

    private static LdapProvider ldapProvider;

    public static LdapProvider getLdapProvider() {
        return ldapProvider;
    }

    private static MailManager mailManager;

    public static MailManager getMailManager() {
        return mailManager;
    }

    private static MediaManager mediaManager;

    public static MediaManager getMediaManager() {
        return mediaManager;
    }

    private static UsersManager usersManager;

    public static UsersManager getUsersManager() {
        return usersManager;
    }

    private static GroupsManager groupsManager;

    public static GroupsManager getGroupsManager() {
        return groupsManager;
    }

    private static DeviceManager deviceManager;

    public static DeviceManager getDeviceManager() {
        return deviceManager;
    }

    private static ConnectionManager connectionManager;

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private static PermissionsManager permissionsManager;

    public static PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public static Geocoder getGeocoder() {
        return Main.getInjector() != null ? Main.getInjector().getInstance(Geocoder.class) : null;
    }

    private static WebServer webServer;

    public static WebServer getWebServer() {
        return webServer;
    }

    private static ServerManager serverManager;

    public static ServerManager getServerManager() {
        return serverManager;
    }

    private static ScheduleManager scheduleManager;

    public static ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    private static GeofenceManager geofenceManager;

    public static GeofenceManager getGeofenceManager() {
        return geofenceManager;
    }

    private static CalendarManager calendarManager;

    public static CalendarManager getCalendarManager() {
        return calendarManager;
    }

    private static NotificationManager notificationManager;

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    private static NotificatorManager notificatorManager;

    public static NotificatorManager getNotificatorManager() {
        return notificatorManager;
    }

    private static VelocityEngine velocityEngine;

    public static VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    private static Client client = ClientBuilder.newClient();

    public static Client getClient() {
        return client;
    }

    private static EventForwarder eventForwarder;

    public static EventForwarder getEventForwarder() {
        return eventForwarder;
    }

    private static AttributesManager attributesManager;

    public static AttributesManager getAttributesManager() {
        return attributesManager;
    }

    private static DriversManager driversManager;

    public static DriversManager getDriversManager() {
        return driversManager;
    }

    private static CommandsManager commandsManager;

    public static CommandsManager getCommandsManager() {
        return commandsManager;
    }

    private static MaintenancesManager maintenancesManager;

    public static MaintenancesManager getMaintenancesManager() {
        return maintenancesManager;
    }

    private static SmsManager smsManager;

    public static SmsManager getSmsManager() {
        return smsManager;
    }

    private static TripsConfig tripsConfig;

    public static TripsConfig getTripsConfig() {
        return tripsConfig;
    }

    public static TripsConfig initTripsConfig() {
        return new TripsConfig(
                config.getLong("report.trip.minimalTripDistance", 500),
                config.getLong("report.trip.minimalTripDuration", 300) * 1000,
                config.getLong("report.trip.minimalParkingDuration", 300) * 1000,
                config.getLong("report.trip.minimalNoDataDuration", 3600) * 1000,
                config.getBoolean("report.trip.useIgnition"),
                config.getBoolean("event.motion.processInvalidPositions"),
                config.getDouble("event.motion.speedThreshold", 0.01));
    }

    private static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> clazz) {
            return objectMapper;
        }

    }

    public static void init(String configFile) throws Exception {

        try {
            config = new Config(configFile);
        } catch (Exception e) {
            config = new Config();
            Log.setupDefaultLogger();
            throw e;
        }

        if (config.getBoolean("logger.enable")) {
            Log.setupLogger(config);
        }

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SanitizerModule());
        objectMapper.registerModule(new JSR353Module());
        objectMapper.setConfig(
                objectMapper.getSerializationConfig().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        if (Context.getConfig().getBoolean("mapper.prettyPrintedJson")) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        client = ClientBuilder.newClient().register(new ObjectMapperContextResolver());

        if (config.hasKey("database.url")) {
            dataManager = new DataManager(config);
        }

        if (config.getBoolean("ldap.enable")) {
            ldapProvider = new LdapProvider(config);
        }

        mailManager = new MailManager();

        mediaManager = new MediaManager(config.getString("media.path"));

        if (dataManager != null) {
            usersManager = new UsersManager(dataManager);
            groupsManager = new GroupsManager(dataManager);
            deviceManager = new DeviceManager(dataManager);
        }

        identityManager = deviceManager;

        if (config.getBoolean("web.enable")) {
            webServer = new WebServer(config);
        }

        permissionsManager = new PermissionsManager(dataManager, usersManager);

        connectionManager = new ConnectionManager();

        tripsConfig = initTripsConfig();

        if (config.getBoolean("sms.enable")) {
            final String smsManagerClass = config.getString("sms.manager.class", SmppClient.class.getCanonicalName());
            try {
                smsManager = (SmsManager) Class.forName(smsManagerClass).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.warn("Error loading SMS Manager class : " + smsManagerClass, e);
            }
        }

        if (config.getBoolean("event.enable")) {
            initEventsModule();
        }

        serverManager = new ServerManager();
        scheduleManager = new ScheduleManager();

        if (config.getBoolean("event.forward.enable")) {
            eventForwarder = new JsonTypeEventForwarder();
        }

        attributesManager = new AttributesManager(dataManager);

        driversManager = new DriversManager(dataManager);

        commandsManager = new CommandsManager(dataManager, config.getBoolean("commands.queueing"));

    }

    private static void initEventsModule() {

        geofenceManager = new GeofenceManager(dataManager);
        calendarManager = new CalendarManager(dataManager);
        maintenancesManager = new MaintenancesManager(dataManager);
        notificationManager = new NotificationManager(dataManager);
        notificatorManager = new NotificatorManager();
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("file.resource.loader.path",
                Context.getConfig().getString("templates.rootPath", "templates") + "/");
        velocityProperties.setProperty("runtime.log.logsystem.class",
                "org.apache.velocity.runtime.log.NullLogChute");

        String address;
        try {
            address = config.getString("web.address", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            address = "localhost";
        }

        String webUrl = URIUtil.newURI("http", address, config.getInteger("web.port", 80), "", "");
        webUrl = Context.getConfig().getString("web.url", webUrl);
        velocityProperties.setProperty("web.url", webUrl);

        velocityEngine = new VelocityEngine();
        velocityEngine.init(velocityProperties);
    }

    public static void init(IdentityManager testIdentityManager, MediaManager testMediaManager) {
        config = new Config();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR353Module());
        client = ClientBuilder.newClient().register(new ObjectMapperContextResolver());
        identityManager = testIdentityManager;
        mediaManager = testMediaManager;
    }

    public static <T extends BaseModel> BaseObjectManager<T> getManager(Class<T> clazz) {
        if (clazz.equals(Device.class)) {
            return (BaseObjectManager<T>) deviceManager;
        } else if (clazz.equals(Group.class)) {
            return (BaseObjectManager<T>) groupsManager;
        } else if (clazz.equals(User.class)) {
            return (BaseObjectManager<T>) usersManager;
        } else if (clazz.equals(Calendar.class)) {
            return (BaseObjectManager<T>) calendarManager;
        } else if (clazz.equals(Attribute.class)) {
            return (BaseObjectManager<T>) attributesManager;
        } else if (clazz.equals(Geofence.class)) {
            return (BaseObjectManager<T>) geofenceManager;
        } else if (clazz.equals(Driver.class)) {
            return (BaseObjectManager<T>) driversManager;
        } else if (clazz.equals(Command.class)) {
            return (BaseObjectManager<T>) commandsManager;
        } else if (clazz.equals(Maintenance.class)) {
            return (BaseObjectManager<T>) maintenancesManager;
        } else if (clazz.equals(Notification.class)) {
            return (BaseObjectManager<T>) notificationManager;
        }
        return null;
    }

}
