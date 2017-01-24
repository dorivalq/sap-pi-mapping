/*package example.dom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class main
{
 public static void main(String[] args) 
  {
      Calendar cal2 =null;

try {   

          //read the xml      
          File data = new File("data.xml");  
          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();         
          DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();           
          Document doc = dBuilder.parse(data);         
          doc.getDocumentElement().normalize();

 NodeList nodes;
for (int i = 0; i < nodes.getLength(); i++) {     
          Node node = nodes.item(i);           
            if (node.getNodeType() == Node.ELEMENT_NODE) {     
                Element element = (Element) node;   
                username = getValue("username", element);
                startdate = getValue("startdate", element);
                enddate = getValue("enddate", element);
              }
   }


date = startdate; 
Date date_int = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(date); 
cal2 = Calendar.getInstance(); 
cal2.setTime(date_int); 


 //loop the child node to update the initial date
          for (int i = 0; i < nodes.getLength(); i++) {    
              Node node = nodes.item(i);           
                if (node.getNodeType() == Node.ELEMENT_NODE) {     
                    Element element = (Element) node;

                    setValue("startdate", element , date_int.toString());
              }
          }

        //write the content in xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("data.xml"));
            transformer.transform(source, result);

    } catch (Exception ex) {    
      log.error(ex.getMessage());       
      ex.printStackTrace();       
    }
}


  private static void setValue(String tag, Element element , String input) {  
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();   
        Node node = (Node) nodes.item(0); 
        node.setTextContent(input);

}*/