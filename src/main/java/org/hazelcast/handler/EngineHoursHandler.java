/*
 * Copyright 2018 - 2019 Anton Tananaev (anton@traccar.org)
 * Copyright 2018 Andrey Kunitsyn (andrey@traccar.org)
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
package org.hazelcast.handler;

import io.netty.channel.ChannelHandler;
import org.hazelcast.BaseDataHandler;
import org.hazelcast.database.IdentityManager;
import org.hazelcast.model.Position;

@ChannelHandler.Sharable
public class EngineHoursHandler extends BaseDataHandler {

    private final IdentityManager identityManager;

    public EngineHoursHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Position handlePosition(Position position) {
        if (!position.getAttributes().containsKey(Position.KEY_HOURS)) {
            Position last = identityManager.getLastPosition(position.getDeviceId());
            if (last != null) {
                long hours = last.getLong(Position.KEY_HOURS);
                if (last.getBoolean(Position.KEY_IGNITION) && position.getBoolean(Position.KEY_IGNITION)) {
                    hours += position.getFixTime().getTime() - last.getFixTime().getTime();
                }
                if (hours != 0) {
                    position.set(Position.KEY_HOURS, hours);
                }
            }
        }
        return position;
    }

}
