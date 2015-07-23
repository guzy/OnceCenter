package oncecenter.util.performance.grade;

import java.util.Date;


import com.once.xenapi.Host;

import oncecenter.Constants;
import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

public class GradeHost {
	public static double gradeHost(VMTreeObjectHost hostObject) throws Exception{
		//Ӧ����ʱ������ж����������Ƿ���ڣ�������ڵĻ����ֶ���ȡ�ڴ��С�ķ�ʽ����������
//		Date currentDate = new Date();
//		if(hostObject.performTimeStamp==null
//				||currentDate.getTime()-hostObject.performTimeStamp.getTime()>60000){
//			Host h = (Host)hostObject.getApiObject();
			Long free=0L;
//			//Long total=0L;
//			 //try {
//					free=h.getMetrics(hostObject.getConnection()).getRecord(hostObject.getConnection()).memoryFree;
//					//total=h.getMetrics(hostObject.getConnection()).getRecord(hostObject.getConnection()).memoryTotal;
////				} catch (BadServerResponse e) {
////					
////					e.printStackTrace();
////				} catch (XenAPIException e) {
////					
////	z				e.printStackTrace();
////				} catch (XmlRpcException e) {
////					
////					e.printStackTrace();
////				}
			try{
				Host host = (Host) hostObject.getApiObject();
				hostObject.setRecord(host.getRecord(hostObject.getConnection()));
			}catch(Exception e){
				e.printStackTrace();
			}
			Host.Record hostRecord = (Host.Record)hostObject.getRecord();
			free = hostRecord.memoryFree;
			 if(hostObject.getMemoryTotalValue()>0){
				 hostObject.setMemoryUsageValue(hostObject.getMemoryTotalValue()-free/1024.0/1024.0);
				 hostObject.setMemoryUsagePercent(hostObject.getMemoryUsageValue()/hostObject.getMemoryTotalValue()*100.0);
			 }
//		}
		if(Constants.maxMemoryTotal==0){
			return 0;
		}else{
			//System.out.println("maxMemoryTotal"+Constant.maxMemoryTotal);
			return MathUtil.RoundingDouble(((hostObject.getMemoryTotalValue()-hostObject.getMemoryUsageValue())/Constants.maxMemoryTotal*100.0), 2);
		}
	}
}
