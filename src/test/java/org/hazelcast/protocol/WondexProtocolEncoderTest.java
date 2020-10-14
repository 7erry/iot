package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class WondexProtocolEncoderTest extends ProtocolTest {
    @Test
    public void testEncode() throws Exception {

        WondexProtocolEncoder encoder = new WondexProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(2);
        command.setType(Command.TYPE_POSITION_SINGLE);
        command.set(Command.KEY_DEVICE_PASSWORD, "0000");

        assertEquals("$WP+GETLOCATION=0000", encoder.encodeCommand(command));

    }

}
