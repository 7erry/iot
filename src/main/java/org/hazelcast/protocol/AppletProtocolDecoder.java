/*
 * Copyright 2018 Anton Tananaev (anton )
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

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.hazelcast.BaseHttpProtocolDecoder;
import org.hazelcast.DeviceSession;
import org.hazelcast.Protocol;

import java.net.SocketAddress;

public class AppletProtocolDecoder extends BaseHttpProtocolDecoder {

    public AppletProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, request.headers().get("From"));
        if (deviceSession != null) {
            sendResponse(channel, HttpResponseStatus.OK);
        } else {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
        }

        return null;
    }

}
