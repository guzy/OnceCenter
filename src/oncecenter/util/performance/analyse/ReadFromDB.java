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

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

public class ReadFromDB {
	/*
	 * This class is a replacement of XML2Metric which is under the same package. 
	 */
	private static final String dbURL = "133.133.135.9:3306";
	private static final String dbUserName = "root";
	private static final String dbPasswd = "onceas";
	private static final String dbName = "bsperformance";
	private static final String dbName4GetAllTable = "information_schema";
	
	public static void getMetricsTimelines(VMTreeObjectHost host) {
		host.clearMetrics();
		List<String> allTableName = getAllTableName(dbName);
		for(String singleTableName : allTableName){
			getMetricsTimelines(host, singleTableName);
		}
	}
	private static Connection getConn(String dbName){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbURL + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUserName,
					dbPasswd);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	public static void getMetricsTimelines(VMTreeObjectHost host, String tableName){
		String dataType = tableName.substring(0, tableName.indexOf("_"));
		String sql = "select * from " + tableName;
		Connection conn = getConn(dbName);
		Statement queryStmt = null;
		ResultSet rs = null;
		try {
			queryStmt = conn.createStatement();
	        rs = queryStmt.executeQuery(sql);
	        while(rs.next()){
	        	if(rs.isFirst()){
	        		host.startTime = rs.getLong(1);//获取列表第1个字段，从1开始编号，此方法比较高效
	        	}
	        	if(rs.isLast()){
	        		host.columns = rs.getRow();
	    	        host.endTime = rs.getLong(1);
	        	}
	        	String vmUuid = rs.getString(2);
	        	String key = null;
	        	String data = null;
	        	if(tableName.contains("cpu")){
	        		String typeNum = rs.getString(3);//CPU等的编号
	        		data = rs.getString(4);
	        		key = "vm:" + vmUuid + ":" + dataType + typeNum;
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("mem")){
	        		key = "vm:" + vmUuid + ":" + dataType + "_total";
	        		data = rs.getString(3);
	        		putData2Host(host, key, data, rs);
	        		key = "vm:" + vmUuid + ":" + dataType + "_free";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("vif")){
	        		String vifID = rs.getString(3);
	        		key = "vm:" + vmUuid + ":" + dataType + "_" + vifID + "_rx";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        		key = "vm:" + vmUuid + ":" + dataType + "_" + vifID + "_tx";
	        		data = rs.getString(5);
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("vbd")){
	        		String vbdID = rs.getString(3);
	        		key = "vm:" + vmUuid + ":" + dataType + "_" + vbdID + "_read";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        		key = "vm:" + vmUuid + ":" + dataType + "_" + vbdID + "_write";
	        		data = rs.getString(5);
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("pif")){
	        		String vifID = rs.getString(3);
	        		key = "host:" + vmUuid + ":" + dataType + "_" + vifID + "_rx";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        		key = "vm:" + vmUuid + ":" + dataType + "_" + vifID + "_tx";
	        		data = rs.getString(5);
	        		putData2Host(host, key, data, rs);
	        	}
	        	if(tableName.contains("pbd")){
	        		String vbdID = rs.getString(3);
	        		key = "host:" + vmUuid + ":" + dataType + "_" + vbdID + "_read";
	        		data = rs.getString(4);
	        		putData2Host(host, key, data, rs);
	        		key = "vm:" + vmUuid + ":" + dataType + "_" + vbdID + "_write";
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
	}
	private static void putData2Host(VMTreeObjectHost host, String key, String data, ResultSet rs){
		if (host.getMetrics().containsKey(key)) {
			host.getMetrics().get(key).add(data);
		} else  {
			List<String> lt = new ArrayList<String>();
			try {
				for(int fillNum = 0; fillNum < rs.getRow(); fillNum++) {
					lt.add("0");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			lt.add(data);
			host.getMetrics().put(key, lt);
		}
	}
	private static List<String> getAllTableName(String dbName){
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
	        	if(rs.getString(1).contains("30min")){
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
	public static int getTableLength(String tableName){
		String sql = "select count(*) from " + tableName;
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
	public static void main(String[] args) {
		System.out.println(getTableLength("cpu_1d"));
	}
}
