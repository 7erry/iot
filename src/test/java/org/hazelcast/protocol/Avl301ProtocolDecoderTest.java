package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;


public class Avl301ProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        Avl301ProtocolDecoder decoder = new Avl301ProtocolDecoder(null);

        verifyNull(decoder, binary(
                "244c0f086058500087335500010d0a"));

        verifyNull(decoder, binary(
                "24480d1001c3065c0d00010d0a"));

        verifyPosition(decoder, binary(
                "24242c0f041710001d0e060146944904ff4ac40000148f0651044b001a081001be06590daa00000108a30d0a"));

    }

}
