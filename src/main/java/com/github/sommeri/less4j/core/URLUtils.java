package com.github.sommeri.less4j.core;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

	public static URL toParentURL(URL inputFile) {
		  String path = inputFile.getPath();
		  int i = path.lastIndexOf('/');
		  if (i != -1) {
			  path = path.substring(0, i + 1);
		  }
		  try {
			return new URL(inputFile.getProtocol(), inputFile.getHost(), inputFile.getPort(), path + inputFile.getQuery());
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	  }
	
}
