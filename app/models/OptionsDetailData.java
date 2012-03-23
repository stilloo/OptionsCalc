package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class OptionsDetailData {

	public String ticker;
	public Map<String,Double> optionsDatePrice = new LinkedHashMap<String,Double>();
	public List<Double> stockPrice = new ArrayList<Double>();
	
}
