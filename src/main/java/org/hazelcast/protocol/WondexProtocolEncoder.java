/*
 * Copyright 2016 - 2019 Anton Tananaev (anton )
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

public class WondexProtocolEncoder extends StringProtocolEncoder {

    public WondexProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "0000");

        switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, "$WP+REBOOT=%s", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_GET_DEVICE_STATUS:
                return formatCommand(command, "$WP+TEST=%s", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_GET_MODEM_STATUS:
                return formatCommand(command, "$WP+GSMINFO=%s", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_IDENTIFICATION:
                return formatCommand(command, "$WP+IMEI=%s", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "$WP+GETLOCATION=%s", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_GET_VERSION:
                return formatCommand(command, "$WP+VER=%s", Command.KEY_DEVICE_PASSWORD);
            default:
                return null;
        }
    }

}
