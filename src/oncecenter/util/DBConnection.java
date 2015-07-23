package oncecenter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnection {
	private Connection con;
	public DBConnection(){
		String CLASSFORNAME ="com.mysql.jdbc.Driver";
		String SERVANDDB ="jdbc:mysql://133.133.134.95:3306/xencenter";
		String USER ="root";
		String PWD ="onceas";
		try
		{             
			Class.forName(CLASSFORNAME);
			con = DriverManager.getConnection(SERVANDDB,USER,PWD);
			}
		catch(Exception e)
		{
			e.printStackTrace();
			}
		}
	
	public int update(String sql) throws Exception
	{            
		Statement stmt=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);   
		int result=stmt.executeUpdate(sql);   
		return result;      
	}   
	
	public ResultSet query(String sql) throws Exception{     
		Statement stmt2=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);    
		ResultSet result=stmt2.executeQuery(sql);     
		return result;     
	} 
	
	public void Destroy(){
		try{
			if(con!=null) con.close();
			}catch(Exception ex){
			ex.printStackTrace();
			}
	}
}
