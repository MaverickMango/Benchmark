package root.script;

import org.eclipse.jgit.lib.Repository;
import root.entity.benchmarks.Defects4JBug;

public class Main {
    public static void main(String[] args) {
        String proj = args[0];
        String id = args[1];
        String version = args[2];
        String workingDir = args[3];
        String targetCommit = args[4];
        Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir);
        Repository b = defects4JBug.getGitRepository("b");
        boolean res = defects4JBug.switchAndClean(b, targetCommit, version, null);
        if (!res) {
            System.out.println(-1);
            System.exit(-1);
        }
        System.out.println(0);
    }
}
