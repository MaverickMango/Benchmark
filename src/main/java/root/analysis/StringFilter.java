package root.analysis;

import java.util.ArrayList;
import java.util.List;

public class StringFilter {

    public static final int STARTS_WITH = 1;
    public static final int ENDS_WITH = 2;
    public static final int EQUALS = 3;
    public static final int NOT_EQUALS = 6;
    public static final int MATCHES = 4;
    public static final int CONTAINS = 5;

    private final List<String> patterns;
    private final int mode;

    public StringFilter(int mode) {
        patterns = new ArrayList<>();
        this.mode = mode;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setAllPattern(List<String> pattern) {
        this.patterns.addAll(pattern);
    }

    public void addPattern(String pattern) {
        patterns.add(pattern);
    }

    public boolean canMatch(String str) {
        for (String pattern :patterns) {
            switch (mode) {
                case 1:
                    if (str.startsWith(pattern)) {
                        return true;
                    }
                    break;
                case 2:
                    if (str.endsWith(pattern)) {
                        return true;
                    }
                    break;
                case 3:
                    if (str.equals(pattern)) {
                        return true;
                    }
                    break;
                case 4:
                    if (str.matches(pattern)) {
                        return true;
                    }
                    break;
                case 5:
                    if (str.contains(pattern)) {
                        return true;
                    }
                    break;
                case 6:
                    if (!str.equals(pattern)) {
                        return true;
                    }
                default:
            }
        }
        return false;
    }
}