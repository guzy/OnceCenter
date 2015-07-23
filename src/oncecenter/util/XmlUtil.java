package oncecenter.util;

import java.io.File;

import oncecenter.Constants;
import oncecenter.util.menuutil.MenuUtil;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class XmlUtil {
	
//	private final static Logger m_logger = Logger.getLogger(MenuUtil.class);
	
	public static Document getDocument(File file) {
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(file);
			return document;
		} catch (DocumentException e) {
//			m_logger.error(e);
			return Constants.NULL_DOCUMENT;
		}  
	}
}
