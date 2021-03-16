/*
 * Copyright 2016 - 2019 Anton Tananaev (anton )
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
package org.hazelcast.handler.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.hazelcast.config.Config;
import org.hazelcast.config.Keys;
import org.hazelcast.database.IdentityManager;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

@ChannelHandler.Sharable
public class AlertEventHandler extends BaseEventHandler {

    private final IdentityManager identityManager;
    private final boolean ignoreDuplicateAlerts;

    public AlertEventHandler(Config config, IdentityManager identityManager) {
        this.identityManager = identityManager;
        ignoreDuplicateAlerts = config.getBoolean(Keys.EVENT_IGNORE_DUPLICATE_ALERTS);
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Object alarm = position.getAttributes().get(Position.KEY_ALARM);
        if (alarm != null) {
            boolean ignoreAlert = false;
            if (ignoreDuplicateAlerts) {
                Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
                if (lastPosition != null && alarm.equals(lastPosition.getAttributes().get(Position.KEY_ALARM))) {
                    ignoreAlert = true;
                }
            }
            if (!ignoreAlert) {
                Event event = new Event(Event.TYPE_ALARM, position.getDeviceId(), position.getId());
                event.set(Position.KEY_ALARM, (String) alarm);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }

}
