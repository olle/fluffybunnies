package com.studiomediatech.amqp.codec;

final class LongUInt {

    private final long value;

    public LongUInt(long value) {

        this.value = value;
    }

    public static LongUInt valueOf(long value) {

        return new LongUInt(value);
    }


    public Long toLong() {

        return Long.valueOf(value);
    }
}
