package oncecenter.views.xenconnectiontreeview.elements;

import oncecenter.maintabs.template.TempSummaryTab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.once.xenapi.Connection;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;
import com.once.xenapi.XenAPIObject;
import com.once.xenapi.Types.Record;

public class VMTreeObjectTemplate extends VMTreeObject {

	transient private TempSummaryTab tempSummaryItem;
	transient private VMTreeObjectSR storageObject;
	transient private VDI vdi;
	transient private VM.Record record;
	
	public VMTreeObjectTemplate(String name, Connection connection,
			XenAPIObject apiObject, VM.Record record) {
		super(name, connection, apiObject);
		this.setRecord(record);
		
	}

	@Override
	public void addChild(VMTreeObject object) {
		
		if(object==null)
			return;
		children.add(object);
		object.setParent(this);
		return;
	}

	@Override
	public void createFolder(Composite parent) {
		
		super.createFolder(parent);
		tempSummaryItem = new TempSummaryTab(folder, SWT.NONE,0,this);
		folder.setSelection(0);
	}

	public VMTreeObjectSR getStorageObject() {
		return storageObject;
	}

	public void setStorageObject(VMTreeObjectSR storageObject) {
		this.storageObject = storageObject;
	}

	public VDI getVdi() {
		return vdi;
	}

	public void setVdi(VDI vdi) {
		this.vdi = vdi;
	}

	public VM.Record getRecord() {
		return record;
	}

	public boolean setRecord(VM.Record record) {
		if(record!=null){
			if(this.record!=null&&this.record.timestamp>record.timestamp)
				return false;
			this.record = record;
			this.setName(record.nameLabel);
			this.setUuid(record.uuid);
		}
		return true;
	}

}
