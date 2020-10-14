/*
 * Copyright 2017 - 2019 Anton Tananaev (anton@traccar.org)
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

import io.netty.channel.ChannelHandler;
import org.hazelcast.database.IdentityManager;
import org.hazelcast.model.Device;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

import java.util.Collections;
import java.util.Map;

@ChannelHandler.Sharable
public class FuelDropEventHandler extends BaseEventHandler {

    public static final String ATTRIBUTE_FUEL_DROP_THRESHOLD = "fuelDropThreshold";

    private final IdentityManager identityManager;

    public FuelDropEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {

        Device device = identityManager.getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!identityManager.isLatestPosition(position)) {
            return null;
        }

        double fuelDropThreshold = identityManager
                .lookupAttributeDouble(device.getId(), ATTRIBUTE_FUEL_DROP_THRESHOLD, 0, true, false);

        if (fuelDropThreshold > 0) {
            Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
            if (position.getAttributes().containsKey(Position.KEY_FUEL_LEVEL)
                    && lastPosition != null && lastPosition.getAttributes().containsKey(Position.KEY_FUEL_LEVEL)) {

                double drop = lastPosition.getDouble(Position.KEY_FUEL_LEVEL)
                        - position.getDouble(Position.KEY_FUEL_LEVEL);
                if (drop >= fuelDropThreshold) {
                    Event event = new Event(Event.TYPE_DEVICE_FUEL_DROP, position.getDeviceId(), position.getId());
                    event.set(ATTRIBUTE_FUEL_DROP_THRESHOLD, fuelDropThreshold);
                    return Collections.singletonMap(event, position);
                }
            }
        }

        return null;
    }

}
