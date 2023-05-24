# Benchmark

---

Here is an implementation to manipulate Git repositories. It uses [BEARS](https://github.com/bears-bugs/bears-benchmark) to simply get Bug-Fix commits.

## Setup

    - jdk-11 # in order to use refactoring-miner
    - gradle > 7

## How To Use

1. Change bears' settings in `src/main/resources/default.properties` if you would like to check bugs in bears.
```
# bears-benchmark root directory
bears-benchmark=/home/liumengjiao/Desktop/CI/bears-benchmark/
```
2. The example to get diffs between commits lays in `src/test/java/attempt`, where `BearsBugsToolsTest.java` shows the usage with bears' bug.
   (Change the annotation before a test from `@Ignore` to `@Test` when you wanna to run it as a junit test case.)
4. some todos list in `src/main/java/attempt/Main.java`.