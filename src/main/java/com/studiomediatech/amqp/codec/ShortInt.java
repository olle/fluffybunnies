package com.studiomediatech.amqp.codec;

final class ShortInt {

    private final short value;

    private ShortInt(short value) {

        this.value = value;
    }

    public static ShortInt valueOf(short value) {

        return new ShortInt(value);
    }


    public Integer toInteger() {

        return Integer.valueOf(value);
    }
}
