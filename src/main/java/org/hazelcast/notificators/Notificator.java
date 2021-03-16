/*
 * Copyright 2018 Anton Tananaev (anton )
 * Copyright 2018 Andrey Kunitsyn (andrey )
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
package org.hazelcast.notificators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;
import org.hazelcast.notification.MessageException;

import java.net.MalformedURLException;
import java.net.ProtocolException;

public abstract class Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notificator.class);

    public void sendAsync(final long userId, final Event event, final Position position) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    sendSync(userId, event, position);
                } catch (MessageException | InterruptedException error) {
                    LOGGER.warn("Event send error", error);
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public abstract void sendSync(long userId, Event event, Position position)
            throws MessageException, InterruptedException, ProtocolException, MalformedURLException;

}
