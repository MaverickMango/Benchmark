# Benchmark

Here is an implementation package in order to construct a benchmark of `bug-inducing commit` and `bug-fixing commit` of GitHub repositories. Now It uses [BEARS](https://github.com/bears-bugs/bears-benchmark) to simply get Bug-Fix commits.
> we follow the defination of `bug-inducing commit` and `bug-fixing commit` in [Exploring and exploiting the correlations between bug-inducing and bug-fixing commits](https://dl.acm.org/doi/10.1145/3338906.3338962).

## Todo

1. Checking whether the Bears' commits is a real `bug-inducing commit`. (Details lay in `src/main/java/attempt/Main.java`)
2. Automatically linking `bug-inducing commit` and `bug-fixing commit` from a git repository.
3. Getting fully code message, such as that in `before-inducing commit`.
> `before-inducing commit` which means the commit before `bug-inducing commit`.

## Setup

    - jdk-11 # in order to use refactoring-miner
    - gradle > 7

## How To Use

1. Change settings in `src/main/resources/default.properties`.
2. The example to get diffs between commits lays in `src/test/java/attempt`, where `BearsBugsToolsTest.java` shows the usage with bears' bug.
   (Change the annotation before a test from `@Ignore` to `@Test` when you wanna to run it as a junit test case.)