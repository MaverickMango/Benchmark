package root.script;

import org.junit.Test;
import root.util.FileUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MethodTest {

    public int binarySearch(int[] lookUpTable, int target) {
        int low = 0;
        int high = lookUpTable.length - 1;
        int middle;
        if (lookUpTable[low] > target || lookUpTable[high] < target) {
            return -1;
        }
        while (low != high) {
            middle = (low + high) >> 1;
            if (target < lookUpTable[middle]) {
                high = middle;
            } else if (target > lookUpTable[middle]) {
                low = middle;
            } else {
                return middle;
            }
        }
        return -1;
    }

    @Test
    public void test() {
        List<String> diff = FileUtils.readEachLine("/home/liumengjiao/Desktop/CI/Benchmark/data/changesInfo/Closure_80/patches/inducing.diff");
        List<String> realDiff = diff.stream().filter(
                line -> !DiffMain.filter(line, null)
        ).collect(Collectors.toList());
        if (!realDiff.isEmpty()) {
            System.out.println(realDiff.size());
        }
    }
}
