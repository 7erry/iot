/*
 * Copyright 2015 - 2020 Anton Tananaev (anton )
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

public class WondexProtocol extends BaseProtocol {

    public WondexProtocol() {
        setSupportedDataCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_GET_MODEM_STATUS,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_GET_VERSION,
                Command.TYPE_IDENTIFICATION);
        setTextCommandEncoder(new WondexProtocolEncoder(this));
        setSupportedTextCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_GET_MODEM_STATUS,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_GET_VERSION,
                Command.TYPE_IDENTIFICATION);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new WondexFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new WondexProtocolEncoder(WondexProtocol.this));
                pipeline.addLast(new WondexProtocolDecoder(WondexProtocol.this));
            }
        });
        addServer(new HazelcastIoTServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new WondexProtocolEncoder(WondexProtocol.this));
                pipeline.addLast(new WondexProtocolDecoder(WondexProtocol.this));
            }
        });
    }

}
