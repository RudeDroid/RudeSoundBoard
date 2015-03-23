package com.rudedroid.rudesoundboard.data;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLParser {
	private ArrayList<CustomSound> barneySounds;
	private Node rootIndex;
	private NodeList itemsIndex;
	
	int count = 0;
	
	public XMLParser() {
		barneySounds = new ArrayList<CustomSound>();
	}
	
	public ArrayList<CustomSound> Parse(InputStream index) {
        try
        {
        	rootIndex = createRoot(index);
            itemsIndex = rootIndex.getChildNodes();
            
            barneySounds = loadSoundList(itemsIndex);
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
        return barneySounds;
	}
	

	private ArrayList<CustomSound> loadSoundList(NodeList nodes) {
		ArrayList<CustomSound> loadedSounds = new ArrayList<CustomSound>();

		for (int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("dict")) {
				loadedSounds.add(loadSound(nodes.item(i)));
				count++;
			}
		}

		return loadedSounds;
	}

	private CustomSound loadSound(Node root) {
		CustomSound loadedSound = new CustomSound();
		NodeList headerData = root.getChildNodes();

		for (int i=0; i<headerData.getLength(); i++) {
			
			if (obtainText(headerData.item(i)).equals("title")) {
				i+=2;
				loadedSound.setTitle(obtainText(headerData.item(i)));
			}

			if (obtainText(headerData.item(i)).equals("sound")) {
				i+=2;
				loadedSound.setSound(obtainText(headerData.item(i)));
			}
		}

		return loadedSound;
	}


	private String obtainText(Node data) {
		StringBuilder text = new StringBuilder();
		NodeList fragments = data.getChildNodes();

		for (int k=0;k<fragments.getLength();k++)
		{
			text.append(fragments.item(k).getNodeValue());
		}

		return text.toString();
	}


	private Element createRoot(InputStream is) {
		Element root = null;
		try {
			//Instantiate the DOM factory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			//Convert to inputSource to use the enconding specified in the xml file.
			InputSource inputSource = new InputSource(is);
			inputSource.setEncoding("UTF-8");

			//Creates a new DOM parser
			DocumentBuilder builder = factory.newDocumentBuilder();

			//Reads the whole XMl file into memory
			Document dom = builder.parse(inputSource);

			//Search of the root node
			root = dom.getDocumentElement();
		} catch (Exception e) {

		}

		return root;
	}
}
