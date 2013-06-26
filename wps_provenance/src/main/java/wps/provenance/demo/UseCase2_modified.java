package wps.provenance.demo;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.openprovenance.model.OPMGraph;

import wps.provenance.ProcessExecution;
import wps.provenance.ProvenanceFactory;
import wps.provenance.base.ProcessingArtifact;
import wps.provenance.opm.ProvenancePublisher;


public class UseCase2_modified {
	public static void main(String[] args) throws Exception {   		
   		URL serviceURL = new URL("http://128.176.133.106:8080/albatross-syn-pop-wps/WebProcessingService");
   		File executeFile = new File("executerequests"+File.separator+"syntheticPopulationProcess_example.xml");
   		InputStream stream = new FileInputStream(executeFile);
   		ProvenanceFactory factory = ProvenanceFactory.getInstance();
   		ProcessExecution execution = factory.generateProcessRecord(stream, serviceURL);
   		
   		
   		
   		URL serviceURL2 = new URL("http://128.176.133.106:8080/albatross-wps/WebProcessingService");
   		File executeFile2 = new File("executerequests"+File.separator+"UncertainAlbatrossProcess_example.xml");
   		InputStream stream2 = new FileInputStream(executeFile2);
   
   		ProcessExecution execution2 = factory.generateProcessRecord(stream2, serviceURL2);
   		
   		//Chain process provenance
   		//Associate execution with successorexecution
   		execution.getSuccessorProcesses().add(execution2);
   		
   		execution.mapMetaData();
   		execution2.mapMetaData();
   		
   		//Chain artifact provenance
   		Set<ProcessingArtifact> outputs = execution.getOutput();
   		String aid1 = "export-file";
   		String aid2 = "export-file-bin";
   		ProcessingArtifact out_aid1 = null;
   		ProcessingArtifact out_aid2 = null;
   		ProcessingArtifact in_aid1 = null;
   		ProcessingArtifact in_aid2 = null;
   		
   		for (ProcessingArtifact output : outputs) {
		    if(output.getGeneralId().equalsIgnoreCase(aid1)){
			out_aid1 = output;
		    }
			
		   if(output.getGeneralId().equalsIgnoreCase(aid2)){
		       out_aid2 = output;
		   }
		}
   		
   		Set<ProcessingArtifact> inputs = execution2.getInput();
		for (ProcessingArtifact input : inputs) {
		    if(input.getGeneralId().equalsIgnoreCase(aid1)){
			in_aid1 = input;
		    }
			
		   if(input.getGeneralId().equalsIgnoreCase(aid2)){
		       in_aid2 = input;
		   }
		}
		
		
		//associate input with predecessor output
		in_aid1.getDerivedByArtifacts().add(out_aid1);
		in_aid2.getDerivedByArtifacts().add(out_aid2);

   		//System.out.println(execution.getProcess().getProcessDescriptionDocument());
   		//Time.getInstance().g
   		//generate provenance statements
   		OPMGraph graph = execution.getProvenance();
   		ProvenancePublisher pp = new ProvenancePublisher();
   	
   		File dir = new File("uscase2_out");
   		dir.mkdir();
   		pp.setTargetdir(dir);
   		pp.setBasefilename("usecase2");
   		pp.makeXML(graph);
   		//pp.makePDF(graph);
   		//pp.makeN3(graph);
   		pp.displayGraph(graph); 
	    

   	}
}
