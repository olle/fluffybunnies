package com.studiomediatech.amqp.codec;

import com.studiomediatech.amqp.codec.AmqpFrame.Type;
import com.studiomediatech.amqp.codec.AmqpMethod.Clazz;
import com.studiomediatech.amqp.codec.AmqpMethod.ID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.nio.charset.Charset;

import java.time.Instant;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public final class Codec {

    private static final Charset UTF8 = Charset.forName("UTF-8");

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
        Codec c = Codec.wrapping(tableBuf);

        while (c._tableIsReadable()) {
            String key = c._readTableFieldName();
            Object value = c._readFieldValue();
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


    private Object _readFieldValue() {

        short fieldType = buf.readUnsignedByte();

        switch (fieldType) {
            case 't':
                return readBoolean();

            case 'b':
                return readShortShortInt();

            case 'B':
                return readShortShortUInt();

            case 'U':
                return readShortInt();

            case 'u':
                return readShortUInt();

            case 'I':
                return readLongInt();

            case 'i':
                return readLongUInt();

            case 'L':
                return readLongLongInt();

            case 'l':
                return readLongLongUInt();

            case 'f':
                return readFloat();

            case 'F':
                return readTable();

            case 'S':
                return readLongString();

            case 's':
                return readShortString();

            case 'd':
                return readDouble();

            case 'D':
                return readDecimal();

            case 'A':
                return readArray();

            case 'T':
                return readTimestamp();

            case 'V':
                return null;

            default:
                throw new IllegalStateException("Unknown or invalid field type: " + (char) fieldType);
        }
    }


    Instant readTimestamp() {

        BigInteger value = readLongLongUInt();

        return Instant.ofEpochSecond(value.longValue());
    }


    List<Object> readArray() {

        long size = buf.readUnsignedInt();

        if (size == 0) {
            return Collections.emptyList();
        }

        List<Object> array = new LinkedList<>();

        ByteBuf arrayBuf = _safeSizedSubBuffer(size);
        Codec c = Codec.wrapping(arrayBuf);

        while (c._arrayIsReadable()) {
            Object value = c._readFieldValue();
            array.add(value);
        }

        return array;
    }


    private boolean _arrayIsReadable() {

        return buf.isReadable();
    }


    BigDecimal readDecimal() {

        short scale = buf.readUnsignedByte();
        long unscaled = buf.readUnsignedInt();

        return BigDecimal.valueOf(unscaled, scale);
    }


    Double readDouble() {

        return Double.valueOf(buf.readDouble());
    }


    Float readFloat() {

        return Float.valueOf(buf.readFloat());
    }


    Long readLongLongInt() {

        return LongLongInt.valueOf(buf.readLong()).toLong();
    }


    BigInteger readLongLongUInt() {

        byte[] val = new byte[8];
        buf.readBytes(val);

        return LongLongUInt.valueOf(val).toBigInteger();
    }


    Integer readLongInt() {

        return LongInt.valueOf(buf.readInt()).toInteger();
    }


    Long readLongUInt() {

        return LongUInt.valueOf(buf.readUnsignedInt()).toLong();
    }


    Integer readShortInt() {

        return ShortInt.valueOf(buf.readShort()).toInteger();
    }


    Integer readShortUInt() {

        return ShortUInt.valueOf(buf.readUnsignedShort()).toInteger();
    }


    String readShortString() {

        short size = buf.readUnsignedByte();
        byte[] str = new byte[size];
        buf.readBytes(str);

        return ShortString.valueOf(str).toString();
    }


    String readLongString() {

        long size = buf.readUnsignedInt();

        if (size <= Integer.MAX_VALUE) {
            byte[] str = new byte[(int) size];
            buf.readBytes(str);

            return LongString.valueOf(str).toString();
        }

        int rest = (int) (size - Integer.MAX_VALUE);

        byte[] p1 = new byte[Integer.MAX_VALUE];
        byte[] p2 = new byte[rest];

        buf.readBytes(p1);
        buf.readBytes(p2);

        return LongString.valueOf(p1, p2).toString();
    }


    Integer readShortShortInt() {

        return ShortShortInt.valueOf(buf.readByte()).toInteger();
    }


    Integer readShortShortUInt() {

        return ShortShortUInt.valueOf(buf.readUnsignedByte()).toInteger();
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


    void writeTable(Map<String, Object> table) {

        int length = _tableLength(table);

        buf.writeInt(length);
    }


    private int _tableLength(Map<String, Object> table) {

        int length = 0;

        for (Entry<String, Object> entry : table.entrySet()) {
            length += _fieldNameLength(entry.getKey());
            length += _fieldValueLength(entry.getValue());
        }

        return length;
    }


    private int _fieldNameLength(String fieldName) {

        return _shortStringLength(fieldName);
    }


    @SuppressWarnings("unchecked")
    private int _fieldValueLength(Object value) {

        if (value == null) {
            return 0;
        }

        boolean $ = false;

        if ($ // NOSONAR
                || value instanceof Boolean // NOSONAR
                || value instanceof ShortShortInt // NOSONAR
                || value instanceof ShortShortUInt) {
            return 1;
        }

        if ($ // NOSONAR
                || value instanceof ShortInt // NOSONAR
                || value instanceof ShortUInt) {
            return 2;
        }

        if ($ // NOSONAR
                || value instanceof LongInt // NOSONAR
                || value instanceof LongUInt // NOSONAR
                || value instanceof Float) {
            return 4;
        }

        if ($ // NOSONAR
                || value instanceof LongLongInt // NOSONAR
                || value instanceof LongLongUInt // NOSONAR
                || value instanceof Double // NOSONAR
                || value instanceof Date // NOSONAR
                || value instanceof Instant) {
            return 8;
        }

        if (value instanceof BigDecimal) {
            return 1 + 4; // scale + long-uint
        }

        if (value instanceof ShortString) {
            return _shortStringLength(value.toString());
        }

        if (value instanceof LongString) {
            return _longStringLength(value.toString());
        }

        if (value instanceof List) {
            return _arrayLength((List<Object>) value);
        }

        if (value instanceof Map) {
            return _tableLength((Map<String, Object>) value);
        }

        throw new IllegalArgumentException("Unkonwn field value, cannot supply length for " + value);
    }


    private int _arrayLength(List<?> values) {

        int length = 4; // fields counter

        for (Object value : values) {
            length += _fieldValueLength(value);
        }

        return length;
    }


    private int _longStringLength(String value) {

        return 4 + value.getBytes(UTF8).length;
    }


    private int _shortStringLength(String value) {

        return 1 + value.getBytes(UTF8).length;
    }
}
