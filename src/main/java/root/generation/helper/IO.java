package root.generation.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;


import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.Statement;
import root.generation.compiler.JavaFileObjectImpl;


public class IO {
	public static void saveProgramVariant(Map<String, String> modifiedJavaSources,
			Map<String, JavaFileObject> compiledClasses, String srcJavaDir, String binJavaDir,
			String programVariantOutputDir, int globalID) throws IOException {
		File srcFile = new File(srcJavaDir);
		File destSrcFile = new File(programVariantOutputDir, "Variant_" + (globalID) + "/src");

		File binFile = new File(binJavaDir);
		File destBinFile = new File(programVariantOutputDir, "Variant_" + (globalID) + "/bin");

		FileUtils.copyDirectory(srcFile, destSrcFile);
		FileUtils.copyDirectory(binFile, destBinFile);

		for (Map.Entry<String, String> entry : modifiedJavaSources.entrySet()) {
			String sourceFilePath = entry.getKey();
			URI uri1 = new File(sourceFilePath).toURI();
			URI uri2 = srcFile.toURI();
			URI uri3 = uri2.relativize(uri1);
			File file = new File(destSrcFile, uri3.getPath());
			FileUtils.writeByteArrayToFile(file, entry.getValue().getBytes());
		}

		for (Map.Entry<String, JavaFileObject> entry : compiledClasses.entrySet()) {
			String fullClassName = entry.getKey();
			String child = fullClassName.replace(".", File.separator) + ".class";

			File file = new File(destBinFile, child);
			byte[] bytes = ((JavaFileObjectImpl) entry.getValue()).getByteCode();
			FileUtils.writeByteArrayToFile(file, bytes);
		}
	}

	public static void saveCompiledClasses(Map<String, JavaFileObject> compiledClasses, File binWorkingDirFile)
			throws IOException {
		for (Map.Entry<String, JavaFileObject> entry : compiledClasses.entrySet()) {
			String fullClassName = entry.getKey();
			String child = fullClassName.replace(".", File.separator) + ".class";

			File file = new File(binWorkingDirFile, child);
			byte[] bytes = ((JavaFileObjectImpl) entry.getValue()).getByteCode();
			FileUtils.writeByteArrayToFile(file, bytes);
		}
	}
	
	
	public static void savePatch(Map<String, String> modifiedJavaSources, String srcJavaDir,
			String patchDir, int globalID) throws IOException, InterruptedException {
		File root = new File(patchDir, "Patch_" + globalID);
		List<String> diffs = new ArrayList<>();
		
		for (Map.Entry<String, String> entry : modifiedJavaSources.entrySet()) {
			String orgFilePath = entry.getKey();
			File patched = new File(root, "patched");
			
			String relative = new File(srcJavaDir).toURI().relativize(new File(orgFilePath).toURI()).getPath();
			File revisedFile = new File(patched, relative);
			FileUtils.writeByteArrayToFile(revisedFile, entry.getValue().getBytes());
			
			List<String> diff = getDiff(orgFilePath, revisedFile.getAbsolutePath());
			diffs.addAll(diff);
			diffs.add("\n");
		}
		
		FileUtils.writeLines(new File(root, "diff"), diffs);
	}
	static List<String> getDiff(String orgFilePath, String revisedFilePath) throws IOException, InterruptedException {
		List<String> params = new ArrayList<String>();
		params.add("diff");
		params.add("-u");
		params.add(orgFilePath);
		params.add(revisedFilePath);
		
		ProcessBuilder builder = new ProcessBuilder(params);
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory();

		Process process = builder.start();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		List<String> lines = new ArrayList<>();
		while ((line = in.readLine()) != null) {
		   lines.add(line);
		}
		process.waitFor();

		in.close();
		
		return lines;
	}

}