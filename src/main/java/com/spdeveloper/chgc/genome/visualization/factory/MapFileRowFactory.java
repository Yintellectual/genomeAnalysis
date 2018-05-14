package com.spdeveloper.chgc.genome.visualization.factory;

import static com.spdeveloper.chgc.genome.visualization.factory.DNARNAUtil.getRNAType;
import static com.spdeveloper.chgc.genome.visualization.factory.DNARNAUtil.mininize;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.MapFileRow;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;

public class MapFileRowFactory {
	private static final Logger log = LoggerFactory.getLogger(MapFileRowFactory.class);
	public static final Map<String, String> colorMapping = new HashMap<String, String>() {
		{
			put("COENZYMEMETABOLISM", "H");
			put("TRANSCRIPTION", "K");
			put("ENERGYPRODUCTIONANDCONVERSION", "C");
			put("DEFENSEMECHANISMS", "V");
			put("INTRACELLULARTRAFFICKINGANDSECRETION", "U");
			put("CELLDIVISIONANDCHROMOSOMEPARTITIONING", "D");
			put("SECONDARYMETABOLITESBIOSYNTHESISTRANSPORTANDCATABOLISM", "Q");
			put("NOTINCOGS", "-");
			put("SIGNALTRANSDUCTIONMECHANISMS", "T");
			put("RNAPROCESSINGANDMODIFICATION", "A");
			put("CELLENVELOPEBIOGENESISOUTERMEMBRANE", "M");
			put("GENERALFUNCTIONPREDICTIONONLY", "R");
			put("LIPIDMETABOLISM", "I");
			put("CARBOHYDRATETRANSPORTANDMETABOLISM", "G");
			put("NUCLEARSTRUCTURE", "Y");
			put("CHROMATINSTRUCTUREANDDYNAMICS", "B");
			put("POSTTRANSLATIONALMODIFICATIONPROTEINTURNOVERCHAPERONES", "O");
			put("TRANSLATIONRIBOSOMALSTRUCTUREANDBIOGENESIS", "J");
			put("NUCLEOTIDETRANSPORTANDMETABOLISM", "F");
			put("CELLMOTILITYANDSECRETION", "N");
			put("DNAREPLICATIONRECOMBINATIONANDREPAIR", "L");
			put("EXTRACELLULARSTRUCTURES", "W");
			put("AMINOACIDTRANSPORTANDMETABOLISM", "E");
			put("CYTOSKELETON", "Z");
			put("FUNCTIONUNKNOWN", "S");
			put("INORGANICIONTRANSPORTANDMETABOLISM", "P");
		}
	};

	public static String color(DNA dna) {

		String color = colorMapping.get(mininize(dna.getCog_class()));
		if (color == null) {
			color = "-";
		}
		return color;
	}

	public static String color(RNA rna) {
		String rnaType = getRNAType(rna);
		if("rRNA".equals(rnaType)) {
			return "G";
		}else {
			return "Z";
		}
	}


	public static MapFileRow toMapFileRow(DNA data) {
		String gene = data.getGene();
		String color = color(data);
		String symbol = "+";
		Integer start = (int) data.getStart().doubleValue();
		Integer stop = (int) data.getStop().doubleValue();
		String product = data.getProduct();
		return new MapFileRow(gene, color, symbol, start, stop, product);
	}

	public static MapFileRow toMapFileRow(RNA rna) {
		String id = rna.getRna();
		String color = color(rna);
		String symbol = "+";
		Integer start = rna.getStart().intValue();
		Integer stop = rna.getStop().intValue();
		if (start <= stop) {
			start -= 500;
			stop += 500;
		} else {
			start += 500;
			stop -= 500;
		}
		if (start < 0) {
			start = 0;
		} else if (stop < 0) {
			stop = 0;
		} else {

		}
		String product = rna.getProduct();
		return new MapFileRow(id, color, symbol, start, stop, product);
	}
}
