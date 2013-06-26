package wps.provenance;

import java.util.UUID;

/**
 * 
 * @author Matthias Hinz
 *
 */
public enum ProvPropertyType {

    // Assign namespaces and labels:
    MIMETYPE("http://openprovenance.org/model/opmo#type", "mimetype"), LITERALDATATYPE("http://openprovenance.org/model/opmo#type", "literalDataType"), SCHEMA("schema", "schema"), ENCODING(
            "http://openprovenance.org/model/opmo#encoding", "encoding"), HREF("href", "href"), STARTTIME("http://openprovenance.org/model/opmo#startTime", "startTime"), ENDTIME(
            "http://openprovenance.org/model/opmo#endTime", "endTime"),

    /**
     * Dublin Core term: Description may include but is not limited to: an
     * abstract, a table of contents, a graphical representation, or a free-text
     * account of the resource.
     */
    ABSTRACT("http://purl.org/dc/terms/description", "abstract"),

    TITLE("http://purl.org/dc/terms/title", "title"),
    /**
     * Dublin Core term: An entity primarily responsible for making the
     * resource. Examples of a Creator include a person, an organization, or a
     * service.
     */
    CREATOR("http://purl.org/dc/terms/creator", "creator"), AVALUE("http://openprovenance.org/model/opmo#avalue", "value");
    private String property;

    private String label;

    private String equalProperties;

    private String id = UUID.randomUUID().toString();

    // private static HashMap<String, ProvPropertyType> propertyTypes = new
    // HashMap<String, ProvPropertyType>();

    public String getProperty()
    {
        return property;
    }

    public String getLabel()
    {
        return label;
    }

    public String getEqualProperties()
    {
        return equalProperties;
    }

    // public ProvPropertyType getPropertyType(String toString){
    // return propertyTypes.get(toString);
    //
    // }

    private ProvPropertyType(String property, String label) {
        this.property = property;
        this.label = label;
        // setPropertyType();
    }

    private ProvPropertyType(String property, String label, String[] equalProperties) {
        this.property = property;
        this.label = label;
        // setPropertyType();
    }

    // private void setPropertyType() {
    // propertyTypes.put(this.toString(), this);
    // }

    public static ProvPropertyType getType(String key)
    {
        ProvPropertyType[] values = values();
        for (ProvPropertyType provPropertyType : values) {
            if (provPropertyType.toString().equalsIgnoreCase(key))
                return provPropertyType;
        }
        return null;

    }

    @Override
    public String toString()
    {

        return "provpropertydefinition-" + id;
    }

}
