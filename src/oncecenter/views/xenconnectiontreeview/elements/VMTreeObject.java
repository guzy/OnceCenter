package oncecenter.views.xenconnectiontreeview.elements;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.Vector;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import oncecenter.action.edit.PageBookViewState;
import oncecenter.maintabs.OnceTabItem;
import oncecenter.views.grouptreeview.elements.VMTreeObjectHostinGroup;
import oncecenter.views.logview.VMEvent;

import com.once.xenapi.Connection;
import com.once.xenapi.XenAPIObject;

public abstract class VMTreeObject implements Serializable{
	//节点信息
	protected String name;
	protected VMTreeObject parent;
	protected ArrayList<VMTreeObject> children;
	transient public ArrayList<Timer> timerList = new ArrayList<Timer>();
	transient public Timer cancelTimer;

	//对应的mainview编号
	transient protected int consoleID;
	//日志信息
	public Vector<VMEvent> events=new Vector<VMEvent>();

	public static enum ItemState{ able, unable, changing};
	public ItemState itemState;
	
	//对应的xenObject信息
	protected String uuid;
	
	transient protected Connection connection;
	transient protected XenAPIObject apiObject;
	transient protected CTabFolder folder;
	transient public ArrayList<OnceTabItem> itemList = new ArrayList<OnceTabItem>();
	
	transient private VMTreeObject shadowObject;
	
	public VMTreeObject(String name){
		this.name=name;
		children=new ArrayList<VMTreeObject>();
		itemState=ItemState.able;
	}
	
	public VMTreeObject(String name,Connection connection,XenAPIObject apiObject){
		this.name=name;
		children=new ArrayList<VMTreeObject>();
		this.connection=connection;
		this.apiObject=apiObject;
		itemState=ItemState.able;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VMTreeObject getParent() {
		return parent;
	}

	public void setParent(VMTreeObject parent) {
		this.parent = parent;
	}

	public int getConsoleID() {
		return consoleID;
	}

	public void setConsoleID(int consoleID) {
		this.consoleID = consoleID;
	}

	public Vector<VMEvent> getEvents() {
		return events;
	}

	public void setEvents(Vector<VMEvent> events) {
		this.events = events;
	}

	public ItemState getItemState() {
		return itemState;
	}

	public void setItemState(ItemState itemState) {
		this.itemState = itemState;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public XenAPIObject getApiObject() {
		return apiObject;
	}

	public void setApiObject(XenAPIObject apiObject) {
		this.apiObject = apiObject;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public VMTreeObject[] getChildren() {
		return (VMTreeObject [])children.toArray(new VMTreeObject[children.size()]);
	}

	public ArrayList<VMTreeObject> getChildrenList() {
		return children;
	}
	
	public void setChildren(ArrayList<VMTreeObject> children) {
		this.children = children;
	}
	
	public CTabFolder getFolder() {
		return folder;
	}

	public void setFolder(CTabFolder folder) {
		this.folder = folder;
	}

	public abstract void addChild(VMTreeObject object);
		
	public void createFolder(Composite parent){
		folder = new CTabFolder(parent, SWT.NONE);
		folder.setTabHeight(20);
		folder.setLayout(new FillLayout());
		folder.setMaximizeVisible(true);
		folder.setMinimizeVisible(true);
		folder.addSelectionListener(new SelectionListener(){

			@Override 
			public void widgetDefaultSelected(SelectionEvent e) {
				
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				OnceTabItem selectedItem=(OnceTabItem)e.item;
				if(selectedItem.composite!=null){
					selectedItem.composite.layout();
				}else{
					selectedItem.Init();
				}
				
				PageBookViewState.addState();
			}
			
		});
	}

	public VMTreeObject getShadowObject() {
		return shadowObject;
	}

	public void setShadowObject(VMTreeObject shadowObject) {
		this.shadowObject = shadowObject;
	}
	
	public VMTreeObjectRoot getRoot(){
		if(getParent().getParent() == null || getParent().getParent() instanceof VMTreeObjectDefault){
			return (VMTreeObjectRoot)getParent();
		}else{
			return (VMTreeObjectRoot)getParent().getParent();
		}
	}
}
