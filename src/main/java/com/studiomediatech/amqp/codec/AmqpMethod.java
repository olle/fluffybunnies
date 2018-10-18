package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.function.Function;

import com.studiomediatech.amqp.protocol.AmqpFrame;


public abstract class AmqpMethod {

    public enum Clazz {

        CONNECTION,
        CHANNEL,
        EXCHANGE,
        QUEUE,
        BASIC,
        TX;

        public static Clazz valueOf(int classId) {

            switch (classId) {
                case 10:
                    return CONNECTION;

                case 20:
                    return CHANNEL;

                case 40:
                    return EXCHANGE;

                case 50:
                    return QUEUE;

                case 60:
                    return BASIC;

                case 90:
                    return TX;

                default:
                    throw new IllegalArgumentException("Invalid class id: " + classId);
            }
        }


        public Function<Codec, AmqpMethod> messageFactoryFor(ID id) {

            switch (id) {
                case START:
                    return AmqpStartMethod::decode;

                case START_OK:
                    return null;

                default:
                    throw new IllegalArgumentException("No static message factory available for: " + id);
            }
        }


        public Function<AmqpMethod, ByteBuf> byteFactoryFor(ID id) {

            switch (id) {
                case START:
                    return null;

                case START_OK:
                    return AmqpStartOkMethod::encode;

                default:
                    throw new IllegalArgumentException("No static byte factory available for: " + id);
            }
        }
    }

    public enum ID {

        START,
        START_OK;

        public static ID valueOf(int methodId) {

            switch (methodId) {
                case 10:
                    return START;

                case 11:
                    return START_OK;

                default:
                    throw new IllegalArgumentException("Invalid method id: " + methodId);
            }
        }
    }

    abstract Clazz getMethodClazz();


    abstract ID getMethodId();

    public static class Encoder extends MessageToByteEncoder<AmqpMethod> {

        @Override
        protected void encode(ChannelHandlerContext ctx, AmqpMethod msg, ByteBuf out) throws Exception {

            Clazz clazz = msg.getMethodClazz();
            ID id = msg.getMethodId();

            ByteBuf buf = clazz.byteFactoryFor(id).apply(msg);
            out.writeBytes(buf);
        }
    }

    public static class Decoder extends MessageToMessageDecoder<AmqpFrame> {

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
}
