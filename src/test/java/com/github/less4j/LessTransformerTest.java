package com.github.less4j;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.github.less4j.core.CssPrinter;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * Uses the wro4j lessCss processor to transform a number of lessCss resources and compare them with expected results.
 * The main benefit is that in order to add new test-case, it is enough to add a new file to test & expected folders
 * (without any java code changes).
 * 
 * @author Alex Objelean
 */
public class LessTransformerTest {
  @Ignore
  @Test
  public void shouldTransformUsingRhinoLessProcessor()
      throws Exception {
    final ResourcePreProcessor processor = new LessCssProcessor();
    genericTransformerTest(processor);
  }
  
  /**
   * This test will pass when the ANTLR based processor will be implemented.
   */
  @Ignore
  @Test
  public void shouldTransformUsingAntlrBasedLessProcessor()
      throws Exception {
    final ResourcePreProcessor processor = new ResourcePreProcessor() {
      @Override
      public void process(Resource resource, Reader reader, Writer writer)
          throws IOException {
        final String css = new CssPrinter().compile(IOUtils.toString(reader));
        IOUtils.write(css, writer);
      }
    };
    genericTransformerTest(processor);
  }

  /**
   * Apply a processor for each resource from a test folder and compare the processed content with the file from
   * expected folder. To add a new test case, just add a new file to the test & expected folder. It should have the same
   * name. If the file is missing in expected folder, the processing will be skipped.
   */
  private void genericTransformerTest(final ResourcePreProcessor processor)
      throws Exception {
    final URL url = LessTransformerTest.class.getResource("less");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
}
