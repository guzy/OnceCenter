//package test;
//
//import java.net.MalformedURLException;
//import java.util.ArrayList;
//import java.util.Set;
//
//import oncecenter.Constants;
//import oncecenter.tree.VMTreeObject;
//import oncecenter.tree.VMTreeObjectPool;
//import oncecenter.tree.VMTreeObjectRoot;
//import oncecenter.tree.VMTreeObjectTemplate;
//import oncecenter.tree.VMTreeObjectVM;
//import oncecenter.tree.VMTreeObject.ItemState;
//import oncecenter.tree.group.VMTreeView;
//
//import org.apache.xmlrpc.XmlRpcException;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.core.runtime.jobs.Job;
//import org.eclipse.swt.widgets.Display;
//
//import com.xensource.xenapi.Connection;
//import com.xensource.xenapi.Host;
//import com.xensource.xenapi.VM;
//import com.xensource.xenapi.Types.BadServerResponse;
//import com.xensource.xenapi.Types.SessionAuthenticationFailed;
//import com.xensource.xenapi.Types.XenAPIException;
//
//public class ParallelTest {
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		
//		class Job1 extends Job{
//			Connection conn;
//			public Job1(Connection c){
//				super("job1");
//				this.conn = c;
//			}
//			@Override 
//		    protected IStatus run(IProgressMonitor monitor) { 
//		        monitor.beginTask("job1...", 100); 
//		        try{
//		        	for(VM vm:VM.getAll(conn)){
//		        		System.out.println("job1"+vm.getNameLabel(conn));
//		        	}
////		        	Set<Host> hostSet = Host.getAll(conn);
////		    		for (Host host : hostSet) {
////
////		    			System.out.println(host.getAddress(conn));
////		    		}
//		        }catch(Exception e){
//		        	e.printStackTrace();
//		        }
//		        monitor.done(); 
//		        return Status.OK_STATUS;  
//		    }
//		}
//
//		class Job2 extends Job{
//			Connection conn;
//			public Job2(Connection c){
//				super("job2");
//				this.conn = c;
//			}
//			@Override 
//		    protected IStatus run(IProgressMonitor monitor) { 
//		        monitor.beginTask("job2...", 100); 
//		        try{
//		        	for(VM vm:VM.getAll(conn)){
//		        		System.out.println("job2"+vm.getMAC(conn));
//		        	}
////		        	Set<Host> hostSet = Host.getAll(conn);
////		    		for (Host host : hostSet) {
////
////		    			System.out.println(host.getRecord(conn));
////		    		}
//		        }catch(Exception e){
//		        	e.printStackTrace();
//		        }
//		        monitor.done(); 
//		        return Status.OK_STATUS;  
//		    }
//		}
//		
//		Connection c = null ;
//		try {
//			c = new Connection("http://133.133.135.8:9363","root","onceas");
//			
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
//		Job1 job1 = new Job1(c);
//		Job2 job2 = new Job2(c);
//		job1.schedule();
//		job2.schedule();
//		
//		try {
//			Thread.sleep(20000);
//		} catch (InterruptedException e) {
//			
//			e.printStackTrace();
//		}
//	}
//	
//	
//}
//
