package root.entities.otherdataset;

import root.entities.benchmarks.Defects4JBug;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class D4JBug extends Defects4JBug {
    private String proj;
    private String id;
    private String fixingCommit;
    private String buggyCommit;
    private boolean canBeRepaired = false;
    private Map<String, List<String>> repairTools; //<correct|plausible, toolName>

    public D4JBug(String proj, String id, String workingDir) {
        super(proj, id, workingDir);
        this.proj = proj;
        this.id = id;
        setFixingCommit(getFixingCommit());
        setBuggyCommit(getBuggyCommit());
    }

    public String getProj() {
        return proj;
    }

    public void setProj(String proj) {
        this.proj = proj;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFixingCommit() {
        if (fixingCommit == null)
            return super.getFixingCommit();
        return fixingCommit;
    }

    public void setFixingCommit(String fixingCommit) {
        this.fixingCommit = fixingCommit;
    }

    public String getBuggyCommit() {
        if (buggyCommit == null)
            return super.getBuggyCommit();
        return buggyCommit;
    }

    public void setBuggyCommit(String buggyCommit) {
        this.buggyCommit = buggyCommit;
    }

    public boolean canBeRepaired() {
        return canBeRepaired;
    }

    private void setCanBeRepaired() {
        this.canBeRepaired = true;
        this.repairTools = new HashMap<>();
    }

    public Map<String, List<String>> getRepairTools() {
        return repairTools;
    }

    public void setRepairTools(Map<String, List<String>> repairTools) {
        setCanBeRepaired();
        this.repairTools = repairTools;
    }

    public void addRepairTool(String toolName, String repairMode) {
        if (!canBeRepaired())
            setCanBeRepaired();
        repairTools.computeIfAbsent(repairMode, k -> new ArrayList<>());
        repairTools.get(repairMode).add(toolName);
    }

    @Override
    public String toString() {
        StringBuilder json = new StringBuilder("{");
        json.append("proj").append(":").append(proj).append(",")
                .append("id").append(":").append(id).append(",")
                .append("fixingCommit").append(":").append(fixingCommit).append(",")
                .append("buggyCommit").append(":").append(buggyCommit).append(",")
                .append("repairTools").append(":").append("{");
        int count = 0;
        for (Map.Entry<String, List<String>> entry :repairTools.entrySet()) {
            String mode = entry.getKey();
            json.append(mode).append(":'");
            List<String> tools = entry.getValue();
            for (String tool :tools) {
                json.append(tool).append(" ");
            }
            json.deleteCharAt(json.length() - 1);
            json.append("',");
            count += tools.size();
        }
        json.append("total_num:").append(count);
        json.append("}}");
        return FileUtils.jsonFormatter(json.toString());
    }
}
