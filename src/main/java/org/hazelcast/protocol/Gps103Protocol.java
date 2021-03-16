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

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.hazelcast.BaseProtocol;
import org.hazelcast.CharacterDelimiterFrameDecoder;
import org.hazelcast.PipelineBuilder;
import org.hazelcast.HazelcastIoTServer;
import org.hazelcast.model.Command;

public class Gps103Protocol extends BaseProtocol {

    public Gps103Protocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_POSITION_STOP,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM,
                Command.TYPE_REQUEST_PHOTO);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(2048, false, "\r\n", "\n", ";", "*"));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new Gps103ProtocolEncoder(Gps103Protocol.this));
                pipeline.addLast(new Gps103ProtocolDecoder(Gps103Protocol.this));
            }
        });
        addServer(new HazelcastIoTServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new Gps103ProtocolEncoder(Gps103Protocol.this));
                pipeline.addLast(new Gps103ProtocolDecoder(Gps103Protocol.this));
            }
        });
    }

}
