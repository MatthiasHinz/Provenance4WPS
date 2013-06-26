package wps.provenance;

import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.apache.log4j.Logger;

import wps.provenance.base.ProcessingArtifact;

public class WPSInput extends ProcessingArtifact {

    private static Logger logger = Logger.getLogger(WPSInput.class);

    /**
     * <pre>
     *           0..*     0..1
     * WPSInput ------------------------- ProcessExecution
     *           wPSInput        &lt;       processExecution
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

    public void assignProperties(InputType input)
    {
        String id = input.getIdentifier().getStringValue();
        this.setId(id + "-" + getId());
        this.setGeneralId(id);

        if (input.getReference() != null) {
            InputReferenceType ref = input.getReference();
            mapInputReferenceType(ref);

        } else if (input.getData() != null) {

            if (input.getData().isSetComplexData() && !input.getData().getComplexData().isNil()) {
                ComplexDataType complex = input.getData().getComplexData();
                mapComplexDataType(complex);
            }

            if (input.getData().isSetLiteralData() && !input.getData().getLiteralData().isNil()) {
                LiteralDataType ldt = input.getData().getLiteralData();
                mapLiteralDataType(ldt);
            }

            if (input.getData().isSetBoundingBoxData() && !input.getData().getBoundingBoxData().isNil()) {
                logger.debug("Metadata element not mapped to provenance: " + input.getData().getBoundingBoxData());
            }
        }

    }

    private void mapInputReferenceType(InputReferenceType ref)
    {
        getProperties().setProperty(ProvPropertyType.HREF.toString(), ref.getHref());
        if (ref.isSetMimeType()) {
            setProperty(ProvPropertyType.MIMETYPE, ref.getMimeType());
        }
        if (ref.isSetEncoding()) {
            setProperty(ProvPropertyType.ENCODING, ref.getEncoding());
        }

        if (ref.isSetSchema()) {
            setProperty(ProvPropertyType.SCHEMA, ref.getSchema());
        }

        if (ref.isSetBodyReference()) {
            logger.debug("Metadata element not mapped to provenance: " + ref.getBodyReference());
        }
        if (ref.isSetBody()) {
            logger.debug("Metadata element not mapped to provenance: " + ref.getBody());
        }
        if (ref.isSetMethod()) {
            logger.debug("Metadata element not mapped to provenance: " + ref.getMethod());
        }
    }

    private void mapLiteralDataType(LiteralDataType ldt)
    {
        if (ldt.isSetDataType() && !ldt.isNil() && ldt.getStringValue() != "") {
            setProperty(ProvPropertyType.LITERALDATATYPE, ldt.getStringValue());
        }

        setProperty(ProvPropertyType.AVALUE, ldt.getStringValue());

        if (ldt.isSetUom()) {
            logger.debug("Metadata element not mapped to provenance: " + ldt.isNil());
        }
        ;
    }

    private void mapComplexDataType(ComplexDataType complex)
    {
        if (complex.isSetMimeType()) {
            setProperty(ProvPropertyType.MIMETYPE, complex.getMimeType());
        }

        if (complex.isSetEncoding()) {
            setProperty(ProvPropertyType.ENCODING, complex.getEncoding());
        }

        if (complex.isSetSchema()) {
            setProperty(ProvPropertyType.SCHEMA, complex.getSchema());
        }
    }

    public void enrichProperties(InputDescriptionType idt)
    {
        mapDescriptionType(idt);
        idt.getComplexData();

        if (idt.isSetComplexData() && !idt.getComplexData().isNil()) {
            SupportedComplexDataInputType complex = idt.getComplexData();
            mapSupportedComplexDataType((SupportedComplexDataType) complex);
        }

        if (idt.isSetLiteralData() && !idt.getLiteralData().isNil()) {
            LiteralInputType lit = idt.getLiteralData();
            if (lit.isSetDataType() && !lit.isNil() && lit.getDataType().getStringValue() != "" && !getProperties().containsKey(ProvPropertyType.LITERALDATATYPE.toString())) {

                setProperty(ProvPropertyType.LITERALDATATYPE, lit.getDataType().getStringValue());
            }
        }

        if (idt.isSetBoundingBoxData() && !idt.getBoundingBoxData().isNil()) {
            logger.debug("Metadata element not mapped to provenance: " + idt.getBoundingBoxData());
        }
        // TODO Auto-generated method stub

    }

    public void assignDefaultValue(InputDescriptionType idt)
    {
        String id = idt.getIdentifier().getStringValue();
        this.setId(id + "-" + getId());
        this.setGeneralId(id);
        mapDescriptionType(idt);
        LiteralInputType lit = idt.getLiteralData();
        if (lit.isSetDataType())
            setProperty(ProvPropertyType.LITERALDATATYPE, lit.getDataType().getStringValue());
        if (lit.isSetDefaultValue())
            setProperty(ProvPropertyType.AVALUE, lit.getDefaultValue());
        // TODO Auto-generated method stub

    }

}
