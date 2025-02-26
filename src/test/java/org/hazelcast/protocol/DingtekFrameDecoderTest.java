package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;

public class DingtekFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        DingtekFrameDecoder decoder = new DingtekFrameDecoder();

        verifyFrame(
                binary("383030303031303131453032383830303138303030303030303030313545303038303545433430303031313836383832323034303130343433303831"),
                decoder.decode(null, null, binary("383030303031303131453032383830303138303030303030303030313545303038303545433430303031313836383832323034303130343433303831")));

    }

}
