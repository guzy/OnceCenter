package oncecenter.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
	public static String getCurrentTime(){
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}
	public static String getCurrentDate(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}
}
