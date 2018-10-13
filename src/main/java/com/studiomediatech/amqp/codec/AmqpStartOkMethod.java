package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AmqpStartOkMethod extends AmqpMethod {

    private AmqpStartOkMethod() {

        // TODO!
    }

    public static ByteBuf encode(AmqpMethod msg) {

        // TODO!
        return Unpooled.copiedBuffer(new byte[] { 'H', 'E', 'L', 'O' });
    }


    public static AmqpStartOkMethod response(AmqpStartMethod msg) {

        return new AmqpStartOkMethod();
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