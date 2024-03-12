package root.entities;

import com.github.javaparser.ast.CompilationUnit;

public class Patch {
    String name;
    String patchAbsPath;//whole path to postfix
    String pathFromRoot;//path to root root
    CompilationUnit unit;
//    private final String content;
    boolean isSingleFile;

    public Patch(String patchAbsPath, String pathFromRoot, CompilationUnit content) {
        this(null, patchAbsPath, pathFromRoot, content);
    }

    public Patch(String name, String patchAbsPath, String pathFromRoot, CompilationUnit content) {
        this.name = name;
        this.patchAbsPath = patchAbsPath;
//        this.content = FileUtils.readFileByLines(patchAbsPath);
        this.pathFromRoot = pathFromRoot;
        this.unit = content;
        this.setSingleFile(true);
    }

    public String getName() {
        if (name == null) {
            this.name = String.valueOf(Stats.counter ++);
        }
        return name;
    }

    public boolean isSingleFile() {
        return isSingleFile;
    }

    public void setSingleFile(boolean singleFile) {
        isSingleFile = singleFile;
    }

    public String getPatchAbsPath() {
        return patchAbsPath;
    }

    public String getPathFromRoot() {
        return pathFromRoot;
    }

    public CompilationUnit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return getName();
    }
}
