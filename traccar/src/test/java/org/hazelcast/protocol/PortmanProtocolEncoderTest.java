package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class PortmanProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodeEngineStop() {

        PortmanProtocolEncoder encoder = new PortmanProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        assertEquals("&&123456789012345,XA5\r\n", encoder.encodeCommand(command));

    }

    @Test
    public void testEncodeEngineResume() {

        PortmanProtocolEncoder encoder = new PortmanProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_RESUME);

        assertEquals("&&123456789012345,XA6\r\n", encoder.encodeCommand(command));

    }

}
