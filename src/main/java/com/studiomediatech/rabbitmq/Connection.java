package com.studiomediatech.rabbitmq;

import com.studiomediatech.amqp.codec.AmqpCodec;

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

                AmqpCodec codec = AmqpCodec.valueOf(in);

                short type = codec.readOctet();
                int channel = codec.readShortUint();
                long size = codec.readLongUint();

                byte[] p1;
                byte[] p2;

                if (size > Integer.MAX_VALUE) {
                    int rest = (int) (size - Integer.MAX_VALUE);
                    p1 = new byte[Integer.MAX_VALUE];
                    p2 = new byte[rest];
                    codec.read(p1);
                    codec.read(p2);
                } else {
                    p1 = new byte[(int) size];
                    codec.read(p1);
                }

                assert 0xce == codec.readOctet();

                // TODO: Spec conformity.
                AmqpFrame frame = new AmqpFrame(type, channel, (int) size);
                frame.payload = p1;
                // frame.bigPayload = p2;

                out.add(frame);
            }
        }
    }
}
