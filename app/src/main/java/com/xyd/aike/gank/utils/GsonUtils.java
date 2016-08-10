package com.xyd.aike.gank.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by xyd-dev on 16/8/9.
 */
public class GsonUtils {

    private static Gson gson = new Gson();

    public static String toJson(Object obj, Type type) {
        return gson.toJson(obj, type);
    }

    public static String toJson(Object obj, Class clazz) {
        return gson.toJson(obj, clazz);
    }

    public static Object fromJson(String str, Type type) {
        return gson.fromJson(str, type);
    }

    public static Object fromJson(String str, Class clazz) {
        return gson.fromJson(str, clazz);
    }

}
