package wps.provenance.base;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import wps.provenance.ProvPropertyType;

public class ProcessStep extends AbstractProvenanceEntity {

   private ProcessingController processingController;
   
   public void setProcessingController(ProcessingController value) {
      this.processingController = value;
   }
   
   public ProcessingController getProcessingController() {
      return this.processingController;
   }
   

   private Date startTime;
   
   public void setStartTime(Date value) {
      this.startTime = value;
      setProperty(ProvPropertyType.STARTTIME, value.toString());
   }
   
   public Date getStartTime() {
      return this.startTime;
   }
   

   private Date endTime;
   
   public void setEndTime(Date value) {
      setProperty(ProvPropertyType.ENDTIME, value.toString());
      this.endTime = value;
   }
   
   public Date getEndTime() {
      return this.endTime;
   }
   
   private Set<ProcessingArtifact> input;
   
   public void setInput(Set<ProcessingArtifact> value) {
      this.input = value;
   }
   
   public Set<ProcessingArtifact> getInput() {
       if(input == null)
	   input = new HashSet<ProcessingArtifact>();
      return this.input;
   }
   
   private Set<ProcessingArtifact> output;
   
   public void setOutput(Set<ProcessingArtifact> value) {
      this.output = value;
   }
   
   public Set<ProcessingArtifact> getOutput() {
       if(output == null)
	   output = new HashSet<ProcessingArtifact>();
      return this.output;
   }
   
   public boolean isSetProcessingController(){
       return processingController != null;
   }
   
   }
