/*
 * Copyright 2017 - 2018 Anton Tananaev (anton )
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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.hazelcast.BaseFrameDecoder;

public class WatchFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ']') + 1;
        if (endIndex > 0) {
            ByteBuf frame = Unpooled.buffer();
            while (buf.readerIndex() < endIndex) {
                byte b1 = buf.readByte();
                if (b1 == '}') {
                    byte b2 = buf.readByte();
                    switch (b2) {
                        case 0x01:
                            frame.writeByte('}');
                            break;
                        case 0x02:
                            frame.writeByte('[');
                            break;
                        case 0x03:
                            frame.writeByte(']');
                            break;
                        case 0x04:
                            frame.writeByte(',');
                            break;
                        case 0x05:
                            frame.writeByte('*');
                            break;
                        default:
                            throw new IllegalArgumentException(String.format(
                                    "unexpected byte at %d: 0x%02x", buf.readerIndex() - 1, b2));
                    }
                } else {
                    frame.writeByte(b1);
                }
            }
            return frame;
        }

        return null;
    }

}
