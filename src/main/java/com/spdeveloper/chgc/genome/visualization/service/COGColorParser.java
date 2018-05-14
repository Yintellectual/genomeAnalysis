package com.spdeveloper.chgc.genome.visualization.service;

import java.io.IOException;
import java.util.Map;

public interface COGColorParser {
	Map<String, String> parse(String fileName, int sheet) throws IOException;
}
