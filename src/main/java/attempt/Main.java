package attempt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        /*
         [todo] make sure the commit('before-inducing commit') before bug-inducing commit has passed all test cases while bug-inducing commit has failed.
            1. switch to the original repository to get the before-inducing commit.
            2. move test cases to the before-inducing commit.
            3. check its precision.
         */
        logger.info("Hello world");
    }
}
