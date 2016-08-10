package com.xyd.aike.gank.utils;

import android.util.Log;

/**
 * Log统一管理类
 */
public class LogUtil {

    private LogUtil() { 
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    // 是否需要打印log，可以在application的onCreate函数里面初始化
    public static boolean isDebug = true;
    private static final String TAG = "LogUtil";

    // 下面四个是默认tag的函数 
    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, msg);
    }

    // 下面是传入自定义tag的函数 
    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.v(tag, msg);
    }

    /**
     * 打印长日志的方法(分次打印)
     * @param tag
     * @return
     */
    public static void logL(String tag, String content) {
        int p = 2000;
        long length = content.length();
        if (length < p || length == p)

            Log.i(tag, content);
        else {
            while (content.length() > p) {
                String logContent = content.substring(0, p);
                content = content.replace(logContent, "");
                Log.i(tag, logContent);
            }
            Log.i(tag, content);
        }
    }

    public static void logL(String content) {
        int p = 2000;
        long length = content.length();
        if (length < p || length == p)
            Log.i(TAG, content);
        else {
            while (content.length() > p) {
                String logContent = content.substring(0, p);
                content = content.replace(logContent, "");
                Log.i(TAG, logContent);
            }
            Log.i(TAG, content);
        }
    }
}