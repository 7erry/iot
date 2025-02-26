/*
 * Copyright 2015 - 2020 Anton Tananaev (anton )
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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.BaseProtocolDecoder;
import org.hazelcast.Context;
import org.hazelcast.DeviceSession;
import org.hazelcast.NetworkMessage;
import org.hazelcast.Protocol;
import org.hazelcast.helper.BitUtil;
import org.hazelcast.helper.Parser;
import org.hazelcast.helper.PatternBuilder;
import org.hazelcast.helper.UnitsConverter;
import org.hazelcast.model.CellTower;
import org.hazelcast.model.Network;
import org.hazelcast.model.Position;
import org.hazelcast.model.WifiAccessPoint;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.regex.Pattern;

public class WatchProtocolDecoder extends BaseProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchProtocolDecoder.class);

    public WatchProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_POSITION = new PatternBuilder()
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([AV]),")               // validity
            .number(" *(-?d+.d+),")              // latitude
            .expression("([NS]),")
            .number(" *(-?d+.d+),")              // longitude
            .expression("([EW])?,")
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*),")                 // course
            .number("(-?d+.?d*),")               // altitude
            .number("(d+),")                     // satellites
            .number("(d+),")                     // rssi
            .number("(d+),")                     // battery
            .number("(d+),")                     // steps
            .number("d+,")                       // tumbles
            .number("(x+),")                     // status
            .expression("(.*)")                  // cell and wifi
            .compile();

    private void sendResponse(Channel channel, String id, String index, String content) {
        if (channel != null) {
            String response;
            if (index != null) {
                response = String.format("[%s*%s*%s*%04x*%s]",
                        manufacturer, id, index, content.length(), content);
            } else {
                response = String.format("[%s*%s*%04x*%s]",
                        manufacturer, id, content.length(), content);
            }
            ByteBuf buf = Unpooled.copiedBuffer(response, StandardCharsets.US_ASCII);
            channel.writeAndFlush(new NetworkMessage(buf, channel.remoteAddress()));
        }
    }

    private String decodeAlarm(int status) {
        if (BitUtil.check(status, 0)) {
            return Position.ALARM_LOW_BATTERY;
        } else if (BitUtil.check(status, 1)) {
            return Position.ALARM_GEOFENCE_EXIT;
        } else if (BitUtil.check(status, 2)) {
            return Position.ALARM_GEOFENCE_ENTER;
        } else if (BitUtil.check(status, 3)) {
            return Position.ALARM_OVERSPEED;
        } else if (BitUtil.check(status, 16)) {
            return Position.ALARM_SOS;
        } else if (BitUtil.check(status, 17)) {
            return Position.ALARM_LOW_BATTERY;
        } else if (BitUtil.check(status, 18)) {
            return Position.ALARM_GEOFENCE_EXIT;
        } else if (BitUtil.check(status, 19)) {
            return Position.ALARM_GEOFENCE_ENTER;
        } else if (BitUtil.check(status, 20)) {
            return Position.ALARM_REMOVING;
        } else if (BitUtil.check(status, 21)) {
            return Position.ALARM_FALL_DOWN;
        }
        return null;
    }

    private Position decodePosition(DeviceSession deviceSession, String data) {

        Parser parser = new Parser(PATTERN_POSITION, data);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));

        position.set(Position.KEY_SATELLITES, parser.nextInt(0));
        position.set(Position.KEY_RSSI, parser.nextInt(0));
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt(0));

        position.set(Position.KEY_STEPS, parser.nextInt(0));

        int status = parser.nextHexInt(0);
        position.set(Position.KEY_ALARM, decodeAlarm(status));
        if (BitUtil.check(status, 4)) {
            position.set(Position.KEY_MOTION, true);
        }

        String[] values = parser.next().split(",");
        int index = 0;

        Network network = new Network();

        int cellCount = Integer.parseInt(values[index++]);
        index += 1; // timing advance
        int mcc = Integer.parseInt(values[index++]);
        int mnc = Integer.parseInt(values[index++]);

        for (int i = 0; i < cellCount; i++) {
            network.addCellTower(CellTower.from(mcc, mnc,
                    Integer.parseInt(values[index++]), Integer.parseInt(values[index++]),
                    Integer.parseInt(values[index++])));
        }

        if (index < values.length && !values[index].isEmpty()) {
            int wifiCount = Integer.parseInt(values[index++]);

            for (int i = 0; i < wifiCount; i++) {
                index += 1; // wifi name
                network.addWifiAccessPoint(WifiAccessPoint.from(
                        values[index++], Integer.parseInt(values[index++])));
            }
        }

        if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
            position.setNetwork(network);
        }

        return position;
    }

    private boolean hasIndex;
    private String manufacturer;

    public boolean getHasIndex() {
        return hasIndex;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(1); // '[' header
        manufacturer = buf.readSlice(2).toString(StandardCharsets.US_ASCII);
        buf.skipBytes(1); // '*' delimiter

        int idIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '*');
        String id = buf.readSlice(idIndex - buf.readerIndex()).toString(StandardCharsets.US_ASCII);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        buf.skipBytes(1); // '*' delimiter

        String index = null;
        int contentIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '*');
        if (contentIndex + 5 < buf.writerIndex() && buf.getByte(contentIndex + 5) == '*'
                && buf.toString(contentIndex + 1, 4, StandardCharsets.US_ASCII).matches("\\p{XDigit}+")) {
            int indexLength = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '*') - buf.readerIndex();
            hasIndex = true;
            index = buf.readSlice(indexLength).toString(StandardCharsets.US_ASCII);
            buf.skipBytes(1); // '*' delimiter
        }

        buf.skipBytes(4); // length
        buf.skipBytes(1); // '*' delimiter

        buf.writerIndex(buf.writerIndex() - 1); // ']' ignore ending

        contentIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ',');
        if (contentIndex < 0) {
            contentIndex = buf.writerIndex();
        }

        String type = buf.readSlice(contentIndex - buf.readerIndex()).toString(StandardCharsets.US_ASCII);

        if (contentIndex < buf.writerIndex()) {
            buf.readerIndex(contentIndex + 1);
        }

        if (type.equals("INIT")) {

            sendResponse(channel, id, index, "INIT,1");

        } else if (type.equals("LK")) {

            sendResponse(channel, id, index, "LK");

            if (buf.isReadable()) {
                String[] values = buf.toString(StandardCharsets.US_ASCII).split(",");
                if (values.length >= 3) {
                    Position position = new Position(getProtocolName());
                    position.setDeviceId(deviceSession.getDeviceId());

                    getLastLocation(position, null);

                    position.set(Position.KEY_BATTERY_LEVEL, Integer.parseInt(values[2]));

                    return position;
                }
            }

        } else if (type.startsWith("UD") || type.equals("AL") || type.equals("WT")) {

            Position position = decodePosition(deviceSession, buf.toString(StandardCharsets.US_ASCII));

            if (type.equals("AL")) {
                if (position != null) {
                    position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                }
                sendResponse(channel, id, index, "AL");
            }

            return position;

        } else if (type.equals("TKQ") || type.equals("TKQ2")) {

            sendResponse(channel, id, index, type);

        } else if (type.equalsIgnoreCase("PULSE")
                || type.equalsIgnoreCase("HEART")
                || type.equalsIgnoreCase("BLOOD")
                || type.equalsIgnoreCase("BPHRT")) {

            if (buf.isReadable()) {

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                getLastLocation(position, new Date());

                String[] values = buf.toString(StandardCharsets.US_ASCII).split(",");
                int valueIndex = 0;

                if (type.equalsIgnoreCase("BPHRT") || type.equalsIgnoreCase("BLOOD")) {
                    position.set("pressureHigh", values[valueIndex++]);
                    position.set("pressureLow", values[valueIndex++]);
                }

                if (valueIndex <= values.length - 1) {
                    position.set(Position.KEY_HEART_RATE, Integer.parseInt(values[valueIndex]));
                }

                return position;

            }

        } else if (type.equals("img")) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            getLastLocation(position, null);

            int timeIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ',');
            buf.readerIndex(timeIndex + 12 + 2);
            position.set(Position.KEY_IMAGE, Context.getMediaManager().writeFile(id, buf, "jpg"));

            return position;

        } else if (type.equals("TK")) {

            if (buf.readableBytes() == 1) {
                byte result = buf.readByte();
                if (result != '1') {
                    LOGGER.warn(type + "," + result);
                }
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            getLastLocation(position, null);

            position.set(Position.KEY_AUDIO, Context.getMediaManager().writeFile(id, buf, "amr"));

            return position;

        }

        return null;
    }

}
