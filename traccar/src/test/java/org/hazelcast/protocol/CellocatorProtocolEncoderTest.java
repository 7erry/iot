package org.hazelcast.protocol;

import org.junit.Ignore;
import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

public class CellocatorProtocolEncoderTest extends ProtocolTest {

    @Ignore
    @Test
    public void testEncode() throws Exception {

        CellocatorProtocolEncoder encoder = new CellocatorProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_OUTPUT_CONTROL);
        command.set(Command.KEY_INDEX, 0);
        command.set(Command.KEY_DATA, "1");

        verifyCommand(encoder, command, binary("4D434750000000000000000000000303101000000000000026"));

    }

}
