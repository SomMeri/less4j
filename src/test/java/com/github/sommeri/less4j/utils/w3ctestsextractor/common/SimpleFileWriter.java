package com.github.sommeri.less4j.utils.w3ctestsextractor.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleFileWriter {
	
	public void write(String filename, String content) throws IOException {
		createFile(filename);

		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(content);
		out.close();
	}

	private void createFile(String filename) throws IOException {
		File file = new File(filename);
		boolean exist = file.createNewFile();
		if (!exist) {
			System.out.println("File already exists.");
		}
	}

}