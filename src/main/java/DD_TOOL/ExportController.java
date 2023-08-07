package DD_TOOL;

import java.io.FileWriter;
import java.io.IOException;

public class ExportController {
	private AstahGateway mGateway = null;
	private FileWriter mWr = null;
	String mDirPath = null;
	
	public ExportController (AstahGateway gw, String dirPath) {
		mGateway = gw;
		mDirPath = dirPath;
		try {
			mWr = new FileWriter(new java.io.File(dirPath + "\\ExportData.txt"));
		} catch (IOException e) {
			mGateway.showMessage("ERROR: can not create FileWriter: " + e.getMessage());
		}
	}
	
	@Override
	protected void finalize() {
		if (mWr != null) {
			try {
				mWr.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	public void run () {
		mGateway.showMessage("export runing: " + mDirPath);
	}
}
