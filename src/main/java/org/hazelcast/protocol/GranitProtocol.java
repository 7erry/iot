/*
 * Copyright 2016 - 2019 Anton Tananaev (anton )
 * Copyright 2017 - 2018 Andrey Kunitsyn (andrey )
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

public class GranitProtocol extends BaseProtocol {

    public GranitProtocol() {
        setSupportedDataCommands(
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE);
        setTextCommandEncoder(new GranitProtocolSmsEncoder(this));
        setSupportedTextCommands(
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_PERIODIC);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new GranitFrameDecoder());
                pipeline.addLast(new GranitProtocolEncoder(GranitProtocol.this));
                pipeline.addLast(new GranitProtocolDecoder(GranitProtocol.this));
            }
        });
    }

}
