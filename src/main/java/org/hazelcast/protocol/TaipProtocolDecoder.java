/*
 * Copyright 2013 - 2018 Anton Tananaev (anton )
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
import org.hazelcast.helper.Checksum;
import org.hazelcast.helper.DateBuilder;
import org.hazelcast.helper.DateUtil;
import org.hazelcast.helper.Parser;
import org.hazelcast.helper.PatternBuilder;
import org.hazelcast.helper.UnitsConverter;
import org.hazelcast.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Pattern;

public class TaipProtocolDecoder extends BaseProtocolDecoder {

    public TaipProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .groupBegin()
            .expression("R[EP]V")                // type
            .groupBegin()
            .number("(dd)")                      // event
            .number("(dddd)")                    // week
            .number("(d)")                       // day
            .groupEnd("?")
            .number("(d{5})")                    // seconds
            .or()
            .expression("(?:RGP|RCQ|RCV|RBR)")   // type
            .number("(dd)?")                     // event
            .number("(dd)(dd)(dd)")              // date (mmddyy)
            .number("(dd)(dd)(dd)")              // time (hhmmss)
            .groupEnd()
            .groupBegin()
            .number("([-+]dd)(d{5})")            // latitude
            .number("([-+]ddd)(d{5})")           // longitude
            .or()
            .number("([-+])(dd)(dd.dddd)")       // latitude
            .number("([-+])(ddd)(dd.dddd)")      // longitude
            .groupEnd()
            .number("(ddd)")                     // speed
            .number("(ddd)")                     // course
            .groupBegin()
            .number("([023])")                   // fix mode
            .number("xx")                        // data age
            .number("(xx)")                      // input
            .number("(dd)")                      // event
            .number("(dd)")                      // hdop
            .or()
            .groupBegin()
            .number("(xx)")                      // input
            .number("(xx)")                      // satellites
            .number("(ddd)")                     // battery
            .number("(x{8})")                    // odometer
            .number("[01]")                      // gps power
            .groupBegin()
            .number("([023])")                   // fix mode
            .number("(dd)")                      // pdop
            .number("dd")                        // satellites
            .number("xxxx")                      // data age
            .number("[01]")                      // modem power
            .number("[0-5]")                     // gsm status
            .number("(dd)")                      // rssi
            .number("([-+]dddd)")                // temperature 1
            .number("xx")                        // seconds from last
            .number("([-+]dddd)")                // temperature 2
            .number("xx")                        // seconds from last
            .groupEnd("?")
            .groupEnd("?")
            .groupEnd()
            .any()
            .compile();

    private Date getTime(long week, long day, long seconds) {
        DateBuilder dateBuilder = new DateBuilder()
                .setDate(1980, 1, 6)
                .addMillis(((week * 7 + day) * 24 * 60 * 60 + seconds) * 1000);
        return dateBuilder.getDate();
    }

    private Date getTime(long seconds) {
        DateBuilder dateBuilder = new DateBuilder(new Date())
                .setTime(0, 0, 0, 0)
                .addMillis(seconds * 1000);
        return DateUtil.correctDay(dateBuilder.getDate());
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        int beginIndex = sentence.indexOf('>');
        if (beginIndex != -1) {
            sentence = sentence.substring(beginIndex + 1);
        }

        Parser parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        Boolean valid = null;
        Integer event = null;

        if (parser.hasNext(3)) {
            event = parser.nextInt();
            position.setTime(getTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0)));
        } else if (parser.hasNext()) {
            position.setTime(getTime(parser.nextInt(0)));
        }

        if (parser.hasNext()) {
            event = parser.nextInt();
        }

        if (parser.hasNext(6)) {
            position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
        }

        if (parser.hasNext(4)) {
            position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_DEG));
            position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_DEG));
        }
        if (parser.hasNext(6)) {
            position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
            position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        }

        position.setSpeed(UnitsConverter.knotsFromMph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));

        if (parser.hasNext(4)) {
            valid = parser.nextInt() > 0;
            int input = parser.nextHexInt();
            position.set(Position.KEY_IGNITION, BitUtil.check(input, 7));
            position.set(Position.KEY_INPUT, input);
            event = parser.nextInt();
            position.set(Position.KEY_HDOP, parser.nextInt());
        }

        if (parser.hasNext(4)) {
            position.set(Position.KEY_INPUT, parser.nextHexInt(0));
            position.set(Position.KEY_SATELLITES, parser.nextHexInt(0));
            position.set(Position.KEY_BATTERY, parser.nextInt(0));
            position.set(Position.KEY_ODOMETER, parser.nextLong(16, 0));
        }

        if (parser.hasNext(4)) {
            valid = parser.nextInt() > 0;
            position.set(Position.KEY_PDOP, parser.nextInt());
            position.set(Position.KEY_RSSI, parser.nextInt());
            position.set(Position.PREFIX_TEMP + 1, parser.nextInt() * 0.01);
            position.set(Position.PREFIX_TEMP + 2, parser.nextInt() * 0.01);
        }

        position.setValid(valid == null || valid);

        if (event != null) {
            position.set(Position.KEY_EVENT, event);
            switch (event) {
                case 22:
                    position.set(Position.KEY_ALARM, Position.ALARM_ACCELERATION);
                    break;
                case 23:
                    position.set(Position.KEY_ALARM, Position.ALARM_BRAKING);
                    break;
                case 24:
                    position.set(Position.KEY_ALARM, Position.ALARM_ACCIDENT);
                    break;
                case 26:
                case 28:
                    position.set(Position.KEY_ALARM, Position.ALARM_CORNERING);
                    break;
                default:
                    break;
            }
        }

        String[] attributes = null;
        beginIndex = sentence.indexOf(';');
        if (beginIndex != -1) {
            int endIndex = sentence.indexOf('<', beginIndex);
            if (endIndex == -1) {
                endIndex = sentence.length();
            }
            attributes = sentence.substring(beginIndex, endIndex).split(";");
        }

        return decodeAttributes(channel, remoteAddress, position, attributes);
    }

    private Position decodeAttributes(
            Channel channel, SocketAddress remoteAddress, Position position, String[] attributes) {

        String uniqueId = null;
        DeviceSession deviceSession = null;
        String messageIndex = null;

        if (attributes != null) {
            for (String attribute : attributes) {
                int index = attribute.indexOf('=');
                if (index != -1) {
                    String key = attribute.substring(0, index).toLowerCase();
                    String value = attribute.substring(index + 1);
                    switch (key) {
                        case "id":
                            uniqueId = value;
                            deviceSession = getDeviceSession(channel, remoteAddress, value);
                            if (deviceSession != null) {
                                position.setDeviceId(deviceSession.getDeviceId());
                            }
                            break;
                        case "io":
                            position.set(Position.KEY_IGNITION, BitUtil.check(value.charAt(0) - '0', 0));
                            position.set(Position.KEY_CHARGE, BitUtil.check(value.charAt(0) - '0', 1));
                            position.set(Position.KEY_OUTPUT, value.charAt(1) - '0');
                            position.set(Position.KEY_INPUT, value.charAt(2) - '0');
                            break;
                        case "ix":
                            position.set(Position.PREFIX_IO + 1, value);
                            break;
                        case "ad":
                            position.set(Position.PREFIX_ADC + 1, Integer.parseInt(value));
                            break;
                        case "sv":
                            position.set(Position.KEY_SATELLITES, Integer.parseInt(value));
                            break;
                        case "bl":
                            position.set(Position.KEY_BATTERY, Integer.parseInt(value) * 0.001);
                            break;
                        case "vo":
                            position.set(Position.KEY_ODOMETER, Long.parseLong(value));
                            break;
                        default:
                            position.set(key, value);
                            break;
                    }
                } else if (attribute.startsWith("#")) {
                    messageIndex = attribute;
                }
            }
        }

        if (deviceSession != null) {
            if (channel != null) {
                if (messageIndex != null) {
                    String response = ">ACK;ID=" + uniqueId + ";" + messageIndex + ";*";
                    response += String.format("%02X", Checksum.xor(response)) + "<";
                    channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
                } else {
                    channel.writeAndFlush(new NetworkMessage(uniqueId, remoteAddress));
                }
            }
            return position;
        }

        return null;
    }

}
