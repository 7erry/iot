package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class ItsProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        ItsProtocolEncoder encoder = new ItsProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        assertEquals("@SET#RLP,OP1,", encoder.encodeCommand(command));

    }

}
