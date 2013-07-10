package com.github.sommeri.less4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.utils.URIUtils;

public abstract class LessSource {

  public abstract LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile, StringSourceException;

  public abstract String getContent() throws FileNotFound, CannotReadFile;

  /**
   * @return absolute uri of less source location. Return null if absolute uri
   *         is not know. If non-null, last path part must be equal to whatever
   *         {@link #getName()} returns.
   * 
   */
  public URI getAbsoluteURI() {
    return null;
  }

  /**
   * Less source name. If the {@link #getAbsoluteURI()} returns non null.
   */
  public String getName() {
    return "";
  }

  public abstract static class AbstractHierarchicalSource extends LessSource {

    protected AbstractHierarchicalSource parent;

    protected long lastModified;

    protected long latestModified;

    protected Collection<LessSource> importedSources;

    public AbstractHierarchicalSource() {
      super();

      importedSources = new ArrayList<LessSource>();
    }

    public AbstractHierarchicalSource(AbstractHierarchicalSource parent) {
      super();
      this.parent = parent;
    }

    protected void setLastModified(long lastModified) {
      this.lastModified = lastModified;
      setLatestModified(lastModified);
    }

    protected void addImportedSource(LessSource source) {
      if (parent != null) {
        parent.addImportedSource(source);
      } else {
        importedSources.add(source);
      }
    }

    public Collection<LessSource> getImportedSources() {
      return importedSources;
    }

    public long getLastModified() {
      return lastModified;
    }

    public long getLatestModified() {
      return latestModified;
    }

    public void setLatestModified(long latestModified) {
      this.latestModified = latestModified;
      if (parent != null && latestModified > parent.getLatestModified()) {
        parent.setLatestModified(latestModified);
      }
    }

  }

  public static class URLSource extends AbstractHierarchicalSource {

    private URL inputURL;

    public URLSource(URL inputURL) {
      super();
      this.inputURL = inputURL;
    }

    public URLSource(URLSource parent, String filename) throws FileNotFound, CannotReadFile {
      super(parent);
      try {
        this.inputURL = new URL(URIUtils.toParentURL(parent.inputURL), filename);
      } catch (MalformedURLException e) {
        throw new FileNotFound();
      }
      parent.addImportedSource(this);
    }

    @Override
    public String getContent() throws FileNotFound, CannotReadFile {
      try {
        URLConnection connection = getInputURL().openConnection();
        Reader input = new InputStreamReader(connection.getInputStream());
        String content = IOUtils.toString(input).replace("\r\n", "\n");
        setLastModified(connection.getLastModified());
        input.close();
        return content;
      } catch (FileNotFoundException ex) {
        throw new FileNotFound();
      } catch (IOException ex) {
        throw new CannotReadFile();
      }

    }

    @Override
    public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile {
      return new URLSource(this, filename);
    }

    public URL getInputURL() {
      return inputURL;
    }

    @Override
    public URI getAbsoluteURI() {
      try {
        return inputURL.toURI();
      } catch (URISyntaxException e) {
        return null;
      }
    }

    @Override
    public String getName() {
      return inputURL.getFile();
    }

    public String toString() {
      return inputURL.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((inputURL == null) ? 0 : inputURL.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      URLSource other = (URLSource) obj;
      if (inputURL == null) {
        if (other.inputURL != null)
          return false;
      } else if (!inputURL.equals(other.inputURL))
        return false;
      return true;
    }

  }

  public static class FileSource extends AbstractHierarchicalSource {

    private File inputFile;

    public FileSource(File inputFile) {
      this.inputFile = inputFile;
    }

    public FileSource(FileSource parent, String filename) {
      super(parent);
      this.inputFile = new File(parent.inputFile.getParentFile(), filename);
      parent.addImportedSource(this);
    }

    @Override
    public URI getAbsoluteURI() {
      return inputFile.getAbsoluteFile().toURI();
    }

    @Override
    public String getName() {
      return inputFile.getName();
    }

    @Override
    public String getContent() throws FileNotFound, CannotReadFile {
      try {
        FileReader input = new FileReader(inputFile);
        String content = IOUtils.toString(input).replace("\r\n", "\n");
        setLastModified(inputFile.lastModified());
        input.close();
        return content;
      } catch (FileNotFoundException ex) {
        throw new FileNotFound();
      } catch (IOException ex) {
        throw new CannotReadFile();
      }
    }

    @Override
    public FileSource relativeSource(String filename) {
      return new FileSource(this, filename);
    }

    public File getInputFile() {
      return inputFile;
    }

    public String toString() {
      return inputFile.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      File canonicalInputFile = getCanonicalFile();
      result = prime * result + ((canonicalInputFile == null) ? 0 : canonicalInputFile.hashCode());
      return result;
    }

    private File getCanonicalFile() {
      try {
        return inputFile.getCanonicalFile();
      } catch (IOException e) {
        return inputFile.getAbsoluteFile();
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FileSource other = (FileSource) obj;
      File absoluteInputFile = getCanonicalFile();
      if (absoluteInputFile == null) {
        if (other.inputFile != null)
          return false;
      } else if (!absoluteInputFile.equals(other.inputFile.getAbsoluteFile()))
        return false;
      return true;
    }

  }

  public static class StringSource extends LessSource {

    private String content;
    private String name;

    public StringSource(String content) {
      this(content, "");
    }
    
    public StringSource(String content, String name) {
      this.content = content;
      this.name = name;
    }

    @Override
    public URI getAbsoluteURI() {
      return null;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getContent() {
      return content;
    }

    @Override
    public LessSource relativeSource(String filename) throws StringSourceException {
      throw new StringSourceException();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((content == null) ? 0 : content.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      StringSource other = (StringSource) obj;
      if (content == null) {
        if (other.content != null)
          return false;
      } else if (!content.equals(other.content))
        return false;
      return true;
    }

  }

  @SuppressWarnings("serial")
  public static class StringSourceException extends Exception {

  }

  @SuppressWarnings("serial")
  public static class FileNotFound extends Exception {

  }

  @SuppressWarnings("serial")
  public static class CannotReadFile extends Exception {

  }

}
