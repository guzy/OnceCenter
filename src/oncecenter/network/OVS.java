/**
 * Institute of Software, Chinese Academy of Sciences
 */
package oncecenter.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oncecenter.util.Ssh;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

/**
 * @author henry
 * @email  wuheng09@gmail.com	
 * @date   2013-7-4
 *
 */
public class OVS {

	public final static int DEFAULT_VLAN = -1;
	
	public final static String DEFAULT_SWITCHER = "ovs0";
	
	public final static String DEFAULT_BOND = "bond0";
	
	public final static int DEFAULT_NETWORK_CARD = 0;
	
	public final static String DEFAULT_BOND_MODEL = "balance-slb";
	
	public final static List<String> INVALID_NETWORK_CARD = null;
	
	public final static VM INVALID_VM = null;
	
	public final static Connection INVALID_CONNECTION = null;
	
	public final static String INVALID_NAME = null;
	
	public final static int MIN_BOND_SIZE = 0;
	
	public final static int MIN_VLAN_ID = 0;
	
	public final static int MAX_VLAN_ID = 2000;
	
	private final Ssh ssh;
	
	/**
	 * 
	 * @param ip
	 * @param user
	 * @param pwd
	 * @throws Exception
	 */
	public OVS(String ip, String user, String pwd) throws Exception {
		this.ssh = new Ssh(ip,  user,  pwd);
		checkSshValid();
	}

	/***********************************************************
	 *   
	 *                     Operation
	 *   
	 ************************************************************/

	/**
	 * For example, the input is eth0,eth1,eth2, note that the networkCard name must be exist.
	 * 
	 * 约束条件：网卡名必须都是有效的
	 * 
	 * @param networkCards, 这里是Set类型，有效防止重复数据
	 * @return
	 */
	public boolean bond(Set<String> networkCards) {
		if(invalidNetworkCards(networkCards)) {
			return false;
		}
		
		try {
			
			// ovs-vsctl -- --if-exists del-port bond0
			// ovs-vsctl add-bond ovs0 bond0 eth0 eth1 eth2 (Params: networkCards)
            // ovs-vsctl set port bond0 bond_mode=balance-slb
			// /usr/local/bin/ovs-vsctl set port bond0 lacp=active
			// 首先确保Bond0不存在,是不是应该记住当前的配置
			ssh.Command(Command.defaultDeleteBondCommand());
			// 执行Bond操作
			ssh.Command(Command.defaultAddBondCommand(networkCards));
			// 指定Bond模式（其实可以合并到执行Bond操作一起）
			ssh.Command(Command.defaultSetBondModelCommand());
//			ssh.Command(Command.defaultActiveLACPCommand());
			return true;
		} catch (Exception e) {
			// 如何回滚??，回滚到当前配置
			return false;
		}
		
	}

	/**
	 * 约束条件：
	 * （1）VM必须存在且是开启状态
	 * （2）vlanID取值必须在【0,2000）之间,
	 * 
	 * @param conn
	 * @param vm
	 * @param vlanID
	 * @return
	 */
	public boolean setTag(Connection conn, VM vm, int vlanID) {
		
		String tapName = OVSUtil.getTapName(conn, vm);
//		String vifName = OVSUtil.getVifName(conn, vm);
		
		if(invalidConnection(conn) || invalidVM(vm, tapName) || invalidVlan(vlanID)) {
			return false;
		}
		
		try {
			ssh.Command(Command.defaultSetTagCommand(tapName, vlanID));
//			ssh.Command(Command.defaultSetTagCommand(vifName, vlanID));
			return true;
		} catch (Exception e) {
			// 如何回滚??
			return false;
		}
	}

	
	/**
	 * 约束条件：如果虚拟机未开启，或者指向错误的虚拟机应用，抛出异常
	 * 
	 * @param conn
	 * @param vm
	 * @return
	 * @throws Exception
	 */
	public int getTag(Connection conn, VM vm) throws Exception {
		//
		String tapName = OVSUtil.getTapName(conn, vm);
		if(this.invalidVMInstance(vm) || this.invalidName(tapName)) {
			throw new Exception("Invalid VM, ignore.");
		}
		
		///usr/local/bin/ovs-vsctl show | grep -v Port  | grep vif7.0 -B 1 | head -1 | awk '{print $2}'
		//先这样吧，哎
		String output = ssh.Command("/usr/local/bin/ovs-vsctl show | grep -v Port  | grep " + tapName +".0 -B 1 | head -1 | awk '{print $2}'");
		try {
			return Integer.parseInt(output.trim());
		} catch (Exception e) {
			return DEFAULT_VLAN;
		}
	}
	
	/**
	 * 不抛出异常，仅输出TRUE或者FALSE
	 * 
	 * @param name
	 * @return
	 */
	public boolean checkInterfaceIP(String name) {
		if(invalidName(name)) {
			return false;
		}
		///usr/local/bin/ovs-vsctl show | grep -v Port  | grep vif7.0 -B 1 | head -1 | awk '{print $2}'
		//先这样吧，哎
		try {
			String output = ssh.Command("ifconfig " + name +" | grep inet | grep -v inet6");
			return ((output == null) || "".equals(output.trim())) ? false : true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/***********************************************************
	 *   
	 *                     Checking
	 *   
	 ************************************************************/

	private boolean invalidConnection(Connection conn) {
		return conn== INVALID_CONNECTION;
	}
	
	private boolean invalidVMInstance(VM vm) {
		return vm == INVALID_VM;
	}
	
	private boolean invalidName(String tapName) {
		return tapName == INVALID_NAME;
	}
	
	private boolean invalidVlan(int vlanID) {
		return vlanID < MIN_VLAN_ID || vlanID >= MAX_VLAN_ID;
	}
	
	private boolean invalidNetworkCards(Set<String> networkCards) {
		return networkCards == INVALID_NETWORK_CARD || networkCards.size() < MIN_BOND_SIZE;
	}

	private boolean invalidVM(VM vm, String tapName) {
		return invalidVMInstance(vm) || invalidName(tapName) ;
//						|| invalidName(vifName);
	}

	private void checkSshValid() throws Exception {
		if(!ssh.Connect()) {
			throw new Exception("请确定你输入的参数正确");
		}
	}
	
	public void close() {
		if(ssh != null) {
			ssh.CloseSsh();
		}
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		OVS ovs = new OVS("133.133.135.12", "root", "onceas");
//		Set<String> list = new HashSet<String>();
//		list.add("eth0");
//		list.add("eth1");
//		System.out.println(ovs.bond(list));
//		Connection conn = new Connection("http://133.133.135.16:9363", "root", "onceas");
//		Set<VM> vmSet = VM.getAll(conn);
//		for(VM vm : vmSet) {
//			long start = System.currentTimeMillis();
////			System.out.println(vm.getNameLabel(conn));
//			System.out.println(ovs.getTag(conn, vm));
//			long end = System.currentTimeMillis();
//			System.out.println((end-start));
//		}
//		System.out.println(ovs.checkInterfaceIP("eth0"));
//		System.out.println(ovs.checkInterfaceIP("eth1"));
//		System.out.println(ovs.checkInterfaceIP("ovs1"));
//		Set<Host> hosts = Host.getAll(conn);
//		for(Host host : hosts) {
//			System.out.println(host.checkValidForOVS(ovs, "ovs0"));
//		}
	}
}
