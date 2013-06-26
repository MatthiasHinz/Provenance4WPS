package wps.provenance;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import wps.provenance.base.ProcessingController;

public class Service extends ProcessingController {
private String name = "WPS";

   @Override
    public Properties getProperties() {
	Properties props = super.getProperties();
	props.setProperty("service", getName()+" "+getVersion());
	props.setProperty("href", getHostURL().toString());
	return props;
    }
   
   public Service(URL hostURL) throws MalformedURLException{
       this.hostURL = hostURL;
       setGeneralId(name);
   }
   
   public void setName(String value) {
      this.name = value;
      setGeneralId(name);
   }
   
   public String getName() {
      return this.name;
   }
   

   

    private String version = "1.0.0";
    
    private URL hostURL;
    
   
   public void setVersion(String value) {
      this.version = value;
   }
   
   public String getVersion() {
      return this.version;
   }
   

   
   public void setHostURL(URL url) {
      this.hostURL = url;
      this.setId(url.toString());
   }
   
   public URL getHostURL() {
      return this.hostURL;
   }
   

   }
