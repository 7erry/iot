package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;
import org.hazelcast.model.Command;

import static org.junit.Assert.assertEquals;

public class GlobalSatProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodeAlarmDismiss() {

        GlobalSatProtocolEncoder encoder = new GlobalSatProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ALARM_DISMISS);

        assertEquals("GSC,123456789012345,Na*48!", encoder.encodeCommand(command));

    }

    @Test
    public void testEncodeOutputControl() {

        GlobalSatProtocolEncoder encoder = new GlobalSatProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_OUTPUT_CONTROL);
        command.set(Command.KEY_INDEX, 1);
        command.set(Command.KEY_DATA, "1");

        assertEquals("GSC,123456789012345,Lo(1,1)*69!", encoder.encodeCommand(command));

    }

}
