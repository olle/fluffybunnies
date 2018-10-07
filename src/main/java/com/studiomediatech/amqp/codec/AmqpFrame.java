package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;


public final class AmqpFrame {

    public enum Type {

        METHOD,
        HEADER,
        BODY,
        HEARTBEAT;

        public static Type valueOf(short type) {

            switch (type) {
                case 1:
                    return METHOD;

                case 2:
                    return HEADER;

                case 3:
                    return BODY;

                case 4:
                    return HEARTBEAT;

                default:
                    throw new IllegalArgumentException("Invalid type " + type + ", only ");
            }
        }
    }

    private final Type type;
    private final int channel;
    private final long size;
    private final ByteBuf payload;

    public AmqpFrame(Type type, int channel, long size, ByteBuf payload) {

        this.type = type;
        this.channel = channel;
        this.size = size;
        this.payload = payload;
    }

    @Override
    public String toString() {

        return "AmqpFrame [type=" + type + ", channel=" + channel + ", size=" + size + ", payload=" + payload + "]";
    }


    boolean unlessMethod() {

        return type != Type.METHOD;
    }


    public ByteBuf payload() {

        return payload;
    }

    public static class Decoder extends ReplayingDecoder<AmqpFrame> {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

            Codec c = Codec.wrapping(in);

            Type type = c.readFrameType();
            int channel = c.readFrameChannelNumber();
            long size = c.readFramePayloadSize();
            ByteBuf payload = c.readFramePayload(size);

            c.readFrameEndMark();

            out.add(new AmqpFrame(type, channel, size, payload));
        }
    }
}
