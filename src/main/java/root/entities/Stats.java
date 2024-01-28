package root.entities;

import java.util.HashMap;
import java.util.Map;

public class Stats {

    public enum General {
        INITIALIZATION_TIME, DIFF_TIME, GENERATION_TIME, VALIDATION_TIME, TOTAL_TIME,
        GENERATED_MUTANTS, COMPILED_MUTANTS, FAILED_MUTANTS
    }
    private static Stats currentStats;
    Map<General, Object> generalStats;

    Stats() {
        generalStats = new HashMap<>();
    }

    public static Stats getCurrentStats() {
        if (currentStats == null) {
            currentStats = new Stats();
        }
        return currentStats;
    }

    public void addGeneralStat(General item, Object value) {
        getCurrentStats().generalStats.put(item, value);
    }
}
