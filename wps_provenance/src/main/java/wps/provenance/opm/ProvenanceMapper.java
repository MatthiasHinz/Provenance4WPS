package wps.provenance.opm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.openprovenance.model.Account;
import org.openprovenance.model.AccountRef;
import org.openprovenance.model.Agent;
import org.openprovenance.model.Annotable;
import org.openprovenance.model.Annotation;
import org.openprovenance.model.Artifact;
import org.openprovenance.model.EmbeddedAnnotation;
import org.openprovenance.model.OPMFactory;
import org.openprovenance.model.OPMGraph;
import org.openprovenance.model.Overlaps;
import org.openprovenance.model.Process;
import org.openprovenance.model.Property;
import org.openprovenance.model.Role;
import org.openprovenance.model.Used;
import org.openprovenance.model.WasControlledBy;
import org.openprovenance.model.WasDerivedFrom;
import org.openprovenance.model.WasGeneratedBy;
import org.openprovenance.model.WasTriggeredBy;

import wps.provenance.ProcessExecution;
import wps.provenance.ProvPropertyType;
import wps.provenance.base.ProcessingArtifact;
import wps.provenance.base.ProcessingController;
import wps.provenance.base.ProvenanceEntity;

public class ProvenanceMapper {

    Logger LOGGER = Logger.getLogger(this.getClass());

    // private HashMap<String, Annotation> annotations = new HashMap<String,
    // Annotation>();
    private ArrayList<Process> processes = new ArrayList<Process>();

    private ArrayList<Annotation> annotations = new ArrayList<Annotation>();

    private ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

    private ArrayList<Overlaps> overlaps = new ArrayList<Overlaps>();

    private ArrayList<Agent> agents = new ArrayList<Agent>();

    private ArrayList<Object> edges = new ArrayList<Object>();

    private ArrayList<Account> accounts = new ArrayList<Account>();

    /**
     * Convert a Java Properties-Object to a List of OPM properties
     * 
     * @param properties
     * @param oFactory
     * @return
     */
    private List<Property> castProperties(Properties properties,
            OPMFactory oFactory)
    {
        List<Property> propertyList = new ArrayList<Property>();
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            ProvPropertyType ptype = ProvPropertyType.getType(key);
            Property property = null;
            if (ptype != null) {
                property = oFactory.newProperty(ptype.getProperty(), properties.getProperty(key));

            } else {
                property = oFactory.newProperty(key, properties.getProperty(key));
            }
            propertyList.add(property);
        }

        return propertyList;

    }

    public OPMGraph map(ProcessExecution processExecution)
    {

        OPMFactory oFactory = new OPMFactory();

        Account executeAccount = oFactory.newAccount("processExecution");

        Collection<Account> executeAccountCollection = Collections.singleton(executeAccount);

        addProcessExecution(processExecution, oFactory, executeAccount, executeAccountCollection, null);

        OPMGraph graph = oFactory.newOPMGraph(accounts, overlaps.toArray(new Overlaps[0]), processes.toArray(new Process[0]), artifacts.toArray(new Artifact[0]), agents.toArray(new Agent[0]), // user,
                                                                                                                                                                                                // wps
                edges.toArray(), annotations.toArray(new Annotation[0]));

        return graph;
    }

    private void addProcessExecution(ProcessExecution processExecution,
            OPMFactory oFactory,
            Account executeAccount,
            Collection<Account> accountCollection,
            Process wasTriggeredByThis)
    {

        Process opmProcess = oFactory.newProcess(processExecution.getId(), accountCollection, processExecution.getGeneralId());
        addAnnotations(oFactory, accountCollection, processExecution, opmProcess);

        if (wasTriggeredByThis != null) {
            String id = UUID.randomUUID().toString().substring(0, 6);
            WasTriggeredBy wtb = oFactory.newWasTriggeredBy(id, opmProcess, wasTriggeredByThis, accountCollection);
            edges.add(wtb);
        }

        processes.add(opmProcess);
        accounts.add(executeAccount);
        Set<ProcessingArtifact> processingArtifacts = null;
        Role role = null;

        processingArtifacts = processExecution.getInput();

        role = oFactory.newRole("wps input");

        for (ProcessingArtifact processingArtifact : processingArtifacts) {
            List<ProcessingArtifact> wdba = processingArtifact.getDerivedByArtifacts();
            addInput(oFactory, accountCollection, opmProcess, role, processingArtifact, wdba);
        }

        processingArtifacts = processExecution.getOutput();

        role = oFactory.newRole("wps output");
        addOutputs(oFactory, accountCollection, opmProcess, processingArtifacts, role);

        // TODO: consider to implement this internals:
        processExecution.getInternalProcessSteps();

        List<ProcessExecution> successors = processExecution.getSuccessorProcesses();
        for (ProcessExecution processExecution2 : successors) {
            addProcessExecution(processExecution2, oFactory, executeAccount, accountCollection, opmProcess);
        }

        if (processExecution.isSetProcessingController()) {
            addController(processExecution, oFactory, accountCollection, opmProcess);
        }
    }

    private void addController(ProcessExecution processExecution,
            OPMFactory oFactory,
            Collection<Account> executeAccountCollection,
            Process opmProcess)
    {
        ProcessingController controllerdata = processExecution.getProcessingController();
        Agent opmAgent = oFactory.newAgent(controllerdata.getId(), executeAccountCollection, controllerdata.getGeneralId());

        agents.add(opmAgent);

        addAnnotations(oFactory, executeAccountCollection, controllerdata, opmAgent);
        WasControlledBy wpsWCB = oFactory.newWasControlledBy(opmProcess, oFactory.newRole(controllerdata.getClass().getSimpleName()), opmAgent, executeAccountCollection);
        edges.add(wpsWCB);
    }

    private void addOutputs(OPMFactory oFactory,
            Collection<Account> executeAccountCollection,
            Process opmProcess,
            Set<ProcessingArtifact> processingArtifacts,
            Role role)
    {

        for (ProcessingArtifact processingArtifact : processingArtifacts) {
            Artifact opmArtifact = oFactory.newArtifact(processingArtifact.getId(), executeAccountCollection, processingArtifact.getGeneralId());
            String id = UUID.randomUUID().toString().substring(0, 6);
            WasGeneratedBy wgb = oFactory.newWasGeneratedBy(id, opmArtifact, role, opmProcess, executeAccountCollection);
            addAnnotations(oFactory, executeAccountCollection, processingArtifact, opmArtifact);
            edges.add(wgb);
            artifacts.add(opmArtifact);
        }
    }



    public void addInput(OPMFactory oFactory,
            Collection<Account> executeAccountCollection,
            Process opmProcess,
            Role role,
            ProcessingArtifact processingArtifact,
            List<ProcessingArtifact> wdba)
    {

        Artifact opmArtifact = oFactory.newArtifact(processingArtifact.getId(), executeAccountCollection, processingArtifact.getGeneralId());

        for (ProcessingArtifact processingWdba : wdba) {
            for (Artifact opmWdba : artifacts) {
                if (opmWdba.getId().equalsIgnoreCase(processingWdba.getId())) {
                    String id = UUID.randomUUID().toString().substring(0, 6);
                    WasDerivedFrom wdf = oFactory.newWasDerivedFrom(id, opmArtifact, opmWdba, executeAccountCollection);
                    edges.add(wdf);
                }
            }

        }

        String id = UUID.randomUUID().toString().substring(0, 6);
        Used used = oFactory.newUsed(id, opmProcess, role, opmArtifact, executeAccountCollection);
        addAnnotations(oFactory, executeAccountCollection, processingArtifact, opmArtifact);
        edges.add(used);
        artifacts.add(opmArtifact);
    }

    private void addAnnotations(OPMFactory oFactory,
            Collection<Account> accountCollection,
            ProvenanceEntity provenanceEntity,
            Annotable opmElement)
    {

        // Collections.singleton(oFactory.newAccountRef(accountlist));
        Properties properties = provenanceEntity.getProperties();

        if (!properties.stringPropertyNames().isEmpty()) {

            if (opmElement instanceof Agent) {
                Collection<AccountRef> accountRefCollection = toAccountRefs(oFactory, accountCollection);
                EmbeddedAnnotation an = oFactory.newEmbeddedAnnotation(provenanceEntity.getId() + "-properties", castProperties(properties, oFactory), accountRefCollection, null);
                oFactory.addAnnotation(opmElement, an);
            } else {
                String id = UUID.randomUUID().toString().substring(0, 6);
                // oFactory.newAnnotation(id, a, property, value, accs)

                Set<String> keys = properties.stringPropertyNames();

                for (String key : keys) {
                    ProvPropertyType ptype = ProvPropertyType.getType(key);
                    String property = null;
                    if (ptype != null) {
                        property = ptype.getProperty();

                    } else {
                        property = key;
                    }

                    // delete invalid tokens from attribute strings
                    String value = properties.get(key).toString();
                    // value = value.replaceAll("\"", "");
                    value = value.replaceAll("“", "");
                    value = value.replaceAll("”", "");
                    // value = value.replaceAll("[^A-Za-z0-9()\\[\\]]", "");
                    // //TODO: reconsider this

                    if (opmElement instanceof Process) {
                        Process concrete = (Process) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);
                    }

                    if (opmElement instanceof Artifact) {
                        Artifact concrete = (Artifact) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                    if (opmElement instanceof Role) {
                        Role concrete = (Role) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                    if (opmElement instanceof WasControlledBy) {
                        WasControlledBy concrete = (WasControlledBy) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                    if (opmElement instanceof WasGeneratedBy) {
                        WasGeneratedBy concrete = (WasGeneratedBy) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                    if (opmElement instanceof WasDerivedFrom) {
                        WasDerivedFrom concrete = (WasDerivedFrom) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                    if (opmElement instanceof Used) {
                        Used concrete = (Used) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                    if (opmElement instanceof WasTriggeredBy) {
                        WasTriggeredBy concrete = (WasTriggeredBy) opmElement;
                        Annotation ann = oFactory.newAnnotation(id, concrete, property, value, accountCollection);
                        oFactory.addAnnotation(concrete, ann);
                        annotations.add(ann);

                    }

                }

            }

        }

    }

    private Collection<AccountRef> toAccountRefs(OPMFactory oFactory,
            Collection<Account> accountCollection)
    {
        Collection<AccountRef> accountRefCollection = new ArrayList<AccountRef>();
        for (Account account : accountCollection) {
            accountRefCollection.add(oFactory.newAccountRef(account));
        }
        return accountRefCollection;
    }

}
