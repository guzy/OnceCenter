package oncecenter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import oncecenter.Constants;

import com.once.xenapi.Connection;
import com.once.xenapi.Console;
import com.once.xenapi.Host;
import com.once.xenapi.Network;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VBD;
import com.once.xenapi.VDI;
import com.once.xenapi.VIF;
import com.once.xenapi.VM;

public class VMUtil {

	//鍒涘缓涓肯板畬鏁寸殑铏氭嫙鏈猴紝鍖呮嫭缃戠粶鍜岀鐩�
	public static VM create(String vmName,long vcpu,long memory
			,VM newVm,Connection connection,Host host
			,boolean isShare,String selectedMediaUuid
			,List<DiskInfo> diskList){
		newVm = createVmVIF(vmName,vcpu,memory,newVm,connection,host);
		if(newVm==null)
			return null;
		if(!createVDIs(vmName,newVm,connection,host
				,isShare,selectedMediaUuid,diskList)){
			//澶勭悊鍑洪敊鎯呭喌
			try {
				newVm.destroy(connection, true);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			return null;
		}
		return newVm;
	}
	
	//鍒涘缓铏氭嫙鏈哄強铏氭嫙鏈虹綉缁�
	public static VM createVmVIF(String vmName,long vcpu,long memory
			,VM newVm,Connection connection,Host host){
		newVm = createVm(vmName,vcpu,memory,newVm,connection,host);
		if(newVm!=null){
			if(!createVIF(newVm,connection,1500,host,ConfigUtil.getNetwork())){
				try {
					newVm.destroy(connection, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}else{
			return null;
		}
		return newVm;
	}
	
	//鍒涘缓铏氭嫙鏈猴紙console鍖呮嫭鍦ㄥ唴锛�
	public static VM createVm(String vmName,long vcpu,long memory
			,VM newVm,Connection connection,Host host){
		try{
			VM.Record record = new VM.Record();
			record.VCPUsParams = new HashMap<String, String> ();
			record.nameLabel = vmName;
			record.HVMBootPolicy = "hvm";
			record.VCPUsMax = (long) vcpu;
			record.VCPUsAtStartup = (long) vcpu;
			record.memoryStaticMax = (long) memory * 1024 * 1024;
			record.memoryDynamicMax = (long) memory * 1024 * 1024;
			record.memoryDynamicMin = (long) 512 * 1024 * 1024;
			record.memoryStaticMin = (long) 0;
			record.actionsAfterCrash = Types.toOnCrashBehaviour("restart");
			record.actionsAfterReboot = Types.toOnNormalExit("restart");
			record.actionsAfterShutdown = Types.toOnNormalExit("destroy");
			record.platform = new HashMap<String, String>();
			record.platform.put("pae", "1");
			record.platform.put("boot", "cd");
			record.platform.put("localtime", "0");
			record.platform.put("acpi", "1");
			record.platform.put("usbdevice", "tablet");
			record.platform.put("serial", "pty");
			record.platform.put("usb", "1");
			record.platform.put("parallel", "none");
			record.platform.put("apic", "1");
			record.platform.put("xen_platform_pci", "1");
			
			newVm = VM.createOn(connection, record, host);
			
			Console.Record consoleRec = new Console.Record();
			consoleRec.protocol = Types.toConsoleProtocol("rfb");
			consoleRec.VM = newVm;
			consoleRec.otherConfig = new HashMap<String, String>();
			consoleRec.otherConfig.put("vnc", "1");
			consoleRec.otherConfig.put("sdl", "0");
			consoleRec.otherConfig.put("vncunused", "1");
			consoleRec.otherConfig.put("vnclisten", "0.0.0.0");

			Console.createOn(connection, consoleRec, host);

		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return newVm;
	}
	
	//涓鸿櫄鎷熸満鍒涘缓鎸囧畾鐨勭綉缁�
	public static boolean createVIF(VM newVm,Connection connection,long MTU,Host host,String network){
		
		try{
			VIF.Record vifRec = new VIF.Record();
			vifRec.VM = newVm;
			
			//杩欎釜鍦版柟鏄笉鏄簲璇ユ敼涓控嬶紵锛燂紵锛�
			if(network.equals(Constants.netName))
			{
				Set<Network> networkSet =  Network.getByNameLabel(connection, network);
				if(networkSet.iterator().hasNext())
					vifRec.network = networkSet.iterator().next();
			}
			
			vifRec.MTU = MTU;
			

			VIF.createOn(connection, vifRec, host);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//涓鸿櫄鎷熸満鍒涘缓瀹屾暣鐨勭鐩樻枃浠跺強鍏夌洏
	public static boolean createVDIs(String vmName,VM newVm
			,Connection connection,Host host
			,boolean isShare,String selectedMediaUuid
			,List<DiskInfo> diskList){
		ArrayList<VDI> vdiList = new ArrayList<VDI>();
		for(DiskInfo disk:diskList){
			VDI vdi = createVDI(vmName,newVm,connection,host,isShare,disk.size,disk.sr,disk.srType);
			if(vdi==null){
				for(VDI v:vdiList){
					try {
						v.destroy(connection);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
				return false;
			}else{
				vdiList.add(vdi);
			}
		}
		if(!createCD(connection,selectedMediaUuid,newVm,host)){
			for(VDI v:vdiList){
				try {
					v.destroy(connection);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return false;
		}
		return true;
	}
	
	//涓鸿櫄鎷熸満鍒涘缓鎸囧畾淇℃伅鐨刅DI
	public static VDI createVDI(String vmName,VM newVm
			,Connection connection,Host host
			,boolean isShare
			,long size,SR sr,String srType){
		VDI vdi1 ;
		try{
			VDI.Record vdiRec1 = new VDI.Record();
			vdiRec1.otherConfig = new HashMap<String, String>();
			vdiRec1.otherConfig.put("virtual_machine", vmName);
			vdiRec1.otherConfig.put("vm_uuid", newVm.toWireString());
			vdiRec1.virtualSize = size;
			vdiRec1.uuid = UUID.randomUUID().toString();
			vdiRec1.type = Types.toVdiType("user");
			if (isShare)
				vdiRec1.sharable = true;
			else
				vdiRec1.sharable = false;
			vdiRec1.nameLabel = vmName;
			vdiRec1.SR = sr;
			String sr_type = srType;
			if (sr_type.equals(TypeUtil.nfsZfsType))
				vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
			else if (sr_type.equals(TypeUtil.gpfsDiskType))
				vdiRec1.location = "file:"+vdiRec1.SR.getLocation(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
			else if (sr_type.equals(TypeUtil.localSrType))
				vdiRec1.location = "file:"+Constants.localsr+"/"+vdiRec1.uuid+".vhd";
			else if (sr_type.equals(TypeUtil.mfsDiskType)||sr_type.equals(TypeUtil.ocfs2DiskType))
				vdiRec1.location = "tap:aio:"+vdiRec1.SR.getLocation(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
			else
				vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(connection)+"/"+vdiRec1.uuid+".vhd";
			vdi1 = VDI.createOn(connection, vdiRec1, host);
			
			VBD.Record vbdRec = new VBD.Record();
			vbdRec.VM = newVm;
			vbdRec.VDI = vdi1;
			vbdRec.bootable = true;
//			vbdRec.device = "xvda";
			vbdRec.device = "hda";
			vbdRec.mode = Types.toVbdMode("rw");
			vbdRec.type = Types.toVbdType("Disk");
			
			VBD.createOn(connection, vbdRec, host);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return vdi1;
	}
	
	//涓鸿櫄鎷熸満鍒涘缓涓枯楀厜鐩�
	public static boolean createCD(Connection connection,String selectedMediaUuid
			,VM newVm,Host host){
		try{
			VDI vdi2 = VDI.getByUuid(connection, selectedMediaUuid);
			VBD.Record cdrom = new VBD.Record();
			cdrom.VM = newVm;
			cdrom.VDI = vdi2;
			cdrom.bootable = true;
			cdrom.type = Types.toVbdType("CD");
//			cdrom.device = "xvdc";
			cdrom.device = "hdc";

			VBD.createOn(connection, cdrom, host);	
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//涓鸿櫄鎷熸満鍒涘缓鍏夌氦鍗�
	public static boolean createFiber(Host host,String vmName,VM vm
			,SR sr,Connection c,String location){
		VDI.Record vdiRec1 = new VDI.Record();
		vdiRec1.otherConfig = new HashMap<String, String>();
		vdiRec1.otherConfig.put("virtual_machine", vmName);
		vdiRec1.otherConfig.put("vm_uuid", vm.toWireString());
		vdiRec1.virtualSize = (long) 20;
		vdiRec1.uuid = UUID.randomUUID().toString();
		vdiRec1.nameLabel = "win2003-64_2";
		vdiRec1.type = Types.toVdiType("user");
		vdiRec1.sharable = false;
		vdiRec1.SR = sr;
		
		vdiRec1.location = "phy:"+location;
		VDI vdi1 = null;
		try{
			vdi1 = VDI.createOn(c, vdiRec1, host);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}		 
				 
		VBD.Record vbdRec = new VBD.Record();
		vbdRec.VM = vm;
		vbdRec.VDI = vdi1;
		vbdRec.bootable = true;
		try{
			vbdRec.device = vm.getAvailableVbdDevice(c);
		}catch(Exception e){
			e.printStackTrace();
			try {
				vdi1.destroy(c);
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
			return false;
		}
		
		vbdRec.mode = Types.toVbdMode("rw");
		vbdRec.type = Types.toVbdType("Fiber");

		try {
			VBD.createOn(c, vbdRec, host);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				vdi1.destroy(c);
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
			return false;
		}
		return true;
	}
	
	public static class DiskInfo {
		public long size;
		public SR sr;
		public String srType;
		public DiskInfo(long size,SR sr,String srType){
			this.size = size;
			this.sr = sr;
			this.srType = srType;
		}
	}
	
	public static boolean AdjustCpuMemory(VM vm,long cpuNumber,long memoryValue,Connection conn){
		try{
			Long vcpuNumber = vm.getVCPUsMax(conn);
			if(vcpuNumber > new Long(cpuNumber))
			{
				//浠庡ぇ鍙樺皬
				vm.setVCPUsNumberLive(conn, new Long(cpuNumber));
				vm.setVCPUsMax(conn, new Long(cpuNumber));
				vm.setVCPUsAtStartup(conn, new Long(cpuNumber));
			}
			else if(vcpuNumber < new Long(cpuNumber))
			{
				//浠庡皬鍙樺ぇ
				vm.setVCPUsMax(conn, new Long(cpuNumber));
				vm.setVCPUsNumberLive(conn, new Long(cpuNumber));
				vm.setVCPUsAtStartup(conn, new Long(cpuNumber));
			}
			
			Long memory = vm.getMemoryStaticMax(conn);
			if(memory > new Long((long)memoryValue * 1024 * 1024 ))
			{
				//浠庡ぇ鍙樺皬
				vm.setMemoryDynamicMax(conn, new Long((long)memoryValue * 1024 * 1024));
				vm.setMemoryDynamicMin(conn, new Long((long)512 * 1024 * 1024));
				vm.setMemoryStaticMax(conn, new Long((long)memoryValue * 1024 * 1024 ));
			}
			else if(memory < new Long((long)memoryValue * 1024 * 1024 ))
			{
				//浠庡皬鍙樺ぇ
				vm.setMemoryStaticMax(conn, new Long((long)memoryValue * 1024 * 1024 ));
				vm.setMemoryDynamicMax(conn, new Long((long)memoryValue * 1024 * 1024));
				vm.setMemoryDynamicMin(conn, new Long((long)512 * 1024 * 1024));
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}