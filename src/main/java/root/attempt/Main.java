package root.attempt;

import org.eclipse.jgit.lib.Repository;
import root.bean.benchmarks.Defects4JBug;

public class Main {
    public static void main(String[] args) {
        String proj = args[0];
        String id = args[1];
        String version = args[2];
        String workingDir = args[3];
        String targetCommit = args[4];
        Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir);
        Repository b = defects4JBug.getGitRepository("b");
        defects4JBug.switchAndClean(b, targetCommit, version, null);
    }
}
