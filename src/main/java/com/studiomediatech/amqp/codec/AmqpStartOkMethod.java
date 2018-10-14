package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Collections;
import java.util.Map;


public class AmqpStartOkMethod extends AmqpMethod {

    private final Map<String, Object> clientProperties;
    private final String mechanisms;
    private final String response;
    private final String locale;

    private AmqpStartOkMethod(Map<String, Object> clientProperties, String mechanisms, String response,
        String locale) {

        this.clientProperties = clientProperties;
        this.mechanisms = mechanisms;
        this.response = response;
        this.locale = locale;
    }

    @Override
    public String toString() {

        return "AmqpStartOkMethod [clientProperties=" + clientProperties + ", mechanisms=" + mechanisms + ", response="
            + response + ", locale=" + locale + "]";
    }


    public static ByteBuf encode(AmqpMethod msg) {

        AmqpStartOkMethod source = (AmqpStartOkMethod) msg;

        ByteBuf buf = Unpooled.buffer();
        Codec c = Codec.wrapping(buf);

        c.writeTable(source.clientProperties);

        return buf; // Unpooled.copiedBuffer(new byte[] { 'H', 'E', 'L', 'O' });
    }


    public static AmqpStartOkMethod response(AmqpStartMethod msg) {

        Map<String, Object> clientProperties = Collections.emptyMap();
        String mechanism = "PLAIN";
        String response = "\0guest\0guest";
        String locale = "en_US";

        return new AmqpStartOkMethod(clientProperties, mechanism, response, locale);
    }


    @Override
    AmqpMethod.Clazz getMethodClazz() {

        return Clazz.CHANNEL;
    }


    @Override
    AmqpMethod.ID getMethodId() {

        return ID.START_OK;
    }
}
