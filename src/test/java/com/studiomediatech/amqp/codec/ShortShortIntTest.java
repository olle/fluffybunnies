package com.studiomediatech.amqp.codec;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ShortShortIntTest {

    @Test
    public void ensureMaxValueIs127() {

        ShortShortInt v = ShortShortInt.valueOf((byte) 128);
        assertEquals(Byte.MIN_VALUE, v.asByte());
    }


    @Test
    public void ensureMinValueIsMinus128() {

        ShortShortInt v = ShortShortInt.valueOf((byte) -129);
        assertEquals(Byte.MAX_VALUE, v.asInt());
    }
}
