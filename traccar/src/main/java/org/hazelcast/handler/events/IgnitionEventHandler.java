/*
 * Copyright 2016 - 2019 Anton Tananaev (anton@traccar.org)
 * Copyright 2016 Andrey Kunitsyn (andrey@traccar.org)
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
import org.hazelcast.database.IdentityManager;
import org.hazelcast.model.Device;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

@ChannelHandler.Sharable
public class IgnitionEventHandler extends BaseEventHandler {

    private final IdentityManager identityManager;

    public IgnitionEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = identityManager.getById(position.getDeviceId());
        if (device == null || !identityManager.isLatestPosition(position)) {
            return null;
        }

        Map<Event, Position> result = null;

        if (position.getAttributes().containsKey(Position.KEY_IGNITION)) {
            boolean ignition = position.getBoolean(Position.KEY_IGNITION);

            Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
            if (lastPosition != null && lastPosition.getAttributes().containsKey(Position.KEY_IGNITION)) {
                boolean oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);

                if (ignition && !oldIgnition) {
                    result = Collections.singletonMap(
                            new Event(Event.TYPE_IGNITION_ON, position.getDeviceId(), position.getId()), position);
                } else if (!ignition && oldIgnition) {
                    result = Collections.singletonMap(
                            new Event(Event.TYPE_IGNITION_OFF, position.getDeviceId(), position.getId()), position);
                }
            }
        }
        return result;
    }

}
