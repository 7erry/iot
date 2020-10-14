package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

public class CastelProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        CastelProtocolEncoder encoder = new CastelProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        verifyCommand(encoder, command, binary("40402000013132333435363738393031323334350000000000458301a94a0d0a"));

    }

}
