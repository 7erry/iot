/*
 * Copyright 2015 Irving Gonzalez
 * Copyright 2015 - 2019 Anton Tananaev (anton )
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hazelcast.protocol;

import org.hazelcast.StringProtocolEncoder;
import org.hazelcast.model.Command;
import org.hazelcast.Protocol;

public class TotemProtocolEncoder extends StringProtocolEncoder {

    public TotemProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "000000");

        switch (command.getType()) {
            // Assuming PIN 8 (Output C) is the power wire, like manual says but it can be PIN 5,7,8
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "*%s,025,C,1#", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "*%s,025,C,0#", Command.KEY_DEVICE_PASSWORD);
            default:
                return null;
        }
    }

}
