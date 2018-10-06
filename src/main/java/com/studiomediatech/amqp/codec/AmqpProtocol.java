package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;


public final class AmqpProtocol {

    private static final short MAJOR = 0;
    private static final short MINOR = 9;
    private static final short REVISION = 1;

    private final short major;
    private final short minor;
    private final short revision;

    private AmqpProtocol() {

        this(MAJOR, MINOR, REVISION);
    }


    private AmqpProtocol(short major, short minor, short revision) {

        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    @Override
    public String toString() {

        return "AmqpProtocol [major=" + major + ", minor=" + minor + ", revision=" + revision + "]";
    }


    public static AmqpProtocol create() {

        return new AmqpProtocol();
    }

    public static class Encoder extends MessageToByteEncoder<AmqpProtocol> {

        @Override
        protected void encode(ChannelHandlerContext ctx, AmqpProtocol msg, ByteBuf out) throws Exception {

            out.writeBytes(
                new byte[] { 'A', 'M', 'Q', 'P', 0, (byte) msg.major, (byte) msg.minor, (byte) msg.revision });
        }
    }

    public static class Decoder extends ReplayingDecoder<AmqpProtocol> {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

            short a = in.readUnsignedByte();

            if (a != 'A') {
                throw new IllegalStateException("Wrong initial byte in AMQP protocol header");
            }

            short m = in.readUnsignedByte();
            short q = in.readUnsignedByte();
            short p = in.readUnsignedByte();

            short zero = in.readUnsignedByte();

            short major = in.readUnsignedByte();
            short minor = in.readUnsignedByte();
            short revision = in.readUnsignedByte();

            out.add(new AmqpProtocol(major, minor, revision));
        }
    }
}
