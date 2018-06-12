package com.spdeveloper.chgc.genome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spdeveloper.chgc.genome.annotation.entity.GeneAnnotated;
import com.spdeveloper.chgc.genome.annotation.service.AnnotationExcelWriter;
import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.rabbitMQ.AnnotationToVirtualizationService;
import com.spdeveloper.chgc.genome.rabbitMQ.FastaToAnnotationService;
import com.spdeveloper.chgc.genome.util.cmd.ExecuteCommandAndReadResultingFile;
import com.spdeveloper.chgc.genome.util.xml.M7Parser;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.BlastOutput;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;
import com.spdeveloper.chgc.genome.visualization.service.COGColorParser;
import com.thoughtworks.xstream.XStream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class GenomeAnalysisApplication {
	public static final String FASTA_FOR_ANNOTATION = "fasta_for_annotation";
	public static final String ANNOTATIONS = "annotations";
	public static final String ANNOTATION_FOR_VIRTUALIZATION  = "annotation_for_virtualization";
	public static final String VIRTUALIZATIONS  = "virtualizations";
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		SpringApplication.run(GenomeAnalysisApplication.class, args);
	}

	/*
	 * MQ Schema（Copied from project GenomePortal, com.spdeveloper.chgc.genome.portal.GenomePortalApplication, 2018-06-11）:
	 * 
	 * The default exchange will direct messages into one of the four queues:
	 * 
	 * 1. fasta_for_annotation 2. annotations 3. annotation_for_virtualization  4. virtualizations
	 * 
	 * according to the specified queue name.
	 * 
	 */
	@Bean
	public Connection rabbitChannel() throws IOException, TimeoutException {
		ConnectionFactory rabbitMQConnectionFactory = new ConnectionFactory();
		rabbitMQConnectionFactory.setHost("localhost");
		Connection rabbitMQConnection = rabbitMQConnectionFactory.newConnection();
		return rabbitMQConnection;
	}
	
	@Autowired
	COGColorParser cOGColorParser;
	@Autowired
	GenePredictionParser genePredictionParser;

	public void writeToFile(String fileName, String content) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(content);
		writer.close();
	}
	
	@Bean("fastaToAnnotationService1")
	public FastaToAnnotationService annotationToVirtualizationService1() {
		return new FastaToAnnotationService();
	}
	@Bean("fastaToAnnotationService2")
	public FastaToAnnotationService fastaToAnnotationService2() {
		return new FastaToAnnotationService();
	}
	
	@Bean
	CommandLineRunner commandLineRunner() {
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {

//				
				
//				Configuration loadedConfig = (Configuration) xstream.fromXML(Paths.get("src", "main", "resources", "files", "test", "xstreamTest.txt").toFile());
//				System.out.println(loadedConfig.toString());
				
//				List<GenePrediction> glimmerSample = new ArrayList<>();
//				List<GenePrediction> zcurveSample = new ArrayList<>();
//
//				glimmerSample = genePredictionParser.parse(
//						Paths.get("src", "main", "resources", "files", "test", "tag.predict"),
//						s -> GenePredictionFactory.fromGlimmerPrediction(s));
//				zcurveSample = genePredictionParser.parse(
//						Paths.get("src", "main", "resources", "files", "test", "zcurve.txt"),
//						s -> GenePredictionFactory.fromZcurvePrediction(s));
//				GenePredictionUtil.combine(glimmerSample, zcurveSample).forEach(System.out::println);
				
//				List<String> glimmerResult = ExecuteCommandAndReadResultingFile.getGlimmerExecutor().executeAndReadResultingFile("~/1009-Genome.fas");
//				log.info(comparison.compare(Files.readAllLines(Paths.get("src", "main", "resources", "files", "test", "tag.predict")).toArray(new String[0]), glimmerResult.toArray(new String[0])));
				//List<String> zcurveResult = ExecuteCommandAndReadResultingFile.getZcurveExecutor().executeAndReadResultingFile("/root/1009-Genome.fas");
				//log.info(comparison.compare(Files.readAllLines(Paths.get("src", "main", "resources", "files", "test", "zcurve.txt")).toArray(new String[0]), zcurveResult.toArray(new String[0])));
				
//				ExecuteCommandAndReadResultingFile extractor = new ExecuteCommandAndReadResultingFile(
//						" /fs/szgenefinding/Glimmer3/bin/extract %s %s > extractResult.txt" , Paths.get("extractResult.txt"));
//				extractor.executeAndReadResultingFile("/root/1009-Genome.fas", "/root/genomevizAdapter/tag.predict").forEach(log::info);
				
				
			}
		};
	}
}
