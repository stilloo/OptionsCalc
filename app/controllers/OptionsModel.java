package controllers;

import java.util.Date;

public class OptionsModel {
	public double getStrikePrice() {
		return strikePrice;
	}
	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}
	public double getOptionPremium() {
		return optionPremium;
	}
	public void setOptionPremium(double optionPremium) {
		this.optionPremium = optionPremium;
	}
	public String getOptionType() {
		return optionType;
	}
	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}
	public Date getOptionDate() {
		return optionDate;
	}
	public void setOptionDate(Date optionDate) {
		this.optionDate = optionDate;
	}
	private double strikePrice;
	private double optionPremium;
	private String optionType; // call, put
	private Date optionDate;
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public int getContracts() {
		return contracts;
	}
	public void setContracts(int contracts) {
		this.contracts = contracts;
	}
	private String transactionType; // buy or sell
	private String ticker;
	private int contracts;
}
