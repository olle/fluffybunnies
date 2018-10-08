package com.studiomediatech.amqp.codec;

import com.studiomediatech.amqp.codec.AmqpFrame.Type;
import com.studiomediatech.amqp.codec.AmqpMethod.Clazz;
import com.studiomediatech.amqp.codec.AmqpMethod.ID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class Codec {

    private static final Charset DEFAULT = Charset.forName("UTF-8");

    private final ByteBuf buf;

    private Codec(ByteBuf buf) {

        this.buf = buf;
    }

    public static Codec wrapping(ByteBuf in) {

        return new Codec(in);
    }


    Type readFrameType() {

        return AmqpFrame.Type.valueOf(buf.readUnsignedByte());
    }


    int readFrameChannelNumber() {

        return buf.readUnsignedShort();
    }


    long readFramePayloadSize() {

        return buf.readUnsignedInt();
    }


    ByteBuf readFramePayload(long size) {

        return _safeSizedSubBuffer(size);
    }


    private ByteBuf _safeSizedSubBuffer(long size) {

        if (size <= Integer.MAX_VALUE) {
            return buf.readRetainedSlice((int) size);
        }

        int rest = (int) (size - Integer.MAX_VALUE);

        byte[] p1 = new byte[Integer.MAX_VALUE];
        byte[] p2 = new byte[rest];

        buf.readBytes(p1);
        buf.readBytes(p2);

        return Unpooled.wrappedBuffer(p1, p2);
    }


    void readFrameEndMark() {

        short eof = buf.readUnsignedByte();

        if (eof != 0xCE) {
            throw new IllegalStateException("Invalid end of frame marker");
        }
    }


    Clazz readMethodClazz() {

        return AmqpMethod.Clazz.valueOf(buf.readUnsignedShort());
    }


    void readInto(byte[] dest) {

        buf.readBytes(dest);
    }


    short readConnectionStartMajorVersion() {

        return buf.readUnsignedByte();
    }


    short readConnectionStartMinorVersion() {

        return buf.readUnsignedByte();
    }


    ID readMethodId() {

        return AmqpMethod.ID.valueOf(buf.readUnsignedShort());
    }


    Map<String, Object> readTable() {

        long size = buf.readUnsignedInt();

        if (size == 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> table = new HashMap<>();

        ByteBuf tableBuf = _safeSizedSubBuffer(size);
        Codec cc = Codec.wrapping(tableBuf);

        while (cc._tableIsReadable()) {
            String key = cc._readTableFieldName();
            Object value = cc._readTableFieldValue();
            table.putIfAbsent(key, value);
        }

        return table;
    }


    private boolean _tableIsReadable() {

        return buf.isReadable();
    }


    private String _readTableFieldName() {

        return readShortString();
    }


    private Object _readTableFieldValue() {

        short fieldType = buf.readUnsignedByte();

        switch (fieldType) {
            case 't':
                return readBoolean();

            case 'b':
                return readShortShortInt();

            case 'F':
                return readFieldTable();

            case 'S':
                return readLongString();

            default:
                throw new IllegalStateException("Unknown or invalid field type: " + (char) fieldType);
        }
    }


    String readShortString() {

        short size = buf.readUnsignedByte();
        byte[] str = new byte[size];
        buf.readBytes(str);

        return new String(str, DEFAULT);
    }


    String readLongString() {

        long size = buf.readUnsignedInt();

        if (size <= Integer.MAX_VALUE) {
            byte[] str = new byte[(int) size];
            buf.readBytes(str);

            return new String(str, DEFAULT);
        }

        int rest = (int) (size - Integer.MAX_VALUE);

        byte[] p1 = new byte[Integer.MAX_VALUE];
        byte[] p2 = new byte[rest];

        buf.readBytes(p1);
        buf.readBytes(p2);

        return Unpooled.wrappedBuffer(p1, p2).toString(DEFAULT);
    }


    Object readFieldTable() {

        return readTable();
    }


    Integer readShortShortInt() {

        return Integer.valueOf(buf.readUnsignedByte());
    }


    Boolean readBoolean() {

        return buf.readUnsignedByte() == 0x00 ? false : true;
    }


    Map<String, Object> readConnectionStartServerProperties() {

        return readTable();
    }


    String readConnectionStartMechanisms() {

        return readLongString();
    }


    String readConnectionStartLocales() {

        return readLongString();
    }
}
