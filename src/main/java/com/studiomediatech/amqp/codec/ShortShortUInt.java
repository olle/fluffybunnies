package com.studiomediatech.amqp.codec;

final class ShortShortUInt {

    private short value;

    private ShortShortUInt(short value) {

        this.value = value;
    }

    public static ShortShortUInt valueOf(short value) {

        return new ShortShortUInt(value);
    }


    public Integer toInteger() {

        return Integer.valueOf(value);
    }
}
