package root.entities;

import com.google.gson.Gson;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stats {
    public static int counter = 0;

    public enum General {
        INITIALIZATION_TIME, DIFF_TIME, GENERATION_TIME, VALIDATION_TIME, TOTAL_TIME,
        GENERATED_MUTANTS, COMPILED_MUTANTS, FAILED_MUTANTS
    }

    public enum Patch {
        PATH_FLOW, GENERATED_TESTS
    }

    private static Stats currentStats;
    Map<General, Object> generalStats;
    Map<String, PatchStats> patchStats;

    Stats() {
        generalStats = new HashMap<>();
        patchStats = new HashMap<>();
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

    public void addPatchStat(String patchName, Patch item, Object value) {
        if (!getCurrentStats().patchStats.containsKey(patchName)) {
            getCurrentStats().patchStats.put(patchName, new PatchStats(patchName));
        }
        PatchStats stats = getCurrentStats().patchStats.get(patchName);
        stats.addStats(item, value);
    }

    @Override
    public String toString() {
        return FileUtils.jsonFormatter("{" +
                "generalStats: " + FileUtils.bean2Json(generalStats) + "," +
                "patchStats:" + FileUtils.bean2Json(patchStats) +
                '}');
    }

    static class PatchStats {
        String name;
        Map<Patch, Object> stats;

        public PatchStats(String name) {
            this.name = name;
            stats = new HashMap<>();
        }

        public void addStats(Patch stat, Object value) {
            stats.put(stat, value);
        }

        @Override
        public String toString() {
            return FileUtils.bean2Json(stats);
        }
    }
}
