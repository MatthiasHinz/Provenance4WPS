package wps.provenance.demo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.apache.xmlbeans.XmlException;
import org.openprovenance.model.OPMGraph;

import wps.provenance.ProcessExecution;
import wps.provenance.ProvenanceFactory;
import wps.provenance.opm.ProvenancePublisher;


public class Make_Realizations_Provenance {
    public static void main(String[] args) throws XmlException, IOException, JAXBException, InterruptedException
    {
        // collect provenance data
        URL serviceURL = new URL("http://localhost:8080/wps/WebProcessingService");
        File executeFile = new File("executerequests" + File.separator + "R_Make_Realizations.xml");
        InputStream stream = new FileInputStream(executeFile);
        ProvenanceFactory factory = ProvenanceFactory.getInstance();
        ProcessExecution execution = factory.generateProcessRecord(stream, serviceURL);
        execution.mapMetaData();

        // generate provenance data:
        OPMGraph graph = execution.getProvenance();
        ProvenancePublisher pp = new ProvenancePublisher();
        File targetdir = new File("target");
        targetdir.mkdir();
        pp.setTargetdir(targetdir);
        pp.setBasefilename("R_make_realizations");
        pp.makeXML(graph);
        pp.makePDF(graph);
        pp.displayGraph(graph);
    }
}
