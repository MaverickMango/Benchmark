package root.entities;

public class Patch {
    String name;
    String patchAbsPath;//whole path to postfix
    String pathFromRoot;//path to root root
    String content;
//    private final String content;
    boolean isSingleFile;

    public Patch(String patchAbsPath, String pathFromRoot, String content) {
        this(null, patchAbsPath, pathFromRoot, content);
    }

    public Patch(String name, String patchAbsPath, String pathFromRoot, String content) {
        this.name = name;
        this.patchAbsPath = patchAbsPath;
//        this.content = FileUtils.readFileByLines(patchAbsPath);
        this.pathFromRoot = pathFromRoot;
        this.content = content;
        this.setSingleFile(true);
    }

    public String getName() {
        if (name == null) {
            this.name = patchAbsPath;
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

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getName();
    }
}
