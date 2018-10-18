package com.studiomediatech.amqp.protocol;

import com.studiomediatech.amqp.codec.AmqpMethod;
import com.studiomediatech.amqp.codec.Codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
                    throw new IllegalArgumentException("Invalid type " + type);
            }
        }


        public static byte toByte(Type type) {

            switch (type) {
                case METHOD:
                    return 0x01;

                case HEADER:
                    return 0x02;

                case BODY:
                    return 0x03;

                case HEARTBEAT:
                    return 0x04;

                default:
                    throw new IllegalArgumentException("Invalid type " + type);
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
                .inTo(out::add);
        }
    }

    public static final class Encoder extends MessageToByteEncoder<AmqpMethod> {

        @Override
        protected void encode(ChannelHandlerContext ctx, AmqpMethod msg, ByteBuf out) throws Exception {

            FrameCodec.wrapping(msg)
                .writingType()
                .writingChannelNumber()
                .writingPayloadSize()
                .writingPayload()
                .writingEndMark()
                .outTo(out::writeBytes);
        }
    }

    private static final class FrameCodec extends Codec {

        private Type type;
        private int channelNumber;
        private long payloadSize;
        private ByteBuf payload;

        private AmqpMethod method;

        private FrameCodec(ByteBuf buf) {

            super(buf);
        }


        private FrameCodec(AmqpMethod method) {

            this(Unpooled.buffer());
            this.method = method;
        }

        private void outTo(Consumer<ByteBuf> target) {

            target.accept(buf);
        }


        private FrameCodec writingEndMark() {

            throw new RuntimeException("NOT YET IMPLEMENTED!");
        }


        private FrameCodec writingPayload() {

            throw new RuntimeException("NOT YET IMPLEMENTED!");
        }


        private FrameCodec writingPayloadSize() {

            throw new RuntimeException("NOT YET IMPLEMENTED!");
        }


        private FrameCodec writingChannelNumber() {

            buf.writeShort(0);

            return this;
        }


        private FrameCodec writingType() {

            buf.writeByte(Type.toByte(Type.METHOD));

            return this;
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


        private void inTo(Consumer<Object> target) {

            target.accept(new AmqpFrame(type, channelNumber, payloadSize, payload));
        }


        protected static FrameCodec wrapping(ByteBuf buf) {

            return new FrameCodec(buf);
        }


        protected static FrameCodec wrapping(AmqpMethod method) {

            return new FrameCodec(method);
        }
    }
}
