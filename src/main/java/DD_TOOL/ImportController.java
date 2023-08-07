package DD_TOOL;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImportController {
	private AstahGateway mGateway = null;
	private FileInputStream mReader = null;
	private String mFilePath = "";
	
	public ImportController (AstahGateway gw, String input) {
		mGateway = gw;
		mFilePath = input;
		try {
			mReader = new FileInputStream(input);
		} catch (FileNotFoundException e) {
			mGateway.showMessage("ERROR: can not open file: " + input);
		}
	}
	
	@Override
	protected void finalize() {
		if (mReader != null) {
			try {
				mReader.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	public void run () {
		mGateway.showMessage("import runing: " + mFilePath);
	}
}
