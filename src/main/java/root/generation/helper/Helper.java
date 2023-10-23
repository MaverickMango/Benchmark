package root.generation.helper;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class Helper {

    public static URL[] getURLs(Collection<String> paths) throws MalformedURLException {
        URL[] urls = new URL[paths.size()];
        int i = 0;
        for (String path : paths) {
            File file = new File(path);
            URL url = file.toURI().toURL();
            urls[i++] = url;
        }

        return urls;
    }

    public static boolean isAbstractClass(Class<?> target) {
        int mod = target.getModifiers();
        boolean isAbstract = Modifier.isAbstract(mod);
        boolean isInterface = Modifier.isInterface(mod);

        if (isAbstract || isInterface)
            return true;

        return false;
    }

    public static boolean isAssertion(Statement stmt) {
        if (stmt instanceof ExpressionStmt) {
            ExpressionStmt exprStmt = (ExpressionStmt)stmt;
            if (exprStmt.getExpression() instanceof MethodCallExpr) {
                MethodCallExpr methodCallExpr = (MethodCallExpr)exprStmt.getExpression();
                if (methodCallExpr.getNameAsString().equals("assertNotNull") ||
                        methodCallExpr.getNameAsString().equals("assertTrue") ||
                        methodCallExpr.getNameAsString().equals("assertFalse") ||
                        methodCallExpr.getNameAsString().equals("assertEquals") ||
                        methodCallExpr.getNameAsString().equals("assertNotEquals") ||
                        methodCallExpr.getNameAsString().equals("fail") ||
                        methodCallExpr.getNameAsString().equals("check")) {
                    return true;
                }
            }
        }

        return false;
    }
}

