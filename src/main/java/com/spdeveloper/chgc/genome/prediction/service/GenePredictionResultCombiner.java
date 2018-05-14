package com.spdeveloper.chgc.genome.prediction.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;

public class GenePredictionResultCombiner {
	private static final Logger log = LoggerFactory.getLogger(GenePredictionResultCombiner.class);
	public static List<GenePrediction> combine(List<GenePrediction> one, List<GenePrediction> two) {
		TreeMap<Integer, GenePrediction> oneMap = toMap(one);
		
		TreeMap<Integer, GenePrediction> twoMap = toMap(two);
		
		// 1. some "stop" position of genes are recognized in both lists, find them.
		oneMap.keySet().retainAll(twoMap.keySet());
		TreeMap<Integer, GenePrediction> chosen = oneMap;
		oneMap = toMap(one);
		// 2. for these shared "stop" positions, get the corresponding genes in both
		// lists.
		// 2.1. choose the longer one among the two genes, add it into "chosen"
		for (Integer stop : chosen.keySet()) {
			chosen.put(stop, chooseFrom(oneMap.get(stop), twoMap.get(stop)));
		}
		

		
		// 3. delete all the genes correspond to the shared stop positions in both
		// lists,
		// and add the rest of the genes together to form a new list called "theRest"
		oneMap.keySet().removeAll(chosen.keySet());
		twoMap.keySet().removeAll(chosen.keySet());
		oneMap.putAll(twoMap);
		TreeMap<Integer, GenePrediction> theRest = oneMap;
		// 4. for each chosen, in "theRest" find all the overlapped genes, add them into
		// "toBeDeleted"
		
		if (theRest != null && !theRest.isEmpty()) {
			List<GenePrediction> toBeDeleted = new ArrayList<>();
			for (Integer stop : chosen.keySet()) {
				pickOverlapped(chosen.get(stop), theRest, toBeDeleted);
			}

			// 5. delete all the elements in "toBeDeleted" from "theRest"
			toBeDeleted.forEach(g -> theRest.remove(new Integer(g.getStop())));

			// 6. put "chosen" and "theRest" together.
			chosen.putAll(theRest);
		}
		List<GenePrediction> result = new ArrayList<GenePrediction>(chosen.values());
		// 7. sort by the start(where the gene starts, the value is getStop() for those
		// genes of negative direction)
		Collections.sort(result, new Comparator<GenePrediction>() {

			@Override
			public int compare(GenePrediction o1, GenePrediction o2) {
				// TODO Auto-generated method stub
				return Integer.compare(o1.getInit(), o2.getInit());
			}

		});

		// 8. rename genes

		for (int i = 1; i <= result.size(); i++) {
			result.get(i - 1).setName(String.format("orf_%04d", i));
		}

		// 9. return as a list
		

		
		return result;
	}

	static void pickOverlapped(GenePrediction chosen, TreeMap<Integer, GenePrediction> theRest,
			List<GenePrediction> toBeDeleted) {
		final Integer threshold = 90;
		int fromKey = chosen.getStart() + 90;
		if (fromKey > theRest.lastKey()) {
			return;
		}
		SortedMap<Integer, GenePrediction> subMapUnderChoice = theRest.subMap(fromKey, theRest.lastKey());
		for (GenePrediction deleteIfOverlap : subMapUnderChoice.values()) {
			if (deleteIfOverlap.getStart() < chosen.getStop() - 89) {
				// overLaps
				toBeDeleted.add(deleteIfOverlap);
			} else {
				break;
			}
		}
	}

	static GenePrediction chooseFrom(GenePrediction one, GenePrediction two) {
		if (one.getLength() >= two.getLength()) {
			return one;
		} else {
			return two;
		}
	}

	static TreeMap<Integer, GenePrediction> toMap(List<GenePrediction> genePredictions) {
		return new TreeMap<>(genePredictions.stream().collect(Collectors.toMap(g -> {
			if (g.isPositive()) {
				return g.getStop();
			} else {
				return g.getStart();
			}
		}, i -> i)));
	}
}
