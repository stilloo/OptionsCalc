package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import org.codehaus.groovy.util.StringUtil;

import antlr.StringUtils;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void optionscalc() {
        render(params);
    }
    
    public static void oldoptionscalc() {
        render(params);
    }
    
    public static void history() {
        render(params);
    }
    
    public static void login() {
        render(params);
    }

    public static void pnlstatus() {
    	Map<String,Long> posInvMap  = new HashMap<String, Long>();
    	 if(session.get("username") !=null)
    	 {
	    	OptionsCalculator calculator = new OptionsCalculator();
	    	
	    	try {
	    		posInvMap =calculator.getPositionForPnLAjax(session.get("username"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	//posInvMap.put("aaplpos",12L);
    	//posInvMap.put("googpos",123L);
    	System.out.println("GOT request");
        renderJSON(posInvMap);
    }

    
    public static void checkLogin() {
    	Map<String,String> map = new HashMap<String, String>();
    	map.put("user", params.get("user"));
    	map.put("pass", params.get("pass"));
    	boolean loginFound = false;
    	try {
			loginFound=  LoginHandler.checkLogin(map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(loginFound)
    	{
    		 session.put("username", params.get("user"));
    		//redirect to optionscalc
    		if(session.get("mainpageURL") !=null)
    		{
    			redirect(session.get("mainpageURL"));
    		}
    		else
    		{
    			redirect("/application/optionscalc");
    		}
    	}
    	else
    	{
    		flash.put("loginerror", "Invalid userid or password");
    		redirect("/application/login");
    	}
    }
    
  
//    public static void showHistory(String ticker, String optionsdate) {
//    	System.out.println(ticker);
//    	System.out.println("optionsdate "+optionsdate);
//    	OptionsData optionsData = null;
//    	try {
//    		optionsData = OptionsGetter.getHistory(ticker, optionsdate);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        render(optionsData);
//    }
    
    public static void showHistory(String ticker, String optionsdate,String expirationDate) {
//     	System.out.println(ticker);
//    	System.out.println(optionsdate);
//    	System.out.println("exp " +expirationDate);
    	OptionsData optionsData = null;
    	try {
    		optionsData = OptionsGetter.getHistory(ticker, optionsdate, expirationDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        render(optionsData);
    }
    
    public static void showOptionDetail(String ticker,String strike,String expiryDate,String type)
    {
    	//System.out.println("Came inside showOptionDetail");
    	OptionsDetailData detailData = null;	
    	try {
			detailData = OptionsGetter.getHistoryOptionDetail(ticker, strike, expiryDate,type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	render(detailData);
    	
    }
    
    public static void sayHello(String myName) {
    	//System.out.println("Rendering");
    	Map<String, Double>  renderMap = null;
    	Map<String, Double>  positionsMap = null;
    	Map<String, String>  positionsURLProfitMap = null;
			List<Map<String,Double>> m = new ArrayList<Map<String,Double>>();
			String url = request.getBase() + request.url ;
		    boolean isSave = Boolean.parseBoolean(params.get("savepos"));    	
    	try {
    		System.out.println("URLLLL is "+url);
    		url = url.replaceAll("sayhello","optionscalc");
    		 String buyOrSell =  null;
			 String contracts =  null;
			 String expirationDate =  null;
			 String strike = null;
			 String callOrPut =  null;
			 String premium= null;
			 String ticker = params.get("ticker");
			 String stockPrice = params.get("stockPrice");
			 String username = params.get("username");
			 String positionName=params.get("posname");
			 Iterator<String> keyItr = params.data.keySet().iterator();
			
			// int size = params.data.size() - 2;
			 
			// System.out.println("size is "+size);
			 //ok its multiple of 6 then
			 int noOfIndexes =   Integer.parseInt(params.get("POITableCount"));
				
			 //System.out.println(("POITableCount is "+noOfIndexes));
			 
			 List<Map<String,String>> positionsList = new ArrayList<Map<String,String>>();
				
			 
			 //System.out.println("index is "+noOfIndexes);
			// System.out.println(params.toString());
			 //now loop over
			 for(int i =1;i<=noOfIndexes;i++)
			 {
				 Map<String,String> map = new HashMap<String, String>();
				 buyOrSell =  params.get("longshort_"+i);
				 if(buyOrSell == null || buyOrSell.trim().equals("--")) continue;
				 //System.out.println("buyOrSell is "+buyOrSell);
				 map.put("buyOrSell", buyOrSell);
				 contracts = params.get("contracts_"+i);
				 //System.out.println("Contracts is "+contracts);
				 map.put("contracts", contracts);
				 expirationDate = params.get("expiration_1");
				 map.put("expiration", expirationDate);
				 strike = params.get("strike_"+i);
				 map.put("strike", strike);
				 callOrPut = params.get("type_"+i);
				 map.put("callOrPut", callOrPut);
				 premium=params.get("premium_"+i);
				 map.put("premium", premium);
				 //System.out.println("map is "+map);
				 positionsList.add(map);
				 
			 }
			 
			 OptionsCalculator calculator = new OptionsCalculator();
			 renderMap = calculator.process(positionsList,ticker,stockPrice,username,positionName,url,isSave);
		  	 renderMap.put("inv", calculator.getInvestment());
		  	 if(session.get("username") !=null)
			 {
		  		 positionsMap= calculator.getPositions(username);
		  		positionsURLProfitMap=calculator.getPositionsURLProfit(username);
			 }
			 m.add(renderMap);
			    
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	 int noOfIndexes =   Integer.parseInt(params.get("POITable1Count"));
			//System.out.println("compare is "+compare);
    	if(noOfIndexes > 0)
    	{
    	try {
    		
   		 String buyOrSell =  null;
			 String contracts =  null;
			 String expirationDate =  null;
			 String strike = null;
			 String callOrPut =  null;
			 String premium= null;
			 String ticker = params.get("ticker");
			 String stockPrice = params.get("stockPrice");
			 Iterator<String> keyItr = params.data.keySet().iterator();
			
			// int size = params.data.size() - 2;
			 
			 List<Map<String,String>> positionsList = new ArrayList<Map<String,String>>();
				
			// System.out.println("size is "+size);
			 //ok its multiple of 6 then
			  noOfIndexes =   Integer.parseInt(params.get("POITable1Count"));
			 
			// System.out.println("POITable1Count is "+noOfIndexes);
			   
			 
			// System.out.println(params.toString());
			 //now loop over
			 for(int i =1;i<=noOfIndexes;i++)
			 {
				 Map<String,String> map = new HashMap<String, String>();
				 buyOrSell =  params.get("longshort_"+i+"c");
				// System.out.println("b "+buyOrSell);
				 if(buyOrSell == null || buyOrSell.trim().equals("--")) continue;
				 //System.out.println("buyOrSell is "+buyOrSell);
				 map.put("buyOrSell", buyOrSell);
				 contracts = params.get("contracts_"+i+"c");
				 //System.out.println("Contracts is "+contracts);
				 map.put("contracts", contracts);
				 expirationDate = params.get("expiration_1");
				 map.put("expiration", expirationDate);
				 strike = params.get("strike_"+i+"c");
				 map.put("strike", strike);
				 callOrPut = params.get("type_"+i+"c");
				 map.put("callOrPut", callOrPut);
				 premium=params.get("premium_"+i+"c");
				 map.put("premium", premium);
				 //System.out.println("map is "+map);
				 positionsList.add(map);
				 
			 }
			 if(!positionsList.isEmpty())
			 {
			 OptionsCalculator calculator = new OptionsCalculator();
			 renderMap = calculator.process(positionsList,ticker,stockPrice,null,null,url,isSave);
			 renderMap.put("inv_c",calculator.getInvestment());
			 //System.out.println("rendermap "+renderMap);
			 m.add(renderMap);
			 }
			    
			 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	}
    	OptionsModelData modelData = new OptionsModelData();
    	modelData.m=m;
    	modelData.positionsMap=positionsMap;
    	modelData.positionURLProfitMap=positionsURLProfitMap;
       //System.out.println("M is "+m);
        render(modelData);
    }
    
    
    public static void share() {
    	//System.out.println(params);
       render(params);
    }

}