package wps.provenance;

import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.apache.log4j.Logger;

import wps.provenance.base.ProcessingArtifact;

public class WPSOutput extends ProcessingArtifact {

    private static Logger logger = Logger.getLogger(WPSOutput.class);

    /**
     * <pre>
     *           0..*     0..1
     * WPSOutput ------------------------- ProcessExecution
     *           wPSOutput        &lt;       processExecution
     * </pre>
     */
    private ProcessExecution processRecord;

    public void setProcessRecord(ProcessExecution value)
    {
        this.processRecord = value;
    }

    public ProcessExecution getProcessRecord()
    {
        return this.processRecord;
    }

    public void assignProperties(OutputDefinitionType odef)
    {
        String outputId = odef.getIdentifier().getStringValue();
        setId(outputId + "-" + getId());
        setGeneralId(outputId);

        if (odef.isSetMimeType()) {
            setProperty(ProvPropertyType.MIMETYPE, odef.getMimeType());
        }

        if (odef.isSetEncoding()) {
            setProperty(ProvPropertyType.ENCODING, odef.getEncoding());
        }

        if (odef.isSetSchema()) {
            setProperty(ProvPropertyType.SCHEMA, odef.getSchema());
        }

        if (odef.isSetUom()) {
            logger.debug("Metadata element not mapped to provenance: " + odef.getUom());
        }

    }

    public void enrichProperties(OutputDescriptionType odt)
    {
        mapDescriptionType(odt);
        odt.getComplexOutput();

        if (odt.isSetComplexOutput() && !odt.getComplexOutput().isNil()) {
            SupportedComplexDataType complex = odt.getComplexOutput();
            mapSupportedComplexDataType(complex);
        }

        if (odt.isSetLiteralOutput() && !odt.getLiteralOutput().isNil()) {
            LiteralOutputType ldt = odt.getLiteralOutput();
            if (ldt.isSetDataType() && !getProperties().containsKey(ProvPropertyType.LITERALDATATYPE.toString()) && !ldt.isNil() && ldt.getDataType().getStringValue() != "")
                setProperty(ProvPropertyType.LITERALDATATYPE, ldt.getDataType().getStringValue());
        }

        if (odt.isSetBoundingBoxOutput() && !odt.getBoundingBoxOutput().isNil()) {
            logger.debug("Metadata element not mapped to provenance: " + odt.getBoundingBoxOutput());
        }

    }

}
