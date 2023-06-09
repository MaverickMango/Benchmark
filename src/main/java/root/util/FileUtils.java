package root.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.eclipse.jgit.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class FileUtils {

    static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    static final int defaultTimeoutSecond = 300;

    public static String formatSeparator(String filePath) {
        return filePath.replaceAll("/", File.separator);
    }

    public static StringBuilder getMap2String(Map<?, ?> map) {
        Set<?> keySet = map.keySet();
        Iterator<?> iterator = keySet.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while(iterator.hasNext()) {
            Object obj = iterator.next();
            stringBuilder.append("<").append(obj).append(", ");
            Object value = map.get(obj);
            if (value instanceof Collection) {
                stringBuilder.append(((Collection<?>) value).size()).append("-");
            }
            stringBuilder.append(value).append(">").append("\n");
        }
        return stringBuilder;
    }

    public static <T> StringBuilder getStrOfIterable(@NonNull T items, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        if (items instanceof Iterable) {
            for (Object obj :(Iterable<?>)items) {
                stringBuilder.append(getStrOfIterable(obj, separator));
            }
        } else {
            stringBuilder.append(items).append(separator);
        }
        return stringBuilder;
    }

    public static void outputMap2File(Map<?, ?> map, String filePath, boolean append) {
        StringBuilder stringBuilder = getMap2String(map);
        FileUtils.writeToFile(stringBuilder.toString(), filePath, append);
    }

    public static void copy(File srcFile, File dstFile) {
        if (!srcFile.exists()) {
            logger.error("srcPath does not exist!");
            return;
        }
        if (srcFile.isFile()) {
            copyFile(srcFile, dstFile);
        } else if(srcFile.isDirectory()) {//拷贝多文件目录可能还是直接调cp命令快一点…
            //todo: test this branch!
            File[] subFiles = srcFile.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    copy(subFile, dstFile);
                }
            }
        }
    }

    private static void copyFile(File srcFile, File dstFile) {
        if (!dstFile.exists() || dstFile.isDirectory()) {
            boolean res = false;
            if (dstFile.isDirectory() && !dstFile.exists()) {
                res = dstFile.mkdirs();
                dstFile = new File(dstFile, srcFile.getName());
            } else if (!dstFile.isDirectory() && !dstFile.getParentFile().exists()) {
                res = dstFile.getParentFile().mkdirs();
            } else {
                dstFile = new File(dstFile, srcFile.getName());
                res = dstFile.getParentFile().exists();
            }
            if (!res) {
                logger.error("Could not create the dstFile file directories!");
                return;
            }
        }
        try (InputStream is = new FileInputStream(srcFile);
             OutputStream os = new FileOutputStream(dstFile)) {
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
        } catch (Exception e) {
            logger.error("Error occurred when copy file " + srcFile.getAbsolutePath() + " to " + dstFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    public static void writeToFile(String content, String filename, boolean append) {
        filename = formatSeparator(filename);
        File file = new File(filename);
        try {
            if (!file.getParentFile().exists()) {
                boolean res = file.getParentFile().mkdirs();
                if (!res) {
                    logger.error("Could not create the target file directories!");
                    return;
                }
            }
            BufferedOutputStream buff;
            buff = new BufferedOutputStream(new FileOutputStream(filename, append));
            buff.write(content.getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static char[] readFileByChars(String fileName) {
        fileName = formatSeparator(fileName);
        Reader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            // 一次读多个字符
            char[] tempChars = new char[1024];
            int charRead;
            reader = new InputStreamReader(new FileInputStream(fileName));
            // 读入多个字符到字符数组中，charRead为一次读取字符数
            while ((charRead = reader.read(tempChars)) != -1) {
                // 同样屏蔽掉\r不显示
                if ((charRead == tempChars.length)
                        && (tempChars[tempChars.length - 1] != '\r')) {
                    sb.append(tempChars);
                } else {
                    for (int i = 0; i < charRead; i++) {
                        if (tempChars[i] != '\r'){
                            sb.append(tempChars[i]);
                        }
                    }
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
//        logger.debug(sb);
        return sb.toString().toCharArray();
    }

    /**
     * 文件读取 去掉换行符
     */
    public static String readFileByLines(String fileName) {
        fileName = formatSeparator(fileName);
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.replace('\r', ' ');
                sb.append(tempString);
//                logger.debug(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return sb.toString();
    }

    public static List<String> readEachLine(String fileName) {
        fileName = formatSeparator(fileName);
        File file = new File(fileName);
        BufferedReader reader = null;

        List<String> list = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                list.add(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return list;
    }

    public static String readOneLine(String fileName) {
        fileName = formatSeparator(fileName);
        File file = new File(fileName);
        BufferedReader reader = null;

        String res = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            res = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return res == null ? "" : res;
    }

    /**
     * read filepath with postfix
     * @param fileDir target file dir to be read
     * @param postfix target postfix of file
     * @return List<String>
     */
    public static List<String> findAllFilePaths(String fileDir, String postfix) {
        fileDir = formatSeparator(fileDir);
        List<String> paths = new ArrayList<>();
        //读取输入路径的文件
        File[] list = new File(fileDir).listFiles();
        if (list == null) {
            logger.error("Target File directory is empty!");
            return paths;
        }
        for(File file : list)
        {
            if(file.isFile())
            {
                if (file.getName().endsWith(postfix)) {
                    paths.add(file.getAbsolutePath());
                }
            } else if (file.isDirectory()) {
                paths.addAll(findAllFilePaths(file.getAbsolutePath(), postfix));
            }
        }
        return paths;
    }


    public static String findOneFilePath(String fileDir, String postfix) {
        fileDir = formatSeparator(fileDir);
        String path = null;
        //读取输入路径的文件
        File[] list = new File(fileDir).listFiles();
        if (list == null) {
            logger.error("Target File directory is empty!");
            return path;
        }
        for(File file : list)
        {
            if(file.isFile())
            {
                if (file.getName().endsWith(postfix)) {
                    path = file.getAbsolutePath();
                    break;
                }
            } else if (file.isDirectory()) {
                String tmp = findOneFilePath(file.getAbsolutePath(), postfix);
                if (tmp != null) {
                    path = tmp;
                    break;
                }
            }
        }
        return path;
    }

    public static List<String> findCurrentFilePaths(String fileDir, String postfix) {
        fileDir = formatSeparator(fileDir);
        List<String> paths = new ArrayList<>();
        //读取输入路径的文件
        File[] list = new File(fileDir).listFiles();
        if (list == null) {
            logger.error("Target File directory is empty!");
            return paths;
        }
        for(File file : list)
        {
            if(file.isFile())
            {
                if (file.getName().endsWith(postfix)) {
                    // 就输出该文件的绝对路径
                    paths.add(file.getAbsolutePath());
                }

            }
        }
        return paths;
    }

    public static List<String> getDirNames(String fileDir) {
        fileDir = formatSeparator(fileDir);
        List<String> paths = new ArrayList<>();
        //读取输入路径的文件
        File[] list = new File(fileDir).listFiles();
        if (list == null) {
            logger.error("Target File directory is empty!");
            return paths;
        }
        for(File file : list)
        {
            if (file.isDirectory()) {
                paths.add(file.getName());
            }
        }
        return paths;
    }

    public static boolean notExists(String filePath) {
        filePath = formatSeparator(filePath);
        File file = new File(filePath);
        return !file.exists();
    }

    public static List<String> executeCommand(String[] cmd) {
        try {
            StringBuilder stringBuilder = new StringBuilder("Running command: ");
            stringBuilder.append(String.join(" ", cmd));
            logger.info(stringBuilder.toString());
            List<String> message = FileUtils.execute(cmd, null, defaultTimeoutSecond, null);
            logger.info("Command result:\n" + FileUtils.getStrOfIterable(message, "\n"));
            message.remove(0);
            return message;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int executeCommand(String[] cmd, String workingDir, @NonNull Integer timeSecond, Map<String, String> env) {
        int res = -1;
        try {
            StringBuilder stringBuilder = new StringBuilder("Running command: ");
            stringBuilder.append(String.join(" ", cmd));
            if (workingDir != null) {
                stringBuilder.append(" in ").append(workingDir);
            }
            logger.info(stringBuilder.toString());
            List<String> message = FileUtils.execute(cmd, workingDir, timeSecond, env);
            logger.info("Command result:\n" + FileUtils.getStrOfIterable(message, "\n"));
            res = Integer.parseInt(message.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<String> execute(String[] command, String workingDir, @NonNull Integer timeSecond, Map<String, String> env) {
        Process process = null;
        int exit = -1;
        final List<String> message = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            if (env != null) {
                Map<String, String> environment = builder.environment();
                environment.putAll(env);
            }
            if (workingDir != null && !FileUtils.notExists(workingDir)) {
                builder.directory(new File(workingDir));
            }
            builder.redirectErrorStream(true);
            process = builder.start();
            final InputStream inputStream = process.getInputStream();
            AtomicBoolean isComplete = new AtomicBoolean(false);
            Thread processReader = new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while((line = reader.readLine()) != null) {
                        message.add(line);
                    }
                    reader.close();
                    isComplete.set(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
            processReader.start();
            try {
                processReader.join(timeSecond * 1000);
                if (!isComplete.get()) {//todo: timeout but do not exit!
                    process.destroy();
                    logger.error("Command " + Arrays.toString(command) + " Timeout!");
                }
                exit = process.waitFor();
                logger.debug("Command exits with code " + exit);
                processReader.interrupt();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        message.add(0, String.valueOf(exit));
        return message;
    }

    private static final Gson gson = new GsonBuilder().create();

    public static String bean2Json(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T json2Bean(String jsonStr, Class<T> objClass) {
        return gson.fromJson(jsonStr, objClass);
    }

    public static String jsonFormatter(String uglyJsonStr) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(uglyJsonStr);
        return gson.toJson(je);
    }

    public static JsonElement readJsonFile(String jsonFilePath) {
        String jsonStr = readFileByLines(jsonFilePath);
        return JsonParser.parseString(jsonStr);
    }

    public static String makeItems2JsonStr(String name, Map<String, Object> items) {
        StringBuilder stringBuilder = new StringBuilder(name);
        if (!name.equals("")) {
            stringBuilder.append(":");
        }
        stringBuilder.append("{");
        for (String key :items.keySet()) {
            stringBuilder.append(key).append(":\"").append(items.get(key)).append("\",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("}");
        return jsonFormatter(stringBuilder.toString());
    }

    public static List<List<String>> readCsv(String filePath, boolean deleteHeader) {
        List<List<String>> list = readEachLine(filePath).stream().map(str -> Arrays.asList(str.split(","))).collect(Collectors.toList());
        if (deleteHeader && list.size() > 1) {
            list.remove(0);
        }
        return list;
    }

    public static void writeCsv(List<List<String>> content, String filePath, boolean deleteHeader) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = deleteHeader ? 1 : 0; i < content.size(); i ++) {
            List<String> line = content.get(i);
            stringBuilder.append(getStrOfIterable(line, ","));
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "\n");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        writeToFile(stringBuilder.toString(), filePath, false);
    }

    /**
     * todo: diff two files with command `diff -u`
     * @param srcPath source file path
     * @param dstPath destination file path
     * @param split ?
     * @return diff result
     */
    public static boolean diff(String srcPath, String dstPath, int split) {
        return false;
    }
}