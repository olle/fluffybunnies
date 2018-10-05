package com.studiomediatech.amqp.codec;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;


/**
 * @author  Olle Törnström
 */
public class AmqpCodec {

    private final ByteBuf buf;

    private AmqpCodec(ByteBuf buf) {

        this.buf = buf;
    }

    public static AmqpCodec valueOf(ByteBuf buf) {

        return new AmqpCodec(buf);
    }


    public short readOctet() {

        return buf.readUnsignedByte();
    }


    public int readShortUint() {

        return buf.readUnsignedShort();
    }


    public long readLongUint() {

        return buf.readUnsignedInt();
    }


    public BigInteger readLongLongUint() {

        byte[] b = new byte[8];
        buf.readBytes(b);

        return new BigInteger(b);
    }


    public void read(byte[] dest) {

        buf.readBytes(dest);
    }
}
