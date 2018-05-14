package com.spdeveloper.chgc.genome.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spdeveloper.chgc.genome.util.debug.ComparisonReportCreator;
import com.spdeveloper.chgc.genome.util.zip.ZipDirectory;
import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.entity.Wrapper;
import com.spdeveloper.chgc.genome.visualization.service.DNAParser;
import com.spdeveloper.chgc.genome.visualization.service.FASParser;
import com.spdeveloper.chgc.genome.visualization.service.MapFileCreator;
import com.spdeveloper.chgc.genome.visualization.service.RNAParser;
import com.spdeveloper.chgc.genome.visualization.service.TabFileCreator;

@Controller
public class SevenFilesController {
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


	private String[] getStrings(InputStream sourceFile) {
		return new BufferedReader(new InputStreamReader(sourceFile)).lines().toArray(String[]::new);
	}
	
	@PostMapping("/createMapFiles")
	public ResponseEntity<Resource> handleFileUpload(@RequestParam("fas") MultipartFile fas,
			@RequestParam("window") int window, @RequestParam("overlap") int overlap,
			@RequestParam("cds") MultipartFile annotationExcel, @RequestParam("dna_sheet_index") Integer dna_sheet_index,
			@RequestParam("rna_sheet_index") Integer rna_sheet_index,
			@RequestParam(required = false, value = "dna_positive_map_sheet_index") Integer dna_positive_map_sheet_index,
			@RequestParam(required = false, value = "dna_negative_map_sheet_index") Integer dna_negative_map_sheet_index,
			@RequestParam(required = false, value = "rna_positive_map_sheet_index") Integer rna_positive_map_sheet_index,
			@RequestParam(required = false, value = "rna_negative_map_sheet_index") Integer rna_negative_map_sheet_index,
			@RequestParam(required = false, value = "gc_sheet_index") Integer gc_sheet_index,
			@RequestParam(required = false, value = "gc_skew_sheet_index") Integer gc_skew_sheet_index,
			@RequestParam(required = false, value = "tab_sheet_index") Integer tab_sheet_index, 
			RedirectAttributes redirectAttributes)
			throws FileNotFoundException, IOException, IllegalClassFormatException {
		long step0 = new Date().getTime();
		
		long step1 = new Date().getTime();
		List<DNA> dnas = dnaPaser.parse(annotationExcel.getInputStream(), dna_sheet_index);
		List<RNA> rnas = rnaPaser.parse(annotationExcel.getInputStream(), rna_sheet_index);
		String[] fasta = fasCounter.parseFile(fas.getInputStream());
		String name = fasCounter.parseFileForName(fas.getInputStream());
		Long fasCount = fasCounter.count(fasta);
		

		long step2 = new Date().getTime();
		Wrapper positiveDNA = new Wrapper();
		Wrapper negativeDNA = new Wrapper();
		mapFileCreator.createMapFilesForDNA(dnas, fasCount, positiveDNA, negativeDNA);
		Wrapper  positiveRNA = new Wrapper();
		Wrapper  negativeRNA = new Wrapper();
		mapFileCreator.createMapFilesForRNA(rnas, fasCount, positiveRNA, negativeRNA);
		Wrapper  gcMap = new Wrapper();
		Wrapper  gc_skewMap = new Wrapper();
		mapFileCreator.createMapFilesForFasta(fasta, fasCount, window, overlap, gcMap, gc_skewMap);
		String[] tabFile = tabFileCreator.createStringArray(dnas, rnas, "Feature "+name);
		
		
		ComparisonReportCreator comparisonReportor = new ComparisonReportCreator();
		
		long step3 = new Date().getTime();
		File inputExcel = getFile("cds", name+".xlsx");
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), dna_positive_map_sheet_index, positiveDNA.getValue());
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), dna_negative_map_sheet_index, negativeDNA.getValue());
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), rna_positive_map_sheet_index, positiveRNA.getValue());
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), rna_negative_map_sheet_index, negativeRNA.getValue());
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), gc_sheet_index, gcMap.getValue());
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), gc_skew_sheet_index, gc_skewMap.getValue());
		comparisonReportor.compareAndAppendLine(annotationExcel.getInputStream(), tab_sheet_index, tabFile);
		
		String testResult = comparisonReportor.flush();
		long step4 = new Date().getTime();
		
		redirectAttributes.addFlashAttribute("saveFilesDuration", step1-step0+"ms");
		redirectAttributes.addFlashAttribute("parseFilesDuration", step2-step1+"ms");
		redirectAttributes.addFlashAttribute("createFilesDuration", step3-step2+"ms");
		redirectAttributes.addFlashAttribute("testResultsDuration", step4-step3+"ms");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	    responseHeaders.set("charset", "utf-8");
		//responseHeaders.setContentType(MediaType.valueOf("text/html"));
		responseHeaders.set("Content-disposition", "attachment; filename=" + "result.zip");	    
	    
		LocalDateTime now = LocalDateTime.now();
		String fileName = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
		
		
		
	    OutputStream output= new FileOutputStream(getFile("zips", fileName+".zip"));  
	    ZipDirectory.zipDirectory(new HashMap<String, String>(){{
	    	put("DNA_Positive.map", fileContent(positiveDNA));
	    	put("DNA_Negative.map", fileContent(negativeDNA));
	    	put("RNA_Positive.map", fileContent(positiveRNA));
	    	put("RNA_Negative.map", fileContent(negativeRNA));
	    	put("gc.map", fileContent(gcMap));
	    	put("gc_skew.map", fileContent(gc_skewMap));
	    	put("sequin.tab", fileContent(new Wrapper(tabFile)));
	    	put("testReport.text", testResult);
	    }}, output);
        output.close();
	    
        Resource resource = new InputStreamResource(new FileInputStream(getFile("zips", fileName+".zip")));
	    ResponseEntity<Resource> result = new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
	    
	    return result;
	}
	private String fileContent(Wrapper wrapper) {
		StringBuilder sb = new StringBuilder();
		Arrays.stream(wrapper.getValue()).forEach(line->{
			sb.append(line);
			sb.append("\n");
		});
		return sb.toString();
	}
	
	private File getFile(String folder, String fileName) {
		return Paths.get("src", "main", "resources", "files", folder, fileName).toFile();
	}

	@GetMapping(value = "download")
	public String downloadPage(){
		return "download";
	}
	

	@GetMapping(value = "/files/mapFiles/{file_name}")
	public ResponseEntity<byte[]> getFile(@PathVariable("file_name") String fileName, HttpServletResponse response)
			throws IOException {
		byte[] data = Files.readAllBytes(Paths.get("src", "main", "resources", "files", "mapFiles", fileName));
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("charset", "utf-8");
		responseHeaders.setContentType(MediaType.valueOf("text/html"));
		responseHeaders.setContentLength(data.length);
		responseHeaders.set("Content-disposition", "attachment; filename=" + fileName);
		return new ResponseEntity<byte[]>(data, responseHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/files/tabFiles/{file_name}")
	public ResponseEntity<byte[]> getTabFile(@PathVariable("file_name") String fileName, HttpServletResponse response)
			throws IOException {
		byte[] data = Files.readAllBytes(Paths.get("src", "main", "resources", "files", "tabFiles", fileName));
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("charset", "utf-8");
		responseHeaders.setContentType(MediaType.valueOf("text/html"));
		responseHeaders.setContentLength(data.length);
		responseHeaders.set("Content-disposition", "attachment; filename=" + fileName);
		return new ResponseEntity<byte[]>(data, responseHeaders, HttpStatus.OK);
	}
	
	@GetMapping(value = "/files/testReport/{file_name}")
	public ResponseEntity<byte[]> getTestReportFile(@PathVariable("file_name") String fileName, HttpServletResponse response)
			throws IOException {
		byte[] data = Files.readAllBytes(Paths.get("src", "main", "resources", "files", "testReport", fileName));
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("charset", "utf-8");
		responseHeaders.setContentType(MediaType.valueOf("text/html"));
		responseHeaders.setContentLength(data.length);
		responseHeaders.set("Content-disposition", "attachment; filename=" + fileName);
		return new ResponseEntity<byte[]>(data, responseHeaders, HttpStatus.OK);
	}
}
