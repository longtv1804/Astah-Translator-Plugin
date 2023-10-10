package DD_TOOL;

import javax.swing.JOptionPane;

import org.osgi.service.log.LogEntry;

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
	
	public void save () throws Exception {
		try {
			mProjectAccessor.save();
		} catch (Exception e) {
			Log.e(TAG, "Exception: save() " + e.getMessage());
			throw new Exception("ERROR: can not save the projecte ??");
		}
	}
	
	
	/* =====================================================================================================
	 * 			get items
	 * =====================================================================================================
	 */
	public INamedElement getProject () {
		if (mProjectAccessor != null) {
			try {
				return mProjectAccessor.getProject();
			} catch (ProjectNotFoundException e) {
				// ignore
			}
		}
		return null;
	}
	public void setLastTagId (String tagStr) {
		if (mProjectAccessor != null) {
			try {
				IModel proj = mProjectAccessor.getProject();
				ITaggedValue curTag = getTag(proj);
				if (curTag != null) {
					deleteTag(curTag);
				}
				addTag(proj, tagStr);
			} catch (Exception e) {
				Log.e(TAG, "setLastTagId() Exception: " + e.getMessage());
			}
		} else {
			Log.e(TAG, "setLastTagId() mProjectAccessor is null");
		}
	}
	
	public int getTagIdStartFromProject () {
		if (mProjectAccessor != null) {
			try {
				IModel proj = mProjectAccessor.getProject();
				ITaggedValue curTag = getTag(proj);
				if (curTag != null) {
					String value = curTag.getValue();
					Log.d(TAG, "setLastTagId() Project TAG existed " + value);
					return Integer.parseInt(value);
				} 
			} catch (Exception e) {
				Log.e(TAG, "setLastTagId() Exception: " + e.getMessage());
			}
		} else {
			Log.e(TAG, "getTagIdStartFromProject() mProjectAccessor is null");
		}
		return -1;
	}
	
	public IDiagram getOpenedDiagram () {
		if (mProjectAccessor != null) {
			try {
				return mProjectAccessor.getViewManager().getDiagramViewManager().getCurrentDiagram();
			} catch (InvalidUsingException e) {
				Log.e(TAG, "getOpenedDiagram() exeption: " + e.getMessage());
			}
		}
		return null;
	}

	public IDiagram[] getOpenedDiagrams () {
		if (mProjectAccessor != null) {
			try {
				return mProjectAccessor.getViewManager().getDiagramViewManager().getOpenDiagrams();
			} catch (InvalidUsingException e) {
				Log.e(TAG, "getOpenedDiagrams() exeption: " + e.getMessage());
			}
		}
		return null;
	}
	
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
			Log.e(TAG, "Exception: getPackages() " + e.getMessage());
			return null;
		}
	}
	
	public INamedElement[] getClasses() {
		INamedElement[] res = null;
		if (mProjectAccessor != null) {
		    try {
				res = mProjectAccessor.findElements(IClass.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "Exception: getClasses() " + e.getMessage());
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
				Log.e(TAG, "Exception: getSequenceDiagrams() " + e.getMessage());
			}
		}
		return dgms;
	}
	
	public INamedElement[] getStateMachineDiagrams() {
		INamedElement[] dgms = null;
		if (mProjectAccessor != null) {
			try {
				dgms = mProjectAccessor.findElements(IStateMachineDiagram.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "Exception: getStateMachineDiagrams() " + e.getMessage());
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
			Log.e(TAG, "Exception: getPresentation() " + e.getMessage());
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
	
	public ITaggedValue getTag (IElement ele) {
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
	
	public boolean addTag (INamedElement ele, String tagValue) {
		if (mProjectAccessor == null || ele == null || tagValue == null) {
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
			editor.createTaggedValue(ele, CommonUtils.DDTAG, tagValue);
			//ele.setAlias2(tag);
			
			transaction.endTransaction();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Exception: addTag() exeption: " + ele.getName() + " " + tagValue + " " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
		return false;
	}
	
	public void deleteTag (ITaggedValue tag) {
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			BasicModelEditor editor = mProjectAccessor.getModelEditorFactory().getBasicModelEditor();
			editor.delete(tag);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "Exception: deleteTag() exeption: " + e.getMessage() + " tag=" + tag);
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
			Log.e(TAG, "Exception: getElementsWithTag() " + e.getMessage());
			return null;
		}
	}
	
	public void importTranslatedData (INamedElement element, String str) {
		if (element == null || str == null || str.isEmpty()) {
			return;
		}
		
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setName(str);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "Exception: importTranslatedData() element: [" + element.getName() + "] - [" + str + "] exeption: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}

	public void importTranslatedData (IInteractionOperand element, String str) {
		if (element == null || str == null || str.isEmpty()) {
			return;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setGuard(str);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "Exception: importTranslatedData() IInteractionOperand: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	public void importTranslatedData (IComment element, String str) {
		if (element == null || str == null || str.isEmpty()) {
			return;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setName(str);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "Exception: importTranslatedData() IInteractionOperand: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	public void importTranslatedData (ITransition element, String strigger, String guard, String act) {
		if (element == null || 
			(CommonUtils.isEmpty(strigger) && CommonUtils.isEmpty(guard) && CommonUtils.isEmpty(act))) {
			return;
		}
		Log.e(TAG, "importTranslatedData() ITransition: " + strigger);
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			if (CommonUtils.isEmpty(strigger) == false) {
				element.setEvent(strigger);
			}
			if (CommonUtils.isEmpty(guard) == false) {
				element.setGuard(guard);
			}
			if (CommonUtils.isEmpty(act) == false) {
				element.setAction(act);
			}
			element.setAction(act);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "Exception: importTranslatedData() ITransition: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	public boolean isCurrentDiagramExportedData () {
		IDiagram dg = getOpenedDiagram();
		if (dg != null) {
			String ddTagVal = getTagValue(dg);
			try {
				int val = Integer.parseInt(ddTagVal);
				if (val > CommonUtils.DD_TAG_MIN_ID_DIAGRAMEXPORTID) {
					return true;
				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return false;
	}
}
