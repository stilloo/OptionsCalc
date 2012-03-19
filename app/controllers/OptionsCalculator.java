/**
 * 
 */
package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anubha_phadnis
 *
 */
public class OptionsCalculator {

	private  List<OptionsModel> optionsModelList = new ArrayList<OptionsModel>();
	private  Map<String, Double> map = new LinkedHashMap<String, Double>();
	private double investment = 0.0;

	public  Map<String, Double> process(List<Map<String,String>> positionsList , String ticker,String stockPriceStr) throws Exception
	{
		double minStrike =Double.MAX_VALUE;
		double maxStrike = Double.MIN_VALUE;
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
				
				
			//	System.out.println("Model here option type "+model.getOptionType());
			//System.out.println("Model here tran type "+model.getTransactionType());
				optionsModelList.add(model);
			}
			
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
