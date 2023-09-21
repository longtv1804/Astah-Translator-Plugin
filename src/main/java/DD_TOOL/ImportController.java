package DD_TOOL;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class ImportController {
	private static String TAG = "ImportController";
	private AstahGateway mGateway = null;
	
	private TranslatedDataHolder mHolder = null;
	private String mInputPath = null;
	
	public ImportController (AstahGateway gw, String input) {
		mGateway = gw;
		mInputPath = input;
	}
	
	@Override
	protected void finalize() {
		
	}
	
	private void importTranSlatedData (INamedElement element, int type) throws Exception {
		String tagValue = checkAndGetTag(element, type);

		if (tagValue != null) {	
			String data = mHolder.getData(tagValue);
			mGateway.importTranslatedData(element, data);
		}
	}
	private String checkAndGetTag(INamedElement element, int inputType) throws Exception {
		ITaggedValue iTag = mGateway.getTag(element);
		if (iTag != null) {
			String tagValue = iTag.getValue();
			if (tagValue == null || tagValue.isEmpty()) {
				throw new Exception("can not find out DDTAG in import data. please check!");
			}
			int holderType = mHolder.getType(tagValue);
			if (holderType != inputType) {
				Log.e(TAG, "type mismatch DDTag:[" + tagValue + "] type: " + inputType + " holderType:" + holderType);
				throw new Exception("DDTAG's type missmatch, please check!");
			}
			return tagValue;
		}
		return null;
	}
	private boolean isNeededToImport(IElement item) {
		boolean res = true;
		
		if (item instanceof ILifeline) {
			ILifeline iLfl = (ILifeline) item;
			IClass baseClass = iLfl.getBase();
			if (baseClass != null) {
				res = false;
				Log.d(TAG, "ignore ILifeline: " + iLfl.getName() + " " + baseClass.getName());
			}
		} else if (item instanceof IMessage) {
			IMessage msg = (IMessage) item;
			IOperation baseOp = msg.getOperation();
			if (baseOp != null) {
				res = false;
				Log.d(TAG, "ignore IMessage: " + msg.getName() + " hasOP: "+ baseOp.getName());
			}
		}
		
		return res;
	}
	
	private void importData (IPackage iPk) throws Exception {
		importTranSlatedData(iPk, CommonUtils.ITEM_TYPE_PACKAGE);
	}
	private void importData (IOperation iOp) throws Exception {
		importTranSlatedData(iOp, CommonUtils.ITEM_TYPE_CLASS_FUNC);
	}
	private void importData (IClass iClass) throws Exception {
		importTranSlatedData(iClass, CommonUtils.ITEM_TYPE_CLASS);
		
		IOperation[] funcs = iClass.getOperations();
		if (funcs != null) {
			for (int i = 0; i < funcs.length; i++) {
				importData(funcs[i]);
			}
		}
	}
	private void importData (IComment iCmt) throws Exception {
		ITaggedValue iTag = mGateway.getTag(iCmt);
		if (iTag != null) {
			String tagValue = iTag.getValue();
			if (tagValue == null || tagValue.isEmpty()) {
				throw new Exception("importData IComment: can not find out DDTAG in import data.\nmaybe you passed incorrect input, please check!");
			}
			int holderType = mHolder.getType(tagValue);
			if (holderType != CommonUtils.ITEM_TYPE_COMMENT) {
				Log.e(TAG, "type mismatch DDTag:[" + tagValue + "] type: " + CommonUtils.ITEM_TYPE_COMMENT + " holderType:" + holderType);
				throw new Exception("DDTAG's type missmatch, please check!");
			}
			
			String data = mHolder.getData(tagValue);
			mGateway.importTranslatedData(iCmt, data);
		}
	}
	private void importData (ICombinedFragment iCbF) throws Exception {
		String tagValue = checkAndGetTag(iCbF, CommonUtils.ITEM_TYPE_COMBINEFRAG);
		
		if (tagValue != null) {
			String name = mHolder.getData(tagValue);
			if (name != null && name.isEmpty() == false) {
				importTranSlatedData(iCbF, CommonUtils.ITEM_TYPE_COMBINEFRAG);
			}
			
			ArrayList<String> guards = mHolder.getGuardsForCombinedFragment(tagValue);
			IInteractionOperand iItOp[] = iCbF.getInteractionOperands();
			if (guards != null && iItOp != null && guards.size() == iItOp.length) {
				for (int i = 0; i < iItOp.length; i++) {
					mGateway.importTranslatedData(iItOp[i], guards.get(i));
				}
			} else {
				Log.e(TAG, "importData ICombinedFragment[" + tagValue + "] guard length mismatch: " 
													+ (guards == null ? "null" : guards.size()) + " " + iItOp.length);
				throw new Exception("combineFragment mismatch");
			}
		}
	}
	private void importData (ILifeline iLfl) throws Exception {
		importTranSlatedData(iLfl, CommonUtils.ITEM_TYPE_LIFELINE);
	}
	private void importData (IMessage iMsg) throws Exception {
		importTranSlatedData(iMsg, CommonUtils.ITEM_TYPE_LIFELINEFUNC);
	}
	private void importData (IInteractionUse iRefer) throws Exception {
		importTranSlatedData(iRefer, CommonUtils.ITEM_TYPE_REFERENCESEQUENCE);
	}
	private void importData (ISequenceDiagram sq) throws Exception {
		importTranSlatedData(sq, CommonUtils.ITEM_TYPE_SEQ);
		
		IPresentation[] psts = mGateway.getPresentation(sq);
		Log.d(TAG, "import ISequenceDiagram: " + sq.getName() + " " + psts.length);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			if (isNeededToImport(item) == false) {
				continue;
			}
			// import comment
			if (item instanceof IComment) {
				importData((IComment)item);
			}
			// import combinFragment
			else if (item instanceof ICombinedFragment) {
				importData((ICombinedFragment)item);
			}
			// import life_line
			else if (item instanceof ILifeline) {
				importData((ILifeline)item);
			}
			// import interaction
			else if (item instanceof IMessage) {
				importData((IMessage)item);
			} else if (item instanceof IInteractionUse) {
				importData((IInteractionUse)item);
			}
		}
	}
	private void importData (IState item) throws Exception {
		importTranSlatedData(item, CommonUtils.ITEM_TYPE_SMC_STATE);
	}
	private void importData (ITransition item) throws Exception {
		String tagValue = checkAndGetTag(item, CommonUtils.ITEM_TYPE_SMC_TRANSITION);
		if (tagValue != null) {
			String trigger 	= mHolder.getTrigerForState(tagValue);
			String guard 	= mHolder.getGuardForState(tagValue);
			String act 		= mHolder.getActionForState(tagValue);
			mGateway.importTranslatedData(item, trigger, guard, act);
		}
	}
	private void importData (IStateMachineDiagram dg) throws Exception {
		IPresentation[] psts = mGateway.getPresentation(dg);
		Log.d(TAG, "start export IStateMachineDiagram: " + dg.getName() + " " + psts.length);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			// export comment
			if (item instanceof IComment) {
				importData((IComment)item);
			}
			// export states
			else if (item instanceof IState) {
				importData((IState)item);
			}
			// export transitions
			else if (item instanceof ITransition) {
				importData((ITransition)item);
			}
		}
	}
	
	public void run () throws Exception {
		Log.d(TAG, "+++++++++++ Start Import ++++++++++++++");
		if (mGateway == null || mInputPath == null) {
			Log.e(TAG, "import ERROR: mGateway=" + mGateway + " inputPath=" + mInputPath);
			return;
		}
		
		try {
			mHolder = new TranslatedDataHolder(mInputPath);
		} catch (Exception e) {
			Log.e(TAG, "run() exception at reading data: " + e.getMessage());
			mHolder = null;
		}
		
		if (mHolder != null) {
			// import package
			INamedElement[] packages = mGateway.getPackages();
			if (packages != null) {
				for (int i = 0; i < packages.length; i++) {
					Log.d(TAG, "package: " + packages[i].getName());
					importData((IPackage)packages[i]);
				}
			}
			
			// import class
			INamedElement[] classes = mGateway.getClasses();
			if (classes != null) {
				for (int i = 0; i < classes.length; i++) {
					if (classes[i] instanceof ISubsystem) {
						continue;
					}
					Log.d(TAG, "class: " + classes[i].getName());
					importData((IClass)classes[i]);
				}
			}
			
			// import all sequence diagrams
			INamedElement[] seqDgs = mGateway.getSequenceDiagrams();
			if (seqDgs != null) {
				for (int i = 0; i < seqDgs.length; i++) {
					Log.d(TAG, "import Sequence Diagram: " + seqDgs[i].getName());
					importData((ISequenceDiagram)seqDgs[i]);
				}
			}
			
			// import all state diagrams
			INamedElement[] stateDgs = mGateway.getStateMachineDiagrams();
			if (stateDgs != null) {
				for (int i = 0; i < stateDgs.length; i++) {
					Log.d(TAG, "import State Diagram: " + stateDgs[i].getName());
					importData((IStateMachineDiagram)stateDgs[i]);
				}
			}
			
			mGateway.save();
			mGateway.showMessage("IMPORT done!");
		} else {
			mGateway.showMessage("ERROR: when reading your data. please check again");
		}
	}
}
