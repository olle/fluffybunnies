package com.studiomediatech.amqp.codec;

import java.util.List;

import com.studiomediatech.amqp.codec.AmqpMethod.Clazz;
import com.studiomediatech.amqp.codec.AmqpMethod.ID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class Decoder extends MessageToMessageDecoder<AmqpFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, AmqpFrame msg, List<Object> out) throws Exception {

        if (msg.unlessMethod()) {
            return;
        }

        ByteBuf buf = msg.payload();
        Codec c = Codec.wrapping(buf);

        Clazz clazz = c.readMethodClazz();
        ID id = c.readMethodId();

        AmqpMethod method = clazz.messageFactoryFor(id).apply(c);
        out.add(method);
    }
}