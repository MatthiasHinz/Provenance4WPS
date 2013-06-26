package wps.provenance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.ResponseFormType;

import org.apache.log4j.Logger;
import org.openprovenance.model.OPMGraph;

import wps.provenance.base.ProcessStep;
import wps.provenance.base.ProcessingArtifact;
import wps.provenance.opm.ProvenanceMapper;

public class ProcessExecution extends ProcessStep {

    private static Logger logger = Logger.getLogger(ProcessExecution.class);

    private List<ProcessStep> internalProcessSteps;

    private List<ProcessExecution> successorProcesses = new ArrayList<ProcessExecution>();

    public void setInternalProcessSteps(List<ProcessStep> value)
    {
        this.internalProcessSteps = value;
    }

    public List<ProcessStep> getInternalProcessSteps()
    {
        return this.internalProcessSteps;
    }

    /**
     * <pre>
     *           0..1     0..*
     * ProcessExecution ------------------------- WPSOutput
     *           processRecord        &gt;       wPSOutput
     * </pre>
     */
    private Set<WPSOutput> wPSOutput;

    /**
     * Only package visibility!
     * 
     * @return
     */
    Set<WPSOutput> getWPSOutput()
    {
        if (this.wPSOutput == null) {
            this.wPSOutput = new HashSet<WPSOutput>();
        }
        return this.wPSOutput;
    }

    /**
     * <pre>
     *           0..1     0..*
     * ProcessExecution ------------------------- WPSInput
     *           processRecord        &gt;       wPSInput
     * </pre>
     */
    private Set<WPSInput> wPSInput;

    /**
     * Only package visibility!
     * 
     * @return
     */
    Set<WPSInput> getWPSInput()
    {
        if (this.wPSInput == null) {
            this.wPSInput = new HashSet<WPSInput>();
        }
        return this.wPSInput;
    }

    private void addWPSInput(WPSInput wpsinput)
    {
        getWPSInput().add(wpsinput);
        getInput().add(wpsinput);
    }

    private void addWPSOutput(WPSOutput wpsoutput)
    {
        getWPSOutput().add(wpsoutput);
        getOutput().add(wpsoutput);
    }

    /**
     * User ProvenanceMapper to generate an OPM graph out of the Information
     * associated with this class
     * 
     * @see ProvenanceMapper
     * @return
     */
    public OPMGraph getProvenance()
    {
        return new ProvenanceMapper().map(this);
    }

    public void mapMetaData()
    {
        mapExecute(getExecuteRequest().getExecute());

        try {
            if (getProcess().getProcessDescription() != null) {
                ProcessDescriptionType pdt = getProcess().getProcessDescription();
                mapProcessDescription(pdt);
            } else
                logger.warn("No process description available. Document won't be mapped to provenance.");
        } catch (Exception e) {
            logger.error("Failed to retrive process description. Document won't be mapped to provenance. Errormessage:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * <pre>
     *           0..*     1..1
     * ProcessRecord ------------------------- ProcessMetadata
     *           executions        &lt;       process
     * </pre>
     */
    private ProcessMetadata process;

    public void setProcess(ProcessMetadata value)
    {
        this.process = value;
        setProcessingController(process.getService());
    }

    public ProcessMetadata getProcess()
    {
        if (process == null)
            process = new ProcessMetadata();
        return this.process;
    }

    private ExecuteDocument executeRequest;

    public void setExecuteRequest(ExecuteDocument value)
    {
        this.executeRequest = value;

    }

    public ExecuteDocument getExecuteRequest()
    {
        return this.executeRequest;
    }

    private ExecuteResponse executeResponse;

    public void setExecuteResponse(ExecuteResponse value)
    {
        this.executeResponse = value;
    }

    public ExecuteResponse getExecuteResponse()
    {
        return this.executeResponse;
    }

    // TODO: pspl. divide mappings further
    // getter methods trigger mapping, if nothing changes?
    private void mapExecute(Execute exec)
    {
        // reset input and output
        wPSInput = null;
        wPSOutput = null;
        String processId = exec.getIdentifier().getStringValue();
        this.setId(processId + "-" + getId());
        this.setGeneralId(processId);

        InputType[] inputArray = exec.getDataInputs().getInputArray();

        for (InputType inputType : inputArray) {
            // create attributed input Object for each inputType
            WPSInput input = new WPSInput();
            input.setProcessRecord(this);
            input.assignProperties(inputType);
            addWPSInput(input);
        }

        ResponseFormType rst = exec.getResponseForm();

        if (rst.isSetRawDataOutput()) {
            WPSOutput output = new WPSOutput();
            OutputDefinitionType odef = rst.getRawDataOutput();
            output.assignProperties(odef);
            output.setProcessRecord(this);
            addWPSOutput(output);

        } else if (rst.isSetResponseDocument()) {
            ResponseDocumentType rdt = rst.getResponseDocument();
            OutputDefinitionType[] roArray = rdt.getOutputArray();
            for (OutputDefinitionType odef : roArray) {
                WPSOutput output = new WPSOutput();
                output.setProcessRecord(this);
                output.assignProperties(odef);
                addWPSOutput(output);
            }

        }
    }

    private void mapProcessDescription(ProcessDescriptionType pdt)
    {
        mapDescriptionType(pdt);

        if (pdt.isSetDataInputs()) {
            InputDescriptionType[] idtlist = pdt.getDataInputs().getInputArray();
            for (InputDescriptionType idt : idtlist) {
                boolean hasfound = false;
                for (ProcessingArtifact artifact : getInput()) {
                    if (artifact instanceof WPSInput) {
                        WPSInput in = (WPSInput) artifact;
                        if (in.getId().startsWith(idt.getIdentifier().getStringValue())) {
                            hasfound = true;
                            in.enrichProperties(idt);
                        }
                        ;
                    }
                }

                if (!hasfound && idt.isSetLiteralData() && idt.getLiteralData().isSetDefaultValue()) {

                    WPSInput input = new WPSInput();
                    input.setProcessRecord(this);
                    input.assignDefaultValue(idt);
                    addWPSInput(input);
                }
            }
        }

        OutputDescriptionType[] odtlist = pdt.getProcessOutputs().getOutputArray();
        for (OutputDescriptionType odt : odtlist) {

            for (ProcessingArtifact artifact : getOutput()) {
                if (artifact instanceof WPSOutput) {
                    WPSOutput out = (WPSOutput) artifact;
                    if (out.getId().startsWith(odt.getIdentifier().getStringValue())) {
                        out.enrichProperties(odt);
                    }
                    ;
                }
            }
        }

    }

    public List<ProcessExecution> getSuccessorProcesses()
    {
        return successorProcesses;
    }

    public void setSuccessorProcesses(List<ProcessExecution> successorProcesses)
    {
        this.successorProcesses = successorProcesses;
    }

}
