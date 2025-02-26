/*
 * Copyright 2015 - 2019 Anton Tananaev (anton )
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

import io.netty.channel.Channel;
import org.hazelcast.BasePipelineFactory;
import org.hazelcast.StringProtocolEncoder;
import org.hazelcast.model.Command;
import org.hazelcast.Protocol;

public class SuntechProtocolEncoder extends StringProtocolEncoder {

    public SuntechProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private String getPrefix(Channel channel) {
        String prefix = "SA200CMD";
        if (channel != null) {
            SuntechProtocolDecoder protocolDecoder =
                    BasePipelineFactory.getHandler(channel.pipeline(), SuntechProtocolDecoder.class);
            if (protocolDecoder != null) {
                String decoderPrefix = protocolDecoder.getPrefix();
                if (decoderPrefix != null && decoderPrefix.length() > 5) {
                    prefix = decoderPrefix.substring(0, decoderPrefix.length() - 3) + "CMD";
                }
            }
        }
        return prefix;
    }

    @Override
    protected Object encodeCommand(Channel channel, Command command) {

        String prefix = getPrefix(channel);

        switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, prefix + ";%s;02;Reboot\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, prefix + ";%s;02;\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_OUTPUT_CONTROL:
                if (command.getAttributes().containsKey(Command.KEY_DATA)) {
                    if (command.getAttributes().get(Command.KEY_DATA).equals("1")) {
                        return formatCommand(command, prefix + ";%s;02;Enable%s\r",
                                Command.KEY_UNIQUE_ID, Command.KEY_INDEX);
                    } else {
                        return formatCommand(command, prefix + ";%s;02;Disable%s\r",
                                Command.KEY_UNIQUE_ID, Command.KEY_INDEX);
                    }
                }
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, prefix + ";%s;02;Enable1\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, prefix + ";%s;02;Disable1\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_ARM:
                return formatCommand(command, prefix + ";%s;02;Enable2\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(command, prefix + ";%s;02;Disable2\r", Command.KEY_UNIQUE_ID);
            default:
                return null;
        }
    }

}
