/*
 * Copyright 2019 - 2020 Anton Tananaev (anton )
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
import org.hazelcast.BaseProtocolDecoder;
import org.hazelcast.DeviceSession;
import org.hazelcast.NetworkMessage;
import org.hazelcast.Protocol;
import org.hazelcast.helper.BitUtil;
import org.hazelcast.helper.Parser;
import org.hazelcast.helper.PatternBuilder;
import org.hazelcast.helper.UnitsConverter;
import org.hazelcast.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class RstProtocolDecoder extends BaseProtocolDecoder {

    public RstProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("RST;")
            .expression("([AL]);")               // archive
            .expression("([^,]+);")              // model
            .expression("(.{5});")               // firmware
            .number("(d{9});")                   // serial number
            .number("(d+);")                     // index
            .number("(d+);")                     // type
            .number("(dd)-(dd)-(dddd) ")         // event date
            .number("(dd):(dd):(dd);")           // event time
            .number("(dd)-(dd)-(dddd) ")         // fix date
            .number("(dd):(dd):(dd);")           // fix time
            .number("(-?d+.d+);")                // latitude
            .number("(-?d+.d+);")                // longitude
            .number("(d+);")                     // speed
            .number("(d+);")                     // course
            .number("(-?d+);")                   // altitude
            .number("([01]);")                   // valid
            .number("(d+);")                     // satellites
            .number("(d+);")                     // hdop
            .number("(xx);")                     // inputs 1
            .number("(xx);")                     // inputs 2
            .number("(xx);")                     // inputs 3
            .number("(xx);")                     // outputs 1
            .number("(xx);")                     // outputs 2
            .number("(d+.d+);")                  // power
            .number("(d+.d+);")                  // battery
            .number("(d+);")                     // odometer
            .number("(d+);")                     // rssi
            .number("(xx);")                     // temperature
            .number("x{4};")                     // sensors
            .number("(xx);")                     // status 1
            .number("(xx);")                     // status 2
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        String archive = parser.next();
        String model = parser.next();
        String firmware = parser.next();
        String serial = parser.next();
        int index = parser.nextInt();
        parser.nextInt(); // type

        if (channel != null && archive.equals("A")) {
            String response = "RST;A;" + model + ";" + firmware + ";" + serial + ";" + index + ";6;FIM;";
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, serial);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setDeviceTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
        position.setFixTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt()));
        position.setCourse(parser.nextInt());
        position.setAltitude(parser.nextInt());
        position.setValid(parser.nextInt() > 0);

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_HDOP, parser.nextInt());
        position.set(Position.PREFIX_IN + 1, parser.nextHexInt());
        position.set(Position.PREFIX_IN + 2, parser.nextHexInt());
        position.set(Position.PREFIX_IN + 3, parser.nextHexInt());
        position.set(Position.PREFIX_OUT + 1, parser.nextHexInt());
        position.set(Position.PREFIX_OUT + 2, parser.nextHexInt());
        position.set(Position.KEY_POWER, parser.nextDouble());
        position.set(Position.KEY_BATTERY, parser.nextDouble());
        position.set(Position.KEY_ODOMETER, parser.nextInt());
        position.set(Position.KEY_RSSI, parser.nextInt());
        position.set(Position.PREFIX_TEMP + 1, (int) parser.nextHexInt().byteValue());

        int status = (parser.nextHexInt() << 8) + parser.nextHexInt();
        position.set(Position.KEY_IGNITION, BitUtil.check(status, 7));
        position.set(Position.KEY_STATUS, status);

        return position;
    }

}
