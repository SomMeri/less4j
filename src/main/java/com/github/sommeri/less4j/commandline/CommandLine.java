package com.github.sommeri.less4j.commandline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.URIUtils;

public class CommandLine {
  private static final String NAME = "less4j";
  private static final String INTRO = "Less4j compiles less files into css files. It can run in two modes: single input file mode or in multiple input files mode. Less4j uses single file mode by default. \n\nBoth modes are able to generate source map files. Source map links elements from compiled css file back to original less files. It is useful when troubleshooting large complicated less files.\n\nSingle file mode: Less4j expects one or two arguments. First one contains input less filename and the second one contains " + "the output css filename. If the output file argument is not present, less4j will print the result into standard output." + "\n\nSource map is generated only if both arguments are supplied.\n\n" + "Multiple files mode: Must be turned on by '-m' or '--multiMode' parameter. Less4j assumes that all input files are " + "less files. All are going to be compiled into css files. Each input file will generate css file with the same name and suffix '.css' and source map file with suffix '.css.map'." + "\n\n";
  private static final String OUTRO = "\nExamples:\n" + " - Compile 'test.less' file and print the result into standard output (does not generate source map):\n  # less4j test.less\n\n" + " - Compile 'test.less' file and print the result into 'test.css' file:\n  # less4j test.less test.css\n\n" + " - Compile 't1.less', 't2.less' and 't3.less' files into 't1.css', 't2.css' and 't3.css':\n  # less4j -m t1.less t2.less t3.less\n\n" + " - Compile 't1.less', 't2.less', 't3.less' files into 't1.css', 't2.css', 't3.css'. Place the result \n  into '..\\css\\' directory:\n  # less4j -m -o ..\\css\\ t1.less t2.less t3.less\n\n";
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
      runAsMultimode(arguments.getFiles(), arguments.getOutputDirectory(), arguments.isSourceMap(), arguments.isPrintIncorrect());
    } else {
      runAsSinglemode(arguments.getFiles(), arguments.isSourceMap(), arguments.isPrintIncorrect());
    }
  }

  private void runAsSinglemode(List<String> files, boolean generateSourceMap, boolean printPartial) {
    if (files.isEmpty()) {
      print.reportError("No file available.");
      return;
    }

    String lessFileName = files.get(0);
    File lessFile = toFile(lessFileName);
    
    String cssFileName = singleModeCssFilename(files);
    File cssFile = toFile(cssFileName);

    String mapFileName = singleModeMapFilename(cssFileName, generateSourceMap);

    try {
      CompilationResult content = compile(lessFile, cssFile, generateSourceMap);
      singleModePrint(files, lessFileName, lessFile, cssFileName, mapFileName, content);
    } catch (Less4jException ex) {
      CompilationResult partialResult = ex.getPartialResult();
      if (printPartial) {
        singleModePrint(files, lessFileName, lessFile, cssFileName, null, partialResult);
        print.reportErrors(ex, lessFileName, lessFile);
      } else {
        print.reportErrorsAndWarnings(ex, lessFileName, lessFile);
      }
      print.reportCouldNotCompileTheFile(lessFileName);
    }

  }

  private String singleModeMapFilename(String cssFileName, boolean generateSourceMap) {
    return !generateSourceMap || cssFileName==null? null: URIUtils.addSuffix(cssFileName, Constants.SOURCE_MAP_SUFFIX);
  }

  private File toFile(String fileName) {
    return fileName==null? null:new File(fileName);
  }

  private String singleModeCssFilename(List<String> files) {
    if (files.size()>1) {
      return files.get(1);
    }
    return null;
  }

  private void singleModePrint(List<String> files, String lessFileName, File lessFile, String cssFileName, String mapFileName, CompilationResult content) {
    if (cssFileName == null) {
      print.printToSysout(content, lessFileName, lessFile);
    } else {
      print.printToFiles(content, lessFileName, lessFile, cssFileName, mapFileName);
    }
  }

  private void runAsMultimode(List<String> files, String outputDirectory, boolean generateSourceMap, boolean printPartial) {
    if (!print.ensureDirectory(outputDirectory))
      return;

    for (String filename : files) {
      File inputFile = new File(filename);
      String cssFilename = toOutputFilename(outputDirectory, filename, Constants.CSS_SUFFIX);
      String mapFilename = generateSourceMap? toOutputFilename(outputDirectory, filename, Constants.FULL_SOURCE_MAP_SUFFIX): null;
      try {
        CompilationResult content = compile(inputFile, toFile(cssFilename), generateSourceMap);
        print.printToFiles(content, filename, inputFile, cssFilename, mapFilename);
      } catch (Less4jException ex) {
        CompilationResult partialResult = ex.getPartialResult();
        if (printPartial) {
          print.printToFiles(partialResult, filename, inputFile, cssFilename, mapFilename);
          print.reportErrors(ex, filename, inputFile);
        } else {
          print.reportErrorsAndWarnings(ex, filename, inputFile);
        }
        print.reportCouldNotCompileTheFile(filename);
      }

    }
  }

  private String toOutputFilename(String outputDirectory, String inputFilename, String dottedSuffix) {
    String filename = URIUtils.changeSuffix(inputFilename, dottedSuffix);
    if (outputDirectory == null || outputDirectory.isEmpty()) {
      return filename;
    }

    if (!outputDirectory.endsWith(SEPARATOR)) {
      outputDirectory += SEPARATOR;
    }

    return outputDirectory + new File(filename).getName();
  }

  private CompilationResult compile(File lessFile, File cssFile, boolean generateSourceMap) throws Less4jException {
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(cssFile);
    configuration.getSourceMapConfiguration().setLinkSourceMap(generateSourceMap && cssFile != null);

    DefaultLessCompiler compiler = new DefaultLessCompiler();
    return compiler.compile(lessFile, configuration);
  }

  private void printVersion(JCommander jCommander) {
    StringBuilder builder = new StringBuilder(NAME);
    builder.append(" ").append(getVersion());
    System.out.println(builder);
  }

  private void printHelp(JCommander jCommander) {
    StringBuilder builder = new StringBuilder();
    builder.append(INTRO);
    jCommander.usage(builder);
    builder.append(OUTRO);
    System.out.println(builder);
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
