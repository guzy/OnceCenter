package oncecenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import oncecenter.action.edit.PageBookViewState;
import oncecenter.util.FileUtil;
import oncecenter.util.snapshotstrategy.SnapshotStrategy;
import oncecenter.views.detailview.VMTreePageBookView;
import oncecenter.views.grouptreeview.VMTreeVMGroupView;
import oncecenter.views.logview.LogView;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectConnectionRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;

import org.apache.xmlrpc.XmlRpcException;
import org.dom4j.Document;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.once.xenapi.Connection;
import com.once.xenapi.Pool;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class Constants {
	public static Vector<Job> jobs = new Vector<Job>();
	// title and name
//	public final static String WORKBENCH_WINDOW_NAME = "网驰平台";
	
	public final static String WORKBENCH_WINDOW_NAME = "博纳讯动服务器虚拟化软件";
	
	public final static String DEFAULT_CONNECTION_TREE_ROOT_NAME = "Xen";
	
	public final static String VM_GROUP_TREE_ROOT_NAME = "虚拟机组";
	
	public final static String VM_GROUP_DEFAULT_NAME = "默认组";
	
	//public final static String HALTED_VM_GROUP_DEFAULT_NAME = "关闭的虚拟机";
	
	public final static String TEMPLATE_GROUP_DEFAULT_NAME = "模板";

	// directory
	public final static String DEFAULT_LOG_ROOT = "log";
	
	public final static String DEFAULT_WORKSPACE_ROOT = "workspace";
	
	public final static String DEFAULT_CONFIG_ROOT = "conf";
	
	public final static String DEFAULT_DATA_ROOT = "data";
	
	
	// file list
	public final static String DEFAULT_VMs_INFO_CONFIG_FILE = "rootInfo.out";
	
	public final static String DEFAULT_SR_TYPES_CONFIG_FILE = "sr.types";
	
	public final static String DEFAULT_MENU_CONFIG_FILE = "menubar.xml";
	
	public final static String DEFAULT_TOOLBAR_CONFIG_FILE = "toolbar.xml";
	
	public final static String DEFAULT_VM_GROUP_CONFIG_FILE = "groupConfig.xml";
	
	public final static String DEFAULT_SNAPSHOTSTRATEGY_CONFIG_FILE = "snapshotInfo.out";
	// menu 
	public static String MENU_MANAGER_NODE = "MenuManager";
	
	public static String MENU_ACTION_NODE = "Action";
	
	public static String MENU_CONTRIBUTION_NODE = "Contribution";
	
	// menu
	public static String MENUXML_ELEMENT_NAME = "name";
	
	public static String MENUXML_ELEMENT_CLASS = "class";
	
	public static String MENUXML_ELEMENT_ENABLE = "enable";
	
	public static String MENUXML_ELEMENT_TYPE = "type";
	
	public static String MENUXML_ELEMENT_TEXT = "text";
	
	public static String MENUXML_ELEMENT_IMAGE = "image";
	
	public static String MENUXML_ELEMENT_DIABLEDIMAGE = "disabledImage";
	
	public static String MENUXML_BOOLEN_FALSE = "false";
	
	public static String MENUXML_BOOLEN_TRUE = "true";
	
	public static IMenuManager parentMenu;
	
	public static enum ActionType{all,pool,host,runningvm,haltedvm,template,sr};
	
	public static Map<IAction,List<ActionType>> menuTypeMap = new HashMap<IAction,List<ActionType>>();
	
	
	// variable
	public static VMTreeObjectConnectionRoot CONNECTIONS_TREE;
	
	public static VMTreeObjectDefault VMGROUPS_TREE;
	
	// null
	public final static Document NULL_DOCUMENT = null;
	
	public final static Object NULL_OBJECT = null;
	//====================================
	public static String netName="eth0";
	public static boolean displayStatusData = false;
	public static String performpath="/opt/xen/performance/15sec.xml";
	public static String performname = File.separator+"15sec.xml";
	//public static String srpath = "/home/share";
	public static String srpath = "/var/run/sr_mount";
	public static String localsr = "/home/local_sr";
	//public static ArrayList<String> srList = new ArrayList<String>();
	public static enum Permission{admin, user};
	public static Permission permission=Permission.admin;
	public static String username;
	public static VMTreeView treeView;
	public static VMTreeVMGroupView groupView;
	public static LogView logView;
	public static IToolBarManager toolBar;
	public static Stack<PageBookViewState> BackStack = new Stack<PageBookViewState>();
	public static Stack<PageBookViewState> NextStack = new Stack<PageBookViewState>();
	public static VMTreePageBookView pageBookView;
	public static Set<String> historyServer = new HashSet<String>();
	
	public static double maxMemoryTotal=0;
	
	public static String p2vDir = "/root/p2v";
	
	public static String vmToolPath = "VmTools.iso";
	
	public static String p2vwindowsvhdPath = "/mypool";
	
	//public static String uploadisoPath = "/home/iso";
	
	public static String exportDir = "/root/export";
	
	//提示信息
	public static String hostRecordError = "获取服务器信息失败，列表可能显示不完整。";
	public static String vmRecordError = "获取虚拟机信息失败，列表可能显示不完整。";
	public static String srRecordError = "获取存储信息失败，列表可能显示不完整。";
	public static String poolRecordError = "获取资源池信息失败，列表可能显示不完整。";
	public static String srShowMountError = "以下目录没有开启NFS服务：";
	public static String srMountError = "挂载以下目录失败，请检查网络：";
	public static String srMountAllError = "挂载目录失败，请检查网络";
	public static String success = "登录成功";
	
	//异常
	public static String srShowMountException = "SHOWMOUNT_FAILED";
	public static String srMountException = "MOUNT_FAILED";
	
	//按钮文字
	public static String backButtonText = "上一步";
	public static String nextButtonText = "下一步";
	public static String finishButtonText = "完成";
	public static String cancelButtonText = "取消";
	
	public static String okButtonText = "确定";
	
	//snapshot strategy
	public static Map<String,SnapshotStrategy> strategyList = new HashMap<String,SnapshotStrategy>();
	
	public static Pool getPool(Connection conn) throws BadServerResponse, XenAPIException,XmlRpcException {
		Set<Pool> pools = Pool.getAll(conn);
		for(Iterator<Pool> iter = pools.iterator(); iter.hasNext();) {
			Pool thisPool = iter.next();
			if(thisPool.getNameLabel(conn) == null || "".equals(thisPool.getNameLabel(conn).trim())) {
				continue;
			}
			return thisPool;
		}
		return null;
	}
	 
	 public static void recordServer(String ip){
		 int size = Constants.historyServer.size();
		 Constants.historyServer.add(ip);
		 if(Constants.historyServer.size()>size){
			 try {
		            FileWriter writer = new FileWriter(FileUtil.getXenCenterRoot() + "historyServers", true);
		            writer.write(ip+",");
		            writer.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		 }
	 }
	 
	 public static void getHistory(){
		 File file = new File(FileUtil.getXenCenterRoot() + "historyServers");
		 if(!file.exists()){
			 try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		 }
		 Reader reader = null;
		 try {
	            reader = new InputStreamReader(new FileInputStream(file));
	            int tempchar;
	            String ip = "";
	            while ((tempchar = reader.read()) != -1) {
	                if (((char) tempchar) == ',') {
	                   Constants.historyServer.add(ip);
	                   ip="";
	                }else{
	                	ip+=(char) tempchar;
	                }
	            }
	            reader.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	 }
	 
	 public static VMTreeObjectHost getSuitableHost(VMTreeObjectPool pool){
		 VMTreeObjectHost host=null;
		 double grade=0;
		for(VMTreeObjectHost h:pool.hostMap.values()){
			//if(h.grade==0){
				h.getGrade();
			//}
			if(h.grade>grade){
				grade=h.grade;
				host=h;
			}
		}
		return host;
	} 
	 
	  
	 public static void addHosttoPool(VMTreeObjectHost hostObject, VMTreeObjectPool poolObject){
		 	if(hostObject.getPerformTimer != null)
				hostObject.getPerformTimer.cancel();
			if(hostObject.getRecordTimer != null)
				hostObject.getRecordTimer.cancel();
			Constants.CONNECTIONS_TREE.getChildrenList().remove(hostObject);
			Connection conn = poolObject.getConnection();
		 	setConnection(conn,hostObject);
		 	poolObject.addChild(hostObject);
		 	ISelection selection = new StructuredSelection(new Object[]{poolObject});
			if(Constants.treeView.getViewer() != null && Constants.treeView != null )
				Constants.pageBookView.selectionChanged(Constants.treeView, selection);
			Constants.treeView.getViewer().setSelection(selection);
		}
	 
	 public static void setConnection(Connection conn,VMTreeObject treeObject){
		 treeObject.setConnection(conn);
		 for(VMTreeObject object:treeObject.getChildrenList()){
			 setConnection(conn,object);
		 }
	 }
}
