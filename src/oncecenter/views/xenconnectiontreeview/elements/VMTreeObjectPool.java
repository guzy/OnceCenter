package oncecenter.views.xenconnectiontreeview.elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import oncecenter.Constants;
import oncecenter.maintabs.EventTab;
import oncecenter.maintabs.pool.HostListTab;
import oncecenter.maintabs.pool.PoolSummaryTab;
import oncecenter.util.FileUtil;
import oncecenter.util.TypeUtil;
import oncecenter.views.grouptreeview.VMTreeVMGroupView;
import oncecenter.views.grouptreeview.elements.VMTreeObjectHostinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectPoolinGroup;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VM;
import com.once.xenapi.XenAPIObject;

public class VMTreeObjectPool extends VMTreeObjectRoot{
	
	transient private PoolSummaryTab poolItem;
	transient private HostListTab hostListItem;
	transient private EventTab logItem;
	transient private Pool.Record record;
	
	//transient private VMTreeObjectPoolinGroup shadowPool;
	
	public VMTreeObjectPool(String name, Connection connection,
			XenAPIObject apiObject, Pool.Record record) {
		super(name, connection, apiObject);
		this.setRecord(record);
	}
	
	@Override
	public void addChild(VMTreeObject object) {
		if(object==null)
			return;
		if(object instanceof VMTreeObjectHost){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectVM||o instanceof VMTreeObjectSR
						||o instanceof VMTreeObjectTemplate)
					break;
			}
			children.add(i, object);
			if(hostMap==null){
				hostMap = new HashMap<Host,VMTreeObjectHost>();
			}
			VMTreeObjectHost hostObject = (VMTreeObjectHost)object;
			hostMap.put((Host)object.getApiObject(), hostObject);
			if(vmMap==null){
				vmMap = new HashMap<VM,VMTreeObjectVM>();
			}
			for(VM vm:hostObject.vmMap.keySet()){
				VMTreeObjectVM vmObject = hostObject.vmMap.get(vm);
				VM.Record vmRecord = (VM.Record)vmObject.getRecord();
				if(this.vmMap.containsKey(vm)){
					System.out.println("delete vm : "+vmObject);
					vmObject.getParent().getChildrenList().remove(vmObject);
					Constants.treeView.getViewer().remove(vmObject);
				}else{
					if(!vmRecord.powerState.equals(Types.VmPowerState.RUNNING)){
						vmObject.getParent().getChildrenList().remove(vmObject);
						this.addChild(vmObject);
					}
					this.vmMap.put(vm, vmObject);
				}
			}
			for(VM template:hostObject.templateMap.keySet()){
				VMTreeObjectTemplate templateObject = hostObject.templateMap.get(template);
				templateObject.getParent().getChildrenList().remove(templateObject);
				if(!this.templateMap.containsKey(template)){
					this.addChild(templateObject);
					this.templateMap.put(template, templateObject);
				}else{
					Constants.treeView.getViewer().remove(templateObject);
				}
			}
			for(SR sr:hostObject.srMap.keySet()){
				VMTreeObjectSR srObject = hostObject.srMap.get(sr);
				if(!this.srMap.containsKey(sr)){
					if(!srObject.getSrType().equals(TypeUtil.localSrType)){
						srObject.getParent().getChildrenList().remove(srObject);
						this.addChild(srObject);
					}
					this.srMap.put(sr, srObject);
				}else{
					srObject.getParent().getChildrenList().remove(srObject);
					Constants.treeView.getViewer().remove(srObject);
				}
			}
//			if(hostObject.vmMap!=null){
//				vmMap.putAll(hostObject.vmMap);
//			}
//			
			object.setParent(this);
			return;
		}else if(object instanceof VMTreeObjectVM){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectVM&&o.getName().compareToIgnoreCase(object.getName())>0||o instanceof VMTreeObjectSR ||o instanceof VMTreeObjectTemplate)
					break;
			}
			children.add(i, object);
			if(vmMap==null){
				vmMap = new HashMap<VM,VMTreeObjectVM>();
			}
			vmMap.put((VM)object.getApiObject(), (VMTreeObjectVM)object);
			object.setParent(this);
			VMTreeVMGroupView.addVM((VMTreeObjectVM)object);
			return;
		}else if(object instanceof VMTreeObjectSR){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectSR&&o.getName().compareToIgnoreCase(object.getName())>0||o instanceof VMTreeObjectTemplate)
					break;
			}
			children.add(i, object);
			if(srMap==null){
				srMap = new HashMap<SR,VMTreeObjectSR>();
			}
			srMap.put((SR)object.getApiObject(), (VMTreeObjectSR)object);
			object.setParent(this);
			return;
		}else if(object instanceof VMTreeObjectTemplate){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectTemplate&&o.getName().compareToIgnoreCase(object.getName())>0)
					break;
			}
			children.add(i, object);
			if(templateMap==null){
				templateMap = new HashMap<VM,VMTreeObjectTemplate>();
			}
			templateMap.put((VM)object.getApiObject(), (VMTreeObjectTemplate)object);
			object.setParent(this);
			return;
		}else{
			children.add(object);
			object.setParent(this);
			return;
		}
	}

	@Override
	public void createFolder(Composite parent) {
		
		super.createFolder(parent);
		poolItem = new PoolSummaryTab(folder, SWT.NONE,0,this);
		hostListItem = new HostListTab(folder, SWT.NONE,1,this);
		logItem = new EventTab(folder,SWT.NONE,2,this);
		folder.setSelection(0);
	}

	public Pool.Record getRecord() {
		return record;
	}

	public boolean setRecord(Pool.Record record) {
		if(record!=null){
			if(this.record!=null&&this.record.timestamp>record.timestamp)
				return false;
			this.record = record;
			this.setName(record.nameLabel);
			this.setUuid(record.uuid);
		}
		return true;
	}

//	public VMTreeObjectPoolinGroup getShadowPool() {
//		return shadowPool;
//	}
//
//	public void setShadowPool(VMTreeObjectPoolinGroup shadowPool) {
//		this.shadowPool = shadowPool;
//	}
	
}
