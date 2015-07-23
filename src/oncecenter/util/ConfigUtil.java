/**
 * 
 */
package oncecenter.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * @author TianFei
 *
 */
public class ConfigUtil {

	static Properties prop = new Properties();
	
	static
	{
		File file = new File(FileUtil.getXenCenterRoot()+"/network.conf");
		try {
			prop.load(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getNetwork() {
		
		String network = prop.getProperty("network");
		return (network != null) ? network : "eth0";
	}
}
