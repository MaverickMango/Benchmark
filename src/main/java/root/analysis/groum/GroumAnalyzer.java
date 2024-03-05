package root.analysis.groum;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import root.analysis.groum.entity.IntraGroum;
import root.analysis.groum.extractor.PreOrderVisitorInMth;

import java.util.ArrayList;

public class GroumAnalyzer {

    public static IntraGroum innerAnalysis(CallableDeclaration mth) {
        PreOrderVisitorInMth visitor = new PreOrderVisitorInMth(false);
        ArrayList<IntraGroum> arg = new ArrayList<>();
        visitor.buildGraph(mth, arg, true);
        assert arg.size() == 1;
        IntraGroum groum = arg.get(0);
        return groum;
    }

}
