package com.studiomediatech.amqp.codec;

import java.nio.charset.Charset;


final class ShortString {

    private String value;

    public ShortString(String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return value;
    }


    public static ShortString valueOf(byte[] value) {

        return new ShortString(new String(value, Charset.forName("UTF-8")));
    }
}
