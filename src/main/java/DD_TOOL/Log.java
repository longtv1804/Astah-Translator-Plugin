package DD_TOOL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Log {
	private static Log mIns = null;
	
	private static String mFilePath = "C:\\Users\\PL_FR_DELL\\Desktop\\abc\\log.txt";
	
	private Log () {
		
	}

	private static void printLog (String fileName, String msg) {
		if (mIns == null) {
			mIns = new Log();
		}
		if (mFilePath != null) {
			try {
				String logmsg = fileName + ": " + msg + "\n";
				Files.write(Paths.get(mFilePath), logmsg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	public static void d (String fileName, String msg) {
		printLog(fileName, msg);
	}
	public static void e (String fileName, String msg) {
		printLog(fileName, msg);
	}
}
