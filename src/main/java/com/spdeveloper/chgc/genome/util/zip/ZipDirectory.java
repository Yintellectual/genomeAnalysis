package com.spdeveloper.chgc.genome.util.zip;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;




public class ZipDirectory {
	public static void doZipFiles(Map<String, Path> files, OutputStream output) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(output);
    	for(String fileName:files.keySet()) {
    		zipFile(files.get(fileName), fileName, zipOut);
    	}
        zipOut.close();
	}
	private static void zipFile(Path sourceFile, String fileName, ZipOutputStream zipOut) throws IOException{
    	ZipEntry zipEntry = new ZipEntry(fileName);
    	zipOut.putNextEntry(zipEntry);
    	zipOut.write(Files.readAllBytes(sourceFile));
    }
	
    public static void zipDirectory(Map<String, String> files, OutputStream output ) throws IOException {
        
    	ZipOutputStream zipOut = new ZipOutputStream(output);
    	for(String fileName:files.keySet()) {
    		zipFile(files.get(fileName), fileName, zipOut);
    	}
        zipOut.close();
    }
	private static void zipFile(String sourceFile, String fileName, ZipOutputStream zipOut) throws IOException{
    	ZipEntry zipEntry = new ZipEntry(fileName);
    	zipOut.putNextEntry(zipEntry);
    	zipOut.write(sourceFile.getBytes());
    }
}