package com.spdeveloper.chgc.genome.util.cmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * just don't forget deleting the temporary files before the task is done.
 *
 * @author yuchen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedProgram {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	//cmdTemplate is a printf template for cmd. 
	private String cmdTemplate;
	//if the program allows specifying the resulting file, then leave programCreatedFile be null. 
	//instead, use your resultingTempFile as the resulting file. 
	private Path programCreatedFile;

	
	public void execute(List<String> choices, Path resultingTempFile, String... args) throws IOException, InterruptedException {

		String cmd = String.format(cmdTemplate, args);
		Process process = Runtime.getRuntime().exec(cmd);

		StreamWaiter streamWaiter = new StreamWaiter(process.getOutputStream(),
				choices);
		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), log::info);
		Thread threadStreamGobbler = new Thread(streamGobbler);
		Thread threadStreamWaiter = new Thread(streamWaiter);
		threadStreamGobbler.start();
		threadStreamWaiter.start();

		process.waitFor();
		log.info("Exit value = "+ process.exitValue() +", from "+ cmd);
		
		if(programCreatedFile != null) {
			log.info("moving "+ programCreatedFile.toFile().getAbsolutePath() + " to "+ resultingTempFile.toFile().getAbsolutePath());
			Files.move(programCreatedFile, resultingTempFile, StandardCopyOption.ATOMIC_MOVE);
		}else {
			log.info("resulting file should be saved as "+ resultingTempFile);
		}
		return;
	}

	private static class StreamWaiter implements Runnable {
		private OutputStream outputStream;
		private List<String> choices;

		public StreamWaiter(OutputStream outputStream, List<String> choices) {
			this.outputStream = outputStream;
			this.choices = choices;
		}

		@Override
		public void run() {
			if (choices != null) {
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
					choices.forEach(s -> {
						try {
							writer.write(s);
							writer.write("\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					writer.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {

			}
		}
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
