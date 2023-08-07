package DD_TOOL;

import java.awt.FileDialog;
import java.awt.Frame;

import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ImportAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
	    AstahGateway gw = new AstahGateway(window);
	    
	    // select file
	    FileDialog fd = new FileDialog(new Frame("select file"), "Choose translated input file", FileDialog.LOAD);
	    fd.setFile("*.txt");
	    fd.setVisible(true);
	    String filePath = fd.getDirectory() + fd.getFile();
	    
	    // running import file's data to astah project
	    ImportController controller = new ImportController(gw, filePath);
	    controller.run();
		return null;
	}
}