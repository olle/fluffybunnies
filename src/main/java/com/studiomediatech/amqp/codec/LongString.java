package com.studiomediatech.amqp.codec;

import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;


final class LongString {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private String value;

    private LongString(String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return value;
    }


    public static LongString valueOf(byte[] value) {

        return new LongString(new String(value, UTF8));
    }


    public static LongString valueOf(byte[] head, byte[] tail) {

        return new LongString(Unpooled.wrappedBuffer(head, tail).toString(UTF8));
    }


    public static LongString valueOf(String value) {

        if (value.length() > Integer.MAX_VALUE) {
            String s1 = value.substring(0, Integer.MAX_VALUE);
            String s2 = value.substring(Integer.MAX_VALUE + 1);

            return valueOf(s1.getBytes(UTF8), s2.getBytes(UTF8));
        }

        return valueOf(value.getBytes(UTF8));
    }
}
