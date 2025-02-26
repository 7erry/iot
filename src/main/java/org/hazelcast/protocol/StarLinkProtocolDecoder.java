/*
 * Copyright 2017 - 2020 Anton Tananaev (anton )
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
import org.hazelcast.Context;
import org.hazelcast.DeviceSession;
import org.hazelcast.Protocol;
import org.hazelcast.helper.DataConverter;
import org.hazelcast.helper.Parser;
import org.hazelcast.helper.PatternBuilder;
import org.hazelcast.model.CellTower;
import org.hazelcast.model.Network;
import org.hazelcast.model.Position;
import org.hazelcast.protobuf.StarLinkMessage;

import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class StarLinkProtocolDecoder extends BaseProtocolDecoder {

    public static final int MSG_EVENT_REPORT = 6;

    private static final Pattern PATTERN = new PatternBuilder()
            .expression(".")                     // protocol head
            .text("SLU")                         // message head
            .number("(x{6}|d{15}),")             // id
            .number("(d+),")                     // type
            .number("(d+),")                     // index
            .expression("(.+)")                  // data
            .text("*")
            .number("xx")                        // checksum
            .compile();

    private String[] dataTags;
    private DateFormat dateFormat;

    public StarLinkProtocolDecoder(Protocol protocol) {
        super(protocol);

        setFormat(Context.getConfig().getString(
                getProtocolName() + ".format", "#EDT#,#EID#,#PDT#,#LAT#,#LONG#,#SPD#,#HEAD#,#ODO#,"
                + "#IN1#,#IN2#,#IN3#,#IN4#,#OUT1#,#OUT2#,#OUT3#,#OUT4#,#LAC#,#CID#,#VIN#,#VBAT#,#DEST#,#IGN#,#ENG#"));

        setDateFormat(Context.getConfig().getString(getProtocolName() + ".dateFormat", "yyMMddHHmmss"));
    }

    public void setFormat(String format) {
        dataTags = format.split(",");
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private double parseCoordinate(String value) {
        int minutesIndex = value.indexOf('.') - 2;
        double result = Double.parseDouble(value.substring(1, minutesIndex));
        result += Double.parseDouble(value.substring(minutesIndex)) / 60;
        return value.charAt(0) == '+' ? result : -result;
    }

    private String decodeAlarm(int event) {
        switch (event) {
            case 6:
                return Position.ALARM_OVERSPEED;
            case 7:
                return Position.ALARM_GEOFENCE_ENTER;
            case 8:
                return Position.ALARM_GEOFENCE_EXIT;
            case 9:
                return Position.ALARM_POWER_CUT;
            case 11:
                return Position.ALARM_LOW_BATTERY;
            case 26:
                return Position.ALARM_TOW;
            case 36:
                return Position.ALARM_SOS;
            case 42:
                return Position.ALARM_JAMMING;
            default:
                return null;
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        int type = parser.nextInt(0);
        if (type != MSG_EVENT_REPORT) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setValid(true);

        position.set(Position.KEY_INDEX, parser.nextInt(0));

        String[] data = parser.next().split(",");
        Integer lac = null, cid = null;
        int event = 0;

        for (int i = 0; i < Math.min(data.length, dataTags.length); i++) {
            if (data[i].isEmpty()) {
                continue;
            }
            switch (dataTags[i]) {
                case "#EDT#":
                    position.setDeviceTime(dateFormat.parse(data[i]));
                    break;
                case "#EID#":
                    event = Integer.parseInt(data[i]);
                    position.set(Position.KEY_ALARM, decodeAlarm(event));
                    position.set(Position.KEY_EVENT, event);
                    if (event == 24) {
                        position.set(Position.KEY_IGNITION, true);
                    } else if (event == 25) {
                        position.set(Position.KEY_IGNITION, false);
                    }
                    break;
                case "#PDT#":
                    position.setFixTime(dateFormat.parse(data[i]));
                    break;
                case "#LAT#":
                    position.setLatitude(parseCoordinate(data[i]));
                    break;
                case "#LONG#":
                    position.setLongitude(parseCoordinate(data[i]));
                    break;
                case "#SPD#":
                    position.setSpeed(Double.parseDouble(data[i]));
                    break;
                case "#HEAD#":
                    position.setCourse(Integer.parseInt(data[i]));
                    break;
                case "#ODO#":
                    position.set(Position.KEY_ODOMETER, (long) (Double.parseDouble(data[i]) * 1000));
                    break;
                case "#IN1#":
                case "#IN2#":
                case "#IN3#":
                case "#IN4#":
                    position.set(Position.PREFIX_IN + dataTags[i].charAt(3), Integer.parseInt(data[i]));
                    break;
                case "#OUT1#":
                case "#OUT2#":
                case "#OUT3#":
                case "#OUT4#":
                    position.set(Position.PREFIX_OUT + dataTags[i].charAt(3), Integer.parseInt(data[i]));
                    break;
                case "#LAC#":
                    if (!data[i].isEmpty()) {
                        lac = Integer.parseInt(data[i]);
                    }
                    break;
                case "#CID#":
                    if (!data[i].isEmpty()) {
                        cid = Integer.parseInt(data[i]);
                    }
                    break;
                case "#VIN#":
                    position.set(Position.KEY_POWER, Double.parseDouble(data[i]));
                    break;
                case "#VBAT#":
                    position.set(Position.KEY_BATTERY, Double.parseDouble(data[i]));
                    break;
                case "#DEST#":
                    position.set("destination", data[i]);
                    break;
                case "#IGN#":
                    position.set(Position.KEY_IGNITION, data[i].equals("1"));
                    break;
                case "#ENG#":
                    position.set("engine", data[i].equals("1"));
                    break;
                case "#TS1#":
                    position.set("sensor1State", Integer.parseInt(data[i]));
                    break;
                case "#TS2#":
                    position.set("sensor2State", Integer.parseInt(data[i]));
                    break;
                case "#TD1#":
                case "#TD2#":
                    StarLinkMessage.mEventReport_TDx message =
                            StarLinkMessage.mEventReport_TDx.parseFrom(DataConverter.parseBase64(data[i]));
                    position.set(
                            "sensor" + message.getSensorNumber() + "Id",
                            message.getSensorID());
                    position.set(
                            "sensor" + message.getSensorNumber() + "Temp",
                            message.getTemperature() * 0.1);
                    position.set(
                            "sensor" + message.getSensorNumber() + "Humidity",
                            message.getTemperature() * 0.1);
                    position.set(
                            "sensor" + message.getSensorNumber() + "Voltage",
                            message.getVoltage() * 0.001);
                    break;
                default:
                    break;
            }
        }

        if (position.getFixTime() == null) {
            getLastLocation(position, null);
        }

        if (lac != null && cid != null) {
            position.setNetwork(new Network(CellTower.fromLacCid(lac, cid)));
        }

        if (event == 20) {
            String rfid = data[data.length - 1];
            if (rfid.matches("0+")) {
                rfid = data[data.length - 2];
            }
            position.set(Position.KEY_DRIVER_UNIQUE_ID, rfid);
        }

        return position;
    }

}
