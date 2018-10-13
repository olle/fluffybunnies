package com.studiomediatech.rabbitmq;

import com.studiomediatech.amqp.codec.AmqpFrame;
import com.studiomediatech.amqp.codec.AmqpMethod;
import com.studiomediatech.amqp.codec.AmqpProtocol;
import com.studiomediatech.amqp.codec.AmqpStartMethod;
import com.studiomediatech.amqp.codec.AmqpStartOkMethod;
import com.studiomediatech.amqp.codec.Decoder;
import com.studiomediatech.amqp.codec.Encoder;

import io.netty.bootstrap.Bootstrap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


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
                            .addLast("logger", new LoggingHandler(LogLevel.INFO))
                            .addLast("protocol-encoder", new AmqpProtocol.Encoder())
                            .addLast("frame-decoder", new AmqpFrame.Decoder())
                            .addLast("method-decoder", new Decoder())
                            .addLast("method-encoder", new Encoder())
                            .addLast("handler", new Negotiate());
                        }
                    });

            b.connect("localhost", 5672).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    static class Negotiate extends SimpleChannelInboundHandler<AmqpMethod> {

        enum State {

            initial,
            start,
            start_ok;
        }

        private State state;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            this.state = State.initial;
            ctx.writeAndFlush(AmqpProtocol.create());
        }


        @Override
        protected void channelRead0(ChannelHandlerContext ctx, AmqpMethod msg) throws Exception {

            if (msg instanceof AmqpStartMethod && state == State.initial) {
                System.out.println("Will respond to start: " + msg);
                ctx.writeAndFlush(AmqpStartOkMethod.response((AmqpStartMethod) msg));
            }
        }
    }
}
