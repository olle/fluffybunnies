package com.studiomediatech.amqp.codec;

final class ShortUInt {

    private final int value;

    private ShortUInt(int value) {

        this.value = value;
    }

    public static ShortUInt valueOf(int value) {

        return new ShortUInt(value);
    }


    public Integer toInteger() {

        return Integer.valueOf(value);
    }
}
