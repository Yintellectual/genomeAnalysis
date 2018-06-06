package com.spdeveloper.chgc.genome.util.excel;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.annotation.entity.GeneAnnotated;
import com.spdeveloper.chgc.genome.util.excel.GeneAnnotatedToExcel;
import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.service.DNAParser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneAnnotatedToExcelTest {
	
	@Autowired
	GeneAnnotatedToExcel geneAnnotatedToExcel;
	
	List<GeneAnnotated> data ;
	
	@Autowired
	DNAParser dnaParser;
	
	@Before
	public void init() {
		data = new ArrayList<GeneAnnotated>();
		data.add(new GeneAnnotated("test1", 1, 2, true, "product", "best_hit_organism", "ko", "cog", "cog_class"));
		data.add(new GeneAnnotated("test2", 3, 4, true, "product", "best_hit_organism", "ko", "cog", "cog_class"));
		data.add(new GeneAnnotated("test3", 5, 6, false, "product", "best_hit_organism", "ko", "cog", "cog_class"));
		data.add(new GeneAnnotated("test4", 7, 8, true, "product", null, "ko", "cog", "cog_class"));
		data.add(new GeneAnnotated("test5", 9, 10, true, "product", "best_hit_organism", "ko", "cog", "cog_class"));
	}
	
	@Test
	public void parseBackIntoMap() throws IOException {
		Path testFile = Paths.get("test.xlsx");
		geneAnnotatedToExcel.write(testFile, data);
		
		try(InputStream inputStream = new FileInputStream(testFile.toFile())) {
			List<DNA> dnas = dnaParser.parse(inputStream, 0);
			assertEquals(5, dnas.size());
			assertEquals("test3", dnas.get(2).getGene());
			assertEquals(6, dnas.get(2).getStart().intValue());
			assertEquals(5, dnas.get(2).getStop().intValue());
			assertEquals("", dnas.get(3).getBest_hit_organism());
		}
	}
	
}
