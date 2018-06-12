package com.spdeveloper.chgc.genome.annotation.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.spdeveloper.chgc.genome.annotation.entity.GeneAnnotated;
import com.spdeveloper.chgc.genome.annotation.entity.RnaAnnotated;
import com.spdeveloper.chgc.genome.annotation.entity.RnaAnnotated.RNAType;
import com.spdeveloper.chgc.genome.dependencyDriver.BlastAllProteinAnnotation;
import com.spdeveloper.chgc.genome.dependencyDriver.GeneExtractor;
import com.spdeveloper.chgc.genome.dependencyDriver.GeneToProteinTranslate;
import com.spdeveloper.chgc.genome.dependencyDriver.RNAmmer;
import com.spdeveloper.chgc.genome.dependencyDriver.RpsBlastProteinAnnotation;
import com.spdeveloper.chgc.genome.dependencyDriver.TRNAScan;
import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.util.xml.M7Parser;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.BlastOutput;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;
import com.spdeveloper.chgc.genome.util.zip.ZipDirectory;
import com.spdeveloper.chgc.genome.visualization.service.COGColorParser;

import reactor.core.publisher.Flux;

import static com.spdeveloper.chgc.genome.GenomeAnalysisApplication.*;

@Service
public class FastaToAnnotationService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	GenePredictionParser genePredictionParser;
	@Autowired
	GenePredictionResultCombiner genePredictionResultCombiner;
	@Autowired
	GeneExtractor geneExtractor;
	@Autowired
	GeneToProteinTranslate geneToProteinTranslate;
	@Autowired
	BlastAllProteinAnnotation blastAllProteinAnnotation;
	@Autowired
	RpsBlastProteinAnnotation rpsBlastProteinAnnotation;
	@Autowired
	TRNAScan tRNAScan;
	@Autowired
	RNAmmer rnammer;
	@Autowired
	AnnotationExcelWriter AnnotationExcelWriter;

	@Autowired
	Connection rabbitMQConnection;

	@PostConstruct
	public void registerToRabbitMQ() throws IOException {
		Channel rabbitMQChannel = rabbitMQConnection.createChannel();
		rabbitMQChannel.queueDeclare(FASTA_FOR_ANNOTATION, true, false, false, null);
		rabbitMQChannel.basicConsume(FASTA_FOR_ANNOTATION, false, new Consumer() {
			@Override
			public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
				// TODO Auto-generated method stub

			}

			@Override
			public void handleRecoverOk(String consumerTag) {
				// TODO Auto-generated method stub

			}

			@Override
			public void handleDelivery(String arg0, Envelope arg1, BasicProperties arg2, byte[] arg3)
					throws IOException {

				String message = new String(arg3);
				String fastaFileString = message.split("@")[1];
				String id = message.split("@")[0];
				
				Path fastaFile = Paths.get(fastaFileString);
				Path annotationFile = null;
				
				try {
					annotationFile = start(fastaFile);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					log.error("Exception during transforming fasta to annotation", e);
				}
				
				if(annotationFile!=null) {
					rabbitMQChannel.queueDeclare(ANNOTATIONS, true, false, false, null);
					rabbitMQChannel.basicPublish("", ANNOTATIONS, null, (id+"@"+annotationFile.toAbsolutePath().toString()).getBytes());
				
					rabbitMQChannel.basicAck(arg1.getDeliveryTag(), false);
				}else {
					rabbitMQChannel.basicNack(arg1.getDeliveryTag(), false, true);	
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

	public Path start(Path fastaFile) throws IOException, InterruptedException {

		Path destineyDir = fastaFile.normalize().getParent();

		// save all the files under a temporary directory which is deleted later
		Path tempDir = Files.createTempDirectory("genomeAnalysis");

		String fastaName = "";
		List<RnaAnnotated> rnaAnnotateds = new ArrayList<>();

		List<GenePrediction> genePrediction = genePredictionResultCombiner.combine(fastaFile.toFile(), tempDir);
		fastaName = Files.readAllLines(fastaFile).get(0).split(">")[1];

		Path geneFasFile = geneExtractor.extract(fastaFile.toFile(), genePrediction, tempDir).toPath();

		Path translatedFile = geneToProteinTranslate.translate(geneFasFile.toFile(), tempDir).toPath();

		Path blastResult = blastAllProteinAnnotation.start(tempDir, translatedFile);

		BlastOutput blastOutput = M7Parser.parse(blastResult.toFile());
		Flux<PrMatch> nrPrMatches = Flux.fromIterable(blastOutput.getBlastOutput_iterations().getPrMatchs());

		Path cogResult = rpsBlastProteinAnnotation.start(tempDir, translatedFile);

		BlastOutput cogOutput = M7Parser.parse(cogResult.toFile());
		Flux<PrMatch> cogPrMathes = Flux.fromIterable(cogOutput.getBlastOutput_iterations().getPrMatchs());

		Flux<GenePrediction> genePredictionFlux = Flux.fromIterable(genePrediction);
		List<GeneAnnotated> geneAnnotateds = Flux.zip(genePredictionFlux, nrPrMatches, cogPrMathes).map(t3 -> {
			return new GeneAnnotated(t3.getT1(), t3.getT2(), t3.getT3());
		}).collectList().block();

		// generate rnaAnnotations using both tRNAScanner and RNAmmer
		Path tRNAScanResult = tRNAScan.start(tempDir, fastaFile);
		List<RnaAnnotated> tRNAAnnotateds = Files.readAllLines(tRNAScanResult).stream().map(RnaAnnotated::parseTRNAscan)
				.filter(e -> e != null).collect(Collectors.toList());
		RnaAnnotated.generateNameByIndexNumber(tRNAAnnotateds, RNAType.tRNA);
		rnaAnnotateds.addAll(tRNAAnnotateds);

		Path rnammerResult = rnammer.start(tempDir, fastaFile);
		List<RnaAnnotated> rRNAAnnotateds = Files.readAllLines(rnammerResult).stream().map(RnaAnnotated::parseRNAmmer)
				.filter(e -> e != null).collect(Collectors.toList());
		RnaAnnotated.generateNameByIndexNumber(rRNAAnnotateds, RNAType.rRNA);
		rnaAnnotateds.addAll(rRNAAnnotateds);

		// write both geneAnnotations and rnaAnnotations to a xlsx file, with
		// geneAnnotations be the first sheet and rnaAnnotations be the second.
		Path annotationFile = Files.createTempFile(tempDir, "genomeAnalysis", "Annotation.xlsx");
		// WriteToFileUtil.writeToFile(geneAnnotateds, annotationFile);
		AnnotationExcelWriter.write(annotationFile, geneAnnotateds, rnaAnnotateds);

		// save file
		Path result = Paths.get(destineyDir.toAbsolutePath().toString(), fastaName + "_Annotation.zip");
		// zip resulting files: fasta(the input file), gene.fas, pr.fas, and
		// Annotation.xlsx
		FileOutputStream resultOutputStream = new FileOutputStream(result.toFile());
		HashMap<String, Path> resultingFiles = new HashMap<String, Path>() {
			{
				put("gene.fas", geneFasFile);
				put("pr.fas", translatedFile);
				put("Annotation.xlsx", annotationFile);
			}
		};
		resultingFiles.put(fastaName + ".fasta", fastaFile);
		ZipDirectory.doZipFiles(resultingFiles, resultOutputStream);
		resultOutputStream.close();

		// delete all temporary files with the folder
		log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
		// log.info("Deletion Disabled, tempDir="+tempDir.toFile().getAbsolutePath());
		FileUtils.deleteDirectory(tempDir.toFile());

		return result;
	}
}
