package wps.provenance.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wps.provenance.ProvPropertyType;

import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataType;

public class ProcessingArtifact extends AbstractProvenanceEntity {

    private static Logger logger = Logger.getLogger(ProcessingArtifact.class);

    /**
     * get the artifacts that this artifact was derived by (if andy)
     */
    private List<ProcessingArtifact> wasDerivedByArtifacts = new ArrayList<ProcessingArtifact>();

    public List<ProcessingArtifact> getDerivedByArtifacts()
    {
        return wasDerivedByArtifacts;
    }

    public void setDerivedByArtifacts(List<ProcessingArtifact> derivedByArtifacts)
    {
        this.wasDerivedByArtifacts = derivedByArtifacts;
    }

    protected void mapSupportedComplexDataType(SupportedComplexDataType complex)
    {
        // given properties (null or value):
        ComplexDataCombinationType def = complex.getDefault();
        ComplexDataDescriptionType defformat = def.getFormat();
        boolean success = tryMatchFormatToProperties(defformat);
        if (!success) {
            ComplexDataDescriptionType[] formats = complex.getSupported().getFormatArray();
            for (ComplexDataDescriptionType format : formats) {
                success = tryMatchFormatToProperties(format);
                if (success = true)
                    break;
            }
            if (success == false) {
                logger.error("Artifact dataformat of " + this.getId() + " does not match processDescriptions. Process description information about this will be discarded");
            }
        }

    }

    protected boolean tryMatchFormatToProperties(ComplexDataDescriptionType format)
    {
        String gmimetype = getProperties().getProperty(ProvPropertyType.MIMETYPE.toString());
        String gschema = getProperties().getProperty(ProvPropertyType.SCHEMA.toString());
        String gencoding = getProperties().getProperty(ProvPropertyType.ENCODING.toString());

        boolean setm = false;
        boolean sets = false;
        boolean sete = false;

        if (gmimetype == null) {
            gmimetype = format.getMimeType();
            setm = true;
        } else {
            if (gmimetype != null && !gmimetype.equalsIgnoreCase(format.getMimeType())) {
                return false;
            }
        }

        if (gschema == null && format.isSetSchema()) {
            gschema = format.getSchema();
            sets = true;
        } else {
            if (gschema != null && !gschema.equalsIgnoreCase(format.getSchema())) {
                return false;
            }
        }

        if (gencoding == null && format.isSetEncoding()) {
            sete = true;
            gencoding = format.getEncoding();
        } else {
            if (gencoding != null && !gencoding.equalsIgnoreCase(format.getEncoding())) {
                return false;
            }
        }

        // if the method didn't break up till here, the format can be matched:

        if (setm) {
            setProperty(ProvPropertyType.MIMETYPE, gmimetype);
        }

        if (sete) {
            setProperty(ProvPropertyType.ENCODING, gencoding);
        }

        if (sets) {
            setProperty(ProvPropertyType.SCHEMA, gschema);
        }

        return true;
    }

}
