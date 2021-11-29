package de.hub.mse.emf.multifile;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGUniverse;
import com.pholser.junit.quickcheck.From;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.impl.svg.SvgGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

@RunWith(JQF.class)
public class SvgTest {

    private static int iteration = 0;

    @Fuzz
    public void svgSalamanderTest(@From(SvgGenerator.class) File inputFile) throws InterruptedException, MalformedURLException {
        System.out.println("STARTING ITERATION "+ ++iteration);
        long start = System.currentTimeMillis();
        System.out.println(inputFile.getAbsolutePath());

        SVGUniverse universe = SVGCache.getSVGUniverse();

        var config = GeneratorConfig.getInstance();
        File workingDir = new File(config.getWorkingDirectory());
        var files = workingDir.listFiles();
        for(var file : files) {
            if(file.isDirectory() ||
                    file.getAbsolutePath().equals(inputFile.getAbsolutePath())) {
                continue;
            }
            universe.loadSVG(file.toURI().toURL());
        }

        universe.loadSVG(inputFile.toURI().toURL());

        var diagram = universe.getDiagram(inputFile.toURI());

        Assert.assertNotNull(diagram);

        long duration = System.currentTimeMillis() - start;
        Thread.sleep(Math.max(0, 1000 - duration));
    }

    @Fuzz
    public void testBatik(@From(SvgGenerator.class) File inputFile) throws IOException, InterruptedException {
        System.out.println("STARTING ITERATION "+ ++iteration);
        long start = System.currentTimeMillis();

        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

        SVGDocument doc = f.createSVGDocument(inputFile.toURI().toString());

        Assert.assertNotNull(doc);

        long duration = System.currentTimeMillis() - start;
        Thread.sleep(Math.max(0, 1000 - duration));
    }

    @Fuzz
    public void testBatikTranscoder(@From(SvgGenerator.class) File inputFile) throws IOException, InterruptedException, TranscoderException {
        System.out.println("STARTING ITERATION "+ ++iteration + " WITH FILE > "+inputFile.getAbsolutePath());
        long start = System.currentTimeMillis();

        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, 1000f);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, 1000f);
        TranscoderInput input = new TranscoderInput(inputFile.toURI().toString());

        var outStream = new ByteArrayOutputStream(0);
        TranscoderOutput output = new TranscoderOutput(outStream);
        try {
            transcoder.transcode(input, output);
            Assert.assertTrue(outStream.size() > 0);
            System.out.println("ITERATION "+ iteration + " SUCCESSFUL!");
        } catch (Exception e) {
            System.out.println("ITERATION "+ iteration + " FAILED!");
            e.printStackTrace();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            Thread.sleep(Math.max(0, 1000 - duration));
        }
    }
}
