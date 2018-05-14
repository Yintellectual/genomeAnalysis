package com.spdeveloper.chgc.genome.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.spdeveloper.chgc.genome.annotation.entity.GeneAnnotated;
import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.prediction.service.GlimmerGenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.M7Parser;
import com.spdeveloper.chgc.genome.prediction.service.ZcurveGenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.M7Parser.BlastOutput;
import com.spdeveloper.chgc.genome.prediction.service.M7Parser.PrMatch;
import com.spdeveloper.chgc.genome.util.cmd.ExecuteCommandAndReadResultingFile;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;

import reactor.core.publisher.Flux;

@Controller
public class GenePredictionNAnnotationController2 {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	GenePredictionParser genePredictionParser;
	@Autowired
	GenePredictionResultCombiner genePredictionResultCombiner;
	
	@PostMapping("/genomeAnnotation2")
	public ResponseEntity<Resource> handleFileUpload(@RequestParam("fas") MultipartFile fas) throws IOException, InterruptedException {
		
		//save .fas file under /files/fas/
		InputStream initialStream = fas.getInputStream();
		byte[] buffer = new byte[initialStream.available()];
		initialStream.read(buffer);
				
		File fastaFile = new File("src/main/resources/files/fas/temp.fas");
		OutputStream outStream = new FileOutputStream(fastaFile);
		outStream.write(buffer);
		outStream.close();

		Path tempDir = Files.createTempDirectory("genomeAnalysis");
		List<GenePrediction> genePrediction = genePredictionResultCombiner.combine(fastaFile, tempDir);
		
		Path geneXlsxFile = Paths.get("src", "main", "resources", "files", "fas", "Gene.xlsx");
		WriteToFileUtil.writeToFile(genePrediction, geneXlsxFile);
		
	    List<String> geneFasData = ExecuteCommandAndReadResultingFile.getExtractor().executeAndReadResultingLines(fastaFile.getAbsolutePath(), geneXlsxFile.toFile().getAbsolutePath());
	    Path geneFasFile = Paths.get("src", "main", "resources", "files", "fas", "Gene.fas");
	    WriteToFileUtil.writeToFile(geneFasData, geneFasFile);
		geneFasData = null;
	    
	    List<String> translatedData = ExecuteCommandAndReadResultingFile.getTranslator().executeAndReadResultingLines(geneFasFile.toFile().getAbsolutePath());
	    Path translatedFile = Paths.get("src", "main", "resources", "files", "fas", "Pr.fas");
	    log.info("translatedData has "+translatedData.size()+" lines.");
	    WriteToFileUtil.writeToFile(
	    		translatedData.stream().map(s->{
	    			if(s.startsWith(">")) {
	    				return s.replaceAll("\\s+.*", "");
	    			}else {
	    				return s.replaceAll("^(V)", "M").replaceAll("^(L)", "M").replaceAll("(\\*)$", "");
	    			}
	    		}).collect(Collectors.toList())
	    		, translatedFile);
	    //gene annotation
	    translatedData = null;
	    
	    File m7File = ExecuteCommandAndReadResultingFile.getBlastall().executeAndReadResultingFile(translatedFile.toFile().getAbsolutePath()).toFile();
	    BlastOutput blastOutput = M7Parser.parse(m7File);
	    Flux<PrMatch> prMatches = Flux.fromIterable(blastOutput.getBlastOutput_iterations().getPrMatchs());
	    
	    Flux<GenePrediction> genePredictionFlux = Flux.fromIterable(genePrediction);
	    List<GeneAnnotated> geneAnnotateds = genePredictionFlux.zipWith(prMatches, (g, p)->{
	    	return new GeneAnnotated(g, p);
	    }  ).collectList().block();
	    
	    Path annotationFile = Paths.get("src", "main", "resources", "files", "fas", "Annotation.xlsx");
	    WriteToFileUtil.writeToFile(geneAnnotateds, annotationFile);
	    
	    //edit and save
	    
	    HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		responseHeaders.set("charset", "utf-8");
		responseHeaders.set("Content-disposition", "attachment; filename=" + "Annotation.xlsx");
		Resource resource = new InputStreamResource(new FileInputStream(annotationFile.toFile()));
		//Resource resource = new InputStreamResource(new FileInputStream(translatedFile.toFile()));
		ResponseEntity<Resource> result = new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
		return result;
	}
}
