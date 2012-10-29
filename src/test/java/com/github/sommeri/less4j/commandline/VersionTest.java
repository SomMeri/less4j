package com.github.sommeri.less4j.commandline;

import org.junit.Test;

public class VersionTest extends CommandLineTest {

  @Test
  public void versionShort() {
    CommandLine.main(new String[] {"-v"});
    assertVersion();
  }

  @Test
  public void versionLong() {
    CommandLine.main(new String[] {"--version"});
    assertVersion();
  }

}
