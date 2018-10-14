package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Collections;
import java.util.Map;


public class AmqpStartOkMethod extends AmqpMethod {

    private final Map<String, Object> clientProperties;
    private final String mechanism;
    private final String response;
    private final String locale;

    private AmqpStartOkMethod(Map<String, Object> clientProperties, String mechanism, String response, String locale) {

        this.clientProperties = clientProperties;
        this.mechanism = mechanism;
        this.response = response;
        this.locale = locale;
    }

    @Override
    public String toString() {

        return "AmqpStartOkMethod [clientProperties=" + clientProperties + ", mechanism=" + mechanism + ", response="
            + response + ", locale=" + locale + "]";
    }


    public static ByteBuf encode(AmqpMethod msg) {

        AmqpStartOkMethod source = (AmqpStartOkMethod) msg;

        ByteBuf buf = Unpooled.buffer();

        Codec c = Codec.wrapping(buf);
        c.writeConnectionStartOkClientProperties(source.clientProperties);
        c.writeConnectionStartOkMechanism(source.mechanism);
        c.writeConnectionStartOkResponse(source.response);
        c.writeConnectionStartOkLocale(source.locale);

        return buf;
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
