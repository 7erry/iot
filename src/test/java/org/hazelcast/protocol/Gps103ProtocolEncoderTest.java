package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class Gps103ProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodePositionPeriodic() throws Exception {

        Gps103ProtocolEncoder encoder = new Gps103ProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_POSITION_PERIODIC);
        command.set(Command.KEY_FREQUENCY, 300);

        assertEquals("**,imei:123456789012345,C,05m", encoder.encodeCommand(command));

    }

    @Test
    public void testEncodeCustom() throws Exception {

        Gps103ProtocolEncoder encoder = new Gps103ProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_CUSTOM);
        command.set(Command.KEY_DATA, "H,080");

        assertEquals("**,imei:123456789012345,H,080", encoder.encodeCommand(command));

    }

}
