package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

public class T800xProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        T800xProtocolEncoder encoder = new T800xProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_CUSTOM);
        command.set(Command.KEY_DATA, "RELAY,0000,On#");

        verifyFrame(
                binary("232381001e000101234567890123450152454c41592c303030302c4f6e23"),
                encoder.encodeCommand(null, command));

    }

}
