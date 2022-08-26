
package de.hub.mse.emf.multifile;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGUniverse;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;
import com.pholser.junit.quickcheck.generator.Size;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.impl.xml.Dictionary;
import de.hub.mse.emf.multifile.impl.xml.XmlDocumentGenerator;
import de.hub.mse.emf.multifile.util.XmlUtil;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(JQF.class)
@Slf4j
public class XmlTest {

    private static int iteration = 0;

    @Fuzz
    public void svgSalamanderTest(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/svgAttributes.dict") Document doc) throws IOException {
        String content = XmlUtil.documentToString(doc);

        var config = GeneratorConfig.getInstance();
        File workingDir = new File(config.getWorkingDirectory());

        System.out.println("STARTING ITERATION " + (++iteration) + " - " + workingDir.getAbsolutePath());

        File inputFile = new File(workingDir, UUID.randomUUID() + ".svg");
        // Files.write(inputFile.toPath(), content.lines().collect(Collectors.toList()));

        SVGUniverse universe = SVGCache.getSVGUniverse();

        /**   for (var file : Objects.requireNonNull(workingDir.listFiles())) {
         if (file.isDirectory() ||
         file.getAbsolutePath().equals(inputFile.getAbsolutePath())) {
         continue;
         }
         universe.loadSVG(file.toURI().toURL());
         }*/

        var newuri = universe.loadSVG(new StringReader(content), inputFile.getName());

        var diagram = universe.getDiagram(newuri);

        Assert.assertNotNull(diagram);
    }

    @Fuzz
    public void testBatik(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/svgAttributes.dict") Document doc) throws IOException {
        String content = XmlUtil.documentToString(doc);

        var config = GeneratorConfig.getInstance();
        File workingDir = new File(config.getWorkingDirectory());

        File inputFile = new File(workingDir, UUID.randomUUID() + ".svg");
        // Files.write(inputFile.toPath(), content.lines().collect(Collectors.toList()));

        System.out.println("STARTING ITERATION " + (++iteration) + " - " + workingDir.getAbsolutePath());

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

        SVGDocument svgdoc = f.createSVGDocument(inputFile.toURI().toString(), new StringReader(content));

        Assert.assertNotNull(svgdoc);
    }

    @Fuzz
    public void testBatikTranscoder(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/svgAttributes.dict") Document doc) throws TranscoderException, IOException {
        String content = XmlUtil.documentToString(doc);
        var config = GeneratorConfig.getInstance();
        File workingDir = new File(config.getWorkingDirectory());

        File inputFile = new File(workingDir, UUID.randomUUID() + ".svg");
        //Files.write(inputFile.toPath(), content.lines().collect(Collectors.toList()));

        System.out.println("STARTING ITERATION " + (++iteration)  + " - " + workingDir.getAbsolutePath());

        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, 1000f);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, 1000f);
        TranscoderInput input = new TranscoderInput(new StringReader(content));

        var outStream = new ByteArrayOutputStream(0);
        TranscoderOutput output = new TranscoderOutput(outStream);
        transcoder.transcode(input, output);
    }

}
