package oncecenter.util.menuutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oncecenter.Constants;
import oncecenter.Constants.ActionType;
import oncecenter.util.FileUtil;
import oncecenter.util.XmlUtil;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;

public class MenuUtil {
	
//	private static final Logger m_logger = Logger.getLogger(MenuUtil.class);
	
	//检查输入xml的有效性
	public static void fillMenu(IContributionManager parent){
		File config = null;
		if(parent instanceof IMenuManager)
			config = new  File(FileUtil.getMenuConfigFile());
		else
			config = new File(FileUtil.getToolbarConfigFile());
		if(config==null||!config.exists()) {
			try {
				throw new FileNotFoundException("File not found: " + config);
			} catch (FileNotFoundException e) {
			}
		} else {
			Document document = XmlUtil.getDocument(config);
			if(isNull(document)) {
				System.out.println("解析xml文件失败");
				return;
			}
			if(parent instanceof IMenuManager)
				createMenu((IMenuManager)parent, document);
			else
				createToolbar((IToolBarManager)parent, document);
		}
	}

	/*******************************************************
	 * 
	 *
	 * 	               Create Menu 
	 * 
	 ********************************************************/
	private static void createMenu(IMenuManager parent, Document document) {
		try {
			Element root = document.getRootElement(); 
			for(Iterator<Element> child = root.elementIterator(); child.hasNext();){ 
				createSubMenu(parent, (Element) child.next());
			}
		} catch (Exception e) {
//			m_logger.error(e);
		} finally {
			if(document != null) {
				document.clearContent();
			}
		}

	}

	
	private static void createSubMenu(IMenuManager parent, Element node) {
		if(hasSubMenu(node)){
			MenuManager newParent = createParentForSubMenu(parent, node);
			for(Iterator<Element> child = node.elementIterator(); child.hasNext();){ 
				createSubMenu(newParent, (Element) child.next());
			}
		} else {
			if (isActionMenu(node)){
				parent.add(createActionMenu(node));
			} else if (isContributionMenu(node)){
				parent.add(createContributionMenu(node));
			} else {
				throw new IllegalArgumentException("Unknown node type");
			}
		}
	}


	private static MenuManager createParentForSubMenu(IMenuManager parent,
			Element node) {
		MenuManager menu = new MenuManager(node.attributeValue(Constants.MENUXML_ELEMENT_NAME));
		parent.add(menu);
		return menu;
	}

	
	/*******************************************************
	 * 
	 *
	 * 	               Create Toolbar 
	 * 
	 ********************************************************/
	private static void createToolbar(IToolBarManager parent, Document document) {
		try {
			Element root = document.getRootElement(); 
			for(Iterator<Element> child = root.elementIterator(); child.hasNext();){ 
				Element node = (Element) child.next();
				if (isActionMenu(node)){
					ActionContributionItem actionContriCI = new  
				  		      ActionContributionItem(createActionMenu(node));  
					actionContriCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);  
					parent.add(actionContriCI);
				} else if (isContributionMenu(node)){
					parent.add(createContributionMenu(node));
				} else {
					throw new IllegalArgumentException("Unknown node type");
				}
			}
		} catch (Exception e) {
			
		} finally {
			if(document != null) {
				document.clearContent();
			}
		}

	}
	
	
	
	//把node变成action或separate
	private static IContributionItem createContributionMenu(Element node) {
		try {
			IContributionItem newContribution = (IContributionItem) createNewInstance(node);
			return newContribution;
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}


	private static IAction createActionMenu(Element node) {
		try {
			IAction newAction = (IAction) createNewInstance(node);
			newAction.setEnabled(isEnable(node.attributeValue(Constants.MENUXML_ELEMENT_ENABLE)));
			String types = node.attributeValue(Constants.MENUXML_ELEMENT_TYPE);
			List<ActionType> actionTypeList = new ArrayList<ActionType>();
			for(String type : types.split(",")){
				actionTypeList.add(ActionType.valueOf(type));
			}
			Constants.menuTypeMap.put(newAction,actionTypeList) ;
			return newAction;
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}


	private static Object createNewInstance(Element node)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
			String classname = node.attributeValue(Constants.MENUXML_ELEMENT_CLASS);
			String menutext = node.attributeValue(Constants.MENUXML_ELEMENT_TEXT);
			String menuimage = node.attributeValue(Constants.MENUXML_ELEMENT_IMAGE);
			String menudisabledimage = node.attributeValue(Constants.MENUXML_ELEMENT_DIABLEDIMAGE);
			Class actionClass = Class.forName(classname);
			if(menutext==null&&menuimage==null&&menudisabledimage==null)
				return actionClass.newInstance();
			Class[] paramTypes = { String.class, String.class, String.class };
			Object[] params = {menutext, menuimage, menudisabledimage};
			Constructor con = actionClass.getConstructor(paramTypes);
			return con.newInstance(params);
	}

	
	/*******************************************************
	 * 
	 *
	 * 	               Common 
	 * 
	 ********************************************************/
	private static boolean isContributionMenu(Element node) {
		return isEuqal(Constants.MENU_CONTRIBUTION_NODE, node.getName());
	}


	private static boolean isActionMenu(Element node) {
		return isEuqal(Constants.MENU_ACTION_NODE, node.getName());
	}


	private static boolean hasSubMenu(Element node) {
		return isManagerMenu(node);
	}


	private static boolean isManagerMenu(Element node) {
		return isEuqal(Constants.MENU_MANAGER_NODE, node.getName());
	}


	private static boolean isEuqal(String expect, String value) {
		return (expect.equals(value)) ? true : false;
	}

	private static boolean isNull(Object obj) {
		return (obj == Constants.NULL_OBJECT) ? true : false;
	}
	
	public static boolean isEnable(String name){
		return ((name == null) || (name.equals("")) || !name.equalsIgnoreCase(Constants.MENUXML_BOOLEN_FALSE));
	}

}
