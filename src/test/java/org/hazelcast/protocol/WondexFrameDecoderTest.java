package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WondexFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        WondexFrameDecoder decoder = new WondexFrameDecoder();

        assertNull(
                decoder.decode(null, null, binary("f0d70b0001ca9a3b")));

        assertEquals(
                binary("313034343938393630312c32303133303332333039353531352c31332e3537323737362c35322e3430303833382c302c3030302c37322c302c32"),
                decoder.decode(null, null, binary("313034343938393630312c32303133303332333039353531352c31332e3537323737362c35322e3430303833382c302c3030302c37322c302c320d0a")));

        assertEquals(binary("d0d70b0001ca9a3b"),
                decoder.decode(null, null, binary("d0d70b0001ca9a3b")));

    }

}
