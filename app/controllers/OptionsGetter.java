package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class OptionsGetter {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		

				
		 URL url = new URL("http://query.yahooapis.com/v1/public/yql?q=%09%09select%20*%20from%20yahoo.finance.quotes%20where%20symbol%3D'AAPL'&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
	     InputStream xmlStream = url.openStream();
	     /*
	     Document doc = readXml(xmlStream);
	     NodeList list = doc.getChildNodes();
	     Node optionsChains = list.item(0).getChildNodes().item(1).getChildNodes().item(0);
	     System.out.println(optionsChains.getNodeName());
	     */
	     XPathFactory  factory=XPathFactory.newInstance();
	     XPath xPath=factory.newXPath();
	   
	     InputSource inputSource = 
	    		    new InputSource(xmlStream);
	     
	    
	     Node root = (Node) xPath.evaluate("query/results/quote", inputSource, XPathConstants.NODE);
	     
	  	
	     String quote = xPath.evaluate("Ask", root);
	     //System.out.println(quote);
	     
	     
	     if(quote == null || "".equals(quote) || "0.00".equals(quote))
			{
	    	 quote = xPath.evaluate("AskRealtime", root);
			 // System.out.println(quote);
			  
			   if(quote == null || "".equals(quote) || "0.00".equals(quote))
			   {
				   quote = xPath.evaluate("LastTradePriceOnly", root);
				   
			   }
		
			}
	     
	     System.out.println(quote);
				
		 url = new URL("http://query.yahooapis.com/v1/public/yql?q=SELECT%20*%20FROM%20yahoo.finance.options%20WHERE%20symbol%3D%22AAPL%22%20AND%20expiration%20in%20(SELECT%20contract%20FROM%20yahoo.finance.option_contracts%20WHERE%20symbol%3D%22AAPL%22)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
	      xmlStream = url.openStream();
	     /*
	     Document doc = readXml(xmlStream);
	     NodeList list = doc.getChildNodes();
	     Node optionsChains = list.item(0).getChildNodes().item(1).getChildNodes().item(0);
	     System.out.println(optionsChains.getNodeName());
	     */
	       factory=XPathFactory.newInstance();
	      xPath=factory.newXPath();
	   
	      inputSource = 
	    		    new InputSource(xmlStream);
	     
	    
	      root = (Node) xPath.evaluate("query/results", inputSource, XPathConstants.NODE);
	     String ticker = xPath.evaluate("optionsChain/@symbol", root);
	     //System.out.println(ticker);
	     
	     NodeList nodes = (NodeList) xPath.evaluate
	    	       ("optionsChain/option", root, XPathConstants.NODESET);
	     
	     //BufferedWriter writer = new BufferedWriter(new FileWriter(new File("aapl0315")));
	     
	    // writer.write("StockPrice,Symbol,Type,Strike,Last,Vol,OpenInt\n");
	     
	     String myDriver = "com.mysql.jdbc.Driver";
	      String myUrl = "jdbc:mysql://localhost/optionsDb";
	      Class.forName(myDriver);
	      Connection conn = DriverManager.getConnection(myUrl, "root", "einstein123");
	      
	      Statement st = conn.createStatement();
	      

	    for (int i = 0; i < nodes.getLength(); i++) {
	    	 String sql = "INSERT INTO optionsTb values (";
	    			   
	         Node optionNode = nodes.item(i);
	         StringBuilder builder = new StringBuilder();
	         builder.append(quote).append(",");
	         String optionSymbol = xPath.evaluate("@symbol", optionNode);
	         //System.out.println("optionSymbol is "+optionSymbol);
	         builder.append("'"+optionSymbol+"'").append(",");
	         String type = xPath.evaluate("@type", optionNode);
	         //System.out.println("type is "+type);
	         builder.append("'"+type+"'").append(",");
	         
	         String strikePrice = xPath.evaluate("strikePrice", optionNode);
	         
	         //System.out.println("strikePrice is "+strikePrice);
	         if("NaN".equals(strikePrice))
	        	 strikePrice="0";
	         builder.append(strikePrice).append(",");
	         String lastPrice = xPath.evaluate("lastPrice", optionNode);
	         if("NaN".equals(lastPrice))
	        	 lastPrice="0";
	         //System.out.println("lastPrice is "+lastPrice);
	         builder.append(lastPrice).append(",");
	         String bid = xPath.evaluate("bid", optionNode);
	         //System.out.println("bid is "+bid);
	         //builder.append(bid).append(",");
	         String ask = xPath.evaluate("ask", optionNode);
	         //System.out.println("ask is "+ask);
	         //builder.append(ask).append(",");
	         String vol = xPath.evaluate("vol", optionNode);
	         if("NaN".equals(vol))
	        	 vol="0";
	         //System.out.println("vol is "+vol);
	         builder.append(vol).append(",");
	         String openInt = xPath.evaluate("openInt", optionNode);
	         if("NaN".equals(openInt))
	        	 openInt="0";
	         //System.out.println("openInt is "+openInt);
	         builder.append(openInt).append(",");
	         Date dt = new Date();
	         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
	         String dateStr = sdf.format(dt);
	         builder.append("'"+dateStr+"'");
	         System.out.println(sql+builder.toString()+")");
	         
	       //  writer.write(builder.toString()+"\n");
	         st.executeUpdate(sql+builder.toString()+")");
	         
	        
	        
	         
	     }
	     
	    // writer.flush();
	   //  writer.close();
	    conn.close();
	   
	     
	}
	
	
	public static List<Map<String,String>> getHistory(String ticker,String date) throws Exception
	{
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		
		  /*
		  Map<String,String> map = new HashMap<String, String>();
		
		  map.put("StockPrice","123");
		  
		  map.put("Symbol", "aapl");
		  
		  map.put("TYPE","C");
		  map.put("Strike","270");
		  map.put("Last","250");
		  map.put("Vol","30");
		  map.put("OpenInt","50");
		  list.add(map);
		  map = new HashMap<String, String>();
 map.put("StockPrice","231");
		  
		  map.put("Symbol", "aapl");
		  
		  map.put("TYPE","C");
		  map.put("Strike","270");
		  map.put("Last","250");
		  map.put("Vol","30");
		  map.put("OpenInt","50");
		  list.add(map);
		  */
		 // System.out.println("list is "+list);
		//ok now query db
		  
		   String myDriver = "com.mysql.jdbc.Driver";
		      String myUrl = "jdbc:mysql://localhost/optionsDb";
		      Class.forName(myDriver);
		      Connection conn = DriverManager.getConnection(myUrl, "root", "einstein123");
		      
		     // PreparedStatement st = conn.prepareStatement("select * from optionsTb where Symbol=? and Options_Date = ?");
		      PreparedStatement st = conn.prepareStatement("select * from optionsTb where  Options_Date = ?");
		      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		      Date dt = formatter.parse (date);
		      java.sql.Date sqlDate = new java.sql.Date(dt.getTime());
			  st.setDate(1,sqlDate);
			  ResultSet rs = st.executeQuery();
			  
			  while(rs.next())
			  {
				  Map<String,String> map = new HashMap<String, String>();
				  map.put("StockPrice",rs.getString("StockPrice"));
				  
				  map.put("Symbol", rs.getString("Symbol"));
				  
				  map.put("TYPE",rs.getString("TYPE"));
				  map.put("Strike",rs.getString("Strike"));
				  map.put("Last",rs.getString("Last"));
				  map.put("Vol",rs.getString("Vol"));
				  map.put("OpenInt",rs.getString("OpenInt"));
				  list.add(map);
			  }
			  
			  rs.close();
			  st.close();
			  conn.close();
			  
		
		return list;
		
	}
	
	
}

