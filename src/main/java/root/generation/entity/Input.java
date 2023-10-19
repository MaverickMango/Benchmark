package root.generation.entity;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;

public class Input {

    private Class<? extends Expression> type;
    private Expression inputExpr;

    public Input(Expression inputExpr) {
        this.inputExpr = inputExpr;
        //todo: how to determine the type of inputExpr?
        //this.type = ?
    }

    public Input(Class<? extends Expression> type, Expression inputExpr) {
        this.type = type;
        this.inputExpr = inputExpr;
    }

    public Class<? extends Expression> getType() {
        return type;
    }

    public void setType(Class<? extends Expression> type) {
        this.type = type;
    }

    public Expression getInputExpr() {
        return inputExpr;
    }

    public void setInputExpr(Expression inputExpr) {
        this.inputExpr = inputExpr;
    }
}
