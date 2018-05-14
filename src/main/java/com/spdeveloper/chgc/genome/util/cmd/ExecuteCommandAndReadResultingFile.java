package com.spdeveloper.chgc.genome.util.cmd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
public class ExecuteCommandAndReadResultingFile {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private String commandTemplate;
	private Path resultingFile;
	private boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

	public ExecuteCommandAndReadResultingFile(String commandTemplate, Path resultingFile) {
		this.commandTemplate = commandTemplate;
		this.resultingFile = resultingFile;
	}
	
	public static ExecuteCommandAndReadResultingFile getGlimmerExecutor() throws IOException {
		String glimmerCMDTemplate = "csh /fs/szgenefinding/Glimmer3/scripts/g3-iterated.csh %s tag";
		Path resultingFile = Paths.get("tag.predict");
		ExecuteCommandAndReadResultingFile glimmerExecuter = new ExecuteCommandAndReadResultingFile(glimmerCMDTemplate, resultingFile);
		return glimmerExecuter; 
	} 
	
	public static ExecuteCommandAndReadResultingFile getZcurveExecutor() throws IOException {
		String zcurveCMDTemplate = "/root/zcurve3.0/zcurve3.0 %s zcurve.txt";
		Path resultingFile = Paths.get("zcurve.txt");
		ExecuteCommandAndReadResultingFile zcurveExecuter = new ExecuteCommandAndReadResultingFile(zcurveCMDTemplate, resultingFile);
		return zcurveExecuter; 
	} 
	
	public static ExecuteCommandAndReadResultingFile getExtractor() {
		return new ExecuteCommandAndReadResultingFile(
				"/root/genomevizAdapter/extractorScript %s %s" , Paths.get("Gene.fas"));
	}
	
	public static ExecuteCommandAndReadResultingFile getTranslator() {
		return new ExecuteCommandAndReadResultingFile(
				"/root/genomevizAdapter/translateScript %s translated.txt" , Paths.get("translated.txt"));
	}
	
	public static ExecuteCommandAndReadResultingFile getBlastall() {
		String blastallTemplate = "blastall -p blastp -i %s -o Pr.nr -d /root/ncbi-blast-2.7.1+/bin/test-db.fas -v 1 -b 1 -e 1e-5 -m7 -a 2";
		return new ExecuteCommandAndReadResultingFile(blastallTemplate, Paths.get("Pr.nr"));
	}
	
	public Path executeAndReadResultingFile(String ...args) throws IOException, InterruptedException{
		Files.deleteIfExists(resultingFile);
		executeAndBlock(args);
		return resultingFile; 
	}
	
	public List<String> executeAndReadResultingLines(String ...args) throws IOException, InterruptedException{
		Files.deleteIfExists(resultingFile);
		log.info("run "+String.format(commandTemplate, args)+": "+executeAndBlock(args));
		return readResultingFile(resultingFile);
	} 
	private int executeAndBlock(String...args) throws IOException, InterruptedException {
		String cmd = String.format(commandTemplate, args);
		log.info("run: "+cmd);
		String homeDirectory = System.getProperty("user.home");
		Process process;
		if (isWindows) {
			process = Runtime.getRuntime().exec(String.format("cmd.exe /c dir %s", homeDirectory));
		} else {
			process = Runtime.getRuntime().exec(cmd);
		}

		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), log::info);
		Thread thread = new Thread(streamGobbler);
		thread.start();
		
		int exitCode = process.waitFor();
		return exitCode;
	}
	private List<String> readResultingFile(Path resultingFile) throws IOException{
		return Files.readAllLines(resultingFile);
	} 
	
	
	private static class StreamGobbler implements Runnable {
		private InputStream inputStream;
		private Consumer<String> consumer;

		public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
			this.inputStream = inputStream;
			this.consumer = consumer;
		}

		@Override
		public void run() {
			new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
		}
	}
}