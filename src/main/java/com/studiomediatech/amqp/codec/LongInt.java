package com.studiomediatech.amqp.codec;

final class LongInt {

    private final int value;

    private LongInt(int value) {

        this.value = value;
    }

    public static LongInt valueOf(int value) {

        return new LongInt(value);
    }


    public Integer toInteger() {

        return Integer.valueOf(value);
    }
}
