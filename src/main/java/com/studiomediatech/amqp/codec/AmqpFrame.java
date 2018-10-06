package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;
import java.util.Optional;


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
    private final byte[] p1;
    private final byte[] p2;

    private AmqpFrame(Type type, int channel, long size, byte[] p1, byte[] p2) {

        this.type = type;
        this.channel = channel;
        this.size = size;
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public String toString() {

        return "AmqpFrame [type=" + type + ", channel=" + channel + ", size=" + size + ", payload-1.length="
            + Optional.ofNullable(p1).map(a -> a.length).orElse(0)
            + ", payload-2.length=" + Optional.ofNullable(p2).map(a -> a.length).orElse(0) + "]";
    }

    public static class Decoder extends ReplayingDecoder<AmqpFrame> {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

            Type type = AmqpFrame.Type.valueOf(in.readUnsignedByte());
            int channel = in.readUnsignedShort();

            long size = in.readUnsignedInt();

            byte[] p1 = null;
            byte[] p2 = null;

            if (size <= Integer.MAX_VALUE) {
                p1 = new byte[(int) size];
                in.readBytes(p1);
            } else {
                int rest = (int) (size - Integer.MAX_VALUE);
                p1 = new byte[Integer.MAX_VALUE];
                p2 = new byte[rest];
                in.readBytes(p1);
                in.readBytes(p2);
            }

            int eof = in.readUnsignedByte();

            if (eof != 0xCE) {
                throw new IllegalStateException("Invalid end of frame marker");
            }

            out.add(new AmqpFrame(type, channel, size, p1, p2));
        }
    }
}
