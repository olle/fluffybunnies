package com.studiomediatech.amqp.codec;

import java.math.BigInteger;


final class LongLongUInt {

    private BigInteger value;

    private LongLongUInt(byte[] value) {

        this.value = new BigInteger(value);
    }

    public static LongLongUInt valueOf(byte[] value) {

        return new LongLongUInt(value);
    }


    public BigInteger toBigInteger() {

        return this.value;
    }
}
