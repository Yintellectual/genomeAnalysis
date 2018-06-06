package com.spdeveloper.chgc.genome.annotation.entity;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.Hit;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.Iteration_hits;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneAnnotatedPrMatchCogConstructorTest {

	private PrMatch sample_Multispecies = new PrMatch("ORF_0001", "MULTISPECIES: chromosomal replication initiator protein DnaA [Lactobacillus casei group] &gt;gi|226735820|sp|B3W6N4.1|DNAA_LACCB RecName: Full=Chromosomal replication initiator protein DnaA &gt;gi|190711127|emb|CAQ65133.1| Chromosomal replication initiator protein dnaA [Lactobacillus casei BL23] &gt;gi|327380862|gb|AEA52338.1| DNA-directed DNA replication initiator protein [Lactobacillus casei LC2W] &gt;gi|327384028|gb|AEA55502.1| DNA-directed DNA replication initiator protein [Lactobacillus casei BD-II] &gt;gi|406356678|emb|CCK20948.1| Chromosomal replication initiator protein DnaA [Lactobacillus casei W56] &gt;gi|511397248|gb|EPC35369.1| Chromosomal replication initiator protein DnaA [Lactobacillus paracasei subsp. paracasei Lpp223] &gt;gi|511451961|gb|EPC81929.1| chromosomal replication initiation protein [Lactobacillus paracasei subsp. paracasei Lpp37]");
	private PrMatch sample_SingleMatch = new PrMatch("ORF_0001", "gi|327385426|gb|AEA56900.1| Deoxynucleoside kinase subfamily, putative [Lactobacillus casei BD-II]");
	private GenePrediction genePrediction = new GenePrediction("ORF_0001", 131, 829, false);
	GeneAnnotated geneAnnotated_Multispecies = new GeneAnnotated(genePrediction, sample_Multispecies, null);
	GeneAnnotated geneAnnotated_SingleMatch = new GeneAnnotated(genePrediction, sample_SingleMatch, null);
	
	
	@Test
	public void takeTheFirstMatchForMULTISPECIES() {
		assertThat(geneAnnotated_Multispecies.getProduct(), containsString("chromosomal replication initiator protein DnaA"));
	}
	
	@Test
	public void keep_MULTISPECIES_as_part_of_Product() {
		assertThat(geneAnnotated_Multispecies.getProduct(), startsWith("MULTISPECIES: "));
	}
	
	@Test
	public void locate_Best_Hit_Organism_withIn_Square_brackets() {
		assertEquals(geneAnnotated_Multispecies.getBest_hit_organism(), "Lactobacillus casei group");
		assertEquals(geneAnnotated_SingleMatch.getBest_hit_organism(), "Lactobacillus casei BD-II");
	}
	
	@Test
	public void IgnoreEverythingFromGiToVerticleBar(){
		assertEquals("Deoxynucleoside kinase subfamily, putative", geneAnnotated_SingleMatch.getProduct());
	}
	
	@Test
	public void setValuesNullOtherwise() {
		String hit_def = null;
		GeneAnnotated sample  = new GeneAnnotated(genePrediction, new PrMatch("ORF_0001", hit_def), null);
		
		assertNull(sample.getBest_hit_organism());
		assertNull(sample.getProduct());
	}
	
	@Test 
	public void worksForCog() {
		
		String cog_hit_def = "COG2252, COG2252, Permeases [General function prediction only].";
		GeneAnnotated sample  = new GeneAnnotated(genePrediction, sample_Multispecies, new PrMatch("ORF_0001", cog_hit_def));
		
		assertEquals("COG2252", sample.getCog());
		assertEquals("General function prediction only", sample.getCog_class());
	}
}
