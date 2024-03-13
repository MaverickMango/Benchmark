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
        DIFF, PATH_FLOW, GENERATED_TESTS
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

    public Map<General, Object> getGeneralStats() {
        return generalStats;
    }

    public Map<String, PatchStats> getPatchStats() {
        return patchStats;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        for (Map.Entry<String, PatchStats> entry : patchStats.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue().toString()).append(",");
        }
        stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "}");
        String uglyJsonStr = "{" +
                "generalStats: " + FileUtils.bean2Json(generalStats) + "," +
                "patchStats:" + stringBuilder +
                '}';
        return FileUtils.jsonFormatter(uglyJsonStr);
    }

    public static class PatchStats {
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
            StringBuilder stringBuilder = new StringBuilder("{");
            for (Map.Entry<Patch, Object> entry : stats.entrySet()) {
                stringBuilder.append(entry.getKey()).append(":").append(entry.getValue().toString()).append(",");
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "}");
            return FileUtils.jsonFormatter(stringBuilder.toString());
        }
    }
}
