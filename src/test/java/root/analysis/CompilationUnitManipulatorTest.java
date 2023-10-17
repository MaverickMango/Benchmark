package root.analysis;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;
import root.util.FileUtils;

import java.util.HashSet;
import java.util.Set;

class CompilationUnitManipulatorTest {

    @Test
    void getFunctionSig() {
        String source = "src/main/java/";
        String cls = "build/classes/java/main/";
        String filePath = "root/analysis/RefactoringMiner.java";
        CompilationUnitManipulator manipulator = new CompilationUnitManipulator(8);
        MethodManipulator methodManipulator = new MethodManipulator();
        Set<Integer> pos = new HashSet<>();
        pos.add(46);
        Set<MethodDeclaration> methodDeclarations = manipulator.extractMethodByPos(FileUtils.readFileByChars(source + filePath), pos, true);
        for (MethodDeclaration mth :methodDeclarations) {
            String functionSig = methodManipulator.getFunctionSig(mth);
            System.out.println(functionSig);
        }
    }
}