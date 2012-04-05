package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class LoginHandler {

	public static boolean checkLogin(Map<String,String> request) throws Exception
	{
		try{
			String user=request.get("user");
			String pass=request.get("pass");
			 String myDriver = "com.mysql.jdbc.Driver";
		      String myUrl = "jdbc:mysql://localhost/optionsDb";
		      Class.forName(myDriver);
		      Connection conn = DriverManager.getConnection(myUrl, "root", "einstein123");
		              Statement st=conn.createStatement();
			           ResultSet rs=st.executeQuery("select * from loginTb where user='"+user+"' and pass='"+pass+"'");
			           int count=0;
			           while(rs.next()){
			           count++;
			           break;
			          }
			           rs.close();
			           st.close();
			           conn.close();
			          if(count>0){
			           return true;
			           }
			  }
			    catch(Exception e){
			    System.out.println(e);
			}
		return false;
		
	}
	
}
