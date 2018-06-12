package com.spdeveloper.chgc.genome.util.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZipUtilUnzipTest {

	@Test
	public void unzipFileByFileName() throws IOException {
		
		Path zipFile = Paths.get("src", "main", "resources", "files", "zips", "Genome_Annotation.zip");
		
		Path annotationExcel = SpDeveloperZipUtil.unzip("Annotation.xlsx", zipFile.toFile());
		
		assertEquals(33376, Files.readAllBytes(annotationExcel).length); 
		
	}
}
