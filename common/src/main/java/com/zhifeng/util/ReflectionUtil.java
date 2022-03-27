package com.zhifeng.util;

/**
 * @author ganzhifeng
 * @date 2022/3/27
 */
public class ReflectionUtil {

    /**
     * 获取调用的方法名
     * @return 方法名称
     */
    public static String getCallMethod() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        //获取调用方法名
        String methodName = stackTrace[3].getMethodName();
        return methodName;
    }

}
