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
package org.hazelcast.protocol;

import java.util.TimeZone;

import org.hazelcast.StringProtocolEncoder;
import org.hazelcast.model.Command;
import org.hazelcast.Protocol;

public class Jt600ProtocolEncoder extends StringProtocolEncoder {

    public Jt600ProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return "(S07,0)";
            case Command.TYPE_ENGINE_RESUME:
                return "(S07,1)";
            case Command.TYPE_SET_TIMEZONE:
                int offset = TimeZone.getTimeZone(command.getString(Command.KEY_TIMEZONE)).getRawOffset() / 60000;
                return "(S09,1," + offset + ")";
            case Command.TYPE_REBOOT_DEVICE:
                return "(S17)";
            default:
                return null;
        }
    }

}
