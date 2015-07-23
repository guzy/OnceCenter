package oncecenter.views.xenconnectiontreeview.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VM;
import com.once.xenapi.XenAPIObject;

public abstract class VMTreeObjectRoot extends VMTreeObject {

	protected String ipAddress;
	protected String username;
	protected String password;
	
	//private String vmGroupConfigPath;
	
	transient public Map<Host,VMTreeObjectHost> hostMap;
	transient public Map<VM,VMTreeObjectVM> vmMap;
	transient public Map<SR,VMTreeObjectSR> srMap;
	transient public Map<VM,VMTreeObjectTemplate> templateMap;
	
	transient public ArrayList<VMTreeObject> temporaryList = new ArrayList<VMTreeObject>();
	
	transient public Timer getRecordTimer;
	transient public Timer getPerformTimer;
	
	public ArrayList<VMTreeObjectHost> historyHosts;
	public ArrayList<VMTreeObjectVM> historyVMs;
	
	public VMTreeObjectRoot(String name, Connection connection,
			XenAPIObject apiObject) {
		super(name, connection, apiObject);
		hostMap = new HashMap<Host,VMTreeObjectHost>();
		vmMap = new HashMap<VM,VMTreeObjectVM>();
		srMap = new HashMap<SR,VMTreeObjectSR>();
		templateMap = new HashMap<VM,VMTreeObjectTemplate>();
	}

	@Override
	public void addChild(VMTreeObject object) {
		

	}
	
	public boolean isOn(VMTreeObjectVM vmObject){
		return vmObject.getRecord()==null?true:
			(vmObject.getRecord().powerState.equals(Types.VmPowerState.RUNNING)?true:false);
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
//	public VMTreeObjectRoot clone(){
//		VMTreeObjectRoot root = null;
//		if(this instanceof VMTreeObjectHost){
//			root = new VMTreeObjectHost(getName(), getConnection(),
//				getApiObject(),((VMTreeObjectHost)this).getRecord());
//		}else{
//			root = new VMTreeObjectPool(getName(), getConnection(),
//					getApiObject(),((VMTreeObjectPool)this).getRecord());
//		}
//		return root;
//	}

//	public String getVmGroupConfigPath() {
//		return vmGroupConfigPath;
//	}
//
//	public void setVmGroupConfigPath(String vmGroupConfigPath) {
//		this.vmGroupConfigPath = vmGroupConfigPath;
//	}

}
