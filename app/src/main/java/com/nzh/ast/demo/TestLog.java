package com.nzh.ast.demo;

import android.util.Log;

/**
 * Created by 31414 on 2019/8/30.
 */
public class TestLog {

    public static void print(String s) {
        Log.e("xxx", s);
        System.out.println(s);
    }

    public static String genMethod(String str) {
        java.lang.String s = " hello ";
        return s+str;
    }
}
