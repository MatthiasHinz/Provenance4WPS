package wps.provenance.opm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.openprovenance.elmo.RdfOPMFactory;
import org.openprovenance.elmo.RdfObjectFactory;
import org.openprovenance.elmo.RepositoryHelper;
import org.openprovenance.model.OPMGraph;
import org.openprovenance.model.OPMSerialiser;
import org.openrdf.elmo.ElmoManager;
import org.openrdf.elmo.ElmoManagerFactory;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.rio.RDFFormat;

public class ProvenancePublisher {
    private File targetdir = new File("publishing");

    private String basefilename = "out";

    private static Logger logger = Logger.getLogger(ProvenancePublisher.class);

    public ProvenancePublisher() {
        if (!initialized) {
            initializeElmo();
            initialized = true;
        }
    }

    static String TEST_NS = "WPS";

    // "http://example.com/pc1-annotation/";

    static ElmoManager manager;

    static ElmoManagerFactory factory;

    static RepositoryHelper rHelper;

    static ElmoModule module;

    static RdfOPMFactory oFactory;

    static boolean initialized = false;

    static void initializeElmo()
    {
        module = new ElmoModule();
        rHelper = new RepositoryHelper();
        rHelper.registerConcepts(module);
        factory = new SesameManagerFactory(module);
        manager = factory.createElmoManager();
        oFactory = new RdfOPMFactory(new RdfObjectFactory(manager, TEST_NS));

    }

    Collection<String[]> prefixes = Collections.singleton(new String[] { "ex", TEST_NS });

    public void setTargetdir(File value)
    {
        if (!value.exists())
            value.mkdirs();
        this.targetdir = value;
    }

    public File getTargetdir()
    {
        return this.targetdir;
    }

    public String getBasefilename()
    {
        return basefilename;
    }

    public void setBasefilename(String basefilename)
    {
        this.basefilename = basefilename;
    }

    public File makeXML(OPMGraph graph) throws JAXBException
    {
        File out = new File(targetdir, "process.xml");
        OPMSerialiser serial = OPMSerialiser.getThreadOPMSerialiser();

        File dest = new File(targetdir, basefilename + ".xml");
        serial.serialiseOPMGraph(dest, graph, true);
        logger.info("Serialized xml file: " + dest.getAbsolutePath());
        return out;
    }

    public File makeN3(OPMGraph graph) throws Exception
    {
        File file = new File(targetdir, basefilename + ".n3.rdf");
        assert manager != null;
        rHelper.dumpToRDF(file, (SesameManager) manager, RDFFormat.N3, prefixes);
        return file;
    }

    public File makePNG(OPMGraph graph) throws IOException, InterruptedException
    {

        // create dot file:
        File dotFile = new File(targetdir, basefilename + ".dot").getAbsoluteFile();
        OPMToDot4WPS dot = new OPMToDot4WPS(true);
        dot.convert(graph, dotFile);

        // create png file
        File pngFile = new File(targetdir, basefilename + ".png").getAbsoluteFile();
        Runtime runtime = Runtime.getRuntime();
        String cmd = "dot -o \"" + pngFile.getAbsolutePath() + "\" -Tpng \"" + dotFile.getAbsolutePath() + "\"";
        java.lang.Process proc = runtime.exec(cmd);
        logger.info("Performed cmd: " + cmd);
        int exit = proc.waitFor();
        if (exit != 0) {
            logger.error("----");
            logger.error("Something went wrong when converting dot to png, exit code: " + exit);
            InputStream is = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bfr = new BufferedReader(isr);
            String line = "Error stream:";
            while (line != null) {
                logger.error(line);
                line = bfr.readLine();
            }
            logger.error("----");
        }

        return pngFile;
    }

    public File makePDF(OPMGraph graph) throws IOException, InterruptedException
    {

        // create dot file:
        File dotFile = new File(targetdir, basefilename + ".dot").getAbsoluteFile();
        OPMToDot4WPS dot = new OPMToDot4WPS(true);
        dot.convert(graph, dotFile);

        // create pdf file
        File pdfFile = new File(targetdir, basefilename + ".pdf").getAbsoluteFile();
        Runtime runtime = Runtime.getRuntime();
        String cmd = "dot -o \"" + pdfFile.getAbsolutePath() + "\" -Tpdf \"" + dotFile.getAbsolutePath() + "\"";
        java.lang.Process proc = runtime.exec(cmd);
        logger.info("Performed cmd: " + cmd);

        int exit = proc.waitFor();
        if (exit != 0) {
            logger.error("----");
            logger.error("Something went wrong when converting dot to pdf, exit code: " + exit);
            InputStream is = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bfr = new BufferedReader(isr);
            String line = "Error stream:";
            while (line != null) {
                logger.error(line);
                line = bfr.readLine();
            }
            logger.error("----");
        }

        return pdfFile;
    }

    public void displayGraph(OPMGraph graph) throws FileNotFoundException, IOException, InterruptedException
    {

        File pngFile = makePNG(graph);

        JFrame provView = new JFrame();
        provView.setLocationByPlatform(true);
        provView.setTitle("OPM Graph View");

        provView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage img = ImageIO.read(pngFile);
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel();
        label.setIcon(icon);
        provView.add(label);

        provView.setSize(img.getWidth() + 10, img.getHeight() + 50);
        provView.setBackground(Color.BLACK);

        provView.setVisible(true);
    }

}
