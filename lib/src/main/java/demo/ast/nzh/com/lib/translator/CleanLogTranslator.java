package demo.ast.nzh.com.lib.translator;

import com.sun.codemodel.internal.JStatement;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;


import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

import static com.sun.tools.javac.util.List.nil;

/**
 * Created by 31414 on 2019/8/28.
 */

// 3: 自定义一个 TreeTranslator。
public class CleanLogTranslator extends TreeTranslator {
    public static final String LOG_TAG = "Log."; //
    public static final String SYSTEM_OUT_TAG = "System.out."; //System.out.
    Messager messager;
    TreeMaker maker;

    public CleanLogTranslator(TreeMaker maker, Messager messager) {
        this.messager = messager;
        this.maker = maker;
    }

    /**
     * 所有 visit 方法一目了然，我们前面提到 AST 每一个节点都代表着源语言中的一个语法结构，
     * 所以我们可以细粒度到指定访问 if、return、try等特定类型节点，只需覆写相应的 visit 方法。
     * <p>
     * 回到我们的需求场景：扫描所有 log 语句，既然是语句，粒度应该为语句块，
     * 所以我们覆写 visitBlock 进行扫描，当扫描到指定语句比如 Log. 时，
     * 就不把整个语句都写入 AST，以此达到清除 log 语句的效果。
     *
     * @param jcBlock
     */
    @Override
    public void visitBlock(JCTree.JCBlock jcBlock) {  // 过滤方法体中的 代码块
        super.visitBlock(jcBlock);

        List<JCTree.JCStatement> statements = jcBlock.getStatements();

        if (statements == null || statements.isEmpty()) {
            return;
        }

        // 创建新的out对象保存过滤后的代码
        List<JCTree.JCStatement> out = nil();
        for (JCTree.JCStatement jcStatement : statements) {

            if (isLogStatements(jcStatement)) {  // 命中则不保存 当前 语句块 。
                // 在gradle 编译中打印日志：
                messager.printMessage(Diagnostic.Kind.WARNING, "LogClear:" + this.getClass().getCanonicalName() + "->" + jcStatement.toString());
            } else {
                out = out.append(jcStatement);
            }
        }
        // 重新写入ast 语法树中
        jcBlock.stats = out;

    }

    // 过滤指定的的 方法 和 成员变量
    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        List<JCTree> classTree = jcClassDecl.defs;

        List<JCTree> newTree = nil(); // 创建新的List,来保存过滤后的 类信息。

        for (JCTree t : classTree) {

            if (t.getKind() == Tree.Kind.METHOD) {

                if (t.toString().contains("public void test()") || t.toString().contains("public int test(int a)")) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "过滤方法-->" + jcClassDecl.name + "-->" + t.toString());
                    continue;
                }

            }
            if (t.getKind() == Tree.Kind.VARIABLE) {
                if (t.toString().contains("String abc") || t.toString().contains("String def")) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "过滤成员变量-->" + jcClassDecl.name + "-->" + t.toString());
                    continue;
                }
            }
            newTree = newTree.append(t);

        }

        jcClassDecl.defs = newTree;
    }

    // new thread 检测
    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);

        if (jcMethodDecl.body == null) {
            return;
        }
        List<JCTree.JCStatement> statements = jcMethodDecl.body.stats;
        for (JCTree.JCStatement s : statements) {

            if (s.getKind() == Tree.Kind.VARIABLE && s.toString().contains("new Thread")) {
                messager.printMessage(Diagnostic.Kind.WARNING, "--代码中含有 new Thread--" + jcMethodDecl.name + "(" + jcMethodDecl.params.toString() + ")");
            }
            if (s.getKind() == Tree.Kind.EXPRESSION_STATEMENT
                    && s.toString().contains("new Thread")
                    && s.toString().contains(".start();")) {
                messager.printMessage(Diagnostic.Kind.WARNING, "--代码中含有 new Thread--" + jcMethodDecl.name + "(" + jcMethodDecl.params.toString() + ")");
            }
        }

    }

    /**
     * 判断是否是 打印日志
     *
     * @param jcStatement
     * @return
     */
    private boolean isLogStatements(JCTree.JCStatement jcStatement) {
        return jcStatement.toString().contains(LOG_TAG) || jcStatement.toString().contains(SYSTEM_OUT_TAG);
    }
}
