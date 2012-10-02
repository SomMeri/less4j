package com.github.sommeri.less4j.utils.w3ctestsextractor.selectors;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.github.sommeri.less4j.utils.w3ctestsextractor.common.CaseBuilder;
import com.github.sommeri.less4j.utils.w3ctestsextractor.common.SimpleFileWriter;

public class SelectorsDownloader extends CaseBuilder {
  private static final String OUTPUT_DIRECTORY = "/output/";

  public static void main(String[] args) throws IOException {
    (new SelectorsDownloader()).doIt();
  }

  public void doIt() throws MalformedURLException, IOException {
    SimpleFileWriter writer = new SimpleFileWriter();
    for (String string : Links.LINKS) {
      String shortName = string.substring(9, string.indexOf(".xml"));
      String link = toFullLink(shortName);
      String comment = string.substring(string.indexOf("xml\">") + 5, string.indexOf("</a>"));

      String content = getContent(link);
      String outputDirectory = getCurrentDirectory() + OUTPUT_DIRECTORY;
      ensureDirectory(outputDirectory);
      writer.write(outputDirectory + shortName + ".less", toComment(link, comment) + content);
    }
  }

  private String toComment(String link, String comment) {
    return "/* " + link + "\n" + comment + "\n*/\n";
  }

  public String getContent(String link) throws MalformedURLException, IOException {
    URL url = new URL(link);
    InputStream is = url.openStream();
    int ptr = 0;
    StringBuffer buffer = new StringBuffer();
    while ((ptr = is.read()) != -1) {
      buffer.append((char) ptr);
    }
    return buffer.toString();
  }

  public String toFullLink(String string) {
    return "http://www.w3.org/Style/CSS/Test/CSS3/Selectors/current/xml/tests/" + string + ".css";
  }
}
