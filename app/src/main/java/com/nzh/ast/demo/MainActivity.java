package com.nzh.ast.demo;

import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nzh.api.annotation.ClassAnnotaion;
import com.nzh.api.annotation.FieldAnnotation;
import com.nzh.api.annotation.MethodAnnotaion;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.nzh.api.annotation.MethodAnnotaion2;

@ClassAnnotaion(layoutId = R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @FieldAnnotation(R.id.text)
    public TextView abc;

    private static String def = "def";

    protected int bbc = 34;

    int cctv = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("onCreate", "-------initView----");
        // 日志
        System.out.println("------onCreate--------");
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    Class clazz = Class.forName("com.nzh.ast.gen.GenClassA");
//                    Method method = clazz.getDeclaredMethod("genMethod", String.class, int.class);
                    Method method = clazz.getMethod("genMethod", String.class, int.class);
                    Object obj = method.invoke(null, "jerry", 33);
                    if (obj != null) {
                        List list = (List) obj;
                        Toast.makeText(MainActivity.this, list.toString(), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    Class clazz = Class.forName("com.nzh.ast.demo.TestLog");
                    Method method = clazz.getMethod("genMethod", String.class);
                    Object obj = method.invoke(null, "jerry");
                    if (obj != null) {
                        String result = (String) obj;
                        Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "-----------onResume-----");
        System.out.println("------onResume--------");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
            }
        });
        t.start();
        System.out.println("------ new Thread--------");
    }

    public void test() {
        Toast.makeText(this, "ooo", Toast.LENGTH_SHORT).show();
    }

    public int test(int a) {
        new Thread() {

            @Override
            public void run() {
                super.run();
            }
        }.start();
        return a + 2;
    }

    @MethodAnnotaion("hahaha")
    public void test3() {
        int a = 6;
        int b = a + 3;
        TestLog.print("abc");
    }


    public void old(int i) {

        int a = 3;
        int b = 4;
        int c = a + b;
    }

    public void old() {

        int a = 6;
        int b = 8;
        int c = a + b;
    }

    @MethodAnnotaion("abc")
    public void old3() {

        int a = 16;
        int b = 18;
        int c = a + b;
    }


}
