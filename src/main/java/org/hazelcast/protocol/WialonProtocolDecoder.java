/*
 * Copyright 2013 - 2020 Anton Tananaev (anton )
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
import org.hazelcast.helper.Parser;
import org.hazelcast.helper.PatternBuilder;
import org.hazelcast.helper.UnitsConverter;
import org.hazelcast.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WialonProtocolDecoder extends BaseProtocolDecoder {

    public WialonProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_ANY = new PatternBuilder()
            .expression("([^#]*)?")              // imei
            .text("#")                           // start byte
            .expression("([^#]+)")               // type
            .text("#")                           // separator
            .expression("(.*)")                  // message
            .compile();

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(dd)(dd)(dd);")             // date (ddmmyy)
            .number("(dd)(dd)(dd);")             // time (hhmmss)
            .number("(dd)(dd.d+);")              // latitude
            .expression("([NS]);")
            .number("(ddd)(dd.d+);")             // longitude
            .expression("([EW]);")
            .number("(d+.?d*)?;")                // speed
            .number("(d+.?d*)?;")                // course
            .number("(?:NA|(-?d+.?d*));")        // altitude
            .number("(?:NA|(d+))")               // satellites
            .groupBegin().text(";")
            .number("(?:NA|(d+.?d*));")          // hdop
            .number("(?:NA|(d+));")              // inputs
            .number("(?:NA|(d+));")              // outputs
            .expression("(?:NA|([^;]*));")       // adc
            .expression("(?:NA|([^;]*));")       // ibutton
            .expression("(?:NA|(.*))")           // params
            .groupEnd("?")
            .compile();

    private void sendResponse(Channel channel, SocketAddress remoteAddress, String type, Integer number) {
        if (channel != null) {
            StringBuilder response = new StringBuilder("#A");
            response.append(type);
            response.append("#");
            if (number != null) {
                response.append(number);
            }
            response.append("\r\n");
            channel.writeAndFlush(new NetworkMessage(response.toString(), remoteAddress));
        }
    }

    private Position decodePosition(Channel channel, SocketAddress remoteAddress, String id, String substring) {

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        Parser parser = new Parser(PATTERN, substring);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));

        if (parser.hasNext()) {
            int satellites = parser.nextInt(0);
            position.setValid(satellites >= 3);
            position.set(Position.KEY_SATELLITES, satellites);
        }

        position.set(Position.KEY_HDOP, parser.nextDouble());
        position.set(Position.KEY_INPUT, parser.next());
        position.set(Position.KEY_OUTPUT, parser.next());

        if (parser.hasNext()) {
            String[] values = parser.next().split(",");
            for (int i = 0; i < values.length; i++) {
                position.set(Position.PREFIX_ADC + (i + 1), values[i]);
            }
        }

        position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());

        if (parser.hasNext()) {
            String[] values = parser.next().split(",");
            for (String param : values) {
                Matcher paramParser = Pattern.compile("(.*):[1-3]:(.*)").matcher(param);
                if (paramParser.matches()) {
                    try {
                        position.set(paramParser.group(1).toLowerCase(), Double.parseDouble(paramParser.group(2)));
                    } catch (NumberFormatException e) {
                        position.set(paramParser.group(1).toLowerCase(), paramParser.group(2));
                    }
                }
            }
        }

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        Parser parser = new Parser(PATTERN_ANY, sentence);
        if (!parser.matches()) {
            return null;
        }

        String id = parser.next();
        String type = parser.next();
        String data = parser.next();

        switch (type) {

            case "L":
                String[] values = data.split(";");
                String imei = values[0].indexOf('.') >= 0 ? values[1] : values[0];
                DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
                if (deviceSession != null) {
                    sendResponse(channel, remoteAddress, type, 1);
                }
                break;

            case "P":
                sendResponse(channel, remoteAddress, type, null); // heartbeat
                break;

            case "D":
            case "SD":
                Position position = decodePosition(channel, remoteAddress, id, data);
                if (position != null) {
                    sendResponse(channel, remoteAddress, "D", 1);
                    return position;
                }
                break;

            case "B":
                String[] messages = data.split("\\|");
                List<Position> positions = new LinkedList<>();

                for (String message : messages) {
                    position = decodePosition(channel, remoteAddress, id, message);
                    if (position != null) {
                        position.set(Position.KEY_ARCHIVE, true);
                        positions.add(position);
                    }
                }

                sendResponse(channel, remoteAddress, type, messages.length);
                if (!positions.isEmpty()) {
                    return positions;
                }
                break;

            case "M":
                deviceSession = getDeviceSession(channel, remoteAddress, id);
                if (deviceSession != null) {
                    position = new Position(getProtocolName());
                    position.setDeviceId(deviceSession.getDeviceId());
                    getLastLocation(position, new Date());
                    position.setValid(false);
                    position.set(Position.KEY_RESULT, data);
                    sendResponse(channel, remoteAddress, type, 1);
                    return position;
                }
                break;

            default:
                break;

        }

        return null;
    }

}
