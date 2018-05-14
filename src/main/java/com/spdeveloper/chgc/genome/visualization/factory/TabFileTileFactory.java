package com.spdeveloper.chgc.genome.visualization.factory;

import static com.spdeveloper.chgc.genome.visualization.factory.DNARNAUtil.getRNAType;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.DNATabFileTile;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.entity.RNATabFileTile;
public class TabFileTileFactory {

	public static final String[][] DNA_TILE_TEMPLATE = new String[][] { 
			{ "%d", "%d", "gene", null, null },
			{ null, null, null, "gene", null }, 
			{ null, null, null, "locus_tag", "%s" },
			{ "%d", "%d", "CDS", null, null }, 
			{ null, null, null, "product", "%s" },
			{ null, null, null, "protein_id", "%s" }, 
			{ null, null, null, "transl_table", "11" } };

	public static final String[][] RNA_TILE_TEMPLATE = new String[][] { 
			{ "%d", "%d", "gene", null, null },
			{ null, null, null, "gene", null }, 
			{ null, null, null, "locus_tag", "%s" },
			{ "%d", "%d", "%s", null, null }, 
			{ null, null, null, "product", "%s" } };
	public static final String DNA_TILE_TEMPLATE_STRING;
	public static final String RNA_TILE_TEMPLATE_STRING;
	static {
		DNA_TILE_TEMPLATE_STRING = toTemplateString(DNA_TILE_TEMPLATE, 7, 5);
		RNA_TILE_TEMPLATE_STRING = toTemplateString(RNA_TILE_TEMPLATE, 5, 5);
	}

	public static String toTemplateString(String[][] arr, int rows, int cols) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				String cell = arr[i][j];
				if (cell == null || cell.isEmpty()) {

				} else {
					sb.append(cell);
				}
				sb.append("\t");	
			}
			sb.append("\n");	
		}
		return sb.toString();
	}

	public static String toDNATile(DNATabFileTile dna) {
		return String.format(DNA_TILE_TEMPLATE_STRING, 
				dna.getStart(), dna.getStop(), 
				dna.getGene(), 
				dna.getStart(), dna.getStop(), dna.getProduct(), "gnl|CHGC|" + dna.getGene());
	}
	public static String[] toDNATileStringArray(DNATabFileTile dna) {
		return toDNATile(dna).split("\\n");
	}
	
	public static String toRNATile(RNATabFileTile rna) {
		return String.format(RNA_TILE_TEMPLATE_STRING, rna.getStart(), rna.getStop(), rna.getRna(), rna.getStart(),
				rna.getStop(), rna.getRnaType(), rna.getProduct());
	}
	public static String[] toRNATileStringArray(RNATabFileTile rna) {
		return 	toRNATile(rna).split("\\n");
	}

	public static DNATabFileTile toDNATabFileTile(DNA dna) {
		Integer start = dna.getStart().intValue();
		Integer stop = dna.getStop().intValue();
		String gene = dna.getGene();
		String product = dna.getProduct();
		return new DNATabFileTile(start, stop, gene, product);
	}

	public static RNATabFileTile toRNATabFileTile(RNA rna) {
		Integer start = rna.getStart().intValue();
		Integer stop = rna.getStop().intValue();
		String rnaType = getRNAType(rna);
		String rna_id = rna.getRna();
		String product = null;
		if ("tRNA".equals(rnaType)) {
			product = getTRNAProduct(rna);
		} else {
			product = rna.getProduct();
		}
		return new RNATabFileTile(start, stop, rnaType, rna_id, product);
	}



	public static String getTRNAProduct(RNA tRNA) {

		String product = tRNA.getProduct().trim();
		String remaining = product.substring(0, product.length()-4);
		String sequece = product.substring(product.length() - 3, product.length()).toUpperCase();
		StringBuilder sb = new StringBuilder(sequece);
		int[] chars = sb.reverse().chars().map(c -> {
			int result = -1;
			switch (c) {
			case 'A':
				result = (int) 'T';
				break;
			case 'G':
				result = (int) 'C';
				break;
			case 'T':
				result = (int) 'A';
				break;
			case 'C':
				result = (int) 'G';
				break;
			}
			return result;
		}).toArray();
		return remaining + "("+new String(chars, 0, 3)+")";
	}
}
