package wps.provenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;

public class ProcessMetadata {

    public boolean isProcessDescriptionSetManually()
    {
        return processDescription != null;
    }

    /**
     * <pre>
     *           0..*     1..1
     * WPSProcess ------------------------- Service
     *           wPSProcess        &gt;       server
     * </pre>
     */
    private Service service;

    public ProcessMetadata() {
    }

    public void setService(Service value)
    {
        this.service = value;
    }

    public Service getService()
    {
        return this.service;
    }

    private ProcessDescriptionsDocument processDescription;

    /**
     * 
     * @return Ei
     * @throws MalformedURLException
     * @throws XmlException
     * @throws IOException
     */
    public ProcessDescriptionsDocument getProcessDescriptionDocument() throws MalformedURLException, XmlException, IOException
    {
        if (isProcessDescriptionSetManually())
            return processDescription;
        // ProcessDescriptionType.Factory.
        ProcessDescriptionsDocument psdt = ProcessDescriptionsDocument.Factory.parse(getDescriptionURL());
        return psdt;
    }

    /**
     * Sets the ProcessdescriptionDocument manually
     * 
     * @param processDescription
     */
    public void setProcessDescriptionsDocument(ProcessDescriptionsDocument processDescription)
    {
        this.processDescription = processDescription;
    }

    public ProcessDescriptionType getProcessDescription() throws MalformedURLException, XmlException, IOException
    {
        return getProcessDescriptionDocument().getProcessDescriptions().getProcessDescriptionArray(0);
    }

    private String identifier;

    public void setIdentifier(String value)
    {
        this.identifier = value;
    }

    public String getIdentifier()
    {
        return this.identifier;
    }

    /**
     * Value derived from host URL, therefore setURL is private
     * 
     * @param value
     */
    @SuppressWarnings("unused")
    private void setDescriptionURL(URL value)
    {
        // this.descriptionURL = value;
    }

    public URL getDescriptionURL() throws MalformedURLException
    {
        Service service = getService();
        String optionalVersionParameter = "";
        if (service.getVersion() != null && service.getVersion() != "") {
            optionalVersionParameter = "&Version=" + service.getVersion();
        }

        return new URL(getService().getHostURL() + "?Service=" + getService().getName() + "&Request=DescribeProcess" + optionalVersionParameter + "&Identifier=" + getIdentifier());
    }

}
