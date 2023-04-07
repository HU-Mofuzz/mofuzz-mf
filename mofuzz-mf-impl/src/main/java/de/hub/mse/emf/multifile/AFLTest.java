package de.hub.mse.emf.multifile;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.impl.svg.SvgGenerator;
import de.hub.mse.emf.multifile.util.ArgUtil;
import de.hub.mse.emf.multifile.util.FailHandler;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.StreamBackedRandom;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import org.junit.AssumptionViolatedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

@RunWith(JQF.class)
public class AFLTest {

    public static Args ARGS = new Args();

    static {
        var argsProperty = System.getProperty("de.hub.mufuzz.mf.args");
        String[] args;
        if(argsProperty == null) {
            args = new String[]{};
        } else {
            args = argsProperty.split(" ");
        }
        ArgUtil.parseArgs(ARGS, args);
        System.out.println(String.join(" ", args));
        System.out.println(ARGS);
        ArgUtil.prepareFiles();
    }


    @Fuzz /* JQF will generate inputs to this method */
    public void withResultHandling(InputStream input) throws Throwable {
        FailHandler handler = new FailHandler(ARGS);
        doTest(input, handler);
    }

    @Fuzz /* JQF will generate inputs to this method */
    public void withoutResultHandling(InputStream input) throws Throwable {
        doTest(input, null);
    }

    private static void doTest(InputStream inputStream, DocumentAwareGraphAwareGuidance.DocumentAwareResultListener listener) throws Throwable {
        SvgGenerator generator = new SvgGenerator();
        File file = generator.generate(new SourceOfRandomness(new StreamBackedRandom(inputStream)), null);
        var method = SvgTest.class.getMethod(ARGS.getTestMethod(), File.class);

        Class<?> expectedException = null;
        var annotation = method.getAnnotation(org.junit.Test.class);
        if(annotation != null) {
            expectedException = annotation.expected();
        }

        Result result;
        Throwable throwable = null;
        try {
            SvgTest svgTest = new SvgTest();
            method.invoke(svgTest, file);
            result = expectedException == null? Result.SUCCESS : Result.FAILURE;
        } catch (AssumptionViolatedException e) {
            result = Result.INVALID;
            throwable = e;
        } catch (TimeoutException e) {
            result = Result.TIMEOUT;
            throwable = e;
        } catch (Throwable t) {
            if(expectedException == null) {
                result = Result.FAILURE;
            } else {
                result = expectedException.isAssignableFrom(t.getClass()) ?
                        Result.SUCCESS : Result.FAILURE;
            }
            throwable = t;
        }
        if(listener != null) {
            listener.handleResultForGeneratedArgs(new Object[]{file}, result, throwable);
        }
        if(result != Result.SUCCESS && throwable != null) {
            throw throwable;
        }

    }

    public static void main(String[] args) throws Throwable {
        var test = new AFLTest();
        test.withResultHandling(new InputStream() {
            Random random = new Random();
            @Override
            public int read() throws IOException {
                return random.nextInt();
            }
        });
    }
}
