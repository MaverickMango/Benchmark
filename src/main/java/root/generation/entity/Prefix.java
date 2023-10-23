package root.generation.entity;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * a method declaration of test, the last statement is the one before trigger assert.
 * if all statements before are $ASSERT, Prefix will have an empty body.
 */
public class Prefix {
    MethodDeclaration prefix;

    public Prefix(MethodDeclaration originalMethod, int lineNumber) {
        this.prefix = originalMethod.clone();
    }
}
