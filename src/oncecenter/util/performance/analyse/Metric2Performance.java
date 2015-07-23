package oncecenter.util.performance.analyse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.once.xenapi.Connection;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;

import oncecenter.util.MathUtil;
import oncecenter.util.performance.PerformAnalyseUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM.Disk;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM.ResourceTypes;

public class Metric2Performance {
	
	/**
	 * Analyze the host performance and change to the list.
	 * @param host
	 */
	public static void analyHostPerformance(VMTreeObjectHost host) {
		if(host.getMetrics()==null||host.getMetrics().size()==0)
			return;
		for(Map.Entry<String, List<String>> e: host.getMetrics().entrySet()) {
			
			//get the cpu information and change to cpu usage list
			if(PerformAnalyseUtil.handleUUID(e.getKey()).equals(host.getUuid())
        			&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()),ConstCharacter.CPU)){
				double[] allcpuofone = PerformAnalyseUtil.toDoubleArray(e.getValue());
				host.getCpuUsageList().put(PerformAnalyseUtil.getCPUNum(e.getKey()), allcpuofone[allcpuofone.length-1]*100);
			}
			
			//get the memory percent and value
			if(PerformAnalyseUtil.handleUUID(e.getKey()).equals(host.getUuid())
        			&& PerformAnalyseUtil.handlePart(e.getKey()) .equals(ConstCharacter.FREEMEMORY)){
				double[] freeMemorys = PerformAnalyseUtil.toDoubleArray(e.getValue());
				host.setMemoryUsageValue(host.getMemoryTotalValue()-freeMemorys[freeMemorys.length-1]/1024.0);
				host.setMemoryUsagePercent(host.getMemoryUsageValue()/host.getMemoryTotalValue()*100.0);
			}
			
			//get the line data of pif send
        	if(PerformAnalyseUtil.handleUUID(e.getKey()).equals(host.getUuid())
        			&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()),ConstCharacter.PIF) 
        			&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()),ConstCharacter.TX)){
        		double[] netSend = PerformAnalyseUtil.toDoubleArray(e.getValue());
        		String s = PerformAnalyseUtil.handlePart(e.getKey());
        		host.getwNetList().put(s.substring(s.indexOf(ConstCharacter.ETH), s.indexOf(ConstCharacter.UNDERSCORE+ConstCharacter.TX)), netSend[netSend.length-1]);
        		host.setwNet(host.getwNet()+netSend[netSend.length-1]);
        	}
        	
        	//get the line data of pif receive
        	if(PerformAnalyseUtil.handleUUID(e.getKey()).equals(host.getUuid())
        			&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()),ConstCharacter.PIF) 
        			&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()),ConstCharacter.RX)){
        		double[] netRev = PerformAnalyseUtil.toDoubleArray(e.getValue());
        		String s = PerformAnalyseUtil.handlePart(e.getKey());
        		host.getrNetList().put(s.substring(s.indexOf(ConstCharacter.ETH), s.indexOf(ConstCharacter.UNDERSCORE+ConstCharacter.RX)), netRev[netRev.length-1]);
        		host.setrNet(host.getrwNet()+netRev[netRev.length-1]);
        	}   	
		}
		
		if(host.getCpuUsageList().size()>0){
			host.setCpuUsagePercent(0);
			for(int i=0;i<host.getCpuUsageList().size();i++){
				host.setCpuUsagePercent(host.getCpuUsagePercent()+host.getCpuUsageList().get(i));
			}
			host.setCpuUsagePercent(host.getCpuUsagePercent()/host.getCpuUsageList().size());
		}
		
		//时间戳
		host.performTimeStamp = null;
		host.performTimeStamp = new Date();
	}
	
	/**
	 * Analyze the virtual machine performance and change to the list.
	 * @param vm
	 */
	public static void analyVMPerformance(VMTreeObjectVM vm) {
		
		if(vm.returnMetric()==null||vm.returnMetric().size()==0){
//			System.out.println("metricsTimelines is null " + vm.getName());
			return;
		}	
		
		for (Entry<String, List<String>> e : vm.returnMetric().entrySet()) {
			// get the cpu information
			if (PerformAnalyseUtil.handleUUID(e.getKey()).equals(vm.getUuid())
					&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()), ConstCharacter.CPU)) {
				double[] allcpuofone = PerformAnalyseUtil.toDoubleArray(e.getValue());
//				System.out.println("*********************");
//				System.out.println("CPU使用率是 = " + allcpuofone[allcpuofone.length-1] * 100);
				vm.getCpuUsageList().put(PerformAnalyseUtil.getCPUNum(e.getKey()), allcpuofone[allcpuofone.length-1] * 100);
			}

			// get the memory information of total
			if(PerformAnalyseUtil.handleUUID(e.getKey()).equals(vm.getUuid())
        			&& PerformAnalyseUtil.handlePart(e.getKey()) .equals(ConstCharacter.MEMORY)){
        		List<String> totalMemorys = e.getValue();
//        		System.out.println("内存总大小是 = " + findMaxTotalMemory(totalMemorys)/1024.0);
        		vm.setMemoryTotalValue(findMaxTotalMemory(totalMemorys)/1024.0);
            } 	
			
			//get the memory information of usage
			if (PerformAnalyseUtil.handleUUID(e.getKey()).equals(vm.getUuid())
					&& PerformAnalyseUtil.handlePart(e.getKey()).equals(ConstCharacter.INTERNALMEMORY)) {
				double[] freeMemorys = PerformAnalyseUtil.toDoubleArray(e.getValue());
//				System.out.println("空闲内存大小是 = " + freeMemorys[freeMemorys.length-1]);
				vm.setMemoryUsagePercent((100 - freeMemorys[freeMemorys.length-1]));
				vm.setMemoryUsageValue(vm.getMemoryUsagePercent()*vm.getMemoryTotalValue()/100.0);
			}

			// get the line data of vif send
			if (PerformAnalyseUtil.handleUUID(e.getKey()).equals(vm.getUuid())
					&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()), ConstCharacter.VIF)
					&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()), ConstCharacter.TX)) {
				double[] netSend = PerformAnalyseUtil.toDoubleArray(e.getValue());
//				System.out.println("网络发送流量是 = " + netSend[netSend.length-1]);
				vm.setwNet(vm.getwNet()+netSend[netSend.length-1]);
				String s = PerformAnalyseUtil.handlePart(e.getKey());
				vm.getwNetList().put(ConstCharacter.ETH + s.substring(4, s.indexOf(ConstCharacter.UNDERSCORE+ConstCharacter.TX)), netSend[netSend.length-1]);
			}

			//get the line data of vif receive
			if (PerformAnalyseUtil.handleUUID(e.getKey()).equals(vm.getUuid())
					&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()), ConstCharacter.VIF)
					&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()), ConstCharacter.RX)) {
				double[] netRev = PerformAnalyseUtil.toDoubleArray(e.getValue());
//				System.out.println("网络接收流量是 = " + netRev[netRev.length-1]);
				vm.setrNet(vm.getrwNet()+netRev[netRev.length-1]);
				String s = PerformAnalyseUtil.handlePart(e.getKey());
				vm.getrNetList().put(ConstCharacter.ETH + s.substring(4, s.indexOf(ConstCharacter.UNDERSCORE+ConstCharacter.RX)), netRev[netRev.length-1]);
			}
			
			//get the information of each application type
			if (PerformAnalyseUtil.handleUUID(e.getKey()).equals(vm.getUuid())
					&& PerformAnalyseUtil.contains(PerformAnalyseUtil.handlePart(e.getKey()), ConstCharacter.APPTYPE)) {
				List<String> Apps = e.getValue();
				String app = Apps.get(Apps.size()-1);
				if (app.indexOf(ConstCharacter.COMMA) >= 0) {
					vm.setAppName(app.split(ConstCharacter.COMMA)[0]);
					String type = app.split(ConstCharacter.COMMA)[1];
					try {
						vm.setAppType(ResourceTypes.valueOf(type));
					} catch (IllegalArgumentException ex) {
						vm.setAppType(ResourceTypes.UNRECOGNIZED);
					}
				} else {
					vm.setAppType(ResourceTypes.UNRECOGNIZED);
					vm.setAppName("UNKNOWN");
				}
			}
		}
		vm.setCpuUsagePercent(0.0);		
		for(double usage:vm.getCpuUsageList().values()){
			vm.setCpuUsagePercent(vm.getCpuUsagePercent()+usage);
		}
		vm.setCpuUsagePercent(vm.getCpuUsagePercent()/vm.getCpuUsageList().size());
		//获取磁盘数据（写入性能数据文件中？在初始化的时候读取vdi信息？可能需要重写）
		try{
			vm.getDiskList().clear();
			vm.setDiskUsageValue(0);
			vm.setDiskTotalValue(0);
			Connection c = vm.getConnection();
			System.out.println("当前处理的虚拟机名字是 = " + vm.getName());
			for(VDI v : VDI.getByVM(c, (VM)vm.getApiObject()))
			{
				Disk disk = vm.new Disk();
				if(v!=null) {
					VDI.Record r = v.getRecord(c);
					disk.setUuid(r.uuid);
					disk.setLocation(r.location);
					disk.setNameDescription(r.nameDescription);
					disk.setNameLabel(r.nameLabel);
					disk.setTotalValue(MathUtil.RoundingDouble(((double)r.virtualSize)/1024.0/1024.0/1024.0, 2));
					disk.setUsageValue(MathUtil.RoundingDouble(((double)r.physicalUtilisation)/1024.0/1024.0/1024.0, 2));
				} else {
					disk.setUuid("Not Exist");
					disk.setLocation("Not Exist");
					disk.setNameDescription("Not Exist");
					disk.setNameLabel("Not Exist");
					disk.setTotalValue(0);
					disk.setUsageValue(0);
				}
				disk.setAvailableSpace(disk.getTotalValue()-disk.getUsageValue());
				disk.setVdi(v);
				vm.setDiskTotalValue(vm.getDiskTotalValue()+disk.getTotalValue());
				vm.setDiskUsageValue(vm.getDiskUsageValue()+disk.getUsageValue());
				vm.getDiskList().add(disk);
			}
			double per = (vm.getDiskTotalValue()==0?0:vm.getDiskUsageValue()/vm.getDiskTotalValue())*100;
			vm.setDiskUsagePercent(per);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * find the most large memeory total number in list
	 * @param memory
	 * @return max
	 */
	public static double findMaxTotalMemory(List<String> memory) {
		double max = 0;
		for(int i=0; i<memory.size(); i++) {
			if(Double.parseDouble(memory.get(i)) > max) {
				max = Double.parseDouble(memory.get(i));
			}
		}
		return max;
	}

}
