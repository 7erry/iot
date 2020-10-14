package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class EasyTrackProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodeEngineStop() {

        EasyTrackProtocolEncoder encoder = new EasyTrackProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        assertEquals("*ET,123456789012345,FD,Y1#", encoder.encodeCommand(command));

    }

}
