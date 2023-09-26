package DD_TOOL;

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TranslatedDataHolder {
	private final static String TAG = "TranslatedDataHolder";
	private String mFilePath = "";
	
	private final static int STEP_START = 0;
	private final static int STEP_READ_DATA = 1;
	
	private class TranslatedData {
		String data = null;
		int type = -1;
		
		public TranslatedData (String data, int type) {
			this.data = data;
			this.type = type;
		}
	}
	
	private class TranslatedCombineFragmentData extends TranslatedData {
		ArrayList<String> mGuardList;
		
		TranslatedCombineFragmentData (String data, int type) {
			super(data, type);
		}
	}
	
	private class TranslatedStateTransition extends TranslatedData {
		String trigger;
		String guard;
		String action;
		TranslatedStateTransition (String data, int type) {
			super(data, type);
			trigger = "";
			guard = "";
			action = "";
		}
		
		void format () {
			if (trigger.equals(CommonUtils.DEFAULT_VALUE_FOR_ITEM)) {
				trigger = "";
			}
			if (guard.equals(CommonUtils.DEFAULT_VALUE_FOR_ITEM)) {
				guard = "";
			}
			if (action.equals(CommonUtils.DEFAULT_VALUE_FOR_ITEM)) {
				action = "";
			}
		}
	}

	private HashMap<String, TranslatedData> mholder;
	private static final String PATTERN_IMPORT_DATA = "([0-9]+) ([0-9]+) ([0-9]+)";
	
	TranslatedDataHolder (String filePath) throws Exception {
		mFilePath = filePath;
		mholder = new HashMap<>();
		init();
	}
	
	private void init () throws Exception {
		if (mFilePath == null || mFilePath.isEmpty()) {
			Log.d(TAG, "FilePath Error: " + mFilePath);
			throw new Exception("incorect input's filePath");
		}
		
		//InputStreamReader sc = new InputStreamReader(new FileInputStream(mFilePath), "UTF-8");

		Scanner sc = new Scanner(new FileInputStream(mFilePath), "UTF-8");
		Pattern rg = Pattern.compile(PATTERN_IMPORT_DATA);
		
		String id = null;
		int type = 0;
		String dataStr = null;
		Matcher mc = null;

		TranslatedCombineFragmentData fmData = null;
		TranslatedStateTransition stateData = null;
		int step = STEP_START;
		int dataCount = 0;
		int smc_trs_triggerCount 	= 0;
		int smc_trs_guardCount 		= 0;
		int smc_trs_actionCount 	= 0;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			switch (step) {
				case STEP_START:
					mc = rg.matcher(line);
					if (mc.find()) {
						id = mc.group(1);
						try {
							type = Integer.parseInt(mc.group(2));
							dataCount = Integer.parseInt(mc.group(3));
							
							if (type == CommonUtils.ITEM_TYPE_SMC_TRANSITION) {
								smc_trs_triggerCount 	= (dataCount & 0x00ff0000) >> 16;
								smc_trs_guardCount		= (dataCount & 0x0000ff00) >> 8;
								smc_trs_actionCount		= (dataCount & 0x000000ff) >> 0;
								dataCount = smc_trs_triggerCount + smc_trs_guardCount + smc_trs_actionCount;
							}
						} catch (NumberFormatException e) {
							Log.d(TAG, "NumberFormatException: initHolder: type: " + mc.group(2) + " datCount: " + mc.group(3));
							throw new Exception("Holder init NumberFormatException: line=" + line);
						}
						step = STEP_READ_DATA;
						fmData = null;
						stateData = null;
						dataStr = "";
					} else {
						Log.d(TAG, "ERROR: initHolder() Regex not match: [" + line + "]");
						throw new Exception("TranlatedData is not Match");
					}
					break;
					
				case STEP_READ_DATA:
					dataCount --;
					if (line.equals(CommonUtils.DEFAULT_VALUE_FOR_ITEM)) {
						line = "";
					}
					
					if (type == CommonUtils.ITEM_TYPE_COMMENT) {
						dataStr += CommonUtils.isEmpty(dataStr) ? line : ("\n" + line);
						if (dataCount == 0) {
							mholder.put(id, new TranslatedData(dataStr, CommonUtils.ITEM_TYPE_COMMENT));
							step = STEP_START;
						}
					} else if (type == CommonUtils.ITEM_TYPE_COMBINEFRAG) {
						if (fmData == null) {
							fmData = new TranslatedCombineFragmentData(line, CommonUtils.ITEM_TYPE_COMBINEFRAG);
						} else {
							if (fmData.mGuardList == null) {
								fmData.mGuardList = new ArrayList<>();
							}
							fmData.mGuardList.add(line);
						}
						if (dataCount == 0) {
							mholder.put(id, fmData);
							fmData = null;
							step = STEP_START;
						}
					} else if (type == CommonUtils.ITEM_TYPE_SMC_TRANSITION) {
						if (stateData == null) {
							stateData = new TranslatedStateTransition("", CommonUtils.ITEM_TYPE_SMC_TRANSITION);
						}
						
						if (smc_trs_triggerCount > 0) {
							stateData.trigger 	+= CommonUtils.isEmpty(stateData.trigger) 	? line : ("\n" + line);
							smc_trs_triggerCount--;
						} 
						else if (smc_trs_guardCount > 0) {
							stateData.guard 	+= CommonUtils.isEmpty(stateData.guard) 	? line : ("\n" + line);
							smc_trs_guardCount--;
						} 
						else if (smc_trs_actionCount > 0){
							stateData.action 	+= CommonUtils.isEmpty(stateData.action) 	? line : ("\n" + line);
							smc_trs_actionCount--;
						}
						if (dataCount == 0) {
							stateData.format();
							mholder.put(id, stateData);
							step = STEP_START;
						}
					} else {
						dataStr = line;
						mholder.put(id, new TranslatedData(dataStr, type));
						step = STEP_START;
					}
					break;
					
				default:
					break;
			}

		}
		sc.close();
		Log.d(TAG, "init holder is successed");
	}
	 
	public String getData (String key) {
		String res = null;
		
		if (mholder.containsKey(key) == true) {
			TranslatedData data = mholder.get(key);
			res = data.data;
		}
		
		return res;
	}
	
	public ArrayList<String> getGuardsForCombinedFragment(String key) {
		if (mholder.containsKey(key) == true) {
			TranslatedData data = mholder.get(key);
			if (data instanceof TranslatedCombineFragmentData) {
				return ((TranslatedCombineFragmentData) data).mGuardList;
			}
		}
		return null;
	}
	
	public String getTrigerForState (String key) {
		if (mholder.containsKey(key) == true) {
			TranslatedData data = mholder.get(key);
			if (data instanceof TranslatedStateTransition) {
				return ((TranslatedStateTransition) data).trigger;
			}
		}
		return null;
	}
	public String getGuardForState (String key) {
		if (mholder.containsKey(key) == true) {
			TranslatedData data = mholder.get(key);
			if (data instanceof TranslatedStateTransition) {
				return ((TranslatedStateTransition) data).guard;
			}
		}
		return null;
	}

	public String getActionForState (String key) {
		if (mholder.containsKey(key) == true) {
			TranslatedData data = mholder.get(key);
			if (data instanceof TranslatedStateTransition) {
				return ((TranslatedStateTransition) data).action;
			}
		}
		return null;
	}
	
	public int getType (String key) {
		if (mholder.containsKey(key) == true) {
			return mholder.get(key).type;
		}
		
		return -1;
	}
}
