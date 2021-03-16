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

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.hazelcast.BaseProtocol;
import org.hazelcast.PipelineBuilder;
import org.hazelcast.HazelcastIoTServer;
import org.hazelcast.model.Command;

public class RuptelaProtocol extends BaseProtocol {

    public RuptelaProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_REQUEST_PHOTO,
                Command.TYPE_CONFIGURATION,
                Command.TYPE_GET_VERSION,
                Command.TYPE_FIRMWARE_UPDATE,
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_SET_CONNECTION,
                Command.TYPE_SET_ODOMETER);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 2, 0));
                pipeline.addLast(new RuptelaProtocolEncoder(RuptelaProtocol.this));
                pipeline.addLast(new RuptelaProtocolDecoder(RuptelaProtocol.this));
            }
        });
    }

}
