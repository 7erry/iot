package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

public class PstProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodeEngineStop() {

        PstProtocolEncoder encoder = new PstProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        verifyCommand(encoder, command, binary("860ddf790600000001060002ffffffffe42b"));

    }

    @Test
    public void testEncodeEngineResume() {

        PstProtocolEncoder encoder = new PstProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_RESUME);

        verifyCommand(encoder, command, binary("860ddf790600000001060001ffffffff0af9"));

    }

}
