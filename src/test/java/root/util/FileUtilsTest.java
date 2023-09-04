package root.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void getPositionsOfDiff() {
        String inducingDiffFile = "data/changesInfo/Time_23/patches/inducing.diff";
        Set<Integer> ori_pos = new HashSet<>();
        Set<Integer> bic_pos = new HashSet<>();
        FileUtils.getPositionsOfDiff(FileUtils.readEachLine(inducingDiffFile), ori_pos, bic_pos, true);
    }
}