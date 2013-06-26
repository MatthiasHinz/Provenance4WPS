package wps.provenance;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.opengis.wps.x100.ExecuteDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

public class ProvenanceFactory {

    private static ProvenanceFactory instance;

    private ProvenanceFactory() {
    };

    public static ProvenanceFactory getInstance()
    {
        if (instance == null)
            instance = new ProvenanceFactory();
        return instance;
    }

    public ProcessExecution generateProcessRecord(InputStream executeRequest,
            URL serviceURL) throws XmlException, IOException
    {
        ProcessExecution execution = new ProcessExecution();

        // Fill with data: process execution document
        XmlOptions option = new XmlOptions();
        option.setLoadTrimTextBuffer();
        ExecuteDocument execDom = ExecuteDocument.Factory.parse(executeRequest, option);
        execution.setExecuteRequest(execDom);

        ProcessMetadata process = new ProcessMetadata();
        String id = execDom.getExecute().getIdentifier().getStringValue();
        process.setIdentifier(id);
        Service wps = new Service(serviceURL);
        process.setService(wps);
        execution.setProcess(process);

        executeRequest.close();
        return execution;
    }

}
