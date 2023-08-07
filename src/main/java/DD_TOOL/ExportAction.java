package DD_TOOL;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ExportAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
	    // select directory for output
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		Integer res = jfc.showSaveDialog(null);
		
		if (res == JFileChooser.APPROVE_OPTION) {
		    File file = jfc.getSelectedFile();
		    
		    // create gateway, controller -> run export
		    AstahGateway gw = new AstahGateway(window);
		    ExportController controller = new ExportController(gw, file.getAbsolutePath());
		    controller.run();
		} else {
			JOptionPane.showMessageDialog(window.getParent(), "can not get ouput directory");
		}
	    return null;
	}
}