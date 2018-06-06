package com.spdeveloper.chgc.genome.visualization.factory;

import com.spdeveloper.chgc.genome.util.excel.WrongExcelFormatException;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;

public class DNARNAUtil {


	public static String mininize(String cog_class) {
		if (cog_class == null || cog_class.isEmpty()) {
			return null;
		} else {
			String[] classes = cog_class.split("\\/");
			return classes[0].replaceAll("\\W", "").trim().toUpperCase();
		}
	}
	
	public static String getRNAType(RNA rna) {
		String product = rna.getProduct();
		if (mininize(product).contains("RIBOSOMALRNA")||mininize(product).contains("RRNA")) {
			return "rRNA";
		} else if (mininize(product).contains("TRNA")) {
			return "tRNA";
		} else {
			throw new WrongExcelFormatException(
					"Some RNA product contains no key words \"Ribosomal RNA\", or \"RRNA\" or \"TRNA\".");
		}
	}
	
}
