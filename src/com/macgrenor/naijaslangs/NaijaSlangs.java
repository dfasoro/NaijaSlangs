package com.macgrenor.naijaslangs;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.stringpattern.PatternRepository;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;

import org.macgrenor.json.JSONArray;
import org.macgrenor.json.JSONObject;

public class NaijaSlangs extends Application {
	public static NaijaSlangs INSTANCE;
	private static Hashtable SLANGS;
	private static ApplicationMenuItem[] menuToRun;
	private final static String DATE_KEY = "__date_ver__";
	protected final static String INFO_KEY = "__info_ver__";
	
	public NaijaSlangs() {
		NaijaSlangs.INSTANCE = this;
		NaijaSlangs.menuToRun = new ApplicationMenuItem[] { new MyMenuItem() };
		NaijaSlangs.SLANGS = DataStore.loadSlangData();
				
		//NaijaSlangs.SLANGS = null; //@TODO remove.
		addToPatternRepository();
		
		
		try {
			while (true) {
				readSlangs();
				Thread.sleep(10 * 60 * 1000);				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//set up a 30minute Thread.
		//to be reset on every call to readSlangs
    }
	
	public synchronized void readSlangs() {
		try {
			JSONObject slang_json = new JSONObject(getSlangData());
			JSONObject data = slang_json.getJSONObject("data");
			
			if (data.length() > 0) {	
				if (slang_json.optBoolean("clear", false)) {
					NaijaSlangs.SLANGS = null;
					//Clear command from Server
				}
				String info_data = slang_json.optString(INFO_KEY);
				
				if (NaijaSlangs.SLANGS == null) {
					NaijaSlangs.SLANGS = new Hashtable(data.length() + 10);
					DataStore.putSlangData(NaijaSlangs.SLANGS);
				}
				if (info_data != null && info_data.length() > 0) {
					NaijaSlangs.SLANGS.put(INFO_KEY, info_data);
				}				
				
				NaijaSlangs.SLANGS.put(DATE_KEY, slang_json.getString(DATE_KEY));
				
				Enumeration data_keys = data.keys();
				
				while (data_keys.hasMoreElements()) {
					String key = (String) data_keys.nextElement();
					JSONArray meaning = data.getJSONArray(key);
					Vector meaning_list = new Vector(2);
					meaning_list.addElement(meaning.get(0));
					meaning_list.addElement(meaning.get(1));
					NaijaSlangs.SLANGS.put(key.toUpperCase(), meaning_list);
				}
				DataStore.saveSlangData();
				
				addToPatternRepository();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void addToPatternRepository() {
		if (NaijaSlangs.SLANGS == null || NaijaSlangs.SLANGS.size() == 0) {
			//readSlangs();
			return;
		}
		
		ApplicationDescriptor appDesc = new ApplicationDescriptor(ApplicationDescriptor.currentApplicationDescriptor(), new String[]{"homescreen"});
		
		PatternRepository.removePatterns(appDesc); //Clear
		
		Enumeration slang_keys = NaijaSlangs.SLANGS.keys();
		
		while (slang_keys.hasMoreElements()) {
			String pattern = (String) slang_keys.nextElement();			
			if (DATE_KEY.equals(pattern)) continue;
			
			StringBuffer regex = new StringBuffer(10 + (pattern.length() * 4));
			regex.append("\\b");
			for (int i = 0; i < pattern.length(); i++) {
				String token = pattern.substring(i, i + 1);
				regex.append("[" + token.toUpperCase() + token.toLowerCase() + "]");
			}
			regex.append("\\b");
						
			pattern = regex.toString();
			
			PatternRepository.addPattern(appDesc, pattern, PatternRepository.PATTERN_TYPE_REGULAR_EXPRESSION, NaijaSlangs.menuToRun);			
		}
	}
	
	protected static String getMeaning(String slang) {
		Hashtable Slangs = DataStore.loadSlangDataRuntime();
		Vector data = (Vector)Slangs.get(slang.toUpperCase());
		if (data != null) {
			return ((String)(data).elementAt(0)) + "\n\nBy: " + ((String)(data).elementAt(1));
		}
		
		return null;
	}
	
	private String getSlangData() {
		String last_date = "0";
		if (NaijaSlangs.SLANGS != null) {
			last_date = ((String)NaijaSlangs.SLANGS.get(DATE_KEY));
		}
		
		String s = null;
		
		//return "{\"__date_ver__\":\"20130105015441\",\"data\":{\"LOL\":[\"Laughing out Loud\",\"@dfasoro\"],\"Issokay\":[\"It's Okay\",\"@dfasoro\"],\"Issorai\":[\"It's alright\",\"@dfasoro\"],\"Issoray\":[\"It's alright\",\"@dfasoro\"],\"LWTMB\":[\"Laff Wan Turn My Belle\",\"@dfasoro\"],\"Hian\":[\"Haba\",\"@dfasoro\"],\"Kilode\":[\"Wetin happen?\",\"@dfasoro\"],\"Isnor\":[\"It's not\",\"@dfasoro\"]}}";
		
		ClientHttpRequest conn = null;
		try {
			conn = new ClientHttpRequest("http://macgrenor.com/slangs/slang.php", 60 * 1000);
			conn.setParameter(DATE_KEY, "" + last_date);
			s = conn.postAndRetrieve();
		} catch (Exception e1) {
			s = null;
			e1.printStackTrace();
		}
		finally {
			if (conn != null) conn.closeAll();
		}
		
		return s;
		//return "{\"__date_ver__\": 109098997, \"data\": {\"LOL\": [\"Laughing Out Loud\", \"@yvonne\"], \"LWKMD\": [\"Laff Wan Kill Me Die\", \"@annette\"], \"IJGB\": [\"I Just Got Back\", \"@naomi\"]}}";
	}
	
	/**
     * Entry point for application
     * @param args Command line arguments (not used)
     */ 
	public static void main(final String[] args) {
		if (args[0].equals("autostartup")) {
			new NaijaSlangs();
		}
		else if (args[0].equals("homescreen")) {
			(new NaijaSlangsUI()).enterEventDispatcher();
		}
    }


}
