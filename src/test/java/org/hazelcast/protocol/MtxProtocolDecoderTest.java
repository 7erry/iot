package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;

public class MtxProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        MtxProtocolDecoder decoder = new MtxProtocolDecoder(null);

        verifyPosition(decoder, text(
                "#MTX,353815011138124,20101226,195550,41.6296399,002.3611174,000,035,000000.00,X,X,1111,000,0,0"));

    }

}
