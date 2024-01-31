package root.entities;

import com.github.javaparser.ast.CompilationUnit;

public class SingleFilePatch extends Patch {

    public SingleFilePatch(String patchAbsPath, String pathFromRoot, CompilationUnit unit) {
        super(patchAbsPath, pathFromRoot, unit);
    }

    public SingleFilePatch(String name, String patchAbsPath, String pathFromRoot, CompilationUnit compilationUnit) {
        super(name, patchAbsPath, pathFromRoot, compilationUnit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SingleFilePatch) {
            SingleFilePatch that = (SingleFilePatch) obj;
            return this.patchAbsPath.equals(that.patchAbsPath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.patchAbsPath.hashCode();
        result = prime * result + this.pathFromRoot.hashCode();
        result = prime * result + this.unit.hashCode();
        return result;
    }
}
