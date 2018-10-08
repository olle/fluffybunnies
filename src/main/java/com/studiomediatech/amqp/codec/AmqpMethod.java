package com.studiomediatech.amqp.codec;

import com.studiomediatech.amqp.codec.AmqpMethod.Connection.AmqpStartMethod;
import com.studiomediatech.amqp.codec.AmqpMethod.Connection.AmqpStartOkMethod;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.Map;
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


        public Function<Codec, AmqpMethod> factoryFor(ID id) {

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

            ByteBuf buf = msg.payload();
            Codec c = Codec.wrapping(buf);

            Clazz clazz = c.readMethodClazz();
            ID id = c.readMethodId();

            AmqpMethod method = clazz.factoryFor(id).apply(c);

            out.add(method);
        }
    }

    public static class Connection {

        public static class AmqpStartMethod extends AmqpMethod {

            private final short versionMajor;
            private final short versionMinor;
            private final Map<String, Object> serverProperties;
            private final String mechanisms;
            private final String locales;

            public AmqpStartMethod(short versionMajor, short versionMinor, Map<String, Object> serverProperties,
                String mechanisms, String locales) {

                this.versionMajor = versionMajor;
                this.versionMinor = versionMinor;
                this.serverProperties = serverProperties;
                this.mechanisms = mechanisms;
                this.locales = locales;
            }

            @Override
            public String toString() {

                return "AmqpStartMethod [versionMajor=" + versionMajor + ", versionMinor=" + versionMinor
                    + ", serverProperties=" + serverProperties + ", mechanisms=" + mechanisms + ", locales=" + locales
                    + "]";
            }


            public static AmqpMethod valueOf(Codec c) {

                short versionMajor = c.readConnectionStartMajorVersion();
                short versionMinor = c.readConnectionStartMinorVersion();
                Map<String, Object> serverProperties = c.readConnectionStartServerProperties();
                String mechanisms = c.readConnectionStartMechanisms();
                String locales = c.readConnectionStartLocales();

                return new AmqpStartMethod(versionMajor, versionMinor, serverProperties, mechanisms, locales);
            }
        }

        public static class AmqpStartOkMethod extends AmqpMethod {

            public static AmqpMethod valueOf(Codec c) {

                return null;
            }
        }
    }
}
