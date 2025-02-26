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

import org.hazelcast.BaseProtocol;
import org.hazelcast.PipelineBuilder;
import org.hazelcast.HazelcastIoTServer;
import org.hazelcast.model.Command;

import io.netty.handler.codec.string.StringEncoder;

public class Gl200Protocol extends BaseProtocol {

    public Gl200Protocol() {
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_REBOOT_DEVICE);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Gl200FrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Gl200ProtocolEncoder(Gl200Protocol.this));
                pipeline.addLast(new Gl200ProtocolDecoder(Gl200Protocol.this));
            }
        });
        addServer(new HazelcastIoTServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Gl200ProtocolEncoder(Gl200Protocol.this));
                pipeline.addLast(new Gl200ProtocolDecoder(Gl200Protocol.this));
            }
        });
    }

}
