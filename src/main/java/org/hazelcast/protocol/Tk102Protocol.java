/*
 * Copyright 2015 - 2018 Anton Tananaev (anton@traccar.org)
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
import org.hazelcast.TrackerServer;

public class Tk102Protocol extends BaseProtocol {

    public Tk102Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 1 + 1 + 10, 1, 1, 0));
                pipeline.addLast(new Tk102ProtocolDecoder(Tk102Protocol.this));
            }
        });
    }

}