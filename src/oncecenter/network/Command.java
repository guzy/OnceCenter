/**
 * Institute of Software, Chinese Academy of Sciences
 */
package oncecenter.network;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.SessionAuthenticationFailed;
import com.once.xenapi.Types.XenAPIException;

/**
 * @author henry
 * @email  wuheng09@gmail.com	
 * @date   2013-7-4
 *
 */
public class Command {

	public final static String OVS_COMMAND_NAME = "/usr/local/bin/ovs-vsctl";
	
	public final static String OVS_PARAMETER_ADDBOND = "add-bond";
	
	public final static String OVS_PARAMETER_SETPORT = "set port";
	
	public final static String OVS_PARAMETER_DELBOND = "-- --if-exists del-port";
	
	public final static String OVS_PARAMETER_TAG = "tag";
	
	public final static String OVS_PARAMETER_LACP = "lacp=active";
	
	public final static String OVS_TAG_NETWORCARD_SEPARATOR = ".";
	
	
	//
	/***********************************************************
	 *   
	 *                     Default Commands
	 *   
	 ************************************************************/
	
	public static String defaultAddBondCommand(Set<String> networkCards) {
		return addBondCommand(OVS.DEFAULT_SWITCHER, OVS.DEFAULT_BOND, networkCards);
	}
	
	public static String defaultDeleteBondCommand() {
		return deleleBondCommand(OVS.DEFAULT_BOND);
	}
	
	public static String defaultSetBondModelCommand() {
		return setBondModelCommand(OVS.DEFAULT_BOND, OVS.DEFAULT_BOND_MODEL);
	}
	
	public static String defaultSetTagCommand(String cardName, int vlanID) {
		return setTagCommand(cardName, OVS.DEFAULT_NETWORK_CARD, vlanID);
	}
	
	public static String defaultActiveLACPCommand() {
		//ovs-vsctl set port <port name> lacp=active
		return activeLACPCommand(OVS.DEFAULT_BOND);
	}
	
	/***********************************************************
	 *   
	 *                     Commands
	 *   
	 ************************************************************/
	
	public static String addBondCommand(String switchName, String bondName, Set<String> networkCards) {
		StringBuffer sb = new StringBuffer();
		for(String network : networkCards) {
			sb.append(network).append(" ");
		}
		return OVS_COMMAND_NAME + " "
				            + OVS_PARAMETER_ADDBOND + " "
							+ switchName + " " 
		                    + bondName   + " "
							+ sb.toString();
	}

	
	public static String deleleBondCommand(String bondName) {
		return OVS_COMMAND_NAME + " "
				            + OVS_PARAMETER_DELBOND + " "
		                    + bondName;
	}
	
	
	public static String setBondModelCommand(String bondName, String bondModel) {
		return OVS_COMMAND_NAME + " "
				            + OVS_PARAMETER_SETPORT + " "
							+ bondName + " " + 
				            "bond_mode=" + bondModel;
	}
	
	public static String setTagCommand(String cardName, int cardID, int vlanID) {
		return OVS_COMMAND_NAME + " "
	            + OVS_PARAMETER_SETPORT + " "
				+ cardName + OVS_TAG_NETWORCARD_SEPARATOR + cardID + " "
				+ OVS_PARAMETER_TAG  + "=" + vlanID; 
	}
	
	public static String activeLACPCommand(String bondName) {
		//ovs-vsctl set port <port name> lacp=active
		return OVS_COMMAND_NAME + " "
        		+ OVS_PARAMETER_SETPORT + " "
        		+ bondName + " "
        		+ OVS_PARAMETER_LACP;
	}
	
	public static void main(String[] args) throws BadServerResponse, SessionAuthenticationFailed, MalformedURLException, XenAPIException, XmlRpcException {
		Connection conn = new Connection("http://133.133.135.12:9363", "root", "onceas");
		Set<VM> vmSet = VM.getAll(conn);
		for(VM vm : vmSet) {
			Set<String> list = new HashSet<String>();
			list.add("eth0");
			list.add("eth1");
			System.out.println(defaultDeleteBondCommand());
			System.out.println(defaultAddBondCommand(list));
			System.out.println(defaultSetBondModelCommand());
			System.out.println(defaultSetTagCommand(OVSUtil.getTapName(conn, vm), 4));
			System.out.println(defaultSetTagCommand(OVSUtil.getVifName(conn, vm), 4));
			System.out.println(defaultActiveLACPCommand());
		}
//		
//		Set<Host> hosts = Host.getAll(conn);
//		for(Host host : hosts) {
//			System.out.println(host.getRecord(conn).address);
//		}
	}
	
}
