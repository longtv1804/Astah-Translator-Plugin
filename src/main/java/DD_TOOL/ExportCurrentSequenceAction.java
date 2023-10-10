package DD_TOOL;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ExportCurrentSequenceAction implements IPluginActionDelegate {
	public Object run(IWindow window) throws UnExpectedException {
		try {
			AstahGateway gw = new AstahGateway(window);
			int needExecute = JOptionPane.YES_OPTION;
			if (gw.isCurrentDiagramExportedData() == true) {
				String msg = 	"This diagram existed translated data!\n" 	+ 
								"maybe it was translated already!\n"		+
								"do you want to continue to export data???";
				String title = 	"Confirm Export Data";
				needExecute = JOptionPane.showOptionDialog(window.getParent(), msg, title, JOptionPane.YES_NO_OPTION, 0, null, null, null);
			}
			if (needExecute == JOptionPane.YES_OPTION) {
			    // select directory for output
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				Integer res = jfc.showSaveDialog(null);
				
				if (res == JFileChooser.APPROVE_OPTION) {
				    File file = jfc.getSelectedFile();
				    
				    // create gateway, controller -> run export
				    Log.setPath(file.getAbsolutePath() + "/Export.log");
				    ExportCurrentSequenceController controller = new ExportCurrentSequenceController(gw, file.getAbsolutePath());
				    controller.run();
				} else if (res == JFileChooser.ERROR_OPTION) {
					JOptionPane.showMessageDialog(window.getParent(), "can not get output directory");
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window.getParent(), e.getMessage());
		}
	    return null;
	}
	
}

