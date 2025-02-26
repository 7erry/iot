/*
 * Copyright 2017 Christoph Krey (c@ckrey.de)
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
import org.hazelcast.PipelineBuilder;
import org.hazelcast.HazelcastIoTServer;
import org.hazelcast.model.Command;

public class Tk103Protocol extends BaseProtocol {

    public Tk103Protocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_MODE_DEEP_SLEEP,
                Command.TYPE_MODE_POWER_SAVING,
                Command.TYPE_ALARM_SOS,
                Command.TYPE_SET_CONNECTION,
                Command.TYPE_SOS_NUMBER,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_POSITION_STOP,
                Command.TYPE_GET_VERSION,
                Command.TYPE_POWER_OFF,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_SET_ODOMETER,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_OUTPUT_CONTROL);
        addServer(new HazelcastIoTServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Tk103FrameDecoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Tk103ProtocolEncoder(Tk103Protocol.this));
                pipeline.addLast(new Tk103ProtocolDecoder(Tk103Protocol.this));
            }
        });
        addServer(new HazelcastIoTServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Tk103ProtocolEncoder(Tk103Protocol.this));
                pipeline.addLast(new Tk103ProtocolDecoder(Tk103Protocol.this));
            }
        });
    }

}
