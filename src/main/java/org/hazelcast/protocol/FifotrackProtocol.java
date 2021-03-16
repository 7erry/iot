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

import io.netty.handler.codec.string.StringEncoder;
import org.hazelcast.BaseProtocol;
import org.hazelcast.PipelineBuilder;
import org.hazelcast.HazelcastIoTServer;
import org.hazelcast.model.Command;

public class FifotrackProtocol extends BaseProtocol {

    public FifotrackProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_REQUEST_PHOTO);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new FifotrackFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new FifotrackProtocolEncoder(FifotrackProtocol.this));
                pipeline.addLast(new FifotrackProtocolDecoder(FifotrackProtocol.this));
            }
        });
    }

}
