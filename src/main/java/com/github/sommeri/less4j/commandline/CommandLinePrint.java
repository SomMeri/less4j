package com.github.sommeri.less4j.commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.platform.Constants;

public class CommandLinePrint {

  private static final String URI_FILE_SEPARATOR = "/";
  private PrintStream standardOut;
  private PrintStream standardErr;

  public CommandLinePrint() {
    this(System.out, System.err);
  }

  public CommandLinePrint(PrintStream standardOut, PrintStream standardErr) {
    super();
    this.standardOut = standardOut;
    this.standardErr = standardErr;
  }

  public void printToSysout(CompilationResult content, String inputfileName, File rootInputFile) {
    if (content == null || content.getCss() == null) {
      reportCouldNotCompileTheFile(inputfileName);
      return;
    }
    standardOut.print(content.getCss());
    printWarnings(inputfileName, rootInputFile, content);
  }

  void reportCouldNotCompileTheFile(String inputfile) {
    reportError("Could not compile the file " + inputfile);
  }

  public void printToFile(CompilationResult content, String outputFile, String inputfileName, File rootInputFile) {
    if (content == null || content.getCss() == null) {
      reportCouldNotCompileTheFile(inputfileName);
      return;
    }
    outputFile(outputFile, content.getCss());
    printWarnings(inputfileName, rootInputFile, content);
  }

  public void printWarnings(String inputfileName, File rootInputFile, CompilationResult content) {
    if (!content.getWarnings().isEmpty())
      standardErr.println("Warnings produced by compilation of " + inputfileName);

    for (Problem warning : content.getWarnings()) {
      standardErr.println(toWarning(warning, rootInputFile));
    }
  }

  private String toWarning(Problem warning, File rootInputFile) {
    return "WARNING " + toString(warning, rootInputFile);
  }

  private String toError(Problem warning, File rootInputFile) {
    return "ERROR " + toString(warning, rootInputFile);
  }

  private String toString(Problem problem, File rootInputFile) {
    String filename = toFilenameReport(rootInputFile, problem.getFile());
    if (!filename.isEmpty())
      filename = filename + " ";
    String lineChar = toLineCharReport(problem);

    if (!lineChar.isEmpty())
      lineChar = lineChar + " ";
    
    return filename + lineChar + problem.getMessage();
  }

  private String toLineCharReport(Problem problem) {
    if (problem.getLine()==-1 || problem.getCharacter()==-1)
      return "";
    
    return problem.getLine() + ":" + problem.getCharacter();
  }
  
  private String toFilenameReport(File rootInputFile, File file) {
    if (file==null)
      return "";

    if (rootInputFile==null) {
      return file.getPath();
    }

    String fileAbsoluteName = file.getAbsoluteFile().getAbsolutePath();
    String rootInputFileAbsoluteName = rootInputFile.getAbsoluteFile().getAbsolutePath();
    if (fileAbsoluteName.equals(rootInputFileAbsoluteName))
      return "";
    
    return relativize(rootInputFile.getAbsoluteFile().getParent(), fileAbsoluteName);
}

  private String relativize(String base, String path) {
    String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();
    relative = relative.replace(URI_FILE_SEPARATOR, Constants.FILE_SEPARATOR);
    return relative;
  }

  private void outputFile(String filename, String content) {
    File file = new File(filename);
    try {
      file.createNewFile();
    } catch (IOException e) {
      reportError("Could not create the file " + filename);
      reportError(e);
      return;
    }

    if (!file.canWrite()) {
      reportError("Can not write into file " + filename);
      return;
    }
    try {
      FileWriter output = new FileWriter(file);
      IOUtils.write(content, output);
      output.close();
    } catch (IOException e) {
      reportError("Can not write into file " + filename);
      reportError(e);
      return;
    }
  }

  protected void reportError(String message) {
    standardErr.println(message);
  }

  public void reportErrorsAndWarnings(Less4jException ex, String inputfileName, File rootInputFile) {
    printWarnings(inputfileName, rootInputFile, ex.getPartialResult());
    reportErrors(ex, inputfileName, rootInputFile);
  }

  public void reportErrors(Less4jException ex, String inputfileName, File rootInputFile) {
    if (!ex.getErrors().isEmpty())
      standardErr.println("Errors produced by compilation of " + inputfileName);

    Set<String> previousMessages = new HashSet<String>();
    for (Problem error : ex.getErrors()) {
      String message = toError(error, rootInputFile);
      if (!previousMessages.contains(message)) {
        standardErr.println(message);
        previousMessages.add(message);
      }
    }
  }

  protected void reportError(Throwable e) {
    e.printStackTrace(standardErr);
  }

}
