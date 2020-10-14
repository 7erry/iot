package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;

public class NavisetFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        NavisetFrameDecoder decoder = new NavisetFrameDecoder();

        verifyFrame(
                binary("1310e4073836383230343030353935383436362a060716"),
                decoder.decode(null, null, binary("1310e4073836383230343030353935383436362a060716")));

    }

}
