package oncecenter.util.performance.analyse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.ErrorMessageDialog;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

public class ReadFromDB {
	/*
	 * This class is a replacement of XML2Metric which is under the same package. 
	 */
	private String dbURL;
	private int dbPort;
	private String dbUserName;
	private String dbPasswd;
	private String dbName;
	private String dbName4GetAllTable = "information_schema";
	
	public ReadFromDB(String dbURL, int dbPort, String dbUserName, String dbPasswd, String dbName){
		this.dbURL = dbURL;
		this.dbPort = dbPort;
		this.dbUserName = dbUserName;
		this.dbPasswd = dbPasswd;
		this.dbName = dbName;
	}
	public void getMetricsTimelines(VMTreeObjectHost host) {
		host.clearMetrics();
		List<String> allTableName = getAllTableName(dbName);
		host.columns = Integer.MAX_VALUE;
		for(String singleTableName : allTableName){
			int currColumn = getMetricsTimelines(host, singleTableName);
			if(currColumn == 0)
				continue;
			if(singleTableName.contains("cpu"))
				currColumn /= getCPUCount(host);//ֻ�����������Ҫ���Ƕ��CPU������
			host.columns = Math.min(host.columns, currColumn);//ѡȡcpu,mem,pif,pbd��¼������Сֵ���Ա�������Խ��
		}
	}
	public void getMetricsTimelines(VMTreeObjectVM vm) {
		vm.newMetics();
		List<String> allTableName = getAllTableName(dbName);
		vm.columns = Integer.MAX_VALUE;
		for(String singleTableName : allTableName){
			int currColumn = getMetricsTimelines(vm, singleTableName);
			if(currColumn == 0)
				continue;
			vm.columns = Math.min(vm.columns, currColumn);
		}
	}
	public int getMetricsTimelines(VMTreeObjectHost host, String tableName){
		int result = 0;
		String dataType = tableName.substring(0, tableName.indexOf("_"));
		String sql = "select * from " + tableName + " where id='" + host.getUuid() + "';";
//		System.out.println("����ִ�е�sql = " + sql);
		Connection conn = getConn(dbName);
		Statement queryStmt = null;
		ResultSet rs = null;
		try {
			queryStmt = conn.createStatement();
	        rs = queryStmt.executeQuery(sql);
	        while(rs.next()){
	        	if(rs.isFirst()){
	        		host.startTime = rs.getLong(1);//��ȡ�б��1���ֶΣ���1��ʼ��ţ��˷����Ƚϸ�Ч
	        	}
	        	if(rs.isLast()){
	        		result = rs.getRow();
	    	        host.endTime = rs.getLong(1);
	        	}
	        	String key = null;
	        	String data = null;
	        	String machineType = "host:";
	        	String hostUuid = rs.getString(2);
	        	if(tableName.contains("cpu")){
	        		String typeNum = rs.getString(3);//CPU�ȵı��
	        		data = rs.getString(4);
	        		key = machineType + hostUuid + ":" + dataType + typeNum;
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("mem")){
	        		key = machineType + hostUuid + ":" + dataType + "_total";
	        		data = rs.getString(3);
	        		putData2Host(host, key, data, rs);
	        		key = machineType + hostUuid + ":" + dataType + "_free";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("pif")){
	        		String vifID = rs.getString(3);
	        		key = machineType + hostUuid + ":" + dataType + "_" + vifID + "_rx";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        		key = machineType + hostUuid + ":" + dataType + "_" + vifID + "_tx";
	        		data = rs.getString(5);
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("pbd")){
	        		String vbdID = rs.getString(3);
	        		key = machineType + hostUuid + ":" + dataType + "_" + vbdID + "_read";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        		key = machineType + hostUuid + ":" + dataType + "_" + vbdID + "_write";
	        		data = rs.getString(5);
	        		putData2Host(host, key, data, rs);
	        	}
	        }       
	        if(rs != null)
	        	rs.close();
	        if(queryStmt != null)
	        	queryStmt.close();
	        if(conn != null) 
	        	conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public int getMetricsTimelines(VMTreeObjectVM vm, String tableName){
		int result = 0;
		String dataType = tableName.substring(0, tableName.indexOf("_"));
		String sql = "select * from " + tableName + " where id='" + vm.getUuid() + "';";
//		System.out.println("����ִ�е�sql = " + sql);
		//update cpu_30min c set c.usage = 70 where c.id='a3ca9de0-5e73-15ee-f18f-bbae7cb867e9';
		Connection conn = getConn(dbName);
		Statement queryStmt = null;
		ResultSet rs = null;
		try {
			queryStmt = conn.createStatement();
	        rs = queryStmt.executeQuery(sql);
	        while(rs.next()){
	        	if(rs.isFirst()){
	        		vm.startTime = rs.getLong(1);//��ȡ�б��1���ֶΣ���1��ʼ��ţ��˷����Ƚϸ�Ч
	        	}
	        	if(rs.isLast()){
	        		result = rs.getRow();
	        		vm.endTime = rs.getLong(1);
	        	}
	        	String key = null;
	        	String data = null;
	        	String machineType = "vm:";
	        	String hostOrVMUuid = rs.getString(2);
	        	if(tableName.contains("cpu")){
	        		String typeNum = rs.getString(3);//CPU�ȵı��
	        		data = rs.getString(4);
	        		key = machineType + hostOrVMUuid + ":" + dataType + typeNum;
	        		putData2VM(vm, key, data, rs);
	        	}
	        	if(tableName.contains("mem")){
	        		key = machineType + hostOrVMUuid + ":" + dataType + "_total";
	        		data = rs.getString(3);
	        		putData2VM(vm, key, data, rs);
	        		key = machineType + hostOrVMUuid + ":" + dataType + "_free";
	        		data = rs.getString(4);
	        		putData2VM(vm, key, data, rs);
	        	}
	        	if(tableName.contains("vif")){
	        		String vifID = rs.getString(3);
	        		key = machineType + hostOrVMUuid + ":" + dataType + "_" + vifID + "_rx";
	        		data = rs.getString(4);
	        		putData2VM(vm, key, data, rs);
	        		key = machineType + hostOrVMUuid + ":" + dataType + "_" + vifID + "_tx";
	        		data = rs.getString(5);
	        		putData2VM(vm, key, data, rs);
	        	}
	        	if(tableName.contains("vbd")){
	        		String vbdID = rs.getString(3);
	        		key = machineType + hostOrVMUuid + ":" + dataType + "_" + vbdID + "_read";
	        		data = rs.getString(4);
	        		putData2VM(vm, key, data, rs);
	        		key = machineType + hostOrVMUuid + ":" + dataType + "_" + vbdID + "_write";
	        		data = rs.getString(5);
	        		putData2VM(vm, key, data, rs);
	        	}
	        }       
	        if(rs != null)
	        	rs.close();
	        if(queryStmt != null)
	        	queryStmt.close();
	        if(conn != null) 
	        	conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	private void putData2Host(VMTreeObjectHost host, String key, String data, ResultSet rs){
		data = handleData(data, key);
		if (host.getMetrics().containsKey(key)) {
			host.getMetrics().get(key).add(data);
		} else  {
			List<String> lt = new ArrayList<String>();
			try {
//				for(int fillNum = 0; fillNum < rs.getRow(); fillNum++) {
//					lt.add("0");
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			lt.add(data);
			host.getMetrics().put(key, lt);
		}
	}
	private void putData2VM(VMTreeObjectVM vm, String key, String data, ResultSet rs){
		data = handleData(data, key);
		if (vm.returnMetric().containsKey(key)) {
			vm.returnMetric().get(key).add(data);
		} else  {
			List<String> lt = new ArrayList<String>();
			try {
//				for(int fillNum = 0; fillNum < rs.getRow(); fillNum++) {
//					lt.add("0");
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			lt.add(data);
			vm.returnMetric().put(key, lt);
		}
	}
	public String handleData(String data, String key){
		double newData = Double.parseDouble(data);
//		if(key.contains("free")) {
//			if(key.contains("host"))
//				newData += new Random().nextInt(100000)+1000000;
//			else
//				newData += new Random().nextInt(1000)+10000;
//		} else if(key.contains("vif") || key.contains("pif")){
//			newData = newData + new Random().nextFloat();
//		} else if(key.contains("vbd"))
//			newData = newData + new Random().nextFloat();
		return String.valueOf(newData);
	}
	private List<String> getAllTableName(String dbName){
		List<String> result = new ArrayList<String>();
		Connection conn = getConn(dbName4GetAllTable);
		String sql = "select table_name from information_schema.tables where table_schema='" 
					+ dbName + "' and table_type='base table'";
		Statement queryStmt = null;
		ResultSet rs = null;
		try {
			queryStmt = conn.createStatement();
	        rs = queryStmt.executeQuery(sql);
	        while(rs.next()){
	        	if(rs.getString(1).contains("30min")){//ֻ����30min��Ӧ�����ݿ��
	        		result.add(rs.getString(1));
	        	}	        	
	        }       
	        if(rs != null)
	        	rs.close();
	        if(queryStmt != null)
	        	queryStmt.close();
	        if(conn != null) 
	        	conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	private int getCPUCount(VMTreeObjectHost host){
		int result = 0;
		Connection conn = getConn(dbName);
		String sql = "select distinct cpu_id from cpu_30min where id='" + host.getUuid() + "'";
		Statement queryStmt = null;
		ResultSet rs = null;
		try {
			queryStmt = conn.createStatement();
	        rs = queryStmt.executeQuery(sql);
	        while(rs.next()){
	        	result++;
	        }       
	        if(rs != null)
	        	rs.close();
	        if(queryStmt != null)
	        	queryStmt.close();
	        if(conn != null) 
	        	conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public int getTableLength(String tableName, String id){
		String sql = "select count(*) from " + tableName + " where cpu_id=" + id;
		Connection conn = getConn(dbName);
		PreparedStatement queryPstmt = null;
		try {
			queryPstmt = conn.prepareStatement(sql);
			ResultSet rs = queryPstmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			if(rs != null) 
	        	rs.close();   
	        if(queryPstmt != null)
	        	queryPstmt.close() ;   
	        if(conn != null) 
	        	conn.close() ;   
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return -1;
	}
	private Connection getConn(String dbName){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbURL + dbPort + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUserName,
					dbPasswd);
		} catch (Exception e) {
			conn = null;
			e.printStackTrace();
		}
		return conn;
	}
	@Override
	public String toString() {
		return "ReadFromDB [dbURL=" + dbURL + ", dbPort=" + dbPort
				+ ", dbUserName=" + dbUserName + ", dbPasswd=" + dbPasswd
				+ ", dbName=" + dbName + ", dbName4GetAllTable="
				+ dbName4GetAllTable + "]";
	}
	
}
