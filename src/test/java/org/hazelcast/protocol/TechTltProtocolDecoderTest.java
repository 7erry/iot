package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;

public class TechTltProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        TechTltProtocolDecoder decoder = new TechTltProtocolDecoder(null);

        verifyPosition(decoder, text(
                "002422269*POS=Y,16:21:20,25/11/09,3809.8063N,01444.7438E,4.17,117.23,0.4,09,40076,56341\r\n"),
                position("2009-11-25 16:21:20.000", true, 38.16344, 14.74573));

        verifyAttributes(decoder, text(
                "002422269,INFOGPRS,V Bat=13.8,TEMP=23,I TIM,15\r\n"));

    }

}
