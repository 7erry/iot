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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.hazelcast.BaseProtocolEncoder;
import org.hazelcast.model.Command;
import org.hazelcast.Protocol;

import java.nio.charset.StandardCharsets;

public class NoranProtocolEncoder extends BaseProtocolEncoder {

    public NoranProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeContent(String content) {

        ByteBuf buf = Unpooled.buffer(12 + 56);

        buf.writeCharSequence("\r\n*KW", StandardCharsets.US_ASCII);
        buf.writeByte(0);
        buf.writeShortLE(buf.capacity());
        buf.writeShortLE(NoranProtocolDecoder.MSG_CONTROL);
        buf.writeInt(0); // gis ip
        buf.writeShortLE(0); // gis port
        buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
        buf.writerIndex(buf.writerIndex() + 50 - content.length());
        buf.writeCharSequence("\r\n", StandardCharsets.US_ASCII);

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_POSITION_SINGLE:
                return encodeContent("*KW,000,000,000000#");
            case Command.TYPE_POSITION_PERIODIC:
                int interval = command.getInteger(Command.KEY_FREQUENCY);
                return encodeContent("*KW,000,002,000000," + interval + "#");
            case Command.TYPE_POSITION_STOP:
                return encodeContent("*KW,000,002,000000,0#");
            case Command.TYPE_ENGINE_STOP:
                return encodeContent("*KW,000,007,000000,0#");
            case Command.TYPE_ENGINE_RESUME:
                return encodeContent("*KW,000,007,000000,1#");
            default:
                return null;
        }
    }

}
