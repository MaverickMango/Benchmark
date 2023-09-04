package root.analysis;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;
import root.util.FileUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ASTManipulatorTest {

    @Test
    void getFunctionSig() {
        String source = "src/main/java/";
        String cls = "build/classes/java/main/";
        String filePath = "root/analysis/RefactoringMiner.java";
        ASTManipulator manipulator = new ASTManipulator(8);
        Set<Integer> pos = new HashSet<>();
        pos.add(46);
        Set<MethodDeclaration> methodDeclarations = manipulator.extractMethodByPos(FileUtils.readFileByChars(source + filePath), pos, true);
        for (MethodDeclaration mth :methodDeclarations) {
            String functionSig = manipulator.getFunctionSig(mth);
            System.out.println(functionSig);
        }
    }
}