
    本例子展示了如下功能：

    1、删除方法中的 打印日志代码。
    2、删除指定的 方法 和 成员变量。
    3、检测方法中 是否有  new Thread 代码。
    4、生成新的类和方法。并在运行时验证。
    5、解析 类文件： MainActivity.java  ， 并在打印输出类信息。
    6、修改类中的方法。 将结果保存在工程目录下的 /output  中。
    7、在现有类 中生成一个方法 ，并在运行时验证。

    环境：
        gradle4.1
        classpath 'com.android.tools.build:gradle:3.0.1'

-------------------------------------------

1、删除类方法中的 日志 ：
        Log.xxxx
        System.out.xxx

2、删除指定的的 方法 和 成员变量

        //过滤如下 成员变量 和 方法

        public void test()
        public int test(int a)
        String abc
        String def

3、检测方法中 是否有  new Thread 代码

        若检测到则在gradle 的build 控制台中输出：

        警告: --代码中含有 new Thread--onResume()
        警告: --代码中含有 new Thread--test(int a)



-----------------------------------------------------------

        下面是利用javaparser 框架来造作代码的例子：

4、
        一：利用javaparser 生成一个类，并包含一个静态方法。
        二：利用javax.annotation.processing.Filer 文件工具 将代码写入到编译路径 ：

                /app/build/generated/source/apt/debug/ gen/GenClassA.class

            这样生成的代码就会被编译并打包进apk.

            运行时在MainActivity 中验证。

                onClick(View v) {

                  Class clazz = Class.forName("com.nzh.ast.gen.GenClassA");
                  Method method = clazz.getMethod("genMethod", String.class, int.class);
                  Object obj = method.invoke(null, "test string", 33);
                  if (obj != null) {
                      List list = (List) obj;
                      Toast.makeText(MainActivity.this, list.toString(), Toast.LENGTH_LONG).show();
                  }

                }



5、 解析 类文件： MainActivity.java  ， 并在打印输出类信息。


6、 修改类文件： MainActivity.java  , 并将结果备份保存在 /output 目录下

        一、在 MainActivity.java 中生成一个 方法：

                public static List testNewMethod(String p1, List myList) {
                       java.util.List list = new ArrayList();
                       return list;
                }


        二、修改 MainActivity.java中的 old 方法：

                修改前：
                    public void old(int i)
                修改后：
                    public void old(int i, Map map)


        三、 删除 old方法 的方法体

                   删除前：
                    public void old() {

                        int a = 6;
                        int b = 8;
                        int c = a + b;
                    }

                    删除后：
                    public void old() {
                    }

         四、修改old3 方法的注解，将@MethodAnnotaion 注解 替换为 @MethodAnnotaion2 注解。并填写相应注解的值

                    修改前：

                         @MethodAnnotaion("abc")
                            public void old3() {

                    修改后：

                         @MethodAnnotaion2(kkk = "hahaha", iii = 666)
                         public void old3() {



7、  在现有类（TestLog.java）中生成一个方法 如下：


                public static String genMethod(String str) {
                    String s = " hello world !!!-8-";
                    return s;
                }

                运行时在MainActivity 中验证 入下：

                    onClick{

                        Class clazz = Class.forName("com.nzh.ast.demo.TestLog");
                        Method method = clazz.getMethod("genMethod", String.class);
                        Object obj = method.invoke(null, "test string");
                        if (obj != null) {
                            String result = (String) obj;
                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                        }

                    }


                运行注意：
                    方式1 ：先执行 gradlew :app:assembleDebug 命令 或 在build 视图中点击 restart 按钮，
                       这样就先生成了代码。然后点击 run app  按钮运行app .

                    方式2 ： 点击 run app  按钮运行app 两次。 第一次是生成代码。第二次将生成的代码 编译打包进apk.



