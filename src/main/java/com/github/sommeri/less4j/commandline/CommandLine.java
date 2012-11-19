package com.github.sommeri.less4j.commandline;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class CommandLine {
  private static final String NAME = "less4j";
  private static final String INTRO = "Less4j compiles less files into css files. It can run in two " + "modes: single input file mode or in multiple input files mode. Less4j uses single file mode by default. " + "\n\n" + "Single file mode: Less4j expects one or two arguments. First one contains input less filename and the second one contains " + "the output css filename. If the output file argument is not present, less4j will print the result into standard output." + "\n\n" + "Multiple files mode: Must be turned on by '-m' or '--multiMode' parameter. Less4j assumes that all input files are " + "less files. All are going to be compiled into css files. Each input file will generate " + "an output file with the same name and suffix '.css'." + "\n\n";
  private static final String OUTRO = "\nExamples:\n" + " - Compile 'test.less' file and print the result into standard output:\n  # less4j test.less\n\n" + " - Compile 'test.less' file and print the result into 'test.css' file:\n  # less4j test.less test.css\n\n" + " - Compile 't1.less', 't2.less' and 't3.less' files into 't1.css', 't2.css' and 't3.css':\n  # less4j -m t1.less t2.less t3.less\n\n" + " - Compile 't1.less', 't2.less', 't3.less' files into 't1.css', 't2.css', 't3.css'. Place the result \n  into '..\\css\\' directory:\n  # less4j -m -o ..\\css\\ t1.less t2.less t3.less\n\n";
  private static final String SEPARATOR = java.io.File.separator;

  private CommandLinePrint print;

  public CommandLine() {
    print = new CommandLinePrint();
  }

  public static void main(String[] args) {
    CommandLine me = new CommandLine();
    me.realMain(args);
  }

  private void realMain(String[] args) {
    Arguments arguments = new Arguments();
    if (args.length == 0) {
      args = new String[] { "--help" };
    }

    try {
      JCommander jCommander = new JCommander(arguments, args);
      jCommander.setProgramName(NAME);

      if (arguments.isHelp()) {
        printHelp(jCommander);
        return;
      }

      if (arguments.isHelp()) {
        printHelp(jCommander);
        return;
      }

      if (arguments.isVersion()) {
        printVersion(jCommander);
        return;
      }

    } catch (ParameterException ex) {
      System.err.println(ex.getMessage());

    }

    if (arguments.isMultiMode()) {
      runAsMultimode(arguments.getFiles(), arguments.getOutputDirectory(), arguments.isPrintIncorrect());
    } else {
      runAsSinglemode(arguments.getFiles(), arguments.isPrintIncorrect());
    }
  }

  private void runAsSinglemode(List<String> files, boolean printPartial) {
    if (files.isEmpty()) {
      print.reportError("No file available.");
      return;
    }

    String inputfile = files.get(0);
    try {
      CompilationResult content = compile(inputfile);
      singleModePrint(files, inputfile, content);
    } catch (Less4jException ex) {
      CompilationResult partialResult = ex.getPartialResult();
      if (printPartial) {
        singleModePrint(files, inputfile, partialResult);
        print.reportErrors(ex, inputfile);
      } else {
        print.reportErrorsAndWarnings(ex, inputfile);
      }
      print.reportCouldNotCompileTheFile(inputfile);
    }

  }

  private void singleModePrint(List<String> files, String inputfile, CompilationResult content) {
    if (files.size() == 1) {
      print.printToSysout(content, inputfile);
    } else {
      print.printToFile(content, files.get(1), inputfile);
    }
  }

  private void runAsMultimode(List<String> files, String outputDirectory, boolean printPartial) {
    if (!ensureDirectory(outputDirectory))
      return;

    for (String filename : files) {
      try {
        CompilationResult content = compile(filename);
        print.printToFile(content, toOutputFilename(outputDirectory, filename), filename);
      } catch (Less4jException ex) {
        CompilationResult partialResult = ex.getPartialResult();
        if (printPartial) {
          print.printToFile(partialResult, toOutputFilename(outputDirectory, filename), filename);
          print.reportErrors(ex, filename);
        } else {
          print.reportErrorsAndWarnings(ex, filename);
        }
        print.reportCouldNotCompileTheFile(filename);
      }

    }
  }

  private boolean ensureDirectory(String outputDirectory) {
    if (outputDirectory == null || outputDirectory.isEmpty())
      return true;

    File directory = new File(outputDirectory);
    if (directory.exists() && !directory.isDirectory()) {
      print.reportError(directory + " is not a directory.");
      return false;
    }

    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        print.reportError("Could not create the directory " + directory + ".");
        return false;
      }
    }

    return true;
  }

  private String toOutputFilename(String outputDirectory, String inputFilename) {
    String filename = changeSuffix(inputFilename);
    if (outputDirectory == null || outputDirectory.isEmpty()) {
      return filename;
    }

    if (!outputDirectory.endsWith(SEPARATOR)) {
      outputDirectory += SEPARATOR;
    }

    return outputDirectory + new File(filename).getName();
  }

  private String changeSuffix(String filename) {
    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf == -1)
      return filename + ".css";

    return filename.substring(0, lastIndexOf) + ".css";
  }

  private CompilationResult compile(String filename) throws Less4jException {
    File inputFile = new File(filename);
    if (!inputFile.exists()) {
      print.reportError("The file " + filename + " does not exists.");
      return null;
    }
    if (!inputFile.canRead()) {
      print.reportError("Cannot read the file " + filename + ".");
      return null;
    }
    String content;
    try {
      FileReader input = new FileReader(inputFile);
      content = IOUtils.toString(input).replace("\r\n", "\n");
      input.close();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    DefaultLessCompiler compiler = new DefaultLessCompiler();
    return compiler.compile(content);
  }

  private void printVersion(JCommander jCommander) {
    StringBuilder builder = new StringBuilder(NAME);
    builder.append(" ").append(getVersion());
    System.out.println(builder);
  }

  private void printHelp(JCommander jCommander) {
    StringBuilder builder = new StringBuilder();
    wrapDescription(builder, jCommander.getColumnSize(), INTRO);
    jCommander.usage(builder);
    builder.append(OUTRO);
    System.out.println(builder);
  }

  private void wrapDescription(StringBuilder out, int columnSize, String description) {
    int max = columnSize;
    String[] words = description.split(" ");
    int current = 0;
    int i = 0;
    while (i < words.length) {
      String word = words[i];
      if (word.length() > max || current + word.length() <= max) {
        out.append(i == 0 ? "" : " ").append(word);
        current += word.length() + 1;
      } else {
        out.append("\n").append(word);
        current = 0;
      }
      i++;
    }
  }

  public static String getVersion() {
    String path = "/version.prop";

    InputStream stream = path.getClass().getResourceAsStream(path);
    if (stream == null)
      return "UNKNOWN";
    Properties props = new Properties();
    try {
      props.load(stream);
      stream.close();
      return (String) props.get("version");
    } catch (IOException e) {
      return "UNKNOWN";
    }
  }
}
