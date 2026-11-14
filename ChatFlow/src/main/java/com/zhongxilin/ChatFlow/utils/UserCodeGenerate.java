package com.zhongxilin.ChatFlow.utils;

import java.util.concurrent.ThreadLocalRandom;


public class UserCodeGenerate {

    /**
     * 生成 "前缀 + 指定位数的随机数字"
     * @param prefix 固定开头
     * @param digitCount 随机数字的位数
     *
     */
    public static String generateUserCode(String prefix, int digitCount){
        long max=(long)Math.pow(10,digitCount);
        long num= ThreadLocalRandom.current().nextLong(max);

        return prefix + String.format("%0" + digitCount + "d", num);

    }
}
