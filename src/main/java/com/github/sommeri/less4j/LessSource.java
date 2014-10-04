package com.github.sommeri.less4j;

import java.io.File;
import java.io.FileInputStream;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.utils.URIUtils;

public abstract class LessSource {

  public abstract LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile, StringSourceException;

  public abstract String getContent() throws FileNotFound, CannotReadFile;

  public abstract byte[] getBytes() throws FileNotFound, CannotReadFile;

  /**
   * @return less source location uri or <code>null</code>. If non-null, last path part must be equal to whatever {@link #getName()} returns.
   * 
   */
  public URI getURI() {
    return null;
  }

  /**
   * Less source name. Can be anything if the {@link #getAbsoluteURI()} returns non null, otherwise last path part.
   */
  public String getName() {
    return null;
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
    private String charsetName;

    public URLSource(URL inputURL) {
      this(inputURL, null);
    }

    public URLSource(URL inputURL, String charsetName) {
      super();
      this.inputURL = inputURL;
      this.charsetName = charsetName;
    }

    public URLSource(URLSource parent, String filename) throws FileNotFound, CannotReadFile {
      this(parent, filename, null);
    }
    
    public URLSource(URLSource parent, String filename, String charsetName) throws FileNotFound, CannotReadFile {
      super(parent);
      try {
        this.inputURL = new URL(URIUtils.toParentURL(parent.inputURL), filename);
      } catch (MalformedURLException e) {
        throw new FileNotFound();
      }
      this.charsetName = charsetName;
      parent.addImportedSource(this);
    }

    @Override
    public String getContent() throws FileNotFound, CannotReadFile {
      try {
        URLConnection connection = getInputURL().openConnection();
        Reader input = charsetName != null ? new InputStreamReader(connection.getInputStream(), charsetName) : new InputStreamReader(connection.getInputStream());
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
    public byte[] getBytes() throws FileNotFound, CannotReadFile {
      try {
        URLConnection connection = getInputURL().openConnection();
        Reader input = new InputStreamReader(connection.getInputStream());
        setLastModified(connection.getLastModified());
        byte[] content = IOUtils.toByteArray(input);
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
      return new URLSource(this, filename, charsetName);
    }

    public URL getInputURL() {
      return inputURL;
    }

    @Override
    public URI getURI() {
      try {
        return inputURL.toURI();
      } catch (URISyntaxException e) {
        return null;
      }
    }

    @Override
    public String getName() {
      return new File(inputURL.getPath()).getName();
    }

    public String toString() {
      return inputURL.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((inputURL == null) ? 0 : inputURL.hashCode());
      result = prime * result + ((charsetName == null) ? 0 : charsetName.hashCode());
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
      if (charsetName == null) {
        if (other.charsetName != null)
          return false;
      } else if (!charsetName.equals(other.charsetName))
        return false;
      return true;
    }

  }

  public static class FileSource extends AbstractHierarchicalSource {

    private File inputFile;
    private String charsetName;

    public FileSource(File inputFile) {
      this(inputFile, null);
    }
    
    public FileSource(File inputFile, String charsetName) {
      this.inputFile = inputFile;
      this.charsetName = charsetName;
    }

    public FileSource(FileSource parent, String filename) {
      this(parent, filename, null);
    }
    
    public FileSource(FileSource parent, String filename, String charsetName) {
      this(parent, new File(parent.getInputFile().getParentFile(), filename), charsetName);
    }

    public FileSource(FileSource parent, File inputFile, String charsetName) {
      super(parent);
      this.inputFile = inputFile;
      this.charsetName = charsetName;
      parent.addImportedSource(this);
    }

    @Override
    public URI getURI() {
      try {
        String path = getInputFile().toString();
        path = URIUtils.convertPlatformSeparatorToUri(path);
        return new URI(path);
      } catch (URISyntaxException e) {
        return null;
      }
    }

    @Override
    public String getName() {
      return getInputFile().getName();
    }

    @Override
    public String getContent() throws FileNotFound, CannotReadFile {
      try {
        Reader input;
        if (charsetName != null) {
          input = new InputStreamReader(new FileInputStream(getInputFile()), charsetName);
        } else {
          input = new FileReader(getInputFile());
        }
        try {
          String content = IOUtils.toString(input).replace("\r\n", "\n");
          setLastModified(getInputFile().lastModified());
          return content;
        } finally {
          input.close();
        }
      } catch (FileNotFoundException ex) {
        throw new FileNotFound();
      } catch (IOException ex) {
        throw new CannotReadFile();
      }
    }

    @Override
    public byte[] getBytes() throws FileNotFound, CannotReadFile {
      try {
        byte[] content = FileUtils.readFileToByteArray(getInputFile());
        setLastModified(getInputFile().lastModified());
        return content;
      } catch (FileNotFoundException ex) {
        throw new FileNotFound();
      } catch (IOException ex) {
        throw new CannotReadFile();
      }
    }

    @Override
    public FileSource relativeSource(String filename) {
      return new FileSource(this, filename, charsetName);
    }

    public File getInputFile() {
      return inputFile;
    }

    public String toString() {
      return getInputFile().toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      File canonicalInputFile = getCanonicalFile();
      result = prime * result + ((canonicalInputFile == null) ? 0 : canonicalInputFile.hashCode());
      result = prime * result + ((charsetName == null) ? 0 : charsetName.hashCode());
      return result;
    }

    private File getCanonicalFile() {
      try {
        return getInputFile().getCanonicalFile();
      } catch (IOException e) {
        return getInputFile().getAbsoluteFile();
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
        if (other.getInputFile() != null)
          return false;
      } else if (!absoluteInputFile.equals(other.getInputFile().getAbsoluteFile()))
        return false;
      if (charsetName == null) {
        if (other.charsetName != null)
          return false;
      } else if (!charsetName.equals(other.charsetName))
        return false;
      return true;
    }

  }

  public static class StringSource extends LessSource {

    private String content;
    private String name;
    private URI uri;

    public StringSource(String content) {
      this(content, null);
    }

    public StringSource(String content, String name) {
      this.content = content;
      this.name = name;
    }

    public StringSource(String content, String name, URI uri) {
      this.content = content;
      this.name = name;
      this.uri = uri;
    }

    @Override
    public URI getURI() {
      return uri;
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
    public byte[] getBytes() {
      return content!=null?content.getBytes():null;
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
