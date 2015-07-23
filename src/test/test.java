//package test;
//
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.net.MalformedURLException;
//import java.net.Socket;
//import java.net.UnknownHostException;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.UUID;
//
//import oncecenter.Constants;
//import oncecenter.tool.MathTool;
//import oncecenter.tool.Ssh;
//import oncecenter.tree.VMTreeObjectVM;
//import oncecenter.util.FileUtil;
//import oncecenter.util.TypeUtil;
//import oncecenter.util.VMUtil;
//
//import org.apache.xmlrpc.XmlRpcException;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.PlatformUI;
//
//import com.xensource.xenapi.Connection;
//import com.xensource.xenapi.Console;
//import com.xensource.xenapi.Host;
//import com.xensource.xenapi.Network;
//import com.xensource.xenapi.PBD;
//import com.xensource.xenapi.Pool;
//import com.xensource.xenapi.SR;
//import com.xensource.xenapi.Task;
//import com.xensource.xenapi.Types;
//import com.xensource.xenapi.VBD;
//import com.xensource.xenapi.VDI;
//import com.xensource.xenapi.VIF;
//import com.xensource.xenapi.Types.BadServerResponse;
//import com.xensource.xenapi.Types.SessionAuthenticationFailed;
//import com.xensource.xenapi.Types.XenAPIException;
//import com.xensource.xenapi.VM;
//
//class ShutdownVM implements Runnable {
//	private VM vm;
//	Connection c;
//
//	public ShutdownVM(VM vm,Connection c) {
//		this.vm = vm;
//		this.c = c;
//	}
//
//	public void run() {
//		try {
//			String powerState = vm.getPowerState(c)
//					.toString();
//			if (powerState.equals("Running")) {
//				System.out.println("successful");
//				vm.cleanShutdown(c);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("failed");
//		}
//	}
//}
//
//
//class A{
//	String s = "classA";
//	int x = 1;
//	void printS(){
//		System.out.println("function A");
//	}
//}
//class B extends A{
//	String s = "classB";
//	int x = 2;
//	void printS(){
//		System.out.println("function B");
//	}
//}
//public class test {
//
//	private int a;
//	static int b;
//	public float add(float a,float b){
//		return a+b;
//	}
//	private float add(float c,float d,float a){
//		return a+c+d;
//	}
//	public static void change(String s,char[] a){
//		s = new String("ok");
//		a[0]='g';
//	}
////	public void modify(){
////		int i,j,k;
////		i=100;
////		while(i>0){
////			j=i*2;
////			k++;
////		}
////	}
//	public static int tt(){
//		String a = "Hello";
//		String b = a.substring(0, 2);
//		System.out.println("b:"+b);
//		//return this.b;
//		return 1;
//	}
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		try {
//			FileOutputStream fos = new FileOutputStream("F:/"+File.separator+"log.txt");
//			BufferedOutputStream bos = new BufferedOutputStream(fos,1024);
//			PrintStream  ps = new PrintStream(bos,false);
//			System.setOut(ps);
//			System.out.println("skfjsldfjslfjsldfk");
//			ps.close();
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
//		
////		test t = new test();
////		System.out.println(t.a);
////		tt();
//////		A a = new B();
//////		System.out.println(a.x);
//////		a.printS();
//////		char [] a={'a','b','c'};
//////		String s = new String("hello");
//////		change(s,a);
//////		System.out.println(s);
//////		System.out.println(String.valueOf(a));
////		
////		int a = 0xFFFFFFF1;
////		//int b = ~a;
////		int b = 4>>>2;
////		int $number;
////		System.out.println(b);
////		System.out.println(066);
////		System.out.println(10*49.3);
////		double d = 5.3E12;
////		double f = 11.1E10f;
////		float aa = 3.14f;
////		int _t = (int)12.0;
////		
////		byte [] array1,array2[];
////		byte array3 [][];
////		byte [][] array4;
////		//array2=arrya1;
////		//array2=array3;
////		//array2=array4;
////		//int #ksjfk;
////		boolean bool;
////		//char ttt = (char)bool;
////		double x = 30;
////		System.out.println(Math.cos(x*Math.PI/180));
////		System.out.println(Math.sin(x*Math.PI/180));
////		System.out.println(Math.cos(Math.toRadians(x)));
////		System.out.println(Math.toDegrees(Math.toRadians(x)));
//		try {
//			
////			Connection c1 = new Connection("http://133.133.135.11:9363", "root", "onceas");
////			Connection c2 = new Connection("http://133.133.135.11:9363", "root", "onceas");
////			List<Connection> list = new ArrayList<Connection>();
////			list.add(c1);
////			list.add(c2);
////			System.out.println(c1==list.get(0));
////			System.out.println(c1==list.get(1));
//////			for(SR sr:SR.getAll(c)){
//////				if(sr.getType(c).equals(TypeUtil.isoSrType)){
//////					System.out.println(sr.getRecord(c));
//////					for(VDI vdi:sr.getVDIs(c)){
//////						System.out.println(vdi);
//////					}
//////					sr.update(c);
//////					for(VDI vdi:sr.getVDIs(c)){
//////						System.out.println(vdi.getRecord(c));
//////					}
//////				}
//////			}
//			Ssh ssh1 = new Ssh("133.133.135.7", "root", "onceas");
//			ssh1.Connect();
//			String cmd = "test -e /root/test";
//			try {
//				ssh1.Command(cmd);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
//	}
//	
//	
//	
//	public static Pool isPool(Connection conn) throws BadServerResponse, XenAPIException,XmlRpcException {
//		Set<Pool> pools = Pool.getAll(conn);
//		Pool thisPool;
//		for(Iterator<Pool> iter = pools.iterator(); iter.hasNext();) {
//			thisPool = iter.next();
//			if(thisPool.getNameLabel(conn) == null || "".equals(thisPool.getNameLabel(conn).trim())) {
//				continue;
//			}
//			return thisPool;
//		}
//		return null;
//	}
//
//}
