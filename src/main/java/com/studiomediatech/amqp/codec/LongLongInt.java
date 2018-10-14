package com.studiomediatech.amqp.codec;

final class LongLongInt {

    private long value;

    private LongLongInt(long value) {

        this.value = value;
    }

    public static LongLongInt valueOf(long value) {

        return new LongLongInt(value);
    }


    public Long toLong() {

        return Long.valueOf(this.value);
    }
}
