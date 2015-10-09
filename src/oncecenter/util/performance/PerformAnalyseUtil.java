package oncecenter.util.performance;

import java.util.List;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM.ResourceTypes;

public class PerformAnalyseUtil {
	
	public static double[] toDoubleArray(List<String> stringArray) {
		if (stringArray == null) {
			return null;
		}
		double[] result = new double[stringArray.size()];
		for (int i = 0; i < stringArray.size(); i++) {
			try{
				if(stringArray.get(i) != null){
					result[i] = Double.parseDouble(stringArray.get(i));
				} else {
					result[i] = 0.0;
				}
			}catch(Exception e){
				e.printStackTrace();
				result[i]=0;
			}
		}
		return result;
	}
	
	public String getAppName(String app) {
		return app.split(",")[0];
	}
	
	public ResourceTypes getAppType(String app) {
		String type = app.split(",")[1];
		try {
			return ResourceTypes.valueOf(type);
		} catch (IllegalArgumentException ex) {
			return ResourceTypes.UNRECOGNIZED;
		}
	}
	
	public static  String handlePart(String key) {
    	int index3 = key.lastIndexOf(":");
    	return key.substring(index3+1);
    }
	
	public static  String handleUUID(String key) {
    	String str1 = null;
    	int index1 = key.indexOf(":");
        int index3 = key.lastIndexOf(":");
        str1 = key.substring(index1+1, index3);
        int index2 = str1.indexOf(":");
        return str1.substring(index2+1);
    }
	
	public static int getCPUNum(String key) {
		int index3 = key.lastIndexOf("u");
    	return Integer.parseInt(key.substring(index3+1));
	}
	
	public static  boolean contains(String mother, String child) {
    	if (mother.indexOf(child, 0)>=0) {
    		return true;
    	} else {
    		return false;
    	}
    }
}
