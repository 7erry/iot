package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class XirgoProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        XirgoProtocolEncoder encoder = new XirgoProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_OUTPUT_CONTROL);
        command.set(Command.KEY_INDEX, 0);
        command.set(Command.KEY_DATA, 1);

        assertEquals("+XT:7005,2,1", encoder.encodeCommand(command));

    }

}
