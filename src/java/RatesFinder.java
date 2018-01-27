import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides exchange rate finding utility
 * @author Michał Śliwa
 */
public class RatesFinder
{   
    //path to nbp xml
    private static final String NBP_PATH = "http://www.nbp.pl/kursy/"+
                                            "xml/a019z180126.xml";
    
    //exchange rates map
    private HashMap<String,Double> rates;
    
    /**
     * Finds exchange rates of two given currencies
     * @param code1 currency code
     * @param code2 currency code
     * @return Map of echange rates tied to currency code
     */
    public HashMap<String,Double> getExchangeRates(String code1, String code2)
    {
        //initialize result map
        rates = new HashMap<>();
        
        //if code1 or code2 is PLN put exchange rate equal to 1.0
        if(code1.equalsIgnoreCase("PLN") || code2.equalsIgnoreCase("PLN"))
            rates.put("PLN", 1.0);
        
        try
        {
            //get new SAXParser factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //get new SAXParser instance
            SAXParser saxParser = factory.newSAXParser();
            
            //handler for reading xml file
            DefaultHandler handler = new DefaultHandler()
            {
                //is in kod_waluty tag
                boolean inKod = false; 
                //is in kurs_sredni tag
                boolean inKurs = false;
                
                boolean isRate1 = false;
                
                boolean isRate2 = false;
                
                /**
                 * Overriden method, detects opening of a tag
                 * @param uri
                 * @param localName
                 * @param qName
                 * @param attributes
                 * @throws SAXException 
                 */
                @Override
                public void startElement(String uri, String localName, 
                        String qName, Attributes attributes) throws SAXException
                {
                    //if tag name is kod_waluty set inKurs to true
                    if (qName.equalsIgnoreCase("kod_waluty"))
                        inKod = true;
                    //if tag name is kurs_sredni set inKurs to true
                    if (qName.equalsIgnoreCase("kurs_sredni"))
                        inKurs = true;
                    
                }
                
                /**
                 * Overriden method, handles coutent of a tag
                 * @param ch
                 * @param start
                 * @param length
                 * @throws SAXException 
                 */
                @Override
                public void characters(char[] ch, int start, int length) 
                        throws SAXException
                {
                    //if in kod_waluty tag 
                    if (inKod)
                    {
                        //check if currency code matches any given code
                        String currCode = new String(ch,start,length);
                        //set isRate1 to true
                        if (currCode.equalsIgnoreCase(code1))
                            isRate1 = true;
                        //set isRate2 to true
                        if (currCode.equalsIgnoreCase(code2))
                            isRate2 = true;
                    }  
                    if (inKurs)
                    {
                        //put rate1 to result map
                        if (isRate1)
                        //replace , with . to allow parsing
                            rates.put(code1, 
                                    Double.parseDouble(
                                            new String(ch,start,length)
                                                    .replace(',', '.')));
                        //put rate2 to result map
                        if (isRate2)
                        //replace , with . to allow parsing    
                            rates.put(code2, 
                                    Double.parseDouble(
                                            new String(ch,start,length)
                                                    .replace(',', '.')));
                    }
                }
                
                /**
                 * Overriden method, handles closings of tags
                 * @param uri
                 * @param localName
                 * @param qName
                 * @throws SAXException 
                 */
                @Override
                public void endElement(String uri, 
                        String localName, String qName) throws SAXException
                {                   
                    //if tag name is kod_waluty set inKurs to false
                    if (qName.equalsIgnoreCase("kod_waluty"))
                        inKod = false;
                    //if tag name is kurs_sredni set inKurs to false
                    if (qName.equalsIgnoreCase("kurs_sredni"))
                        inKurs = false;
                    //if tag name is pozycja reset rates checks
                    if (qName.equalsIgnoreCase("pozycja"))
                    {
                        isRate1 = false;
                        isRate2 = false;
                    }
                }               
            };
            
            //create url to xml file
            URL xmlUrl = new URL(NBP_PATH);
            //parse NBP xml using defined handler
            saxParser.parse(xmlUrl.openStream(), handler);
        }
        catch (IOException | ParserConfigurationException | SAXException ex)
        {
            //print exceptions
            System.out.println(ex);
        }
        //return exchange rates map
        return rates;
    }
}
