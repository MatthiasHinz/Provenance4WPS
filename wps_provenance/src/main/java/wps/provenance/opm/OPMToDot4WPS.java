package wps.provenance.opm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.openprovenance.model.Account;
import org.openprovenance.model.AccountRef;
import org.openprovenance.model.Agent;
import org.openprovenance.model.Annotable;
import org.openprovenance.model.Artifact;
import org.openprovenance.model.Edge;
import org.openprovenance.model.EmbeddedAnnotation;
import org.openprovenance.model.Label;
import org.openprovenance.model.Node;
import org.openprovenance.model.OPMDeserialiser;
import org.openprovenance.model.OPMFactory;
import org.openprovenance.model.OPMGraph;
import org.openprovenance.model.OPMUtilities;
import org.openprovenance.model.Process;
import org.openprovenance.model.Property;
import org.openprovenance.model.Role;
import org.openprovenance.model.printer.AccountColorMapEntry;
import org.openprovenance.model.printer.AgentMapEntry;
import org.openprovenance.model.printer.ArtifactMapEntry;
import org.openprovenance.model.printer.EdgeStyleMapEntry;
import org.openprovenance.model.printer.OPMPrinterConfigDeserialiser;
import org.openprovenance.model.printer.OPMPrinterConfiguration;
import org.openprovenance.model.printer.ProcessMapEntry;

/** Serialisation of  OPM Graphs to DOT format. */
public class OPMToDot4WPS {
/*package*/ void setU(OPMUtilities value) {
this.u = value;
}

/*package*/ OPMUtilities getU() {
   return this.u;
}

    public final static String DEFAULT_CONFIGURATION_FILE="defaultConfig.xml";
    public final static String DEFAULT_CONFIGURATION_FILE_WITH_ROLE="defaultConfigWithRole.xml";
    public final static String USAGE="opm2dot opmFile.xml out.dot out.pdf [configuration.xml]";
private OPMUtilities u = new OPMUtilities();

    OPMFactory of=new OPMFactory();



    public static void main(String [] args) throws Exception {
        if ((args==null) || (args.length==0) || (args.length>4)) {
            System.out.println(USAGE);
            return;
        }

        String opmFile=args[0];
        String outDot=args[1];
        String outPdf=args[2];
        String configFile=((args.length==4) ? args[3] : null);

        OPMToDot4WPS converter=((configFile==null) ? new OPMToDot4WPS() : new OPMToDot4WPS(configFile));

        converter.convert(opmFile,outDot,outPdf);
    }


    public OPMToDot4WPS() {
        InputStream is=this.getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_FILE);
        init(is);
    }
    public OPMToDot4WPS(boolean withRoleFlag) {
        InputStream is;
        if (withRoleFlag) {
            is=this.getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_FILE_WITH_ROLE);
        } else {
            is=this.getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_FILE);
        }
        init(is);
    }

    public OPMToDot4WPS(String configurationFile) {
        this();
        init(configurationFile);
    }

    public OPMPrinterConfigDeserialiser getDeserialiser() {
        return OPMPrinterConfigDeserialiser.getThreadOPMPrinterConfigDeserialiser();
    }
    
    public void init(String configurationFile) {
        OPMPrinterConfigDeserialiser printerDeserial=getDeserialiser();
        try {
            OPMPrinterConfiguration opc=printerDeserial.deserialiseOPMPrinterConfiguration(new File(configurationFile));
            init(opc);
        } catch (JAXBException je) {
            je.printStackTrace();
        }
    }

    public void init(InputStream is) {
        OPMPrinterConfigDeserialiser printerDeserial=getDeserialiser();
        try {
            OPMPrinterConfiguration opc=printerDeserial.deserialiseOPMPrinterConfiguration(is);
            init(opc);
        } catch (JAXBException je) {
            je.printStackTrace();
        }
    }

    public void init(OPMPrinterConfiguration configuration) {
        if (configuration==null) return;

        if (configuration.getEdges()!=null) {
            if (configuration.getEdges().getDefault()!=null) {
                defaultEdgeStyle=configuration.getEdges().getDefault();
            }
            
            for (EdgeStyleMapEntry edge: configuration.getEdges().getEdge()) {
                edgeStyleMap.put(edge.getType(),edge);
            }
        }

        if (configuration.getProcesses()!=null) {
            if (configuration.getProcesses().isDisplayValue()!=null) {
                this.displayProcessValue=configuration.getProcesses().isDisplayValue();
            }
            if (configuration.getProcesses().isColoredAsAccount()!=null) {
                this.displayProcessColor=configuration.getProcesses().isColoredAsAccount();
            }
            for (ProcessMapEntry process: configuration.getProcesses().getProcess()) {
                processNameMap.put(process.getValue(),process.getDisplay());
            }
        }

        if (configuration.getArtifacts()!=null) {
            if (configuration.getArtifacts().isDisplayValue()!=null) {
                this.displayArtifactValue=configuration.getArtifacts().isDisplayValue();
            }
            if (configuration.getArtifacts().isColoredAsAccount()!=null) {
                this.displayArtifactColor=configuration.getArtifacts().isColoredAsAccount();
            }
            for (ArtifactMapEntry artifact: configuration.getArtifacts().getArtifact()) {
                artifactNameMap.put(artifact.getValue(),artifact.getDisplay());
            }
        }

        if (configuration.getAgents()!=null) {
            if (configuration.getAgents().isDisplayValue()!=null) {
                this.displayAgentValue=configuration.getAgents().isDisplayValue();
            }
            if (configuration.getAgents().isColoredAsAccount()!=null) {
                this.displayAgentColor=configuration.getAgents().isColoredAsAccount();
            }
            for (AgentMapEntry agent: configuration.getAgents().getAgent()) {
                agentNameMap.put(agent.getValue(),agent.getDisplay());
            }
        }

        if (configuration.getAccounts()!=null) {
            if (configuration.getAccounts().getDefaultAccount()!=null) {
                this.defaultAccountLabel=configuration.getAccounts().getDefaultAccount();
            }
            if (configuration.getAccounts().getDefaultColor()!=null) {
                this.defaultAccountColor=configuration.getAccounts().getDefaultColor();
            }
            for (AccountColorMapEntry account: configuration.getAccounts().getAccount()) {
                accountColourMap.put(account.getName(),account.getColor());
            }
        }

        if (configuration.getGraphName()!=null) {
            this.name=configuration.getGraphName();
        }

    }

    public void convert(String opmFile, String dotFile, String pdfFile)
        throws java.io.FileNotFoundException, java.io.IOException, JAXBException {
        convert (OPMDeserialiser.getThreadOPMDeserialiser().deserialiseOPMGraph(new File(opmFile)),dotFile,pdfFile);
    }

    public void convert(OPMGraph graph, String dotFile, String pdfFile)
        throws java.io.FileNotFoundException, java.io.IOException {
        convert(graph,new File(dotFile));
        Runtime runtime = Runtime.getRuntime();
        @SuppressWarnings("unused")
	java.lang.Process proc = runtime.exec("dot -o " + pdfFile + " -Tpdf " + dotFile);
    }

    public void convert(OPMGraph graph, File file) throws java.io.FileNotFoundException{
        OutputStream os=new FileOutputStream(file);
        convert(graph, new PrintStream(os));
    }

    public void convert(OPMGraph graph, PrintStream out) {
        List<Edge> edges=u.getEdges(graph);

        prelude(out);

        if (graph.getProcesses()!=null) {
            for (Process p: graph.getProcesses().getProcess()) {
                emitProcess(p,out);
            }
        }

        if (graph.getArtifacts()!=null) {
            for (Artifact p: graph.getArtifacts().getArtifact()) {
                emitArtifact(p,out);
            }
        }

        if (graph.getAgents()!=null) {
            for (Agent p: graph.getAgents().getAgent()) {
                emitAgent(p,out);
            }
        }

        for (Edge e: edges) {
            emitDependency(e,out);
        }
        

        postlude(out);
       
    }

    boolean collapseAnnotations=true;

    static int embeddedAnnotationCounter=0;

    boolean suspressAnnotations=true;
    
    public void emitAnnotations(Annotable node, PrintStream out) {
        if(suspressAnnotations)
            return;
        if (collapseAnnotations) {

            EmbeddedAnnotation newAnn=null;

            for (JAXBElement<? extends EmbeddedAnnotation> ann: node.getAnnotation()) {
                EmbeddedAnnotation emb=ann.getValue();
                of.expandAnnotation(emb);
                if (filterAnnotation(emb)) {
                    List<Property> properties=emb.getProperty();
                    if (newAnn==null) {
                        newAnn=of.newEmbeddedAnnotation("eid"+(embeddedAnnotationCounter++),
                                                        new LinkedList<Property>(),
                                                        null,
                                                        null);
                    }
                    newAnn.getProperty().addAll(properties);
                }
            }

            if (newAnn!=null) {
                emitAnnotation(node.getId(),newAnn,out);
            }

        } else {
            for (JAXBElement<? extends EmbeddedAnnotation> ann: node.getAnnotation()) {
                EmbeddedAnnotation emb=ann.getValue();
                of.expandAnnotation(emb);
                if (filterAnnotation(emb)) emitAnnotation(node.getId(), emb,out);
            }
        }
    }


    //////////////////////////////////////////////////////////////////////
    ///
    ///                              NODES
    ///
    //////////////////////////////////////////////////////////////////////

    public void emitProcess(Process p, PrintStream out) {
        HashMap<String,String> properties=new HashMap<String, String>();

        emitNode(p.getId(),
                 addProcessShape(p,addProcessLabel(p, addProcessColor(p,properties))),
                 out);

        emitAnnotations(p,out);
    }

    public void emitArtifact(Artifact a, PrintStream out) {
        HashMap<String,String> properties=new HashMap<String, String>();

        emitNode(a.getId(),
                 addArtifactShape(a,addArtifactLabel(a, addArtifactColor(a,properties))),
                 out);

        emitAnnotations(a,out);
    }

    public void emitAgent(Agent ag, PrintStream out) {
        HashMap<String,String> properties=new HashMap<String, String>();

        emitNode(ag.getId(),
                 addAgentShape(ag,addAgentLabel(ag, addAgentColor(ag,properties))),
                 out);

        emitAnnotations(ag,out);

    }

    public void emitAnnotation(String id, EmbeddedAnnotation ann, PrintStream out) {
        HashMap<String,String> properties=new HashMap<String, String>();
        String newId=annotationId(ann.getId(),id);
        emitNode(newId,
                 addAnnotationShape(ann,addAnnotationColor(ann,addAnnotationLabel(ann,properties))),
                 out);
        HashMap<String,String> linkProperties=new HashMap<String, String>();
        emitEdge(newId,id,addAnnotationLinkProperties(ann,linkProperties),out,true);
    }


    public boolean filterAnnotation(EmbeddedAnnotation ann) {
        if (ann instanceof Label) {
            return false;
        } else {
            return true;
        }
    }

    int annotationCount=0;
    public String annotationId(String id,String node) {
        if (id==null) {
            return "ann" + node + (annotationCount++);
        } else {
            return id;
        }
    }

    public HashMap<String,String> addAnnotationLinkProperties(EmbeddedAnnotation ann, HashMap<String,String> properties) {
        properties.put("arrowhead","none");
        properties.put("style","dashed");
        properties.put("color","gray");
        return properties;
    }
    
    public HashMap<String,String> addProcessShape(Process p, HashMap<String,String> properties) {
        properties.put("shape","polygon");
        properties.put("sides","4");
        return properties;
    }

    public HashMap<String,String> addProcessLabel(Process p, HashMap<String,String> properties) {
        properties.put("label",processLabel(p));
        return properties;
    }

    public HashMap<String,String> addProcessColor(Process p, HashMap<String,String> properties) {
        if (displayProcessColor) {
            properties.put("color",processColor(p));
            properties.put("fontcolor",processColor(p));
        }
        return properties;
    }

    public HashMap<String,String> addArtifactShape(Artifact p, HashMap<String,String> properties) {
        // default is good for artifact
        return properties;
    }

    public HashMap<String,String> addArtifactColor(Artifact a, HashMap<String,String> properties) {
        if (displayArtifactColor) {
            properties.put("color",artifactColor(a));
            properties.put("fontcolor",artifactColor(a));
        }
        return properties;
    }

    public HashMap<String,String> addArtifactLabel(Artifact p, HashMap<String,String> properties) {
        properties.put("label",artifactLabel(p));
        return properties;
    }

    public HashMap<String,String> addAgentShape(Agent p, HashMap<String,String> properties) {
        properties.put("shape","polygon");
        properties.put("sides","8");
        return properties;
    }

    public HashMap<String,String> addAgentLabel(Agent p, HashMap<String,String> properties) {
        properties.put("label",agentLabel(p));
        return properties;
    }

    public HashMap<String,String> addAgentColor(Agent a, HashMap<String,String> properties) {
        if (displayAgentColor) {
            properties.put("color",agentColor(a));
            properties.put("fontcolor",agentColor(a));
        }
        return properties;
    }

    public HashMap<String,String> addAnnotationShape(EmbeddedAnnotation ann, HashMap<String,String> properties) {
        properties.put("shape","note");
        return properties;
    }
    public HashMap<String,String> addAnnotationLabel(EmbeddedAnnotation ann, HashMap<String,String> properties) {
        String label="";
        label=label+"<<TABLE cellpadding=\"0\" border=\"0\">\n";
        for (Property prop: ann.getProperty()) {
            label=label+"	<TR>\n";
            label=label+"	    <TD align=\"left\">" + convertProperty(prop.getUri()) + ":</TD>\n";
            label=label+"	    <TD align=\"left\">" + convertValue(prop.getValue()) + "</TD>\n";
            label=label+"	</TR>\n";
        }
        label=label+"    </TABLE>>\n";
        properties.put("label",label);
        return properties;
    }

   public String convertValue(Object v) {
         String label=""+v;
         return label;
         //TODO check <-- removed conversion
//         int i=label.lastIndexOf("#");
//         int j=label.lastIndexOf("/");
//         return label.substring(Math.max(i,j)+1, label.length());
     }

    public String convertProperty(String label) {
        int i=label.lastIndexOf("#");
        int j=label.lastIndexOf("/");
        return label.substring(Math.max(i,j)+1, label.length());
    }


    public HashMap<String,String> addAnnotationColor(EmbeddedAnnotation ann, HashMap<String,String> properties) {
        if (displayAnnotationColor) {
            properties.put("color",annotationColor(ann));
            properties.put("fontcolor","black");
            //properties.put("style","filled");
        }
        return properties;
    }


    boolean displayProcessValue=false;
    boolean displayProcessColor=false;
    boolean displayArtifactValue=false;
    boolean displayArtifactColor=false;
    boolean displayAgentColor=false;
    boolean displayAgentValue=false;
    boolean displayAnnotationColor=true;
    
    
//    //TODO: check / all variables had been set true
//    boolean displayProcessValue=true;
//    boolean displayProcessColor=true;
//    boolean displayArtifactValue=true;
//    boolean displayArtifactColor=true;
//    boolean displayAgentColor=true;
//    boolean displayAgentValue=true;
//    boolean displayAnnotationColor=true;

    public String processLabel(Process p) {
        if (displayProcessValue) {
            return convertProcessName(""+of.getLabel(p));
        } else {
            return p.getId();
        }
    }
    public String processColor(Process p) {
        // Note, I should compute effective account membership
        List<String> colors=new LinkedList<String>();
        for (AccountRef acc: p.getAccount()) {
            String accountLabel=((Account)acc.getRef()).getId();
            String colour=convertAccount(accountLabel);
            colors.add(colour);
        }

        return selectColor(colors);
    }

    // returns the first non transparent color
    public String selectColor(List<String> colors) {
        String tr="transparent";
        for (String c: colors) {
            if (!(c.equals(tr))) return c;
        }
        return tr;
    }
        
    public String artifactLabel(Artifact p) {
        if (displayArtifactValue) {
            return convertArtifactName(""+of.getLabel(p));
        } else {
            return p.getId();
        }
    }
    public String artifactColor(Artifact p) {
        // Note, I should compute effective account membership
        List<String> colors=new LinkedList<String>();
        for (AccountRef acc: p.getAccount()) {
            String accountLabel=((Account)acc.getRef()).getId();
            String colour=convertAccount(accountLabel);
            colors.add(colour);
        }
        return selectColor(colors);
    }
    public String agentColor(Agent p) {
        // Note, I should compute effective account membership
        List<String> colors=new LinkedList<String>();
        for (AccountRef acc: p.getAccount()) {
            String accountLabel=((Account)acc.getRef()).getId();
            String colour=convertAccount(accountLabel);
            colors.add(colour);
        }
        return selectColor(colors);
    }


    public String annotationColor(EmbeddedAnnotation ann) {
        List<String> colors=new LinkedList<String>();
        colors.add("gray");
        return selectColor(colors);
    }

    public String agentLabel(Agent p) {
        if (displayAgentValue) {
            return convertAgentName(""+of.getLabel(p));
        } else {
            return p.getId();
        }
    }

    HashMap<String,String> processNameMap=new HashMap<String,String>();
    public String convertProcessName(String process) {
        String name=processNameMap.get(process);
        if (name!=null) return name;
        return process;
    }
    HashMap<String,String> artifactNameMap=new HashMap<String,String>();
    public String convertArtifactName(String artifact) {
        String name=artifactNameMap.get(artifact);
        if (name!=null) return name;
        return artifact;
    }
    HashMap<String,String> agentNameMap=new HashMap<String,String>();
    public String convertAgentName(String agent) {
        String name=agentNameMap.get(agent);
        if (name!=null) return name;
        return agent;
    }


    //////////////////////////////////////////////////////////////////////
    ///
    ///                              EDGES
    ///
    //////////////////////////////////////////////////////////////////////

    public void emitDependency(Edge e, PrintStream out) {
        HashMap<String,String> properties=new HashMap<String, String>();

        List<AccountRef> accounts=e.getAccount();
        if (accounts.isEmpty()) {
            accounts=new LinkedList<AccountRef>();
            accounts.add(of.newAccountRef(of.newAccount(defaultAccountLabel)));
        }
            
        for (AccountRef acc: accounts) {
            String accountLabel=((Account)acc.getRef()).getId();
            addEdgeAttributes(accountLabel,e,properties);
            emitEdge( ((Node)e.getEffect().getRef()).getId(),
                      ((Node)e.getCause().getRef()).getId(),
                      properties,
                      out,
                      true);
        }
    }

    public HashMap<String,String> addEdgeAttributes(String accountLabel,
                                                    Edge e,
                                                    HashMap<String,String> properties) {
        String colour=convertAccount(accountLabel);
        properties.put("color",colour);
        properties.put("fontcolor",colour);
        properties.put("style",getEdgeStyle(e));
        addEdgeLabel(e,properties);
        return properties;
    }


    /* Displays type if any, role otherwise. */
    public void addEdgeLabel(Edge e, HashMap<String,String> properties) {
        String label=null;
        String type=of.getType(e);
        if (type!=null) {
            label=type;
        } else if (getEdgePrintRole(e)) {
            Role role=of.getRole(e);
            if (role!=null && role.getValue()!=null) {
                label=displayRole(role.getValue());
                properties.put("fontsize","8");
            }
        }
        if (label!=null) {
            properties.put("label",convertEdgeLabel(label));
            if (properties.get("fontsize")==null) {
                properties.put("fontsize","10");
            }
        }
    }

    public String displayRole(String role) {
        return "(" + role + ")";
    }

    public String convertEdgeLabel(String label) {
        return label.substring(label.indexOf("#")+1, label.length());
    }


    HashMap<String,String> accountColourMap=new HashMap<String,String>();
    public String convertAccount(String account) {
        String colour=accountColourMap.get(account);
        if (colour!=null) return colour;
        return defaultAccountColor;
    }

    String defaultEdgeStyle;
    HashMap<String,EdgeStyleMapEntry> edgeStyleMap=new HashMap<String,EdgeStyleMapEntry>();
    
    public String getEdgeStyle(Edge edge) {
        String name=edge.getClass().getName();
        EdgeStyleMapEntry style=edgeStyleMap.get(name);
        if (style!=null) return style.getStyle();
        return defaultEdgeStyle;
    }

    public boolean getEdgePrintRole(Edge edge) {
        String name=edge.getClass().getName();
        EdgeStyleMapEntry style=edgeStyleMap.get(name);
        if (style!=null) {
            Boolean flag=style.isPrintRole();
            if (flag==null) return false;
            return flag;
        } else {
            return false;
        }
    }

    
    //////////////////////////////////////////////////////////////////////
    ///
    ///                              DOT FORMAT GENERATION
    ///
    //////////////////////////////////////////////////////////////////////
    
    
    String name;
    String defaultAccountLabel;
    String defaultAccountColor;


    public void emitNode(String name, HashMap<String,String> properties, PrintStream out) {
    	//TODO: check
    	name = '"'+name+'"';
        StringBuffer sb=new StringBuffer();
        sb.append(name);
        emitProperties(sb,properties);
        out.println(sb.toString());
    }


    public void emitEdge(String src, String dest, HashMap<String,String> properties, PrintStream out, boolean directional) {
    	//TODO: check
    	src = '"'+src+'"';
    	dest = '"'+dest+'"';
    	
    	StringBuffer sb=new StringBuffer();
        sb.append(src);
        if (directional) {
            sb.append(" -> ");
        } else {
            sb.append(" -- ");
        }
        sb.append(dest);
        emitProperties(sb,properties);
        out.println(sb.toString());
    }

    public void emitProperties(StringBuffer sb,HashMap<String,String> properties) {
        sb.append(" [");
        boolean first=true;
        for (String key: properties.keySet()) {
            if (first) {
                first=false;
            } else {
                sb.append(",");
            }
            String value=properties.get(key);
            sb.append(key);
            if (value.startsWith("<")) {
                sb.append("=");
                sb.append(value);
            } else {
                sb.append("=\"");
                sb.append(value);
                sb.append("\"");
            }


        }
        sb.append("]");
    }

    void prelude(PrintStream out) {
        out.println("digraph " + name + " { rankdir=\"BT\"; ");
    }

    void postlude(PrintStream out) {
        out.println("}");
        out.close();
    }


    


    
}


