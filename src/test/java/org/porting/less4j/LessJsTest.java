package org.porting.less4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.porting.less4j.core.parser.DummyLessCompiler;

/**
 * The test reproduces test files found in original less.js 
 * implementation. As less.js has only only one tag and that tag 
 * is one year old, we took tests from the master branch.
 *   
 */
public class LessJsTest {

	private static final String inputLessDir = "src\\test\\resources\\less\\";
	private static final String expectedCssDir = "src\\test\\resources\\css\\";
	
	@Test
	public void runAllTests() throws Exception {
		Iterator<File> it = FileUtils.iterateFiles(new File(inputLessDir), null, false);
        while(it.hasNext()){
            File lessFile = it.next();
            File cssFile = findCorrespondingCss(lessFile);
            //TODO it would be cool if each one of these would be a separate test in gui and report
            testCompiler(lessFile, cssFile);
        }
	}

	protected void testCompiler(File lessFile, File cssFile) throws Exception {
		String less = IOUtils.toString(new FileReader(lessFile));
		String css = IOUtils.toString(new FileReader(cssFile));

		ILessCompiler compiler = getCompiler();
		assertEquals("Incorrectly compiled the file " + lessFile, css, compiler.compile(less));
	}

	protected ILessCompiler getCompiler() {
		return new DummyLessCompiler();
	}

	private File findCorrespondingCss(File lessFile) {
		String lessFileName = lessFile.getName();
		String cssFileName = convertToOutputFilename(lessFileName);
		File cssFile = new File(expectedCssDir + cssFileName);
		return cssFile;
	}

	private String convertToOutputFilename(String name) {
		return name.substring(0, name.length()-5) + ".css";
	}
	
}
