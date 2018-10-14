package com.studiomediatech.amqp.codec;

import java.nio.charset.Charset;


final class ShortString {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private String value;

    public ShortString(String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return value;
    }


    public static ShortString valueOf(byte[] value) {

        return new ShortString(new String(value, UTF8));
    }


    public static ShortString valueOf(String value) {

        return new ShortString(value);
    }


    public int length() {

        return 1 + value.getBytes(UTF8).length;
    }
}
