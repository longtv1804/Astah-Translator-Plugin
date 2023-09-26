package DD_TOOL;

public class CommonUtils {
	public static String DDTAG = "DDTAG";
	
	// constants
	public static final int ITEM_TYPE_PACKAGE 				= 1;
	public static final int ITEM_TYPE_CLASS 					= 2;
	public static final int ITEM_TYPE_CLASS_FUNC 				= 3;
	public static final int ITEM_TYPE_SEQ		 				= 4;
	public static final int ITEM_TYPE_LIFELINE 				= 5;
	public static final int ITEM_TYPE_LIFELINEFUNC 			= 6;
	public static final int ITEM_TYPE_COMMENT 				= 7;
	public static final int ITEM_TYPE_COMBINEFRAG 			= 8;
	public static final int ITEM_TYPE_REFERENCESEQUENCE 		= 9;
	
	public static final int ITEM_TYPE_SEQ_STATE_MACHINE		= 10;
	public static final int ITEM_TYPE_SMC_STATE				= 12;
	public static final int ITEM_TYPE_SMC_TRANSITION			= 13;
	
	public static String DEFAULT_VALUE_FOR_ITEM = "999000234999"; // dummy data
	
	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
	
	public static String getTypeString (int type) {
		switch (type) {
		case ITEM_TYPE_PACKAGE: 			return "ITEM_TYPE_PACKAGE";
		case ITEM_TYPE_CLASS: 				return "ITEM_TYPE_CLASS";
		case ITEM_TYPE_CLASS_FUNC: 			return "ITEM_TYPE_CLASS_FUNC";
		case ITEM_TYPE_SEQ: 				return "ITEM_TYPE_SEQ";
		case ITEM_TYPE_LIFELINE: 			return "ITEM_TYPE_LIFELINE";
		case ITEM_TYPE_LIFELINEFUNC: 		return "ITEM_TYPE_LIFELINEFUNC";
		case ITEM_TYPE_COMMENT: 			return "ITEM_TYPE_COMMENT";
		case ITEM_TYPE_COMBINEFRAG: 		return "ITEM_TYPE_COMBINEFRAG";
		case ITEM_TYPE_REFERENCESEQUENCE: 	return "ITEM_TYPE_REFERENCESEQUENCE";
		case ITEM_TYPE_SEQ_STATE_MACHINE: 	return "ITEM_TYPE_SEQ_STATE_MACHINE";
		case ITEM_TYPE_SMC_STATE: 			return "ITEM_TYPE_SMC_STATE";
		case ITEM_TYPE_SMC_TRANSITION: 		return "ITEM_TYPE_SMC_TRANSITION";
		default:
			return "";
		}
	}
}
