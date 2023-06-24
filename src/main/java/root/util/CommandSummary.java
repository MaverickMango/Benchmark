package root.util;

import java.io.File;
import java.util.*;

public class CommandSummary {

    public LinkedHashMap<String, String> command = new LinkedHashMap<>();

    public CommandSummary() {
    }

    public CommandSummary(String[] pCommand) {
        read(pCommand);
    }

    public void read(String[] pCommand) {
        for (int i = 0; i < pCommand.length; i++) {
            String key = pCommand[i];
            if (key.startsWith("-")) {
                if (i < pCommand.length - 1 && !pCommand[i + 1].startsWith("-")) {
                    command.put(key, pCommand[i + 1]);
                    i++;
                } else
                    command.put(key, null);
            }
        }
    }

    public String[] flat() {
        List<String> values = new ArrayList<>();
        for (String key : this.command.keySet()) {
            values.add(key);
            String v = command.get(key);
            if (v != null)
                values.add(v);
        }
        String[] re = new String[values.size()];
        return values.toArray(re);
    }

    public void append(String k, String v) {
        if (this.command.containsKey(k)) {
            String vold = this.command.get(k);
            this.command.put(k, vold + File.pathSeparator + v);
        } else {
            this.command.put(k, v);
        }
    }
}
