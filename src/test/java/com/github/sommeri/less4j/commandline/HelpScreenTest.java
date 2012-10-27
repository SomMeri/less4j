package com.github.sommeri.less4j.commandline;

import org.junit.Test;

public class HelpScreenTest extends CommandLineTest {

  @Test
  public void helpScreenNoArgs() {
    CommandLine.main(new String[] {});
    assertHelpScreen();
  }

  @Test
  public void helpScreenShort() {
    CommandLine.main(new String[] {"-h"});
    assertHelpScreen();
  }

  @Test
  public void helpScreenLong() {
    CommandLine.main(new String[] {"--help"});
    assertHelpScreen();
  }

}
