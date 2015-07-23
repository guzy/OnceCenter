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
	 * Լ�����������������붼����Ч��
	 * 
	 * @param networkCards, ������Set���ͣ���Ч��ֹ�ظ�����
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
			// ����ȷ��Bond0������,�ǲ���Ӧ�ü�ס��ǰ������
			ssh.Command(Command.defaultDeleteBondCommand());
			// ִ��Bond����
			ssh.Command(Command.defaultAddBondCommand(networkCards));
			// ָ��Bondģʽ����ʵ���Ժϲ���ִ��Bond����һ��
			ssh.Command(Command.defaultSetBondModelCommand());
//			ssh.Command(Command.defaultActiveLACPCommand());
			return true;
		} catch (Exception e) {
			// ��λع�??���ع�����ǰ����
			return false;
		}
		
	}

	/**
	 * Լ��������
	 * ��1��VM����������ǿ���״̬
	 * ��2��vlanIDȡֵ�����ڡ�0,2000��֮��,
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
			// ��λع�??
			return false;
		}
	}

	
	/**
	 * Լ����������������δ����������ָ�����������Ӧ�ã��׳��쳣
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
		//�������ɣ���
		String output = ssh.Command("/usr/local/bin/ovs-vsctl show | grep -v Port  | grep " + tapName +".0 -B 1 | head -1 | awk '{print $2}'");
		try {
			return Integer.parseInt(output.trim());
		} catch (Exception e) {
			return DEFAULT_VLAN;
		}
	}
	
	/**
	 * ���׳��쳣�������TRUE����FALSE
	 * 
	 * @param name
	 * @return
	 */
	public boolean checkInterfaceIP(String name) {
		if(invalidName(name)) {
			return false;
		}
		///usr/local/bin/ovs-vsctl show | grep -v Port  | grep vif7.0 -B 1 | head -1 | awk '{print $2}'
		//�������ɣ���
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
			throw new Exception("��ȷ��������Ĳ�����ȷ");
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
