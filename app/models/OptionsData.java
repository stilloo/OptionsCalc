package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OptionsData {

	public List<Map<String,String>> datalist = new ArrayList<Map<String,String>>();
	public List<String> expirationDates = new ArrayList<String>();
	public String selectedExpirationDate;
	public String ticker;
	public String optionsDate;
	public String stockPrice;
	
}
