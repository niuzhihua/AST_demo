package demo.ast.nzh.com.lib;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import com.google.auto.service.AutoService;
import com.nzh.api.annotation.ClassAnnotaion;
import com.nzh.api.annotation.MethodAnnotaion;
import com.nzh.api.annotation.MethodAnnotaion2;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import demo.ast.nzh.com.lib.translator.CleanLogTranslator;


/**
 * Created by 31414 on 2019/8/27.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyProcessor extends AbstractProcessor {

    // 1: 初始化抽象语法树对象
    Trees trees;
    JavacTrees javacTrees;
    // 用来打印日志
    private Messager mMessager;

    // 用来生成代码
    TreeMaker maker;

    //文件工具。
    Filer filer;

    Names names;

    String projectDir; // android 工程目录路径
    String javaDir;   // 默认java代码存放路径
    // Android包名
    String packageDir = "com" + File.separator + "nzh" + File.separator + "ast" + File.separator + "demo" + File.separator;
    // Android 包名路径
    String packageName = "com.nzh.ast.demo";
    // 类名
    private Name className;
    // 文档 ： http://www.docjar.com/html/api/com/sun/tools/javac/tree/TreeMaker.java.html
    // http://openjdk.java.net/groups/compiler/processing-code.html

    //测试例子： http://hg.openjdk.java.net/jdk8/jdk8/langtools
    // http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/1ff9d5118aae/test/tools/javac
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        projectDir = System.getProperty("user.dir");
        javaDir = File.separator + "app" + File.separator + "src" + File.separator + "main" + File.separator + "java";

        mMessager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        if (processingEnvironment instanceof JavacProcessingEnvironment) {
            JavacProcessingEnvironment e = (JavacProcessingEnvironment) processingEnvironment;
            trees = Trees.instance(e);
            this.javacTrees = JavacTrees.instance(processingEnv);
            Context context = ((JavacProcessingEnvironment) processingEnvironment).getContext();
            maker = TreeMaker.instance(context);
            names = Names.instance(context);
        }

    }

    /**
     * 2: 操作 AST
     * 推荐使用javaparser 库来操作 AST 抽象语法树。
     * javaparser 教程官网：
     * https://javaparser.org/getting-started/
     * https://github.com/javaparser/javaparser
     * 例子：
     * https://github.com/javaparser/javaparser/tree/master/javaparser-core-testing/src/test/java/com/github/javaparser
     * https://www.programcreek.com/java-api-examples/index.php?api=com.github.javaparser.JavaParser
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        https://github.com/feelschaotic/AopClearLog   OK
        // https://github.com/kangyonggan/extra    ExtraProcessor.java  CacheProcessor.java
        if (set == null || set.size() == 0) {
            return true;
        }

        // 0：清除 日志代码 OK
        setCleanLog(roundEnvironment);
        // 1: 生成代码
        mMessager.printMessage(Diagnostic.Kind.WARNING, "---generateJavaCode start---");
        CompilationUnit compileUnit = generateJavaCode();   // OK
        String code = compileUnit.toString();

        mMessager.printMessage(Diagnostic.Kind.WARNING, "-----code--------");
        mMessager.printMessage(Diagnostic.Kind.WARNING, code);
        mMessager.printMessage(Diagnostic.Kind.WARNING, "---generateJavaCode end---\r\n\r\n");

        // 2 ：解析java 类文件： MainActivity.java
        mMessager.printMessage(Diagnostic.Kind.WARNING, "-----parseJavaCode start------");
        parseJavaCode();
        mMessager.printMessage(Diagnostic.Kind.WARNING, "-----parseJavaCode end------\r\n\r\n");

        // 3: 修改java 类文件： MainActivity.java
        mMessager.printMessage(Diagnostic.Kind.WARNING, "-----modifyCode start------");
        modifyCode();
        mMessager.printMessage(Diagnostic.Kind.WARNING, "-----modifyCode end------");
        // 处理自定义的注解
//        genMethod(roundEnvironment);
        // 生成代码到编译目录并跟随源码一起编译
        genClassThenCompiler(code);

        // 4 ： 在现有类中生成一个方法 。
        genMethodInExsitClass();

        return true;
    }


    /**
     * 生成一个测试类。包含一个静态方法
     *
     * @return
     */
    private CompilationUnit generateJavaCode() {

        CompilationUnit compileUnit = new CompilationUnit();    // 生成一个编译单元(文件)
        compileUnit.setPackageDeclaration("com.nzh.ast.gen");   // 生成一个包名

        ClassOrInterfaceDeclaration classDeclaration = compileUnit.addClass("GenClassA");  // 在此编译单元(文件)下 生成一个类。

        classDeclaration.setPublic(true);
        // 给类添加 注解ClassAnnotaion，并赋值
        NodeList<MemberValuePair> pair0 = new NodeList<>();
        MemberValuePair annoValue0 = new MemberValuePair();
        annoValue0.setName("layoutId");  // 注解中方法名
        IntegerLiteralExpr intExpr0 = new IntegerLiteralExpr(666);
        annoValue0.setValue(intExpr0);
        pair0.add(annoValue0);
        NormalAnnotationExpr annotationWithValue = new NormalAnnotationExpr(new com.github.javaparser.ast.expr.Name(ClassAnnotaion.class.getSimpleName()), pair0);
        classDeclaration.addAnnotation(annotationWithValue);

        // 在类中生成一个方法
        MethodDeclaration methodDeclaration = classDeclaration.addMethod("genMethod");
        methodDeclaration.setPublic(true);
        methodDeclaration.setStatic(true);
        methodDeclaration.setType(java.util.List.class);    // 方法返回类型

        // 为方法添加参数列表
        NodeList<Parameter> params = new NodeList<>();
        Parameter p = new Parameter();
        SimpleName paramName1 = new SimpleName("p1");
        p.setName(paramName1);
        p.setType(new TypeParameter("java.lang.String"));
        Parameter p2 = new Parameter();
        SimpleName paramName2 = new SimpleName("p2");
        p2.setName(paramName2);
        p2.setType(new TypeParameter("int"));
        params.add(0, p);
        params.add(1, p2);

        methodDeclaration.setParameters(params);                          // 给方法添加参数列表
        // 给方法添加注解 MethodAnnotaion2 ，并赋值
        NodeList<MemberValuePair> pair = new NodeList<>();
        MemberValuePair annoValue = new MemberValuePair();
        annoValue.setName("kkk");  // 注解中方法名
        StringLiteralExpr strValueExpr = new StringLiteralExpr("hahaha");
        annoValue.setValue(strValueExpr);

        MemberValuePair annoValue2 = new MemberValuePair();
        annoValue2.setName("iii");  // 注解中方法名
        IntegerLiteralExpr intExpr = new IntegerLiteralExpr(666);
        annoValue2.setValue(intExpr);
        pair.add(annoValue);
        pair.add(annoValue2);

        methodDeclaration.addAnnotation(new NormalAnnotationExpr(new com.github.javaparser.ast.expr.Name(MethodAnnotaion2.class.getSimpleName()), pair));


        BlockStmt blockStmt = new BlockStmt();                           // 定义一个代码块对象

        // 定义一个 声明变量的表达式对象

        NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
        // 定义一个变量 : List list ;
        SimpleName varName = new SimpleName("list");
        VariableDeclarator listVar = new VariableDeclarator(new TypeParameter("java.util.List"), varName);
        // 没有这个设置表示： List list；，有表示：List list = new ArrayList();
        listVar.setInitializer("new ArrayList()");  // 或 null
        variableDeclarators.add(listVar);

        // 添加表达式
        blockStmt.addStatement(new VariableDeclarationExpr(variableDeclarators));

        // 添加表达式
        NameExpr nameExpr = new NameExpr("p1");
        MethodCallExpr methodCallExpr = new MethodCallExpr(nameExpr, "equals");
        methodCallExpr.addArgument("\"test_str\"");
        blockStmt.addStatement(methodCallExpr);
        // 在方法体中添加代码语句。
        blockStmt.addStatement(" int a = 0;");
        blockStmt.addStatement(" boolean b = \"abc\".equals(p1) ;");

        // 在方法体中添加代码 :
        // list.add(p1);
        // list.add(" hello world !");
        MethodCallExpr methodCallExpr2 = new MethodCallExpr(new NameExpr(varName), "add");
        methodCallExpr2.addArgument(new NameExpr(paramName1));
        blockStmt.addStatement(methodCallExpr2);
        MethodCallExpr methodCallExpr3 = new MethodCallExpr(new NameExpr(varName), "add");
        methodCallExpr3.addArgument("\" hello world ! \"");
        blockStmt.addStatement(methodCallExpr3);

        // 添加 返回值表达式
        ReturnStmt returnStmt = new ReturnStmt();
        NameExpr returnNameExpr = new NameExpr();
        returnNameExpr.setName(varName.asString());

        returnStmt.setExpression(returnNameExpr);   // 设置返回值
        blockStmt.addStatement(returnStmt);         // 添加返回值代码块

        methodDeclaration.setBody(blockStmt);

        // 导入所有需要的包
        compileUnit.addImport(ClassAnnotaion.class);
        compileUnit.addImport(MethodAnnotaion2.class);
        compileUnit.addImport(java.util.List.class);
        compileUnit.addImport(ArrayList.class);             // 导入包
        compileUnit.addImport("java.util.Map");             // 导入包

        return compileUnit;
    }

    /**
     * 使用javaparser 框架 解析 一个类文件： MainActivity.java
     * https://javaparser.org/#transform
     */
    private void parseJavaCode() {
        try {

            String sourcePath = projectDir + javaDir + File.separator + packageDir + "MainActivity.java";
            // "D:\\android_studio_workspace\\bicai\\AST_demo\\app\\src\\main\\java\\com\\nzh\\ast\\demo\\MainActivity.java"
            CompilationUnit compilationUnit = StaticJavaParser.parse(new File(sourcePath));
            if (compilationUnit != null) {
                mMessager.printMessage(Diagnostic.Kind.WARNING, "new File(D:..MainActivity.java)解析类成功！\n\n");
            }

            java.util.List<ClassOrInterfaceDeclaration> list = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration clazz : list) {
                // 类信息：全类名
                String className = compilationUnit.getPackageDeclaration().get().getNameAsString() + "." + clazz.getName().toString();
                mMessager.printMessage(Diagnostic.Kind.WARNING, "name:" + className);

                // 方法信息：方法名
                java.util.List<MethodDeclaration> methods = clazz.getMethods();
                for (MethodDeclaration m : methods) {
                    String methodInfo = m.getType().asString() + " " + m.getNameAsString() + m.getParameters().toString();
                    mMessager.printMessage(Diagnostic.Kind.WARNING, "方法信息:" + methodInfo);

                }

                // 成员变量信息
                java.util.List<FieldDeclaration> fields = clazz.findAll(FieldDeclaration.class);

                for (FieldDeclaration f : fields) {
                    String staticF = f.isStatic() ? "static" : "";
                    String m = f.getAccessSpecifier().asString();

                    String type = f.getElementType().toString();

                    VariableDeclarator var = f.getVariables().get(0);
                    String name = var.getNameAsString();

                    mMessager.printMessage(Diagnostic.Kind.WARNING, "成员变量:" + m + " " + staticF + " " + type + " " + name + " ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改 java 源代码，并保存到工程目录下。
     */
    private void modifyCode() {
        // 添加一个方法：testAddMethod
        try {


            String javaFile = "MainActivity.java";
            String sourcePath = projectDir + javaDir + File.separator + packageDir + javaFile;


            CompilationUnit compilationUnit = StaticJavaParser.parse(new File(sourcePath));
            if (compilationUnit != null) {
                mMessager.printMessage(Diagnostic.Kind.WARNING, "new File(D:..MainActivity.java)解析类成功！\n\n");
            }


            Path path = CodeGenerationUtils.packageAbsolutePath(projectDir + javaDir, packageName);
            SourceRoot sourceRoot = new SourceRoot(path);

            CompilationUnit cu = sourceRoot.parse("", javaFile);
            if (cu != null) {
                // 例子一、向 $javaFile 文件的第1个类中 添加一个方法 public  List testNewMethod(String p1,List myList)
                ClassOrInterfaceDeclaration clazz = cu.findAll(ClassOrInterfaceDeclaration.class).get(0);

                java.util.List<MethodDeclaration> genMethods = clazz.getMethodsByName("testNewMethod");
                if (genMethods == null || genMethods.size() == 0) {
                    MethodDeclaration addMethod = clazz.addMethod("testNewMethod", Modifier.Keyword.PUBLIC);
                    // 设置方法参数
                    addMethod.addParameter(String.class, "p1");
                    Parameter myNewParam = addMethod.addAndGetParameter(java.util.List.class, "myList");
                    addMethod.setStatic(true);
                    // 设置方法返回值
                    addMethod.setType(java.util.List.class);

                    BlockStmt block = new BlockStmt();
                    NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
                    // 定义一个变量 : List list ;
                    VariableDeclarator varList = new VariableDeclarator(new TypeParameter(java.util.List.class.getName()), new SimpleName("list"));
                    // 没有这个设置表示： List list；，有表示：List list = new ArrayList();
                    varList.setInitializer("new ArrayList()");  // 或 null
                    // 添加 ArrayList 的导包
                    cu.addImport(ArrayList.class);

                    variableDeclarators.add(varList);

                    VariableDeclarationExpr var = new VariableDeclarationExpr(variableDeclarators);

                    ReturnStmt returnStmt = new ReturnStmt(varList.getNameAsString());  // 返回创建的变量  varList 值为 “list”
                    // 添加表达式
                    block.addStatement(var);  // 在方法body中添加 代码；
                    block.addStatement(returnStmt);         // // 在方法体中添加 返回值语句。

                    addMethod.setBody(block);
                }


                cu.accept(new ModifierVisitor<Void>() {
                    @Override
                    public Visitable visit(FieldDeclaration n, Void arg) {
                        return super.visit(n, arg);
                    }


                    @Override
                    public Visitable visit(MethodDeclaration n, Void arg) {

                        //  例子二、修改test(int a)方法 ，并 增加一个参数 Map map
                        if (n.getNameAsString().equals("old") && n.getParameters().size() == 1) {
                            n.addParameter(Map.class, "map");  //
                        }

                        // 例子三、修改test()方法 ， 清除方法体，这样就成为了一个空方法。
                        if (n.getNameAsString().equals("old") && n.getParameters().size() == 0) {
                            n.setBody(new BlockStmt());
                        }

                        // 例子四、修改test3 方法的注解，将@MethodAnnotaion 注解 替换为 @MethodAnnotaion2 注解。并填写相应注解的值。
                        if (n.getNameAsString().equals("old3")) {

                            AnnotationExpr methodAnno = n.getAnnotationByClass(MethodAnnotaion.class).get();

                            methodAnno.remove();                    // 删除原注解

                            NodeList<MemberValuePair> pair = new NodeList<>();

                            MemberValuePair p = new MemberValuePair();
                            p.setName("kkk");  // 注解中方法名
                            StringLiteralExpr strValueExpr = new StringLiteralExpr("hahaha");
                            p.setValue(strValueExpr);
                            MemberValuePair p2 = new MemberValuePair();
                            p2.setName("iii");  // 注解中方法名
                            IntegerLiteralExpr intExpr = new IntegerLiteralExpr(666);
                            p2.setValue(intExpr);
                            pair.add(p);
                            pair.add(p2);

                            // 添加新注解的 导包
                            cu.addImport(MethodAnnotaion2.class);
                            // 添加新注解 MethodAnnotaion2
                            n.addAnnotation(new NormalAnnotationExpr(new com.github.javaparser.ast.expr.Name(MethodAnnotaion2.class.getSimpleName()), pair));
                        }

                        return super.visit(n, arg);
                    }

                    @Override
                    public Visitable visit(IfStmt n, Void arg) {
                        return super.visit(n, arg);
                    }
                }, null);

                // 保存方式：
                // 1：保存修改 到指定目录
                String output = projectDir + File.separator + "output"; // OK
                // 2：保存到编译目录：保存到这个目录后，java代码会被编译。
                String compilerDir = projectDir + "/app/build/generated/source/apt/debug/" + packageDir;
                sourceRoot.saveAll(
                        CodeGenerationUtils.mavenModuleRoot(TestAnalyzer.class)
                                .resolve(Paths.get(output)));

                //3： 保存修改到 当前的源文件
//                 sourceRoot.saveAll();

                mMessager.printMessage(Diagnostic.Kind.WARNING, "---over---- ");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在现有类中生成方法(谨慎使用)
     */
    private void genMethodInExsitClass() {
        try {
            String javaFile = "TestLog.java";
            String returnValue = " hello ";
            Path path = CodeGenerationUtils.packageAbsolutePath(projectDir + javaDir + File.separator, packageName);
            SourceRoot sourceRoot = new SourceRoot(path);

            CompilationUnit cu = sourceRoot.parse("", javaFile);
            if (cu != null) {
                // 例子一、向 $javaFile 文件的第1个类中 添加一个方法 public  List testAddMethod(String p1,List myList)
                ClassOrInterfaceDeclaration clazz = cu.findAll(ClassOrInterfaceDeclaration.class).get(0);

                java.util.List<MethodDeclaration> genMethods = clazz.getMethodsByName("genMethod");
                MethodDeclaration addMethod;
                if (genMethods.size() == 1) {
                    addMethod = genMethods.get(0);
                    addMethod.removeBody();
                } else {
                    addMethod = clazz.addMethod("genMethod", Modifier.Keyword.PUBLIC);
                    // 设置方法参数 String str
                    addMethod.addParameter(String.class, "str");
                    addMethod.setStatic(true);
                    // 设置方法返回值
                    addMethod.setType(String.class);
                }

                // 下面是方法体的实现
                BlockStmt block = new BlockStmt();
                NodeList<VariableDeclarator> varList = new NodeList<>();
                // 定义一个变量 : String s ;
                VariableDeclarator var = new VariableDeclarator(new TypeParameter(String.class.getName()), new SimpleName("s"));
                //
                var.setInitializer("\"" + returnValue + "\"");  // 或 null

                varList.add(var);

                VariableDeclarationExpr statement = new VariableDeclarationExpr(varList);

                String expr = var.getNameAsString() + "+" + addMethod.getParameter(0).getNameAsString();

                ReturnStmt returnStmt = new ReturnStmt(expr);  //  返回 s + str


                // 添加表达式
                block.addStatement(statement);  // 在方法body中添加 代码；

                block.addStatement(returnStmt);         // // 在方法体中添加 返回值语句。

                addMethod.setBody(block);

                //  保存修改到 当前的源文件
                sourceRoot.saveAll();
                mMessager.printMessage(Diagnostic.Kind.WARNING, "---genMethod方法生成成功。。。---- ");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用jdk api 中的 文件工具Filer ，将生成的代码保存到文件。
     * 这样生成的代码就是会编译并打包进apk.
     *
     * @param sourceCode
     */
    private void genClassThenCompiler(String sourceCode) {


        Writer writer = null;
        try {
            JavaFileObject fileObject = filer.createSourceFile("GenClassA");
            writer = fileObject.openWriter();
            writer.write(sourceCode);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 处理注解 ClassAnnotaion
    private void getMyAnnotationInfo(RoundEnvironment env) {

        Set<? extends Element> sets = env.getElementsAnnotatedWith(ClassAnnotaion.class);
        if (sets == null || sets.size() == 0) {
            return;
        }
        for (Element element : sets) {
            // e.getSimpleName().toString() : 类名 MainActivity
            // e.asType().toString()        : 全类名 com.nzh.ast.demo.MainActivity
            // e.getEnclosingElement().toString() ：包名 com.nzh.ast.demo
            // e.getEnclosedElements() : 获取当前类所有信息（包括 方法 ，成员 等等）
//            String s = e.getSimpleName().toString() + "-" + e.asType().toString() + "-" + e.getEnclosingElement().toString();
//            for (Element subE : e.getEnclosedElements()) {
//                mMessager.printMessage(Diagnostic.Kind.WARNING, subE.getSimpleName().toString());
//                if ("onCreate".equals(subE.getSimpleName().toString())) {
//                    genCode(e);
//                }
//            }

            if (element.getKind() == ElementKind.METHOD) {
                String packageName = ((JCTree.JCClassDecl) trees.getTree(element.getEnclosingElement())).sym.toString();
                String annotationValue = (String) getAnnotationParameter(element, MethodAnnotaion.class, "value", null);
                mMessager.printMessage(Diagnostic.Kind.WARNING, " packageName-->" + packageName + "annotationValue-->" + annotationValue);
//                 TestAnalyzer.defineVariable(trees, name, maker, e, varName, List.of(maker.Literal(packageName)));
                // generateVariable(e, Flags.PUBLIC | Flags.STATIC, "abcv", String.class, "-");
            }
        }
    }

    /**
     * 清除类文件中的日志
     *
     * @param roundEnvironment
     */
    private void setCleanLog(RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver() && trees != null) {

            Set<? extends Element> sets = roundEnvironment.getRootElements();

            sets.stream().filter(it -> it.getKind() == ElementKind.CLASS)  // 过滤类
                    .forEach(it -> ((JCTree) trees.getTree(it)).accept(new CleanLogTranslator(maker, mMessager)));
            //for(Element e:sets){
//                JCTree jcTree = (JCTree) trees.getTree(e);
//                jcTree.accept(new CleanLogTranslator(mMessager));
//            }

        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }


    /**
     * 获取注解的值
     *
     * @param element
     * @param annoClass    注解的Class类型
     * @param name         注册中的参数名
     * @param defaultValue 默认值
     * @return
     */
    public static Object getAnnotationParameter(Element element, Class annoClass, String name, Object defaultValue) {
        AnnotationMirror annotationMirror = getAnnotationMirror(element, annoClass.getName());
        if (annotationMirror == null) {
            return defaultValue;
        }

        for (ExecutableElement ee : annotationMirror.getElementValues().keySet()) {
            if (ee.getSimpleName().toString().equals(name)) {
                Object value = annotationMirror.getElementValues().get(ee).getValue();
                if (value instanceof List) {// array
                    List list = (List) value;
                    String result[] = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Attribute.Constant constant = (Attribute.Constant) list.get(i);
                        // this value type possible not String
                        result[i] = (String) constant.getValue();
                    }
                    return result;
                } else if (value instanceof com.sun.tools.javac.code.Type.ClassType) {
                    return value.toString();
                }

                return value;
            }
        }

        return defaultValue;
    }

    public static AnnotationMirror getAnnotationMirror(Element element, String name) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (name.equals(annotationMirror.getAnnotationType().toString())) {
                return annotationMirror;
            }
        }

        return null;
    }
}
