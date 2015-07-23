package test;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import oncecenter.Constants;
import oncecenter.util.performance.drawchart.DrawVmPerformance;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.apache.xmlrpc.XmlRpcException;

import vncviewer.VncViewer;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Network;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.SR.Record;
import com.once.xenapi.Types;
import com.once.xenapi.VDI;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VIF;
import com.once.xenapi.VM;

public class GZYTest {

	public static void testJava(){
		double a = 11.81378;
		double b = Math.floor(a);
		System.out.println(b);
		System.out.println((long)b);
	}
	
	public static void testSROperation(Connection c){
		try {
			Collection<Record> sr_records = SR.getAllRecords(c).values();
			for(Record sr_record : sr_records){
				System.out.println(sr_record.uuid);
			}
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	public static void testPoolOperation(Connection c){
		String pool_uuid = "007fc8d0-cf73-f824-f82d-f76fe4bbbd9f";
		Pool pool;
		try {
			pool = Pool.getByUuid(c, pool_uuid);
			com.once.xenapi.Pool.Record record = pool.getRecord(c);
			//defaultSR属性已经废弃
			System.out.println(record.defaultSR);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	public static void testVDIOperation(Connection c, VM vm){
		long size_gb = 1;
		String vdi_uuid = "g1";
		try {
			VDI vdi = VDI.createDataDisk(c, vdi_uuid, size_gb);
			if(vdi!= null){
				VM.createDataVBD(c, vm.getUuid(c), vdi_uuid);//将硬盘绑定到虚拟机
			}
			VM.deleteDataVBD(c, vm.getUuid(c), "g1");
			VDI.deleteDataDisk(c, "g1");
			Set<VDI> vdiSet = VDI.getByVM(c, vm);
			Iterator<VDI> vdiIterator = vdiSet.iterator();
			while(vdiIterator.hasNext()) {
				System.out.println(vdiIterator.next().getUuid(c));
			}	
			String source = "vm:5b2569c6-d707-c372-2329-b19e3eade7f5:vbd_xvda_write";
			String uuid = DrawVmPerformance.handleUUID(source);
			String part = DrawVmPerformance.handlePart(source);
			System.out.println(part);
			Date date = new Date(Long.parseLong("1411971187608"));
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String t = df.format(date);
	        System.out.println(t);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	public static void testVIFOperation(Connection c){
//		VIF vif = VIF.getByUuid(c, "c889995d-f0b4-e1af-517a-7474acfffc79");
//		VM vm_ref = VM.getByUuid(c, "84e33bc0-f8d0-529f-1e84-248fd8ddeabc");
//		String phyNetwork = "my net";
//		vif.set_physical_network(c, vm_ref, phyNetwork);
		try {
			for (Network networks : Network.getAll(c)){
				System.out.println(networks.getRecord(c).nameLabel);
				System.out.println(networks.getRecord(c).uuid);
			}
			VM vm = VM.getByUuid(c, "4d6aada6-4f6e-fc2d-504c-f5efeb499a88");
			for(VIF vif:vm.getVIFs(c)){
				VIF.Record record = vm.getVIFRecord(c, vif);
				String tag = vm.getTag(c, vif);
				System.out.println("VLAN号是 = " + tag);
				System.out.println(record.network.getNameLabel(c));
				String phyNetwork = record.network.getNameLabel(c);
//				vif.set_physical_network(c, vm, phyNetwork);
				System.out.println(record.network.getNameLabel(c));
			}
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	public static void testHostOperation(Connection c){
		String UUID_36 = "528859c4-92ad-406f-404b-23c33d353034";
		String UUID_12 = "f7ab0c4a-f6fa-493b-65f4-95e9b2a6e860";
//			Host host = Host.getByUuid(c, UUID_36);
//			System.out.println(host.getNameLabel(c));
//			System.out.println(Host.getHA(c, host));
//	        host.stopPerformanceXML(c);
//	        host.startPerformanceXML(c);
			Host host = Types.toHost(UUID_12);
//			String license = host.genLicense(c, "300");
//			System.out.println("license = " + license);
//			System.out.println(host.verifyLicense(c, license));
	}
	
	public static void testVMOperation(Connection c){
		String nameLabel = "beyond_cloud_1";
		try {
			Set<VM> vmSet = VM.getByNameLabel(c, nameLabel);
			Iterator<VM> vmIterator = vmSet.iterator();
			VM vm = null;
			while(vmIterator.hasNext()) {
				vm = vmIterator.next();
				VM.Record vm_record = vm.getRecord(c);
				String powerState = vm.getPowerState(c).toString();
				System.out.println("虚拟机" + vm.getNameLabel(c) + "执行操作前的运行状态是：" + powerState);
				vm.cleanShutdown(c);
				System.out.println("虚拟机" + vm.getNameLabel(c) + "执行操作后的运行状态是：" + powerState);
			}
			String uuid = "20e1ddc4-61ab-0ee8-91ec-455a87f981dc";
			Map<VM, VM.Record> vmRecords = VM.getAllRecords(c);
			System.out.println(vmRecords.size());
			VM.Record vm_record = vm.getRecord(c);
			String powerState = vm.getPowerState(c).toString();
			System.out.println("虚拟机" + vm.getNameLabel(c) + "执行操作前的运行状态是：" + powerState);
			vm.destroy(c, true);
			vm.start(c, true, true);
			vm.pause(c);
			vm.unpause(c);
			vm.cleanShutdown(c);
			vm.revert(c);
			vm.resume(c, true);
			vm.suspend(c);
			vm.hardReboot(c);
			vm.hardShutdown(c);
			vm.cleanReboot(c);
			System.out.println("虚拟机" + vm.getNameLabel(c) + "执行操作后的运行状态是：" + powerState);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	public static void testVNC(Connection c, String vm_uuid, String hostIp){
		VncViewer localVncViewer = new VncViewer();
		String [] args=new String [4];
		 args[0]="HOST";
		 args[1]=hostIp;
		 args[2]="PORT";
		 try{
			 VM vm_instance= VM.getByUuid(c, vm_uuid);
			 String location =  vm_instance.getVNCLocation(c);
			 args[3] = location.substring(location.indexOf(":")+1);
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 localVncViewer.mainArgs=args;
		 localVncViewer.inAnApplet = false;
		 localVncViewer.inSeparateFrame = true;
		 localVncViewer.init();
		 localVncViewer.start();
	}
	
	public static void testFastCreateTime(Connection c){
		String template_uuid = "ce155fce-3273-c5a0-ba3e-56ac78b3ed38";
		String host_uuid = "f7ab0c4a-f6fa-493b-65f4-95e9b2a6e860";
		try {
			Host host = Host.getByUuid(c, host_uuid);
			VM temp = VM.getByUuid(c, template_uuid);
			for(int i=0;i<5;i++){
				long time1 = System.currentTimeMillis();
				VM newVm = temp.createClone(c, "gzy" + i);
				newVm.startOn(c, host, false, true);
				long time2 = (System.currentTimeMillis() - time1)/1000;
				System.out.println("创建并启动" + newVm.getNameLabel(c) + "耗时 " + time2 + "秒");
			}
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) throws BadServerResponse, XenAPIException, XmlRpcException, MalformedURLException {
		Connection c = new Connection("http://133.133.131.11:9363", "root", "123456");
		Set<VM> vmSet = VM.getAll(c);
		for(VM singleVM : vmSet){
			System.out.println(singleVM.getNameLabel(c));
		}
//		testVNC(c, "2728064c-6345-4a57-acda-6abefa8cc6a3", "133.133.135.12");
//		testFastCreateTime(c);
//		testHostOperation(c);
	}
}
