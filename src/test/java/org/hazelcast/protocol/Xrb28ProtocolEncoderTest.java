package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class Xrb28ProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodePositionPeriodic() {

        Xrb28ProtocolEncoder encoder = new Xrb28ProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_POSITION_PERIODIC);
        command.set(Command.KEY_FREQUENCY, 300);

        assertEquals("\u00ff\u00ff*SCOS,OM,123456789012345,D1,300#\n", encoder.encodeCommand(null, command));

    }

    @Test
    public void testEncodeCustom() {

        Xrb28ProtocolEncoder encoder = new Xrb28ProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_CUSTOM);
        command.set(Command.KEY_DATA, "S7,0,3,0,0,20,25");

        assertEquals("\u00ff\u00ff*SCOS,OM,123456789012345,S7,0,3,0,0,20,25#\n", encoder.encodeCommand(null, command));

    }

}
