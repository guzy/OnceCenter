package test;

import java.io.File;   
import java.io.FileWriter;   
import java.io.IOException;   
import java.io.Writer;   
import java.util.Iterator;   

import org.dom4j.Attribute;
import org.dom4j.Document;  
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper; 
import org.dom4j.Element;  
import org.dom4j.Node;
import org.dom4j.io.SAXReader;  
import org.dom4j.io.XMLWriter; 

public class Dom4jDemo 
{   
//	public void createXml(String fileName) 
//	{   
//	Document document = DocumentHelper.createDocument();   
//	Element employees=document.addElement("employees");   
//	Element employee=employees.addElement("employee");  
//	Element name= employee.addElement("name"); 
//	name.setText("ddvip");
//	Element sex=employee.addElement("sex");  
//	sex.setText("m");  
//	Element age=employee.addElement("age");  
//	age.setText("29"); 
//	try 
//	{   
//		Writer fileWriter=new FileWriter(fileName);   
//		XMLWriter xmlWriter=new XMLWriter(fileWriter); 
//		xmlWriter.write(document);  
//		xmlWriter.close();  
//		} catch (IOException e) {   
//			System.out.println(e.getMessage());  
//			}  
//	}  
//	public void parserXml(String fileName)
//	{  
//		File inputXml=new File(fileName);  
//		SAXReader saxReader = new SAXReader();  
//		try {
//			Document document = saxReader.read(inputXml);  
//			Element menuBar=document.getRootElement(); 
//			for(Iterator i = menuBar.elementIterator(); i.hasNext();){ 
//				Element menuManager = (Element) i.next(); 
////				for(Iterator j = menuManager.elementIterator(); j.hasNext();){ 
////					Element node=(Element) j.next();   
////					//node.attribute("name");
////					System.out.println(node.getName()+":"+node.getText()+node.attribute("name").getValue());  
////					} 
//				parserMenu(menuManager);
//				}
//			} catch (DocumentException e) {
//				System.out.println(e.getMessage()); 
//				}  
//		System.out.println("dom4j parserXml"); 
//	}  
	
	public void createXml(String fileName) {
		Document document = DocumentHelper.createDocument();
		Element catalog  = document.addElement("catalog");
		catalog.addComment("An XML Catalog");
		catalog.addProcessingInstruction("target", "text");
		Element journal = catalog.addElement("journal");
		journal.addAttribute("title", "ksjdfksdjfk");
		Element title = journal.addElement("title");
		title.setText("skdljflsjkfklsjdflskdjflskdjf");
		Element author = journal.addElement("author");
		document.addDocType("A", "b", "c");
		try{
			Writer w = new FileWriter(fileName);
			org.dom4j.io.OutputFormat format=org.dom4j.io.OutputFormat.createPrettyPrint();
			format.setEncoding("GBK");
			XMLWriter xmlW = new XMLWriter(w,format);
			xmlW.write(document);
			xmlW.close();
			w.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void parseXml(String filename){
		File file = new File(filename);
		SAXReader saxReader = new SAXReader();
		System.out.println(filename);
		try{
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			System.out.println(root.getName());
//			for(int i=0;i<root.nodeCount();i++){
//				Node node = root.node(i);
//				System.out.println(node.getNodeType());
//				System.out.println(node.getName());
//				node.setText("skdfjksdjfksd");
//			}
			for(Iterator<Element> journalIter=root.elementIterator();journalIter.hasNext();){
				//System.out.println("journal");
				Element journal = journalIter.next();
				System.out.println(journal.getName());
				System.out.println(journal.attributeValue("title"));
				Attribute a = journal.attribute("title");
				System.out.println(a.getName());
				System.out.println(a.getValue());
				a.setValue("titksjdf");
//				for(Iterator<Element> articleIter=journal.elementIterator();articleIter.hasNext();){
//					Element article = articleIter.next();
//					if(article.getName().equals("article")){
//						for(Iterator<Element> Iter=article.elementIterator();Iter.hasNext();){
//							Element element = Iter.next();
//							if(element.getName().equals("title")){
//								System.out.println("title:"+element.getText());
//							}else if (element.getName().equals("author")){
//								
//							}
//						}
//					}
//				}
			}
			try{
				Writer w = new FileWriter(filename);
				XMLWriter xmlW = new XMLWriter(w);
				xmlW.write(document);
				xmlW.close();
				w.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void parserMenu(Element menuManager){
		for(Iterator i = menuManager.elementIterator();i.hasNext();){
			Element node = (Element)i.next();
			if(node.getName().equals("MenuManager")){
				System.out.println("MenuManager"+" name = "+node.attributeValue("name"));
				parserMenu(node);
			}else{
				System.out.println("Action in "+menuManager.attributeValue("name")+" name = "+node.attributeValue("name"));
			}
		}
	}
	
	public static void main(String[] args) {
		Dom4jDemo demo = new Dom4jDemo();
		demo.createXml("D:\\test\\test.xml");
		//demo.parseXml("D:\\test\\test.xml");
	}
}   
