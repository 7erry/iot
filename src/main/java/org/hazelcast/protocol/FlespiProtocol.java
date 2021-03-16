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

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.hazelcast.BaseProtocol;
import org.hazelcast.PipelineBuilder;
import org.hazelcast.HazelcastIoTServer;

public class FlespiProtocol extends BaseProtocol {

    public FlespiProtocol() {
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new HttpRequestDecoder(4096, 8192, 128 * 1024));
                pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                pipeline.addLast(new FlespiProtocolDecoder(FlespiProtocol.this));
            }
        });
    }
}
