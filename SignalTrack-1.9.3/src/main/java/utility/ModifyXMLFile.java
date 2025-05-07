package utility;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ModifyXMLFile {

	public ModifyXMLFile(File file) {

	   try {
		   final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		   final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		   final Document doc = docBuilder.parse(file.getPath());
		
		doc.getDocumentElement().normalize();

		final Node node = doc.getElementsByTagName("source_configuration").item(0);
		
		final String frequency = getNodeAttr("frequency", node);

	   } catch (SAXException | IOException | ParserConfigurationException sae) {
		sae.printStackTrace();
	   }
	}
	
	private String getNodeAttr(String attrName, Node node ) {
		final NamedNodeMap attrs = node.getAttributes();

	    
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	    	final Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    
	    return "";
	}
}
