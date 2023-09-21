package DD_TOOL;

public class CommonUtils {
	public static String DDTAG = "DDTAG";
	
	// constants
	public static int ITEM_TYPE_PACKAGE 				= 1;
	public static int ITEM_TYPE_CLASS 					= 2;
	public static int ITEM_TYPE_CLASS_FUNC 				= 3;
	public static int ITEM_TYPE_SEQ		 				= 4;
	public static int ITEM_TYPE_LIFELINE 				= 5;
	public static int ITEM_TYPE_LIFELINEFUNC 			= 6;
	public static int ITEM_TYPE_COMMENT 				= 7;
	public static int ITEM_TYPE_COMBINEFRAG 			= 8;
	public static int ITEM_TYPE_REFERENCESEQUENCE 		= 9;
	
	public static int ITEM_TYPE_SEQ_STATE_MACHINE		= 10;
	public static int ITEM_TYPE_SMC_STATE				= 12;
	public static int ITEM_TYPE_SMC_TRANSITION			= 13;
	
	public static String DEFAULT_VALUE_FOR_ITEM = "999000234999"; // dummy data
	
	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
}
