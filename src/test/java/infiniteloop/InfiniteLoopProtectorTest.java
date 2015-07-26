package infiniteloop;

import java.io.File;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import com.github.sommeri.less4j.utils.w3ctestsextractor.common.CaseBuilder;

/**
 * Created by Igor on 08.07.2015.
 */
public class InfiniteLoopProtectorTest extends CaseBuilder {
    public static void main(String[] args) throws Exception {
        InfiniteLoopProtectorTest test = new InfiniteLoopProtectorTest();
        try{
        	test.compileLess("/testInfiniteLoop.less");
        } catch (RuntimeException e){
        	System.out.println(e.getMessage());
        }
    }

    public LessCompiler.CompilationResult compileLess(String file) throws Less4jException {
    	String filePath = getCurrentDirectory() + file;
        LessCompiler.CompilationResult result = new ThreadUnsafeLessCompiler().compile(new File(filePath));
        return result;
    }
}