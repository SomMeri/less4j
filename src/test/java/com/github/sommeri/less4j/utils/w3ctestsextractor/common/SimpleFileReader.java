package com.github.sommeri.less4j.utils.w3ctestsextractor.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SimpleFileReader {
	private BufferedReader reader;

	public SimpleFileReader(String filename) {
		super();
		try {
			this.reader = new BufferedReader(new FileReader(filename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String readLine() {
		String line;
		try {
			line = reader.readLine();
			return line;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void skipHeaderIncluding(String headerEnd) {
		String line = readLine();
		while(line!=null && !line.equals(headerEnd)) {
			line = readLine();
		}
		
	}

	public void assertLine(String string) {
		String line = readLine();
		if (!string.equals(line)) {
			throw new IllegalStateException("Expected: " + string + " Was: "+ line);
		}
	}
	
	
}
