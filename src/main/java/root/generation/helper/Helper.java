package root.generation.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entity.Skeleton;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Helper {

    private final static Logger logger = LoggerFactory.getLogger(Helper.class);

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

    public static String getType(Expression node) {
        String qualifiedName = "Object";
        try {
            ResolvedType resolvedType = node.calculateResolvedType();
            if (resolvedType.isPrimitive()) {
                qualifiedName = resolvedType.asPrimitive().getBoxTypeQName();
            } else if (resolvedType.isReferenceType()) {
                qualifiedName = resolvedType.asReferenceType().getQualifiedName();
            } else {
                logger.error("TBD. Unsupported type of node. 'Object' type will be return.");
            }
        } catch (UnsolvedSymbolException e) {
            logger.error("Dependency lacking! " + e.getMessage() + ", 'Object' type will be returned.");
        }
        return qualifiedName;
    }

    public static Expression constructPrintStmt2Instr(Expression expression) {
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName(new SimpleName("println"));
        FieldAccessExpr fieldAccessExpr = new FieldAccessExpr();
        fieldAccessExpr.setName(new SimpleName("out"));
        NameExpr expr = new NameExpr();
        expr.setName(new SimpleName("System"));
        fieldAccessExpr.setScope(expr);
        methodCallExpr.setScope(fieldAccessExpr);
        NodeList<Expression> nodeList = new NodeList<>();
        nodeList.add(expression);
        methodCallExpr.setArguments(nodeList);
        return methodCallExpr;
    }

    private static String characterTable[] = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
            "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
            "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    public static String getRandomID() {
        int count = 4;
        String id = "";
        for (int i = 0; i < count; i++) {
            int index = new Random().nextInt(characterTable.length - 1);
            id = (id + characterTable[index]);
        }
        return id;
    }

    public static Map<String, String> getJavaSources(Map<Skeleton, CompilationUnit> map) {
        Map<String, String> javaSources = new HashMap<>();
        for (Map.Entry<Skeleton, CompilationUnit> entry :map.entrySet()) {
            String path = entry.getKey().getAbsolutePath();
            if (!path.contains("\\\\") && path.contains("\\")) {
                path = path.replaceAll("\\\\", "\\\\\\\\");
            }
            javaSources.put(path, map.get(entry.getKey()).toString());
        }
        return javaSources;
    }
}

