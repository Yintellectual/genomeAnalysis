package com.spdeveloper.chgc.genome.util.system;

public class SystemUtil {
	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.toUpperCase().contains("WINDOWS");
	}
}
