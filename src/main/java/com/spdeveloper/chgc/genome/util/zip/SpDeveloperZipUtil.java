package com.spdeveloper.chgc.genome.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SpDeveloperZipUtil {
	public static void doZipFiles(Map<String, Path> files, OutputStream output) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(output);
		for (String fileName : files.keySet()) {
			zipFile(files.get(fileName), fileName, zipOut);
		}
		zipOut.close();
	}

	private static void zipFile(Path sourceFile, String fileName, ZipOutputStream zipOut) throws IOException {
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		zipOut.write(Files.readAllBytes(sourceFile));
	}

	public static void zipDirectory(Map<String, String> files, OutputStream output) throws IOException {

		ZipOutputStream zipOut = new ZipOutputStream(output);
		for (String fileName : files.keySet()) {
			zipFile(files.get(fileName), fileName, zipOut);
		}
		zipOut.close();
	}

	private static void zipFile(String sourceFile, String fileName, ZipOutputStream zipOut) throws IOException {
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		zipOut.write(sourceFile.getBytes());
	}

	public static Path unzip(String suffix, File fileZip) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
		ZipEntry zipEntry = zis.getNextEntry();
		File newFile = null;
		while (zipEntry != null) {
			if (zipEntry.getName().endsWith(suffix)) {
				String fileName = zipEntry.getName();
				newFile = Paths.get(fileZip.toPath().toAbsolutePath().getParent().toString(), fileName).toFile();
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				break;
			} else {
				zipEntry = zis.getNextEntry();
			}
		}
		zis.closeEntry();
		zis.close();
		if (newFile == null) {
			return null;
		} else {
			return newFile.toPath();
		}
	}
}