package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

public class BceProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        BceProtocolEncoder encoder = new BceProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_OUTPUT_CONTROL);
        command.set(Command.KEY_INDEX, 1);
        command.set(Command.KEY_DATA, "1");

        verifyCommand(encoder, command, binary("79df0d86487000000600410aff00550048"));

    }

}
