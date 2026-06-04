package com.munjie.omni.utils;

import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;

public class KeyPairConvertUtil {

    // 将 KeyPair 转为 PEM 字符串
    public static String keyPairToString(KeyPair keyPair) throws IOException {
        StringWriter writer = new StringWriter();
        KeyPairUtils.writeKeyPair(keyPair, writer);
        return writer.toString();
    }

    // 将 PEM 字符串还原为 KeyPair
    public static KeyPair stringToKeyPair(String pem) throws IOException {
        try (Reader reader = new StringReader(pem)) {
            return KeyPairUtils.readKeyPair(reader);
        }
    }
}