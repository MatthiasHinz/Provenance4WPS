package wps.provenance.base;
import java.util.Properties;


public interface ProvenanceEntity {
public void setId(String value);
   
   public String getId();
   
   public void setProperties(Properties value);
   
   public Properties getProperties();
   
   }
