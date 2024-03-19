package root.analysis.soot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SootUpAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(SootUpAnalyzer.class);
//    private final String binJavaDir;
//    private final String binTestDir;
//    private final Set<String> dependencies;
//    private final String complianceLevel;
//    public JavaView view;
//    protected JavaIdentifierFactory identifierFactory;
//    protected static AbstractCallGraphAlgorithm algorithm;
//    private final String algorithmName;
//
    public SootUpAnalyzer(String binJavaDir, String binTestDir,
                    Set<String> dependencies, String complianceLevel) {
//        this.binJavaDir = binJavaDir;
//        this.binTestDir = binTestDir;
//        this.dependencies = dependencies;
//        this.complianceLevel = complianceLevel;
//        algorithmName = "RTA";
////        createViewForClassPath(binJavaDir, binTestDir, dependencies, complianceLevel);
    }
//
//    public void reset() {
//        createViewForClassPath(binJavaDir, binTestDir, dependencies, complianceLevel);
//    }
//
//    private void createViewForClassPath(String binJavaDir, String binTestDir,
//                                        Set<String> dependencies, String complianceLevel) {
//        List<String> classPaths = new ArrayList<>();
//        classPaths.add(binJavaDir);
//        classPaths.add(binTestDir);
//        List<AnalysisInputLocation> inputLocations = new ArrayList<>();
////        inputLocations.add(new DefaultRTJarAnalysisInputLocation());//不分析java自带的类
//        classPaths.forEach(cp -> inputLocations.add(new JavaClassPathAnalysisInputLocation(cp)));
//
//        view = new JavaView(inputLocations);
//        identifierFactory = view.getIdentifierFactory();
//        algorithm = createAlgorithm(view);
//    }
//
//    private AbstractCallGraphAlgorithm createAlgorithm(JavaView view) {
//        if (algorithmName.equals("RTA")) {
//            return new RapidTypeAnalysisAlgorithm(view);
//        } else {
//            return new ClassHierarchyAnalysisAlgorithm(view);
//        }
//    }
//
//    private CallGraph loadCallGraph(MethodSignature entryPoint) {
//        return algorithm.initialize(Collections.singletonList(entryPoint));
//    }
//
//    private JavaClassType getClassSignature(String fullyQualifiedClassName) {
//        return identifierFactory.getClassType(fullyQualifiedClassName);
//    }
//
//    public MethodSignature getMethodSignature(String fullyQualifiedClassName, String methodName,
//                                              String fqReturnType, List<String> parameterTypes) {
//        if (view == null) {
//            reset();
//        }
//        JavaClassType classSignature = getClassSignature(fullyQualifiedClassName);
//        SootClass sc = view.getClass(classSignature).orElse(null);
//        if (sc == null) {
//            StringBuilder stringBuilder = new StringBuilder(fullyQualifiedClassName);
//            int index = fullyQualifiedClassName.lastIndexOf(".");
//            stringBuilder.replace(index, index + 1, "$");
//            fullyQualifiedClassName = stringBuilder.toString();
//            classSignature = getClassSignature(fullyQualifiedClassName);
//            sc = view.getClass(classSignature).orElse(null);
//            if (sc == null) {
//                logger.error("Can not get Soot class of " + fullyQualifiedClassName);
//                return null;
//            }
//        }
//
//        MethodSignature mainMethodSignature = getMthSig(classSignature, sc, methodName, fqReturnType, parameterTypes);
//        if (mainMethodSignature == null) {
//            StringBuilder stringBuilder = new StringBuilder(fqReturnType);
//            int index = fqReturnType.lastIndexOf(".");
//            stringBuilder.replace(index, index + 1, "$");
//            String newFqRet = stringBuilder.toString();
//            mainMethodSignature = getMthSig(classSignature, sc, methodName, newFqRet, parameterTypes);
//            if (mainMethodSignature == null) {
//                logger.error(mainMethodSignature + " not found in classloader");
//                return null;
//            }
//        }
//        return mainMethodSignature;
//    }
//
//    private MethodSignature getMthSig(JavaClassType classSignature, SootClass sc,
//                                      String methodName, String fqReturnType, List<String> parameterTypes) {
//        MethodSignature mainMethodSignature = identifierFactory.getMethodSignature(
//                classSignature, methodName, fqReturnType, parameterTypes
//        );
//        SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
//        return m != null ? mainMethodSignature : null;
//    }
//
//    public List<List<MethodSignature>> getPathFromEntryToOut(MethodSignature entryPoint, MethodSignature outerPoint) {
//        if (view == null) {
//            reset();
//        }
//        CallGraph callGraph = loadCallGraph(entryPoint);
//        List<List<MethodSignature>> entryToOut = new ArrayList<>();
//        List<MethodSignature> path = new ArrayList<>();
//        backForwardDfs(callGraph, entryPoint, outerPoint, path, entryToOut, new HashSet<>());
//        return entryToOut;
//    }
//
//    public static void backForwardDfs(CallGraph callGraph, MethodSignature entryPoint, MethodSignature current,
//                                      List<MethodSignature> path, List<List<MethodSignature>> entryToOut,
//                                      Set<MethodSignature> usedSigs) {
//        if (usedSigs.contains(current)) {//callgraph可能会有环
//            return;
//        }
//        path.add(0, current);
//        usedSigs.add(current);
//        if (current.toString().equals(entryPoint.toString())) {
//            entryToOut.add(path);
//            return;
//        }
//        List<MethodSignature> currentPath = new ArrayList<>(path);
//        Set<MethodSignature> methodSignatures = callGraph.callsTo(current);
//        for (MethodSignature mth :methodSignatures) {
//            backForwardDfs(callGraph, entryPoint, mth, path, entryToOut, usedSigs);
//            path = new ArrayList<>(currentPath);
////            usedSigs = new HashSet<>(path);//不加这一句只会有一条路径，每次不一样；加了之后不停机。
//        }
//    }
}
