package org.hazelcast.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitBufferTest {
    
    @Test
    public void test() {
        BitBuffer buffer = new BitBuffer();

        buffer.write(0b100100);
        buffer.write(0b110110);
        buffer.write(0b111111);
        buffer.write(0b111111);

        assertEquals(0b100, buffer.readUnsigned(3));
        assertEquals(-7, buffer.readSigned(4));
        assertEquals(0b10110, buffer.readUnsigned(5));
    }

}
