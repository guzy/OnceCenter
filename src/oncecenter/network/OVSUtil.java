/**
 * Institute of Software, Chinese Academy of Sciences
 */
package oncecenter.network;


import java.net.MalformedURLException;
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
public class OVSUtil {

	public final static String INVALID_NETWORK_CARD = null;
	
	public final static int INVALID_NETWORK_CARD_ID = -1;
	
	public final static String VIF = "vif";
	
	public final static String TAP= "tap";
	
	/**
	 * 如果VM处于关机状态，返回NUll
	 * 
	 * @param conn
	 * @param vm
	 * @return
	 */
	public static String getVifName(Connection conn, VM vm) {
		long id = INVALID_NETWORK_CARD_ID;
		try {
			id = vm.getDomid(conn);
		} catch (Exception e) {
			// this can be ignored
		}
		
		return isInvalid(id) ? INVALID_NETWORK_CARD : VIF + id;
	}

	
	/**
	 * 如果VM处于关机状态，返回NUll
	 * 
	 * @param conn
	 * @param vm
	 * @return
	 */
	public static String getTapName(Connection conn, VM vm) {
		long id = 0;
		try {
			id = vm.getDomid(conn);
		} catch (Exception e) {
			// this can be ignored
		}
		return isInvalid(id) ? INVALID_NETWORK_CARD : TAP + id;
	}
	
	
	/**
	 * 对于关机的VM，返回的ID值是-1
	 * 
	 * @param id
	 * @return
	 */
	private static boolean isInvalid(long id) {
		return id == INVALID_NETWORK_CARD_ID;
	}
	
	/**
	 * @param args
	 * @throws XmlRpcException 
	 * @throws XenAPIException 
	 * @throws MalformedURLException 
	 * @throws SessionAuthenticationFailed 
	 * @throws BadServerResponse 
	 */
	public static void main(String[] args) throws BadServerResponse, SessionAuthenticationFailed, MalformedURLException, XenAPIException, XmlRpcException {
		Connection conn = new Connection("http://133.133.135.12:9363", "root", "onceas");
		Set<VM> vmSet = VM.getAll(conn);
		for(VM vm : vmSet) {
			System.out.println(getVifName(conn, vm));
			System.out.println(getTapName(conn, vm));
		}
		
	}

}
