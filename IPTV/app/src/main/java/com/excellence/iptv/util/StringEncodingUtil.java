package com.excellence.iptv.util;

import java.io.UnsupportedEncodingException;

import static java.lang.Integer.toHexString;

/**
 * StringEncodingUtil
 *
 * @author ggz
 * @date 2018/4/10
 */

public class StringEncodingUtil {

    byte[] byteArray;

    public StringEncodingUtil(byte[] byteArray) {
        super();
        this.byteArray = byteArray;
    }

    public String makeString() {
        String str;
        String encoding = switchEncoding();

        try {
            if (byteArray[0] == 0x10) {
                str = new String(byteArray, 3, byteArray.length - 3, encoding);
            } else {
                str = new String(byteArray, 1, byteArray.length - 1, encoding);
            }
            return str;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            str = new String(byteArray);
            return str;
        }
    }

    private String switchEncoding() {
        switch (byteArray[0]) {
            case 0x01:
                return "ISO-8859-5";
            case 0x02:
                return "ISO-8859-6";
            case 0x03:
                return "ISO-8859-7";
            case 0x04:
                return "ISO-8859-8";
            case 0x05:
                return "ISO-8859-9";
            case 0x06:
                return "ISO-8859-10";
            case 0x07:
                return "ISO-8859-11";
            case 0x08:
                return "ISO-8859-12";
            case 0x09:
                return "ISO-8859-13";
            case 0x0A:
                return "ISO-8859-14";
            case 0x0B:
                return "ISO-8859-15";
            case 0x10:
                return "ISO-8859-" + toHexString(byteArray[2] & 0xFF);
            case 0x11:
                return "ISO-10646";
            case 0x12:
                return "KSX1001-2004";
            case 0x13:
                return "GB2312";
            case 0x14:
                return "GB2312-1980";
            default:
                return "GBK";
        }
    }

}
