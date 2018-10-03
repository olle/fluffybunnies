package com.studiomediatech.rabbitmq;

import io.netty.bootstrap.Bootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;


/**
 * @author  Olle Törnström
 */
public final class Connection {

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();

            b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline()
                            .addLast("decoder", new AmqpFrame.Decoder())
                            .addLast("logger", new LoggingHandler(LogLevel.INFO))
                            .addLast("handler", new Negotiate());
                        }
                    });

            b.connect("localhost", 5672).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    static class Negotiate extends SimpleChannelInboundHandler<AmqpFrame> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            ctx.writeAndFlush(Unpooled.copiedBuffer(new byte[] { 'A', 'M', 'Q', 'P', 0, 0, 9, 1 }));
        }


        @Override
        protected void channelRead0(ChannelHandlerContext ctx, AmqpFrame msg) throws Exception {

            System.out.println(">>>>> RECEIVED FRAME: " + msg);
        }
    }

    static class AmqpFrame {

        private final int type;
        private final int channel;
        private final int size;
        private byte[] payload;

        private AmqpFrame(int type, int channel, int size) {

            this.type = type;
            this.channel = channel;
            this.size = size;
        }

        @Override
        public String toString() {

            return "AmqpFrame [type=" + type + ", channel=" + channel + ", size=" + size + ", payload-bytes-read="
                + payload.length + "]";
        }

        static class Decoder extends ReplayingDecoder<AmqpFrame> {

            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

                int type = (int) in.readByte();
                int channel = in.readShort();
                int size = in.readInt();

                byte[] payload = new byte[size];
                in.readBytes(payload);

                assert 0xce == in.readByte();

                AmqpFrame frame = new AmqpFrame(type, channel, size);
                frame.payload = payload;

                out.add(frame);
            }
        }
    }
}
