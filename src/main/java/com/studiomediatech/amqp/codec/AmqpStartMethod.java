package com.studiomediatech.amqp.codec;

import java.util.Map;


public class AmqpStartMethod extends AmqpMethod {

    private final short versionMajor;
    private final short versionMinor;
    private final Map<String, Object> serverProperties;
    private final String mechanisms;
    private final String locales;

    private AmqpStartMethod(short versionMajor, short versionMinor, Map<String, Object> serverProperties,
        String mechanisms, String locales) {

        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.serverProperties = serverProperties;
        this.mechanisms = mechanisms;
        this.locales = locales;
    }

    @Override
    public String toString() {

        return "AmqpStartMethod [versionMajor=" + versionMajor + ", versionMinor=" + versionMinor
            + ", serverProperties=" + serverProperties + ", mechanisms=" + mechanisms + ", locales=" + locales
            + "]";
    }


    public static AmqpStartMethod decode(Codec c) {

        short versionMajor = c.readConnectionStartMajorVersion();
        short versionMinor = c.readConnectionStartMinorVersion();
        Map<String, Object> serverProperties = c.readConnectionStartServerProperties();
        String mechanisms = c.readConnectionStartMechanisms();
        String locales = c.readConnectionStartLocales();

        return new AmqpStartMethod(versionMajor, versionMinor, serverProperties, mechanisms, locales);
    }


    @Override
    AmqpMethod.Clazz getMethodClazz() {

        return Clazz.CHANNEL;
    }


    @Override
    AmqpMethod.ID getMethodId() {

        return ID.START;
    }
}
