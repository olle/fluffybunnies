package com.studiomediatech.amqp.codec;

import com.studiomediatech.amqp.codec.AmqpMethod.Connection.AmqpStartMethod;
import com.studiomediatech.amqp.codec.AmqpMethod.Connection.AmqpStartOkMethod;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.function.Function;


public class AmqpMethod {

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


        public Function<ByteBuf, AmqpMethod> factoryFor(ID id) {

            switch (id) {
                case START:
                    return AmqpStartMethod::valueOf;

                case START_OK:
                    return AmqpStartOkMethod::valueOf;

                default:
                    throw new IllegalArgumentException("No static factory available for: " + id);
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

    public static class Decoder extends MessageToMessageDecoder<AmqpFrame> {

        @Override
        protected void decode(ChannelHandlerContext ctx, AmqpFrame msg, List<Object> out) throws Exception {

            if (msg.unlessMethod()) {
                return;
            }

            ByteBuf buf = msg.buffer();

            Clazz clazz = AmqpMethod.Clazz.valueOf(buf.readUnsignedShort());
            ID id = AmqpMethod.ID.valueOf(buf.readUnsignedShort());

            AmqpMethod method = clazz.factoryFor(id).apply(buf);

            out.add(method);
        }
    }

    public static class Connection {

        public static class AmqpStartMethod extends AmqpMethod {

            private short versionMajor;
            private short versionMinor;

            public AmqpStartMethod(short versionMajor, short versionMinor) {

                this.versionMajor = versionMajor;
                this.versionMinor = versionMinor;
            }

            @Override
            public String toString() {

                return "AmqpStartMethod [versionMajor=" + versionMajor + ", versionMinor=" + versionMinor + "]";
            }


            public static AmqpMethod valueOf(ByteBuf buf) {

                short versionMajor = buf.readUnsignedByte();
                short versionMinor = buf.readUnsignedByte();

                return new AmqpStartMethod(versionMajor, versionMinor);
            }
        }

        public static class AmqpStartOkMethod extends AmqpMethod {

            public static AmqpMethod valueOf(ByteBuf buf) {

                return null;
            }
        }
    }
}
