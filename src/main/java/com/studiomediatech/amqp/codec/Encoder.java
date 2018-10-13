package com.studiomediatech.amqp.codec;

import com.studiomediatech.amqp.codec.AmqpMethod.Clazz;
import com.studiomediatech.amqp.codec.AmqpMethod.ID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<AmqpMethod> {

    @Override
    protected void encode(ChannelHandlerContext ctx, AmqpMethod msg, ByteBuf out) throws Exception {

        Clazz clazz = msg.getMethodClazz();
        ID id = msg.getMethodId();

        ByteBuf buf = clazz.byteFactoryFor(id).apply(msg);
        out.writeBytes(buf);
    }
}