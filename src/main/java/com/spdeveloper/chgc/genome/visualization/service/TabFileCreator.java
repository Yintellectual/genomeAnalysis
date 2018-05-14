package com.spdeveloper.chgc.genome.visualization.service;

import java.io.IOException;
import java.util.List;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;

public interface TabFileCreator{
	public void create(List<DNA> dnas, List<RNA> rnas, String fileName)throws IOException;
	public String[] createStringArray(List<DNA> dnas, List<RNA> rnas, String fileName)throws IOException;
}
