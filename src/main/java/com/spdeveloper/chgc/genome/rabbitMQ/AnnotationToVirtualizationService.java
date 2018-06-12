package com.spdeveloper.chgc.genome.rabbitMQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.spdeveloper.chgc.genome.util.zip.SpDeveloperZipUtil;
import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.entity.Wrapper;
import com.spdeveloper.chgc.genome.visualization.service.DNAParser;
import com.spdeveloper.chgc.genome.visualization.service.FASParser;
import com.spdeveloper.chgc.genome.visualization.service.MapFileCreator;
import com.spdeveloper.chgc.genome.visualization.service.RNAParser;
import com.spdeveloper.chgc.genome.visualization.service.TabFileCreator;

import static com.spdeveloper.chgc.genome.GenomeAnalysisApplication.*;

@Service
public class AnnotationToVirtualizationService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	MapFileCreator mapFileCreator;
	@Autowired
	TabFileCreator tabFileCreator;
	@Autowired
	DNAParser dnaPaser;
	@Autowired
	RNAParser rnaPaser;
	@Autowired
	FASParser fasCounter;

	@Autowired
	Connection rabbitMQConnection;

	@Value("${files.storage.local.permanent}")
	String permanentDirString;

	
	@PostConstruct
	public void registerToRabbitMQ() throws IOException {

		Path VIRTUALIZATION_FAILED = Paths.get(permanentDirString, "VIRTUALIZATION_FAILED");
		Channel rabbitMQChannel = rabbitMQConnection.createChannel();
		rabbitMQChannel.queueDeclare(ANNOTATION_FOR_VIRTUALIZATION, true, false, false, null);
		rabbitMQChannel.basicConsume(ANNOTATION_FOR_VIRTUALIZATION, false, new Consumer() {
			
			@Override
			public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void handleRecoverOk(String consumerTag) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void handleDelivery(String arg0, Envelope arg1, BasicProperties arg2, byte[] arg3) throws IOException {
				// TODO Auto-generated method stub
				String message = new String(arg3);
				String annotationZipString = message.split("@")[1];
				String id = message.split("@")[0];
				
				Path annotationZip = Paths.get(annotationZipString);
				Path virtualizationZip = null;
				try {
					virtualizationZip = start(annotationZip);
				} catch (IllegalClassFormatException e) {
					// TODO Auto-generated catch block
					log.error("Exception during transforming annotation to virtualization", e);
				}
				
				if(virtualizationZip!=null) {
					 rabbitMQChannel.queueDeclare(VIRTUALIZATIONS, true, false, false, null);
					 rabbitMQChannel.basicPublish("", VIRTUALIZATIONS, null,
							 (id+"@"+virtualizationZip.toAbsolutePath().toString()).getBytes());

					rabbitMQChannel.basicAck(arg1.getDeliveryTag(), false);
				}else {
					rabbitMQChannel.basicNack(arg1.getDeliveryTag(), false, false);
					rabbitMQChannel.basicPublish("", VIRTUALIZATIONS, null, (id+"@"+Paths.get(annotationZip.getParent().toString())).getBytes());
				}
				
			}
			
			@Override
			public void handleConsumeOk(String consumerTag) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void handleCancelOk(String consumerTag) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void handleCancel(String consumerTag) throws IOException {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	public Path start(Path zipFile) throws FileNotFoundException, IOException, IllegalClassFormatException {
		Path fas = SpDeveloperZipUtil.unzip(".fasta", zipFile.toFile());
		Path annotationExcel = SpDeveloperZipUtil.unzip("Annotation.xlsx", zipFile.toFile());

		Path result = start(fas.toFile(), annotationExcel.toFile());

		Files.delete(fas);
		Files.delete(annotationExcel);

		return result;
	}

	public Path start(File fas, File annotationExcel)
			throws FileNotFoundException, IOException, IllegalClassFormatException {
		return start(fas, annotationExcel, 2000, 1000, 0, 1);
	}

	/*
	 * By default, window=2000 && overlap=1000 && dna_sheet_index=0 &&
	 * dna_sheet_index=1
	 */
	public Path start(File fas, File annotationExcel, int window, int overlap, int dna_sheet_index, int rna_sheet_index)
			throws FileNotFoundException, IOException, IllegalClassFormatException {
		Path parentDir = fas.toPath().toAbsolutePath().getParent();
		String name = fasCounter.parseFileForName(new FileInputStream(fas));
		Path result = Paths.get(parentDir.toString(), name + "_Virtualization.zip");

		List<DNA> dnas = dnaPaser.parse(new FileInputStream(annotationExcel), dna_sheet_index);
		List<RNA> rnas = rnaPaser.parse(new FileInputStream(annotationExcel), rna_sheet_index);
		String[] fasta = fasCounter.parseFile(new FileInputStream(fas));
		Long fasCount = fasCounter.count(fasta);

		Wrapper positiveDNA = new Wrapper();
		Wrapper negativeDNA = new Wrapper();
		mapFileCreator.createMapFilesForDNA(dnas, fasCount, positiveDNA, negativeDNA);
		Wrapper positiveRNA = new Wrapper();
		Wrapper negativeRNA = new Wrapper();
		mapFileCreator.createMapFilesForRNA(rnas, fasCount, positiveRNA, negativeRNA);
		Wrapper gcMap = new Wrapper();
		Wrapper gc_skewMap = new Wrapper();
		mapFileCreator.createMapFilesForFasta(fasta, fasCount, window, overlap, gcMap, gc_skewMap);
		String[] tabFile = tabFileCreator.createStringArray(dnas, rnas, "Feature " + name);

		OutputStream output = new FileOutputStream(result.toFile());
		SpDeveloperZipUtil.zipDirectory(new HashMap<String, String>() {
			{
				put("DNA_Positive.map", fileContent(positiveDNA));
				put("DNA_Negative.map", fileContent(negativeDNA));
				put("RNA_Positive.map", fileContent(positiveRNA));
				put("RNA_Negative.map", fileContent(negativeRNA));
				put("gc.map", fileContent(gcMap));
				put("gc_skew.map", fileContent(gc_skewMap));
				put("sequin.tab", fileContent(new Wrapper(tabFile)));
			}
		}, output);
		output.close();

		return result;
	}

	private String fileContent(Wrapper wrapper) {
		StringBuilder sb = new StringBuilder();
		Arrays.stream(wrapper.getValue()).forEach(line -> {
			sb.append(line);
			sb.append("\n");
		});
		return sb.toString();
	}
}