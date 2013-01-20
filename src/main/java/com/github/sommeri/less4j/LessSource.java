package com.github.sommeri.less4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.utils.URLUtils;

public abstract class LessSource {
	
	public abstract LessSource relativeSource(String filename) throws MalformedURLException, StringSourceException;

	public abstract String getContent() throws IOException;
	
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
			this.latestModified = lastModified;
			if (parent != null && lastModified > parent.latestModified) {
				parent.latestModified = lastModified;
			}
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
		
	}

	public static class URLSource extends AbstractHierarchicalSource {
		
		private URL inputURL;
		
		public URLSource(URL inputURL) {
			super();
			this.inputURL = inputURL;
		}
		
		public URLSource(URLSource parent, String filename) throws MalformedURLException {
			super(parent);
			this.inputURL = new URL(URLUtils.toParentURL(parent.inputURL), filename);
			parent.addImportedSource(this);
		}
		
		@Override
		public String getContent() throws IOException {
 			URLConnection connection = getInputURL().openConnection();
			Reader input = new InputStreamReader(connection.getInputStream());
			String content = IOUtils.toString(input).replace("\r\n", "\n");
			setLastModified(connection.getLastModified());
			input.close();
			return content;
		}
		
		@Override
		public LessSource relativeSource(String filename) throws MalformedURLException {
			return new URLSource(this, filename);
		}
		
		public URL getInputURL() {
			return inputURL;
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
		public String getContent() throws IOException {
			FileReader input = new FileReader(inputFile);
			String content = IOUtils.toString(input).replace("\r\n", "\n");
			setLastModified(inputFile.lastModified());
			input.close();
			return content;
		}
		
		@Override
		public LessSource relativeSource(String filename) {
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
			result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
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
			FileSource other = (FileSource) obj;
			if (inputFile == null) {
				if (other.inputFile != null)
					return false;
			} else if (!inputFile.equals(other.inputFile))
				return false;
			return true;
		}
		
	}
	
	public static class StringSource extends LessSource {
		
		private String content;
		
		public StringSource(String content) {
			this.content = content;
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

	public static class StringSourceException extends Exception {
		
	}

}
