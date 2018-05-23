package com.spdeveloper.chgc.genome.util.xml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class M7Parser {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BlastOutput{
		private BlastOutput_iterations BlastOutput_iterations;
	}
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BlastOutput_iterations{
		private List<PrMatch> prMatchs;
	}
	
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PrMatch{
		private String name;
		private Iteration_hits Iteration_hits;
		
		public PrMatch(String name, String Hit_def) {
			this.name = name;
			this.Iteration_hits = new Iteration_hits(new Hit(Hit_def));
		}
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Iteration_hits{
		private Hit Hit;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Hit{
		private String Hit_def;
	}
	
	public static BlastOutput parse(File m7File) {
		XStream xstream = new XStream();
		xstream.alias("BlastOutput", BlastOutput.class);
		xstream.alias("Iteration", PrMatch.class);
		xstream.aliasField("Iteration_query-def", PrMatch.class, "name");
		xstream.aliasField("Hit_def", PrMatch.class, "product");
		xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
		xstream.ignoreUnknownElements();
		xstream.addImplicitCollection(BlastOutput_iterations.class, "prMatchs", PrMatch.class);
		return (BlastOutput)xstream.fromXML(m7File);
	}
}
