package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;

public class Patch {
    private String name;
    private final String patchAbsPath;
    private final String patchDir;
    private final CompilationUnit compilationUnit;

    public Patch(String patchAbsPath, String patchDir, CompilationUnit unit) {
        this.patchAbsPath = patchAbsPath;
        this.patchDir = patchDir;
        this.compilationUnit = unit;
    }

    public Patch(String name, String patchAbsPath, String patchDir, CompilationUnit compilationUnit) {
        this.name = name;
        this.patchAbsPath = patchAbsPath;
        this.patchDir = patchDir;
        this.compilationUnit = compilationUnit;
    }

    public String getName() {
        if (name == null) {
            return patchAbsPath;
        }
        return name;
    }

    public String getPatchAbsPath() {
        return patchAbsPath;
    }

    public String getPatchDir() {
        return patchDir;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    @Override
    public String toString() {
        return name;
    }
}
