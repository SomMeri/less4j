package com.github.sommeri.less4j.commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.utils.ProblemsPrinter;
import com.github.sommeri.less4j.utils.ProblemsPrinter.SourceNamePrinter;
import com.github.sommeri.less4j.utils.URIUtils;

public class CommandLinePrint {

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

  public void printToFiles(CompilationResult content, String lessFileName, File rootLessFile, String cssFilename, String mapFilename) {
    if (content == null || content.getCss() == null) {
      reportCouldNotCompileTheFile(lessFileName);
      return;
    }
    outputFile(cssFilename, content.getCss());
    if (mapFilename != null && content.getSourceMap() != null)
      outputFile(mapFilename, content.getSourceMap());
    printWarnings(lessFileName, rootLessFile, content);
  }

  public void printWarnings(String inputfileName, File rootInputFile, CompilationResult content) {
    SourceNamePrinter sourceNamePrinter = new RelativeFileSourceNamePrinter(rootInputFile);
    ProblemsPrinter problemsPrinter = new ProblemsPrinter(sourceNamePrinter);
    
    if (!content.getWarnings().isEmpty())
      standardErr.println("Warnings produced by compilation of " + inputfileName);

    String warnings = problemsPrinter.printWarnings(content.getWarnings());
    standardErr.print(warnings);
  }

  private void outputFile(String filename, String content) {
    File file = new File(filename);
    outputFile(filename, file, content);
  }

  private void outputFile(String reportFilename, File file, String content) {
    try {
      file.createNewFile();
    } catch (IOException e) {
      reportError("Could not create the file " + reportFilename);
      reportError(e);
      return;
    }

    if (!file.canWrite()) {
      reportError("Can not write into file " + reportFilename);
      return;
    }
    try {
      FileWriter output = new FileWriter(file);
      IOUtils.write(content, output);
      output.close();
    } catch (IOException e) {
      reportError("Can not write into file " + reportFilename);
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
    SourceNamePrinter sourceNamePrinter = new RelativeFileSourceNamePrinter(rootInputFile);
    ProblemsPrinter problemsPrinter = new ProblemsPrinter(sourceNamePrinter);

    if (!ex.getErrors().isEmpty())
      standardErr.println("Errors produced by compilation of " + inputfileName);

    String errors = problemsPrinter.printErrors(ex.getErrors());
    standardErr.print(errors);
  }

  protected void reportError(Throwable e) {
    e.printStackTrace(standardErr);
  }

}

class RelativeFileSourceNamePrinter implements SourceNamePrinter {

  private final File rootInputFile;

  public RelativeFileSourceNamePrinter(File rootInputFile) {
    super();
    this.rootInputFile = rootInputFile;
  }

  @Override
  public String printSourceName(LessSource source) {
    File file = source instanceof LessSource.FileSource ? ((LessSource.FileSource) source).getInputFile() : null;

    if (file == null)
      return "";

    if (rootInputFile == null) {
      return file.getPath();
    }

    File absoluteRoot = rootInputFile.getAbsoluteFile();
    File absoluteFile = file.getAbsoluteFile();

    if (sameFile(absoluteRoot, absoluteFile))
      return "";

    return URIUtils.relativize(absoluteRoot.getParentFile(), absoluteFile);
  }

  private boolean sameFile(File file1, File file2) {
    String fileAbsoluteName = file2.getAbsolutePath();
    String rootInputFileAbsoluteName = file1.getAbsolutePath();
    boolean equals = fileAbsoluteName.equals(rootInputFileAbsoluteName);
    return equals;
  }

}
