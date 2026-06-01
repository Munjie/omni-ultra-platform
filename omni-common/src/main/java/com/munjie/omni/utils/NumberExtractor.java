package com.munjie.omni.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberExtractor {

    /**
     * 从给定的字符串中提取所有数字.
     *
     * @param input 待提取数字的字符串
     * @return 包含所有提取数字的列表
     */
    public static String extractNumbers(String input) {

        // 定义一个正则表达式模式，匹配连续的数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        // 查找并提取所有匹配的数字
        while (matcher.find()) {
            return matcher.group();
        }
        return null;

    }

}