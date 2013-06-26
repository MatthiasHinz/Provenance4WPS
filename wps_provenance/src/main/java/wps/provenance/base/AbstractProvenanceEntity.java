package wps.provenance.base;

import java.util.Properties;
import java.util.UUID;

import wps.provenance.ProvPropertyType;

import net.opengis.wps.x100.DescriptionType;

/**
 * 
 * @author Matthias Hinz
 *
 */
public abstract class AbstractProvenanceEntity implements ProvenanceEntity {

    private String id = UUID.randomUUID().toString().substring(0, 6);

    /**
     * Refers to the ID of the general object class, e.g. a process identifier
     * for an individual process-executions, an input identifier for an
     * individual artifact Getter method returns id if generalID is not set
     */
    private String generalId;

    public void setId(String value)
    {
        this.id = value;
    }

    public String getId()
    {
        return this.id;
    }

    private Properties properties = new Properties();

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * 
     * @param type
     *            Predefined property type
     * @param value
     *            The properties value
     */
    public void setProperty(ProvPropertyType type,
            String value)
    {
        getProperties().setProperty(type.toString(), value);
    }

    /**
     * 
     * @param type
     *            The type of property, e.g. http://example.org/property
     * @param value
     *            The properties value
     */
    public void setProperty(String type,
            String value)
    {
        getProperties().setProperty(type, value);
    }

    public String getGeneralId()
    {
        if (generalId == null)
            return getId();
        return generalId;
    }

    public void setGeneralId(String generalID)
    {
        this.generalId = generalID;
    }

    protected void mapDescriptionType(DescriptionType pdt)
    {
        if (pdt.isSetAbstract() && !pdt.getAbstract().isNil() && pdt.getAbstract().getStringValue() != "") {
            setProperty(ProvPropertyType.ABSTRACT, pdt.getAbstract().getStringValue());
        }

        String title = pdt.getTitle().getStringValue();
        if (title != null && title != "") {
            setProperty(ProvPropertyType.TITLE, pdt.getTitle().getStringValue());
        }
        // pdt.getMetadataArray(); TODO not yet implemented
    }

}
