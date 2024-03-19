package root.generation.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.entities.Skeleton;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

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
                return methodCallExpr.getNameAsString().equals("assertNotNull") ||
                        methodCallExpr.getNameAsString().equals("assertTrue") ||
                        methodCallExpr.getNameAsString().equals("assertFalse") ||
                        methodCallExpr.getNameAsString().equals("assertEquals") ||
                        methodCallExpr.getNameAsString().equals("assertNotEquals") ||
                        methodCallExpr.getNameAsString().equals("fail") ||
                        methodCallExpr.getNameAsString().equals("check");
            }
        }

        return false;
    }

    public static boolean isReferenceType(Expression expr) {
        ResolvedType resolvedType = getResolvedType(expr);
        return resolvedType != null && resolvedType.isReferenceType();
    }

    public static ResolvedType getResolvedType(Expression node) {
        if (node.isUnaryExpr()) {
            return getResolvedType(node.asUnaryExpr().getExpression());
        }
        try {
            return node.calculateResolvedType();
        } catch (UnsolvedSymbolException e) {
            logger.error("Dependency lacking! " + e.getMessage() + ", 'Object' type will be returned.");
        } catch (UnsupportedOperationException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static String getType(Expression node) {
        if (node.isUnaryExpr()) {
            return getType(node.asUnaryExpr().getExpression());
        }
        String qualifiedName = "Object";
        ResolvedType resolvedType = getResolvedType(node);
        if (resolvedType == null) {
            logger.error("TBD. Unsupported type of node. 'Object' type will be return.");
        } else if (resolvedType.isPrimitive()) {
            qualifiedName = resolvedType.asPrimitive().getBoxTypeQName();
        } else if (resolvedType.isReferenceType()) {
            qualifiedName = resolvedType.asReferenceType().getQualifiedName();
        }
        return qualifiedName;
    }

    public static int getLine(CtElement n, boolean isStart) {
        SourcePosition position = n.getPosition();
        if (position.isValidPosition()) {
            return isStart ? position.getLine() : position.getEndLine();
        }
        return -1;
    }

    public static boolean isCondition(Node n) {
        return n instanceof Expression && "java.lang.Boolean".equals(Helper.getType((Expression) n));
    }

    public static Statement constructFileOutputStmt2Instr(String filePath, Expression expression) {
        BlockStmt blockStmt = new BlockStmt();
        NodeList<Statement> statements = new NodeList<>();
        /*
        File file = new File(filename);
        */
        ExpressionStmt expressionStmt = new ExpressionStmt();
        VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr();
        NodeList<VariableDeclarator> vars = new NodeList<>();
        VariableDeclarator variableDeclarator = new VariableDeclarator();
        variableDeclarator.setName(new SimpleName("file"));
        variableDeclarator.setType("File");
        ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
        objectCreationExpr.setType("File");
        NodeList<Expression> nodeList = new NodeList<>();
        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr(filePath);
        nodeList.add(stringLiteralExpr);
        objectCreationExpr.setArguments(nodeList);
        variableDeclarator.setInitializer(objectCreationExpr);
        vars.add(variableDeclarator);
        variableDeclarationExpr.setVariables(vars);
        expressionStmt.setExpression(variableDeclarationExpr);
        statements.add(expressionStmt);
        /*
        try {
            BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(filename, true));
            buff.write(expression.getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
        TryStmt tryStmt = new TryStmt();
        BlockStmt blockStmt0 = new BlockStmt();
        NodeList<Statement> blockStmts = new NodeList<>();
        variableDeclarationExpr = new VariableDeclarationExpr();
        vars = new NodeList<>();
        variableDeclarator = new VariableDeclarator();
        variableDeclarator.setName("buff");
        variableDeclarator.setType("BufferedOutputStream");
        objectCreationExpr = new ObjectCreationExpr();
        objectCreationExpr.setType("BufferedOutputStream");
        nodeList = new NodeList<>();
        ObjectCreationExpr objectCreationExpr1 = new ObjectCreationExpr();
        objectCreationExpr1.setType("FileOutputStream");
        NodeList<Expression> nodeList1 = new NodeList<>();
        nodeList1.add(stringLiteralExpr);
        nodeList1.add(new BooleanLiteralExpr(true));
        objectCreationExpr1.setArguments(nodeList1);
        nodeList.add(objectCreationExpr1);
        objectCreationExpr.setArguments(nodeList);
        variableDeclarator.setInitializer(objectCreationExpr);
        vars.add(variableDeclarator);
        variableDeclarationExpr.setVariables(vars);
        blockStmts.add(new ExpressionStmt(variableDeclarationExpr));

        expressionStmt = new ExpressionStmt();
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setScope(new NameExpr("buff"));
        methodCallExpr.setName("write");
        nodeList = new NodeList<>();
        MethodCallExpr methodCallExpr1 = new MethodCallExpr();
        methodCallExpr1.setScope(expression);
        methodCallExpr1.setName("getBytes");
        nodeList1 = new NodeList<>();
        FieldAccessExpr fieldAccessExpr = new FieldAccessExpr();
        fieldAccessExpr.setScope(new NameExpr("StandardCharsets"));
        fieldAccessExpr.setName("UTF_8");
        nodeList1.add(fieldAccessExpr);
        methodCallExpr1.setArguments(nodeList1);
        nodeList.add(methodCallExpr1);
        methodCallExpr.setArguments(nodeList);
        expressionStmt.setExpression(methodCallExpr);
        blockStmts.add(expressionStmt);

        expressionStmt = new ExpressionStmt();
        methodCallExpr = new MethodCallExpr();
        methodCallExpr.setScope(new NameExpr("buff"));
        methodCallExpr.setName("flush");
        expressionStmt.setExpression(methodCallExpr);
        blockStmts.add(expressionStmt);

        expressionStmt = new ExpressionStmt();
        methodCallExpr = new MethodCallExpr();
        methodCallExpr.setScope(new NameExpr("buff"));
        methodCallExpr.setName("close");
        expressionStmt.setExpression(methodCallExpr);
        blockStmts.add(expressionStmt);

        blockStmt0.setStatements(blockStmts);
        tryStmt.setTryBlock(blockStmt0);

        NodeList<CatchClause> catchClauses = new NodeList<>();
        CatchClause catchClause = new CatchClause();
        BlockStmt blockStmt1 = new BlockStmt();
        NodeList<Statement> catchStmts = new NodeList<>();
        methodCallExpr = new MethodCallExpr();
        NameExpr nameExpr = new NameExpr("e");
        methodCallExpr.setScope(nameExpr);
        methodCallExpr.setName("printStackTrace");
        expressionStmt = new ExpressionStmt(methodCallExpr);
        catchStmts.add(expressionStmt);
        blockStmt1.setStatements(catchStmts);
        catchClause.setBody(blockStmt1);
        Parameter exception = new Parameter();
        exception.setName(new SimpleName("e"));
        exception.setType("IOException");
        catchClause.setParameter(exception);
        catchClauses.add(catchClause);
        tryStmt.setCatchClauses(catchClauses);

        statements.add(tryStmt);
        blockStmt.setStatements(statements);
        return blockStmt;
    }

    public static Statement constructPrintStmt2Instr(Expression expression) {
        //can't get this from defects4j: System.out.println(${expression});
        ExpressionStmt stmt = new ExpressionStmt();
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
        stmt.setExpression(methodCallExpr);
        return stmt;
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

