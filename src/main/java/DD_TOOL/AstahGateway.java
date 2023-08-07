package DD_TOOL;

import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IWindow;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate.UnExpectedException;

public class AstahGateway {

	private IWindow mWindow = null;
	private ProjectAccessor mProjectAccessor = null;
	
	public AstahGateway (IWindow win) {
		mWindow = win;
	    try {
	    	if (mProjectAccessor == null) {
		        AstahAPI api = AstahAPI.getAstahAPI();
		        mProjectAccessor = api.getProjectAccessor();
		        mProjectAccessor.getProject();
	    	}
	    } catch (Exception e) {
	    	mProjectAccessor = null;
	    }
	}
	
	
	public void showMessage (String msg) {
		if (mWindow != null) {
			JOptionPane.showMessageDialog(mWindow.getParent(), msg);
		}
	}
	
	
}
