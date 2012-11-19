package com.github.sommeri.less4j.commandline;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class Arguments {
  @Parameter(description = "[list of files]", required=true)
  private List<String> files = new ArrayList<String>();

  public List<String> getFiles() {
    return files;
  }
  
  @Parameter(names = {"-h", "--help"}, help = true, description="Print this help screen.")
  private boolean help;

  @Parameter(names = {"-v", "--version"}, help = true, description="Print version.")
  private boolean version;

  @Parameter(names={"-m", "--multiMode"}, description="Turn on multi less files mode.")
  private boolean multiMode;
  
  @Parameter(names={"-o", "--outputDir"}, description="Specify the output directory. If not present, Less4j will place output files into current directory. This option is available only in multiple files mode.")
  private String outputDirectory;

  //FIXME: add this to wiki
  @Parameter(names={"-pi", "--printIncorrect"}, description="Print result of syntactically incorrect input file. If not present, syntactically incorrect input files produce no result. If present, less4j prints whatever came out of the compilation as a compilation result - most likely an invalid CSS. ")
  private boolean printIncorrect;

  public boolean isHelp() {
    return help;
  }

  public boolean isMultiMode() {
    return multiMode;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public boolean isVersion() {
    return version;
  }
  
  public boolean isPrintIncorrect() {
    return printIncorrect;
  }

}
