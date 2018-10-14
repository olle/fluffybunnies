package com.studiomediatech.amqp.protocol;

import com.studiomediatech.amqp.codec.AmqpMethod;
import com.studiomediatech.amqp.codec.Codec;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;
import java.util.function.Consumer;


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

    private Type type;
    private int channelNumber;
    private long payloadSize;
    private ByteBuf payload;

    private AmqpFrame(Type type, int channelNumber, long payloadSize, ByteBuf payload) {

        this.type = type;
        this.channelNumber = channelNumber;
        this.payloadSize = payloadSize;
        this.payload = payload;
    }

    @Override
    public String toString() {

        return "AmqpFrame [type=" + type + ", channelNumber=" + channelNumber + ", payloadSize=" + payloadSize
            + ", payload=" + payload + "]";
    }


    public boolean unlessMethod() {

        return type != Type.METHOD;
    }


    public ByteBuf payload() {

        return payload;
    }

    public static final class Decoder extends ReplayingDecoder<AmqpFrame> {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

            FrameCodec.wrapping(in)
                .readingType()
                .readingChannelNumber()
                .readingPayloadSize()
                .readingPayload()
                .readingEndMark()
                .into(out::add);
        }
    }

    public static final class Encoder extends MessageToByteEncoder<AmqpMethod> {

        @Override
        protected void encode(ChannelHandlerContext ctx, AmqpMethod msg, ByteBuf out) throws Exception {
        }
    }

    private static final class FrameCodec extends Codec {

        private Type type;
        private int channelNumber;
        private long payloadSize;
        private ByteBuf payload;

        private FrameCodec(ByteBuf buf) {

            super(buf);
        }

        private FrameCodec readingType() {

            type = AmqpFrame.Type.valueOf(buf.readUnsignedByte());

            return this;
        }


        private FrameCodec readingChannelNumber() {

            channelNumber = buf.readUnsignedShort();

            return this;
        }


        private FrameCodec readingPayloadSize() {

            payloadSize = buf.readUnsignedInt();

            return this;
        }


        private FrameCodec readingPayload() {

            payload = _safeSizedSubBuffer(payloadSize);

            return this;
        }


        private FrameCodec readingEndMark() {

            short eof = buf.readUnsignedByte();

            if (eof != 0xCE) {
                throw new IllegalStateException("Invalid end of frame marker");
            }

            return this;
        }


        private void into(Consumer<Object> target) {

            target.accept(new AmqpFrame(type, channelNumber, payloadSize, payload));
        }


        protected static FrameCodec wrapping(ByteBuf buf) {

            return new FrameCodec(buf);
        }
    }
}
