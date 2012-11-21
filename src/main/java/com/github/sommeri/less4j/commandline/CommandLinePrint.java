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

  public void printToSysout(CompilationResult content, String inputfile) {
    if (content == null || content.getCss() == null) {
      reportCouldNotCompileTheFile(inputfile);
      return ;
    }
    standardOut.print(content.getCss());
    printWarnings(inputfile, content);
  }

   void reportCouldNotCompileTheFile(String inputfile) {
    reportError("Could not compile the file " + inputfile);
  }

  public void printToFile(CompilationResult content, String outputFile, String inputfile) {
    if (content == null || content.getCss() == null) {
      reportCouldNotCompileTheFile(inputfile);
      return ;
    }
    outputFile(outputFile, content.getCss());
    printWarnings(inputfile, content);
  }

  public void printWarnings(String inputfile, CompilationResult content) {
    if (!content.getWarnings().isEmpty())
      standardErr.println("Warnings produced by compilation of " + inputfile);
    
    for (Problem warning : content.getWarnings()) {
      standardErr.println(toWarning(warning));
    }
  }

  private String toWarning(Problem warning) {
    return "WARNING " + toString(warning);
  }

  private String toError(Problem warning) {
    return "ERROR " + toString(warning);
  }

  private String toString(Problem problem) {
    return problem.getLine() +":" + problem.getCharacter()+ " " + problem.getMessage();
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

  public void reportErrorsAndWarnings(Less4jException ex, String inputfile) {
    printWarnings(inputfile, ex.getPartialResult());
    reportErrors(ex, inputfile);
  }

  public void reportErrors(Less4jException ex, String inputfile) {
    if (!ex.getErrors().isEmpty())
      standardErr.println("Errors produced by compilation of " + inputfile);
    
    Set<String> previousMessages = new HashSet<String>();
    for (Problem error : ex.getErrors()) {
      String message = toError(error);
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
