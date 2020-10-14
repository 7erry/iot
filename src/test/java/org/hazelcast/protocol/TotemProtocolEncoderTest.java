package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class TotemProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        TotemProtocolEncoder encoder = new TotemProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(2);
        command.setType(Command.TYPE_ENGINE_STOP);
        command.set(Command.KEY_DEVICE_PASSWORD, "000000");

        assertEquals("*000000,025,C,1#", encoder.encodeCommand(command));

    }

}
