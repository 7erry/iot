package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class PretraceProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodePositionPeriodic() throws Exception {

        PretraceProtocolEncoder encoder = new PretraceProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_POSITION_PERIODIC);
        command.set(Command.KEY_FREQUENCY, 300);

        assertEquals("(123456789012345D221300,300,,^69)", encoder.encodeCommand(command));

    }

    @Test
    public void testEncodeCustom() throws Exception {

        PretraceProtocolEncoder encoder = new PretraceProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_CUSTOM);
        command.set(Command.KEY_DATA, "D21012");

        assertEquals("(123456789012345D21012^44)", encoder.encodeCommand(command));

    }

}
