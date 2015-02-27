import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.jdom2.output.XMLOutputter;


public class ItemAdder {

	private static File xmlFile;
	private static File xsdFile;

	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("Der skal være en sti til en xml-fil som input!");
			System.exit(0);
		}

		xmlFile = new File(args[0]);
		xsdFile = new File("cloud.xsd");
		String key = "BD341089138DED715E5ABEA7";
		HttpURLConnection con = null;
		URLConnection urlconnection = null;
		int response = 0;

		XMLReaderJDOMFactory xsdFactory;
		try {
			xsdFactory = new XMLReaderXSDFactory(xsdFile);
			SAXBuilder builder = new SAXBuilder(xsdFactory);
			URL createURL = new URL("http://services.brics.dk/java4/cloud/createItem");
			URL modifyURL = new URL("http://services.brics.dk/java4/cloud/modifyItem");
			URL adjustURL = new URL("http://services.brics.dk/java4/cloud/adjustItemStock");		

			Namespace ns = Namespace.getNamespace("http://www.cs.au.dk/dWebTek/2014");
			Document xmlDoc = builder.build(xmlFile);
			Element itemName = xmlDoc.getRootElement().getChild("itemName", ns);
			Element adjustment = xmlDoc.getRootElement().getChild("adjustment", ns);
			Element shopKey = new Element("shopKey", ns);

			//createItem
			urlconnection = createURL.openConnection();
			con = (HttpURLConnection) urlconnection;
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.connect();

			response = con.getResponseCode();
			if(response >=200 && response < 300){
				Element createItem = new Element("createItem", ns);
				System.out.println("Response: " + response);
				shopKey.setText(key);
				createItem.addContent(shopKey);
				createItem.addContent(itemName.clone());
				Document createDoc = new Document(createItem);
				new XMLOutputter().output(createDoc,con.getOutputStream());
			}
			else
				System.out.println("Fejl: " + response);

			//adjustItemStock
			urlconnection = adjustURL.openConnection();
			con = (HttpURLConnection) urlconnection;
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.connect();
			
			response = con.getResponseCode();
			if(response >=200 && response < 300){
				System.out.println("Response: " + response);
				Element adjustItem = new Element("adjustItemStock", ns);
				shopKey.setText(key);
				adjustItem.addContent(shopKey);
				adjustItem.addContent(itemName.clone());
				adjustItem.addContent(adjustment);
				Document adjustDoc = new Document(adjustItem);
				new XMLOutputter().output(adjustDoc,con.getOutputStream());
			}
			else
				System.out.println("Fejl: " + response);

			//modifyItem
			urlconnection = modifyURL.openConnection();
			con = (HttpURLConnection) urlconnection;
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.connect();
			
			response = con.getResponseCode();
			if(response >=200 && response < 300){
				System.out.println("Response: " + response);
				InputStream stream = con.getInputStream();
				Document responseDoc = builder.build(stream);
				Element modifyItem = new Element("modifyItem", ns);
				modifyItem.addContent(shopKey.clone());
				modifyItem.addContent(responseDoc.getRootElement().clone());
				modifyItem.addContent(itemName.clone());
				modifyItem.addContent(xmlDoc.getRootElement().getChild("itemPrice",ns).clone());
				modifyItem.addContent(xmlDoc.getRootElement().getChild("itemURL",ns).clone());
				modifyItem.addContent(xmlDoc.getRootElement().getChild("itemDescription",ns).clone());
				Document modifyDoc = new Document(modifyItem);
				new XMLOutputter().output(modifyDoc,con.getOutputStream());
			}

		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			con.disconnect();
		}
	}
}
