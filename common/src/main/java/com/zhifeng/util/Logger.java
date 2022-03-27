package com.zhifeng.util;

/**
 * @author ganzhifeng
 * @date 2022/3/27
 */
public class Logger {

    /**
     * 带有方法名的输出，方法名放前面
     * @param s 待输出的字符串
     */
    public static void debug(Object s) {
        String content = null;
        if (null != s) {
            content = s.toString().trim();
        } else {
            content = "";
        }
        String out = String.format("%20s |>  %s ", ReflectionUtil.getCallMethod(), content);
        System.out.println(out);
    }


}
