package DD_TOOL;

import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.exception.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ModelFinder;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IWindow;

public class AstahGateway {
	private static final String TAG = "AstahGateway";

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
	
	@Override
	public void finalize() {
		mWindow = null;
	}
	
	public void showMessage (String msg) {
		if (mWindow != null) {
			JOptionPane.showMessageDialog(mWindow.getParent(), msg);
		}
	}
	
	public void save () {
		try {
			mProjectAccessor.save();
		} catch (Exception e) {
			Log.e(TAG, "save() " + e.getMessage());
		}
	}
	
	
	/* =====================================================================================================
	 * 			get items
	 * =====================================================================================================
	 */
	
	public String getProjectName () {
		String res = null;
		if (mProjectAccessor != null) {
			try {
				res = mProjectAccessor.getProject().getName();
			} catch (ProjectNotFoundException e) {
				// ignore
			}
		}
		return res;
	}
	
	public INamedElement[] getPackages () {
		try {
			return mProjectAccessor.findElements(IPackage.class);
		} catch (Exception e) {
			Log.e(TAG, "getPackages() " + e.getMessage());
			return null;
		}
	}
	
	public INamedElement[] getClasses() {
		INamedElement[] res = null;
		if (mProjectAccessor != null) {
		    try {
				res = mProjectAccessor.findElements(IClass.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "getClasses() " + e.getMessage());
			}
		}
		return res;
	}
	
	public INamedElement[] getSequenceDiagrams() {
		INamedElement[] dgms = null;
		if (mProjectAccessor != null) {
			try {
				dgms = mProjectAccessor.findElements(ISequenceDiagram.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "getSequenceDiagrams() " + e.getMessage());
			}
		}
		return dgms;
	}
	
	public IPresentation[] getPresentation(IDiagram diagram) {
		if (diagram == null) {
			return null;
		}
	    try {
			return diagram.getPresentations();
		} catch (InvalidUsingException e) {
			Log.e(TAG, "getPresentation() " + e.getMessage());
			return null;
		}
	}

	/* =====================================================================================================
	 * 													tag handle
	 * =====================================================================================================
	 */
	public boolean hasTag (INamedElement ele) {
		if (ele != null) {
			return getTag(ele) != null;
		}
		return false;
	}
	
	public String getTagValue (INamedElement ele) {
		if (ele != null) {
			return ele.getTaggedValue(CommonUtils.DDTAG);
		}
		return null;
	}
	
	public ITaggedValue getTag (INamedElement ele) {
		if (ele != null) {
			ITaggedValue[] tags = ele.getTaggedValues();
			if (tags != null) {
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].getKey().equals(CommonUtils.DDTAG) == true) {
						return tags[i];
					}
				}
			}
		}
		return null;
	}
	
	public boolean addTag (INamedElement ele, String tag) {
		if (mProjectAccessor == null || ele == null || tag == null) {
			return false;
		}
		
		ITaggedValue eleTag = getTag(ele);
		if (eleTag != null) {
			deleteTag(eleTag);
		}
		
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			
			BasicModelEditor editor = mProjectAccessor.getModelEditorFactory().getBasicModelEditor();
			editor.createTaggedValue(ele, CommonUtils.DDTAG, tag);
			//ele.setAlias2(tag);
			
			transaction.endTransaction();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "addTag() exeption: " + ele.getName() + " " + tag + " " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
		return false;
	}
	
	private void deleteTag (ITaggedValue tag) {
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			BasicModelEditor editor = mProjectAccessor.getModelEditorFactory().getBasicModelEditor();
			editor.delete(tag);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "deleteTag() exeption: " + e.getMessage() + " tag=" + tag);
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	public INamedElement[] getElementsWithTag () {
		try {
			return mProjectAccessor.findElements(new ModelFinder() {
				@Override
				public boolean isTarget(INamedElement ele) {
					return hasTag(ele);
				}
			});
		} catch (ProjectNotFoundException e) {
			Log.e(TAG, "getElementsWithTag() " + e.getMessage());
			return null;
		}
	}
}
