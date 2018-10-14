package com.studiomediatech.amqp.codec;

final class ShortShortInt {

    private final byte value;

    private ShortShortInt(byte value) {

        this.value = value;
    }

    public static ShortShortInt valueOf(byte value) {

        return new ShortShortInt(value);
    }


    public byte asByte() {

        return this.value;
    }


    public int asInt() {

        return (int) this.value;
    }
}
