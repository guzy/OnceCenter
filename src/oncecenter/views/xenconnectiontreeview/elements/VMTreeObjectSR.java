package oncecenter.views.xenconnectiontreeview.elements;

import oncecenter.maintabs.sr.SRSummaryTab;
import oncecenter.maintabs.sr.VDIListTab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.XenAPIObject;

public class VMTreeObjectSR extends VMTreeObject {
	
	private String srType;
	transient private SR.Record record;
	transient private SRSummaryTab srItem;
	transient private VDIListTab vdiItem;

	public VMTreeObjectSR(String name, Connection connection,
			XenAPIObject apiObject, SR.Record record) {
		super(name, connection, apiObject);
		this.setRecord(record);
		if(record!=null)
			this.srType = record.type;
		
	}
	
	@Override
	public void addChild(VMTreeObject object) {
		
		if(object==null)
			return;
		children.add(object);
		object.setParent(this);
		return;
	}
	
	public String getSrType() {
		return srType;
	}
	public void setSrType(String srType) {
		this.srType = srType;
	}

	@Override
	public void createFolder(Composite parent) {
		
		super.createFolder(parent);
		srItem = new SRSummaryTab(folder, SWT.NONE,0,this);
		vdiItem = new VDIListTab(folder, SWT.NONE,1,this);
		folder.setSelection(0);
	}
	
	public SR.Record getRecord() {
		return record;
	}

	public boolean setRecord(SR.Record record) {
		if(record!=null){
			if(this.record!=null&&this.record.timestamp>record.timestamp)
				return false;
			this.record = record;
			//System.out.println(record.timestamp);
			this.setName(record.nameLabel);
			this.setUuid(record.uuid);
			this.setSrType(record.type);
		}
		return true;
	}

	public SRSummaryTab getSrItem() {
		return srItem;
	}

	public VDIListTab getVdiItem() {
		return vdiItem;
	}

}
