package wps.provenance.demo;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.openprovenance.model.OPMGraph;

import wps.provenance.ProcessExecution;
import wps.provenance.ProvenanceFactory;
import wps.provenance.opm.ProvenancePublisher;


public class UseCase1 {
	public static void main(String[] args) throws Exception {   		
   		URL serviceURL = new URL("http://localhost:8080/wps/WebProcessingService");
   		File executeFile = new File("usecase_requests"+File.separator+"request_usecase1.xml");
   		InputStream stream = new FileInputStream(executeFile);
   		ProvenanceFactory factory = ProvenanceFactory.getInstance();
   		ProcessExecution execution = factory.generateProcessRecord(stream, serviceURL);
   		execution.mapMetaData();

   		//System.out.println(execution.getProcess().getProcessDescriptionDocument());
   
   		//generate provenance statements
   		OPMGraph graph = execution.getProvenance();
   		ProvenancePublisher pp = new ProvenancePublisher();
   	
   		File dir = new File("uscase1_out");
   		dir.mkdir();
   		pp.setTargetdir(dir);
   		pp.setBasefilename("usecase1");
   		pp.makeXML(graph);
   		pp.makePDF(graph);
   		//pp.makeN3(graph);
   		pp.displayGraph(graph); 
	    

   	}
}
