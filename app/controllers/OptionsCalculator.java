/**
 * 
 */
package controllers;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.mysql.jdbc.StringUtils;



/**
 * @author anubha_phadnis
 *
 */
public class OptionsCalculator {

	private  List<OptionsModel> optionsModelList = new ArrayList<OptionsModel>();
	private  Map<String, Double> map = new LinkedHashMap<String, Double>();
	private double investment = 0.0;

	public  Map<String, Double> process(List<Map<String,String>> positionsList , String ticker,String stockPriceStr,String username,String positionName,String url,boolean isSave) throws Exception
	{
		double minStrike =Double.MAX_VALUE;
		double maxStrike = Double.MIN_VALUE;
		
		 Connection conn = null;
		 Statement st = null;
		if(!StringUtils.isNullOrEmpty(positionName)  && isSave)
		{
		 String myDriver = "com.mysql.jdbc.Driver";
	      String myUrl = "jdbc:mysql://localhost/optionsDb";
	      Class.forName(myDriver);
	       conn = DriverManager.getConnection(myUrl, "root", "einstein123");
	      
	       st = conn.createStatement();
		} 
		
		for(Map<String,String> map:positionsList)
		{
			//System.out.println("map is "+map);
			int numOfContracts = Integer.parseInt(map.get("contracts"));
			for(int i = 0;i <numOfContracts;i++)
			{
				
				//System.out.println("map here is "+map);
				OptionsModel model = new OptionsModel();
				String buyOrSell =  map.get("buyOrSell");
				String expirationDate = map.get("expiration");
				String strike = map.get("strike");
				if(Double.parseDouble(strike) < minStrike)
					minStrike=Double.parseDouble(strike);
				if(Double.parseDouble(strike) > maxStrike)
					maxStrike=Double.parseDouble(strike);
				String callOrPut = map.get("callOrPut");
				String premium=map.get("premium");	
				//System.out.println(callOrPut);
				model.setOptionType(callOrPut);
				//System.out.println(strike);
				model.setStrikePrice(Double.parseDouble(strike));
				//System.out.println(buyOrSell);
				model.setTransactionType(buyOrSell);
				//System.out.println(premium);
				model.setOptionPremium(Double.parseDouble(premium));
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
				Date date = dateFormat.parse(expirationDate);
				model.setOptionDate(date);
				if(!StringUtils.isNullOrEmpty(positionName) && i == 0 && isSave)
				{
					 String sql = "INSERT INTO positionsTb values (";
					 StringBuilder builder = new StringBuilder();
					 builder.append(sql);
					 builder.append("'"+username+"'").append(",");
					 builder.append("'"+positionName+"'").append(",");
					 builder.append("'"+ticker+"'").append(",");
					 builder.append(0.00).append(",");
					 java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
			         String dateStr = sdf.format(date);
			     	 builder.append("'"+dateStr+"'").append(",");
					 builder.append("'"+buyOrSell.charAt(0)+"'").append(",");
					 builder.append(numOfContracts).append(",");
					 builder.append("'"+callOrPut+"'").append(",");
					 builder.append(Double.parseDouble(strike)).append(",");
					 builder.append(Double.parseDouble(premium));
					 builder.append(")");
					 System.out.println("SQL is "+builder.toString());
					 st.addBatch(builder.toString());
					 
					
					 
				}
				
				
				//now insert into positionsURLTb for position name and url
				
				
			//	System.out.println("Model here option type "+model.getOptionType());
			//System.out.println("Model here tran type "+model.getTransactionType());
				optionsModelList.add(model);
			}
			
			
			
		}
		
		if(!StringUtils.isNullOrEmpty(positionName) && isSave)
		{
		 //insert into positionsURLTb for position name and url
		 PreparedStatement ps = conn.prepareStatement("insert into positionsURLTb values (?,?,?,?,?,?)");
		 ps.setString(1, username);
		 ps.setString(2, positionName);

		  // System.out.println("URL is "+url);
		   	 String [] parameters=url.split("&");
			   	 String newurl = "";
			   	 int cnt=1;
			   	 int loopcnt=0;
			    for(int i = 3;i < parameters.length ; i++ )
			   	 {
			   	    String[] keyval=parameters[i].split("=");
			   	 	
			   	 		String [] split = keyval[0].split("_");
			   	 		System.out.println("incrementing to "+cnt);
			   	 			
			   	 		loopcnt++;
			   	 	if(loopcnt % 6 ==0)
			   	 	{
		   	 			cnt++;
		   	 		loopcnt=1;
			   	 	}
			   	 		if(split.length == 1 || keyval.length == 1)
			   	 		{
			   	 			newurl+=parameters[i]+"&";
			   	 		}
			   	 		else
			   	 		{
			   	 			newurl+=split[0]+"_"+cnt+"="+keyval[1]+"&";
			   	 		}
			   	 	
			   	 }
			    newurl=parameters[0]+"&"+parameters[1]+"&"+parameters[2]+"&"+newurl;
			    String mainURL=newurl.replaceAll("sayhello","optionscalc");
			 
		 ps.setString(3, mainURL);
		 long timeNow = Calendar.getInstance().getTimeInMillis();
		 java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);

		 ps.setTimestamp(4, ts);
		 double positionValue = getInvestment(optionsModelList);
		 ps.setDouble(5, positionValue);
		 ps.setString(6, ticker);
		 ps.executeUpdate();
		 ps.close();
		}
		
		if(st !=null && conn!=null )
		{
		
		
			try
			{
				
		  st.executeBatch();
			}
			catch(Exception e)
			{
				
			}
			
		   st.close();
		     
		    // writer.flush();
		   //  writer.close();
		   conn.close();
		    
		}
		
		/*
		model = new OptionsModel();
		model.setOptionType("CALL");
		model.setStrikePrice(600);
		model.setTransactionType("SELL");
		model.setOptionPremium(80);
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		date = dateFormat.parse("01/14/2014");
		model.setOptionDate(date);
		
		optionsModelList.add(model);
		*/
		
		double stockPrice = Double.parseDouble(stockPriceStr);
		
		if(minStrike == Double.MAX_VALUE ) minStrike=maxStrike;
		if(maxStrike < stockPrice) maxStrike=stockPrice;
		//System.out.println(minStrike);
		//System.out.println(maxStrike);
		//ok lets calculate profit
		double minPrice = minStrike/2;
		double maxPrice = maxStrike  + minPrice;
		double max=0,min=0;
		
		for(double i = minPrice; i <maxPrice;i=i+2)
		{
			
			double pnl = populateModelToPlot((double)i);
			if(pnl < min) min = pnl;
			if(pnl > max) max = pnl;
			
		}
		
		
		//System.out.println("min "+min);
		//System.out.println("max "+max);
		//plot a graph
		map.put("min", min);
		//System.out.println("max is "+max);
		map.put("max", max);
		
			
		return map;
		
		
	}
	
	public Map<String,Double> getPositions(String username) throws Exception
	{
		Map<String,Double> positionMap = new HashMap<String, Double>();
		 String myDriver = "com.mysql.jdbc.Driver";
	      String myUrl = "jdbc:mysql://localhost/optionsDb";
	      Class.forName(myDriver);
	      Connection conn = DriverManager.getConnection(myUrl, "root", "einstein123");
	      String sql = "select * from positionsTb where userId='"+username+"' order by positionName";
	      PreparedStatement st = conn.prepareStatement(sql);
	      ResultSet rs = st.executeQuery();
	      String currentPosition=null;
	      Map<String,List<OptionsModel>> l=new HashMap<String, List<OptionsModel>>();
	      List<OptionsModel> modelList = null;
	      while(rs.next())
	      {
	    	 
	    	  String positionName = rs.getString("positionName");
	    	  String userId  = rs.getString("userId");
	    	  String ticker = rs.getString("Ticker");
	    	  double longShortStockPrice=rs.getDouble("LongShortStockPrice");
	    	  java.sql.Date expiration = rs.getDate("Expiration");
	    	  String buyOrSell = rs.getString("BuyOrSell");
	    	  int contracts = rs.getInt("Contracts");
	    	  String type = rs.getString("TYPE");
	    	  int strike = rs.getInt("Strike");
	    	  double premium = rs.getDouble("Premium");
	    	  
	    	
	    	  if(currentPosition ==null || !currentPosition.equals(positionName))
	    	  {
	    		  
	    		  //moving to new position
	    		  modelList = new ArrayList<OptionsModel>();
	    		  l.put(positionName,modelList);
		      }
	    	  
	    	
	    	  for(int i = 0 ; i < contracts ;i++)
	    	  {
	    		  OptionsModel model = new OptionsModel();
	    		  model.setOptionPremium(premium);
		    	  model.setOptionDate(expiration);
		    	  model.setOptionType(type);
		    	  model.setStrikePrice(strike);
		    	  if(buyOrSell.equals("B"))
		    	  {
		    		  model.setTransactionType("BUY");
		    	  }
		    	  else if (buyOrSell.equals("S"))
		    	  {
		    		  model.setTransactionType("SELL");
		    	  }
		    	  modelList.add(model);
	    	  }
	    	  System.out.println("modellist is "+modelList);
	    	  currentPosition=positionName;
	    	
	      }
	     
	     Iterator<String> itr =  l.keySet().iterator();
	     while(itr.hasNext())
	     {
	    	 String pos = itr.next();
	    	 List<OptionsModel> opmodel = l.get(pos);
	    	  double investment = getInvestment(opmodel);
	    	  System.out.println("inv "+investment);
	    	  positionMap.put(pos, investment);
	     }
	     
	      
	      
		rs.close();
		st.close();
		conn.close();
	      return positionMap;
	}
	
	public Map<String,List<OptionsModel>> getPositionsModel(String username) throws Exception
	{
		  String myDriver = "com.mysql.jdbc.Driver";
	      String myUrl = "jdbc:mysql://localhost/optionsDb";
	      Class.forName(myDriver);
	      Connection conn = DriverManager.getConnection(myUrl, "root", "einstein123");
	      String sql = "select * from positionsTb where userId='"+username+"' order by positionName";
	      PreparedStatement st = conn.prepareStatement(sql);
	      ResultSet rs = st.executeQuery();
	      String currentPosition=null;
	      Map<String,List<OptionsModel>> l=new HashMap<String, List<OptionsModel>>();
	      List<OptionsModel> modelList = null;
	      while(rs.next())
	      {
	    	 
	    	  String positionName = rs.getString("positionName");
	    	  String userId  = rs.getString("userId");
	    	  String ticker = rs.getString("Ticker");
	    	  double longShortStockPrice=rs.getDouble("LongShortStockPrice");
	    	  java.sql.Date expiration = rs.getDate("Expiration");
	    	  String buyOrSell = rs.getString("BuyOrSell");
	    	  int contracts = rs.getInt("Contracts");
	    	  String type = rs.getString("TYPE");
	    	  int strike = rs.getInt("Strike");
	    	  double premium = rs.getDouble("Premium");
	    	  
	    	
	    	  if(currentPosition ==null || !currentPosition.equals(positionName))
	    	  {
	    		  
	    		  //moving to new position
	    		  modelList = new ArrayList<OptionsModel>();
	    		  l.put(positionName,modelList);
		      }
	    	  
	    	
	    	  for(int i = 0 ; i < contracts ;i++)
	    	  {
	    		  OptionsModel model = new OptionsModel();
	    		  model.setOptionPremium(premium);
		    	  model.setOptionDate(expiration);
		    	  model.setOptionType(type);
		    	  model.setStrikePrice(strike);
		    	  model.setTicker(ticker);
		    	  if(buyOrSell.equals("B"))
		    	  {
		    		  model.setTransactionType("BUY");
		    	  }
		    	  else if (buyOrSell.equals("S"))
		    	  {
		    		  model.setTransactionType("SELL");
		    	  }
		    	  modelList.add(model);
	    	  }
	    	  System.out.println("modellist is "+modelList);
	    	  currentPosition=positionName;
	    	
	      }
	   
	      
	      
		rs.close();
		st.close();
		conn.close();
	      return l;
	}
	
	public Map<String,String> getPositionsURLProfit(String username) throws Exception
	{
		Map<String,String> positionMap = new LinkedHashMap<String, String>();
		 String myDriver = "com.mysql.jdbc.Driver";
	      String myUrl = "jdbc:mysql://localhost/optionsDb";
	      Class.forName(myDriver);
	      Connection conn = DriverManager.getConnection(myUrl, "root", "einstein123");
	      String sql = "select * from positionsURLTb where userId='"+username+"' order by CreationDate desc";
	      PreparedStatement st = conn.prepareStatement(sql);
	      ResultSet rs = st.executeQuery();
	      String currentPosition=null;
	      Map<String,List<OptionsModel>> l=new HashMap<String, List<OptionsModel>>();
	      List<OptionsModel> modelList = null;
	      while(rs.next())
	      {
	    	 
	    	  String positionName = rs.getString("positionName");
	    	  String userId  = rs.getString("userId");
	    	  String url = rs.getString("URL");
	    	
	    	  positionMap.put(positionName, url);
	      }
	      
		rs.close();
		st.close();
		  
		conn.close();
		
		Map<String,List<OptionsModel>> model = getPositionsModel(username);
		System.out.println("PositionModel is "+model);
	/*
		try
		{
			Iterator<String> keyModelStr = model.keySet().iterator();
			while(keyModelStr.hasNext())
			{
				String posName= keyModelStr.next();
				List<OptionsModel> positionModel = model.get(posName);
				//now get P & L for each object in model, for this we need to get updated premium from yql !
				ExecutorService threadExecutor = Executors.newFixedThreadPool(positionModel.size());
				for(OptionsModel opModel:positionModel)
				{
				    threadExecutor.execute(new PremiumCalculator(opModel));
				}
				threadExecutor.shutdown();
			    threadExecutor.awaitTermination(4,TimeUnit.SECONDS);
	
				double investment = getInvestment(positionModel);
				//System.out.println("inv dynamic position"+investment);
				String url = positionMap.get(posName);
				if(positionMap.containsKey(posName))
				{
					positionMap.remove(posName);
				}
				posName+=" " + (long)investment;
				//positionMap.put(pos, investment);
				//System.out.println("url is "+url);
				positionMap.put(posName, url);
			}
		}
		catch(Exception e)
		{
			
		}
		
	*/
		
	    return positionMap;
	}
	
	public Map<String,Long> getPositionForPnLAjax(String username) throws Exception
	{
		Map<String,Long> positionPnLMap = new LinkedHashMap<String, Long>();
		
		Map<String,List<OptionsModel>> model = getPositionsModel(username);
		System.out.println("PositionModel is "+model);
	
		try
		{
			Iterator<String> keyModelStr = model.keySet().iterator();
			long start = System.currentTimeMillis();
			
			while(keyModelStr.hasNext())
			{
				String posName= keyModelStr.next();
				List<OptionsModel> positionModel = model.get(posName);
				long originalInvestment = (long) getInvestment(positionModel);
				ExecutorService threadExecutor = Executors.newFixedThreadPool(model.size());
			    threadExecutor.execute(new PositionsCalculator(positionModel));
				long currentInvestment = (long) getInvestment(positionModel);
				threadExecutor.shutdown();
			    threadExecutor.awaitTermination(60,TimeUnit.SECONDS);
				//System.out.println("inv dynamic position"+investment);
				long pnl = 0;
				if(originalInvestment > 0 )
				{
					//its a buy position
					pnl = currentInvestment - originalInvestment;
				}
				else
				{
					//its a sell position
					pnl =  originalInvestment - currentInvestment;
				}
				//positionMap.put(pos, investment);
				System.out.println("pos name is "+posName);
				positionPnLMap.put(posName, pnl);
			}
			
			long end = System.currentTimeMillis() - start;
		    System.out.println("TIME SPENT "+end);
		}
		catch(Exception e)
		{
			
		}
		
		return positionPnLMap;
	}
	
	public class PositionsCalculator implements Runnable
	{
		List<OptionsModel> positionModel;
		
		public PositionsCalculator(List<OptionsModel> positionModel)
		{
			this.positionModel=positionModel;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//now get P & L for each object in model, for this we need to get updated premium from yql !
			ExecutorService threadExecutor = Executors.newFixedThreadPool(positionModel.size());
			for(OptionsModel opModel:positionModel)
			{

			    threadExecutor.execute(new PremiumCalculator(opModel));
			}
			threadExecutor.shutdown();
		    try {
				threadExecutor.awaitTermination(60,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
		}
		
		
	}
	
	public class PremiumCalculator implements Runnable
	{
		private OptionsModel opModel;
		public PremiumCalculator(OptionsModel opModel)
		{
			this.opModel=opModel;
		}
		
		public void run() {
		// TODO Auto-generated method stub
		String newstring = new SimpleDateFormat("yyyy-MM").format(opModel.getOptionDate());
		
		 try {
			URL url = new URL("http://query.yahooapis.com/v1/public/yql?q=SELECT%20option%20FROM%20yahoo.finance.options%20WHERE%20symbol%3D'"+opModel.getTicker()+"'%20AND%20expiration%3D'"+newstring+"'%20and%20option.strikePrice%3D"+(long)opModel.getStrikePrice()+"%20and%20%20option.type%3D'"+opModel.getOptionType()+"'&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
			System.out.println("url is "+url);
			InputStream xmlStream = url.openStream();
			  XPathFactory  factory=XPathFactory.newInstance();
			     XPath xPath=factory.newXPath();
			     InputSource inputSource = 
			    		    new InputSource(xmlStream);
			     Node root = (Node) xPath.evaluate("//option", inputSource, XPathConstants.NODE);
			     //Find why we are getting exception sometime on this !
			     String premium = xPath.evaluate("lastPrice", root);
			     System.out.println("root "+root + " premium "+premium);
			     opModel.setOptionPremium(Double.parseDouble(premium));
			    xmlStream.close();
		 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
	}
	}

	public double getInvestment()
	{

		for(OptionsModel model:optionsModelList)
		{
			if(model.getOptionType().equals("C") && model.getTransactionType().equals("BUY"))
			{
				//ok its call option
				investment+=model.getOptionPremium();
				
			}
			else if(model.getOptionType().equals("C") && model.getTransactionType().equals("SELL"))
			{
				investment-=model.getOptionPremium();
				
			}
			else if(model.getOptionType().equals("P") && model.getTransactionType().equals("BUY"))
			{
				investment+=model.getOptionPremium();
				
			}
			else if(model.getOptionType().equals("P") && model.getTransactionType().equals("SELL"))
			{
				investment-=model.getOptionPremium();
				
			}
		}
		return investment*100;
	}
	
	public double getInvestment(List<OptionsModel> modelList)
	{
		 double investment = 0.0;
		for(OptionsModel model:modelList)
		{
			//System.out.println(model.getOptionType() + " "+model.getTransactionType());
			if(model.getOptionType().equals("C") && model.getTransactionType().equals("BUY"))
			{
				//ok its call option
				investment+=model.getOptionPremium();
				
			}
			else if(model.getOptionType().equals("C") && model.getTransactionType().equals("SELL"))
			{
				investment-=model.getOptionPremium();
				
			}
			else if(model.getOptionType().equals("P") && model.getTransactionType().equals("BUY"))
			{
				investment+=model.getOptionPremium();
				
			}
			else if(model.getOptionType().equals("P") && model.getTransactionType().equals("SELL"))
			{
				investment-=model.getOptionPremium();
				
			}
		}
		return investment*100;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		//process();
		
		
		
		
	}
	
	
	
	public  double populateModelToPlot(double stockPriceAtExpiration)
	{
		List<Double> pnlList = calculatePnL(stockPriceAtExpiration);
		//add all pnl for this stockPrice
		double sum = 0 ;
		for(Double pnl:pnlList)
		{
			sum+=pnl;
		}
	
		//System.out.println("Sum is "+sum);
		map.put(String.valueOf(stockPriceAtExpiration), sum);
		return sum;
	}
	
	public  List<Double> calculatePnL(double stockPriceAtExpiration)
	{

		List<Double> pnlList = new ArrayList<Double>();
		
		for(OptionsModel model:optionsModelList)
		{
			double pnl = 0;
			boolean isProfit = false;
			//System.out.println("Model option type "+model.getOptionType());
			//System.out.println("Model tran type "+model.getTransactionType());
			if(model.getOptionType().equals("C") && model.getTransactionType().equals("BUY"))
			{
				//ok its call option
				double profitableStockPrice = model.getStrikePrice() + model.getOptionPremium();
				
				if(stockPriceAtExpiration < profitableStockPrice)
				{
					if(stockPriceAtExpiration < model.getStrikePrice())
					{
						//max loss is option premium
						pnl = model.getOptionPremium()*100;
					}
					else
					{
						pnl = (profitableStockPrice - stockPriceAtExpiration) * 100;
					}
					//System.out.println("There is a loss of "+ pnl);
					
				}
				else if(stockPriceAtExpiration == profitableStockPrice)
				{
					//System.out.println("There is no profit or loss, its break even");
				}
				else
				{
					pnl = (stockPriceAtExpiration - profitableStockPrice)*100;
					//System.out.println("There is a profit of "+pnl);
					isProfit=true;
						
				}
					
			}
			else if(model.getOptionType().equals("C") && model.getTransactionType().equals("SELL"))
			{
				//ok its call option
			
				double lossStockPrice = model.getStrikePrice() + model.getOptionPremium();
				
				if(stockPriceAtExpiration < lossStockPrice)
				{
					if(stockPriceAtExpiration < model.getStrikePrice())
					{
						pnl = model.getOptionPremium() * 100;
					}
					else
					{
						pnl = (lossStockPrice - stockPriceAtExpiration) * 100;
					}
					//System.out.println("There is a profit of "+ pnl);
					isProfit=true;
					
				}
				else if(stockPriceAtExpiration == lossStockPrice)
				{
					//System.out.println("There is no profit or loss, its break even");
				}
				else
				{
					pnl = (stockPriceAtExpiration - lossStockPrice)*100;
					System.out.println("There is a loss of "+pnl);
						
				}
			}
			else if(model.getOptionType().equals("P") && model.getTransactionType().equals("BUY"))
			{
				//ok its put option
				double profitableStockPrice = model.getStrikePrice() - model.getOptionPremium();
				
				if(stockPriceAtExpiration < profitableStockPrice)
				{
					pnl = (profitableStockPrice - stockPriceAtExpiration) * 100;
					//System.out.println("There is a profit of "+ pnl);
					isProfit=true;
					
				}
				else if(stockPriceAtExpiration == profitableStockPrice)
				{
					//System.out.println("There is no profit or loss, its break even");
				}
				else
				{
					if(stockPriceAtExpiration < model.getStrikePrice())
					{
						pnl = (stockPriceAtExpiration - (model.getStrikePrice() - model.getOptionPremium()) ) * 100;
					}
					else
					{
						pnl = model.getOptionPremium() * 100;
					}
					//System.out.println("There is a loss of "+pnl);
						
				}
			}
			else if(model.getOptionType().equals("P") && model.getTransactionType().equals("SELL"))
			{
				//ok its put option
				double profitableStockPrice = model.getStrikePrice() - model.getOptionPremium();
				
				if(stockPriceAtExpiration < profitableStockPrice)
				{
					pnl = (profitableStockPrice - stockPriceAtExpiration) * 100;
					//System.out.println("There is a loss of "+ pnl);
					
				}
				else if(stockPriceAtExpiration == profitableStockPrice)
				{
					//System.out.println("There is no profit or loss, its break even");
				}
				else
				{
					if(stockPriceAtExpiration < model.getStrikePrice())
					{
						pnl = (stockPriceAtExpiration - (model.getStrikePrice() - model.getOptionPremium()) ) * 100;
					}
					else
					{
						pnl = model.getOptionPremium() * 100;
					}
					//System.out.println("There is a profit of "+pnl);
					isProfit=true;
						
				}
			}
			if(!isProfit)
			{
				pnlList.add(-1*pnl);
			}
			else
			{
				pnlList.add(pnl);
			}
					
		}
		return pnlList;
		
		
	}

}
