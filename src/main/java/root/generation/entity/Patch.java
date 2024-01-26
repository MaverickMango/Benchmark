package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;
import root.util.FileUtils;

public class Patch {
    private String name;
    private final String patchAbsPath;//whole path to postfix
    private final String pathFromRoot;//path to root root
    private final CompilationUnit compilationUnit;
//    private final String content;

    public Patch(String patchAbsPath, String pathFromRoot, CompilationUnit unit) {
        this.patchAbsPath = patchAbsPath;
//        this.content = FileUtils.readFileByLines(patchAbsPath);
        this.pathFromRoot = pathFromRoot;
        this.compilationUnit = unit;
    }

    public Patch(String name, String patchAbsPath, String pathFromRoot, CompilationUnit compilationUnit) {
        this.name = name;
        this.patchAbsPath = patchAbsPath;
//        this.content = FileUtils.readFileByLines(patchAbsPath);
        this.pathFromRoot = pathFromRoot;
        this.compilationUnit = compilationUnit;
    }

    public String getName() {
        if (name == null) {
            this.name = patchAbsPath;
        }
        return name;
    }

    public String getPatchAbsPath() {
        return patchAbsPath;
    }

    public String getPathFromRoot() {
        return pathFromRoot;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    @Override
    public String toString() {
        return getName();
    }
}
