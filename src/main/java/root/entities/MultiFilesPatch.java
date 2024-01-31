package root.entities;

import com.github.javaparser.ast.CompilationUnit;

import java.util.HashSet;
import java.util.List;

public class MultiFilesPatch extends Patch {

    List<SingleFilePatch> allSingleFiles;

    public MultiFilesPatch(String patchAbsPath, String pathFromRoot, CompilationUnit unit) {
        this(null, patchAbsPath, pathFromRoot, unit);
    }

    public MultiFilesPatch(String name, String patchAbsPath, String pathFromRoot, CompilationUnit unit) {
        super(name, patchAbsPath, pathFromRoot, unit);
        this.setSingleFile(false);
        SingleFilePatch singleFile = new SingleFilePatch(patchAbsPath, pathFromRoot, unit);
        allSingleFiles.add(singleFile);
    }

    public void addSingleFile(String patchAbsPath, String pathFromRoot, CompilationUnit unit) {
        SingleFilePatch singleFile = new SingleFilePatch(patchAbsPath, pathFromRoot, unit);
        allSingleFiles.add(singleFile);
    }

    public List<SingleFilePatch> getAllSingleFiles() {
        return allSingleFiles;
    }

    @Override
    public String toString() {
        return (1 + allSingleFiles.size()) + "-" + this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MultiFilesPatch) {
            MultiFilesPatch that = (MultiFilesPatch) obj;
            if (this.allSingleFiles.size() != that.allSingleFiles.size())
                return false;
            return new HashSet<>(this.allSingleFiles).containsAll(that.allSingleFiles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.allSingleFiles.hashCode();
        return result;
    }
}
