package DD_TOOL;

import java.awt.FileDialog;
import java.awt.Frame;

import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ImportAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
	    // select file
	    FileDialog fd = new FileDialog(new Frame("select file"), "Choose translated input file", FileDialog.LOAD);
	    fd.setFile("*.txt");
	    fd.setVisible(true);
	    
	    if (CommonUtils.isEmpty(fd.getDirectory()) == false && CommonUtils.isEmpty(fd.getFile()) == false) {
		    String filePath = fd.getDirectory() + fd.getFile();
		    
		    // running import file's data to astah project
		    Log.setPath(fd.getDirectory() + "/Import.log");
		    AstahGateway gw = new AstahGateway(window);
		    ImportController controller = new ImportController(gw, filePath);
		    try {
		    	controller.run();
		    } catch (Exception e) {
				gw.showMessage("Exception on import(): " + e.getMessage());
			}
	    }
		return null;
	}
}