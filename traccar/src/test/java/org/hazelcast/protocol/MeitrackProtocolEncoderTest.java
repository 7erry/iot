package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class MeitrackProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        MeitrackProtocolEncoder encoder = new MeitrackProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_POSITION_SINGLE);

        assertEquals("@@Q25,123456789012345,A10*68\r\n", encoder.encodeCommand(command));

        command.setDeviceId(1);
        command.setType(Command.TYPE_REQUEST_PHOTO);

        assertEquals("@@D46,123456789012345,D03,1,camera_picture.jpg*1F\r\n", encoder.encodeCommand(command));

        command.setDeviceId(1);
        command.setType(Command.TYPE_SEND_SMS);
        command.set(Command.KEY_PHONE, "15360853789");
        command.set(Command.KEY_MESSAGE, "Meitrack");

        assertEquals("@@f48,123456789012345,C02,0,15360853789,Meitrack*B0\r\n", encoder.encodeCommand(command));

    }

}
