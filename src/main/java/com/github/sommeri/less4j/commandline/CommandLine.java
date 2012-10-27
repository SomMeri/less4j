package com.github.sommeri.less4j.commandline;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class CommandLine {
  private static final String INTRO = "Less4j compiles less files into css files. It can run in two " + "modes: single input file mode or in multiple input files mode. Less4j uses single file mode by default. " + "\n\n" + "Single file mode: Less4j expects one or two arguments. First one contains input less filename and the second one contains " + "the output css filename. If the output file argument is not present, less4j will print the result into standard output." + "\n\n" + "Multiple file mode: Must be turned on by '-m' or '--multiMode' parameter. Less4j assumes that all input files are " + "less files. All are going to be compiled into css files. Each input file will generate " + "an output file with the same name and suffix '.css'." + "\n\n";
  private static final String OUTRO = "\nExamples:\n" + " - Compile 'test.less' file and print the result into standard output:\n  # less4j test.less\n\n" + " - Compile 'test.less' file and print the result into 'test.css' file:\n  # less4j test.less test.css\n\n" + " - Compile 't1.less', 't2.less' and 't3.less' files into 't1.css', 't2.css' and 't3.css':\n  # less4j -m t1.less t2.less t3.less\n\n" + " - Compile 't1.less', 't2.less', 't3.less' files into 't1.css', 't2.css', 't3.css'. Place the result \n  into '..\\css\\' directory:\n  # less4j -m -o ..\\css\\ t1.less t2.less t3.less\n\n";
  private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

  public static void main(String[] args) {
    Arguments arguments = new Arguments();
    if (args.length == 0) {
      args = new String[] { "--help" };
    }

    try {
      JCommander jCommander = new JCommander(arguments, args);
      jCommander.setProgramName("less4j");

      if (arguments.isHelp()) {
        printHelp(jCommander);
        return;
      }

    } catch (ParameterException ex) {
      System.err.println(ex.getMessage());
      
    }

    if (arguments.isMultiMode()) {
      runAsMultimode(arguments.getFiles(), arguments.getOutputDirectory());
    } else {
      runAsSinglemode(arguments.getFiles());
    }
  }

  private static void runAsSinglemode(List<String> files) {
    if (files.isEmpty()) {
      reportError("No file available.");
      return ;
    }
    
    String inputfile = files.get(0);
    String content = compile(inputfile);
    if (content==null) {
      reportError("Could not compile the file " + inputfile);
    }

    if (files.size()==1) {
      System.out.print(content);
      return ;
    }

    outputFile(files.get(1), content);
  }

  private static void runAsMultimode(List<String> files, String outputDirectory) {
    if (!ensureDirectory(outputDirectory))
      return ;
    
    for (String filename : files) {
      String content = compile(filename);
      if (content!=null) {
        outputFile(toOutputFilename(outputDirectory, filename), content);
      } else {
        reportError("Could not compile the file " + filename);
      }
    }
  }

  private static boolean ensureDirectory(String outputDirectory) {
    if (outputDirectory==null || outputDirectory.isEmpty())
      return true;
    
    File directory = new File(outputDirectory);
    if (directory.exists() && !directory.isDirectory()) {
      reportError(directory + " is not a directory.");
      return false;
    }
    
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        reportError("Could not create the directory " + directory + ".");
        return false;
      }
    }
    
    return true;
  }

  private static void outputFile(String filename, String content) {
    File file = new File(filename);
    try {
      file.createNewFile();
    } catch (IOException e) {
      reportError("Could not create the file " + filename);
      reportError(e);
      return ;
    }
    
    if (!file.canWrite()) {
      reportError("Can not write into file " + filename);
      return ;
    }
    try {
      FileWriter output = new FileWriter(file);
      IOUtils.write(content, output);
      output.close();
    } catch (IOException e) {
      reportError("Can not write into file " + filename);
      reportError(e);
      return ;
    }
  }

  private static String toOutputFilename(String outputDirectory, String inputFilename) {
    String filename = changeSuffix(inputFilename);
    if (outputDirectory==null || outputDirectory.isEmpty()) {
      return filename;
    }
      
    if (!outputDirectory.endsWith(SEPARATOR)) {
      outputDirectory += SEPARATOR;
    }
    
    return outputDirectory + new File(filename).getName();
  }

  private static String changeSuffix(String filename) {
    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf==-1)
      return filename + ".css";
    
    return filename.substring(0, lastIndexOf) + ".css";
  }

  private static String compile(String filename) {
    File inputFile = new File(filename);
    if (!inputFile.exists()) {
      reportError("The file " + filename + " does not exists.");
      return null;
    }
    if (!inputFile.canRead()) {
      reportError("Cannot read the file " + filename + ".");
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
    try {
      return compiler.compile(content);
    } catch (Less4jException e) {
      reportErrors(e);
    }
    return null;
  }

  private static void printHelp(JCommander jCommander) {
    StringBuilder builder = new StringBuilder();
    wrapDescription(builder, jCommander.getColumnSize(), INTRO);
    jCommander.usage(builder);
    builder.append(OUTRO);
    System.out.println(builder);
  }

  private static void wrapDescription(StringBuilder out, int columnSize, String description) {
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

  private static void reportError(String message) {
    System.err.println(message);
  }

  private static void reportErrors(Less4jException e) {
    //FIXME: create some real error reporting
    e.printStackTrace();
  }

  private static void reportError(Throwable e) {
    //FIXME: create some real error reporting
    e.printStackTrace();
  }

}
