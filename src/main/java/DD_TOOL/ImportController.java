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
	
	protected int mPackageCount 				= 0;
	protected int mClassCount 					= 0;
	protected int mClassFunctionCount 			= 0;
	protected int mSeqDiagramCount 				= 0;
	protected int mSeqLifeLineCount				= 0;
	protected int mSeqMessageCount 				= 0;
	protected int mSeqCommentCount 				= 0;
	protected int mSeqCombineFragmentCount		= 0;
	protected int mStateDiagramCount			= 0;
	protected int mSdStateCount				    = 0;
	protected int mSdTransitionCount			= 0;
	
	protected void count (int type) {
		switch (type) {
		case CommonUtils.ITEM_TYPE_PACKAGE: 			mPackageCount++; 			break;
		case CommonUtils.ITEM_TYPE_CLASS: 				mClassCount++; 				break;
		case CommonUtils.ITEM_TYPE_CLASS_FUNC: 			mClassFunctionCount++; 		break;
		case CommonUtils.ITEM_TYPE_SEQ: 				mSeqDiagramCount++; 		break;
		case CommonUtils.ITEM_TYPE_LIFELINE: 			mSeqLifeLineCount++; 		break;
		case CommonUtils.ITEM_TYPE_LIFELINEFUNC: 		mSeqMessageCount++; 		break;
		case CommonUtils.ITEM_TYPE_COMMENT: 			mSeqCommentCount++; 		break;
		case CommonUtils.ITEM_TYPE_COMBINEFRAG: 		mSeqCombineFragmentCount++; break;
		case CommonUtils.ITEM_TYPE_SEQ_STATE_MACHINE: 	mStateDiagramCount++; 		break;
		case CommonUtils.ITEM_TYPE_SMC_STATE: 			mSdStateCount++; 			break;
		case CommonUtils.ITEM_TYPE_SMC_TRANSITION: 	    mSdTransitionCount++; 		break;
		}
	}

	protected String summary () {
		String ret = "";
		ret += String.format("%-30s %10d\n", "Packages", 				mPackageCount            );
		ret += String.format("%-30s %10d\n", "Classes", 				mClassCount              );
		ret += String.format("%-30s %10d\n", "    Class's Funcs", 		mClassFunctionCount      );
		ret += String.format("%-30s %10d\n", "Sequence Diagram", 		mSeqDiagramCount         );
		ret += String.format("%-30s %10d\n", "    LifeLine", 			mSeqLifeLineCount        );
		ret += String.format("%-30s %10d\n", "    LifeLine's Message", 	mSeqMessageCount         );
		ret += String.format("%-30s %10d\n", "    Comment", 			mSeqCommentCount         );
		ret += String.format("%-30s %10d\n", "    Combine Fragment", 	mSeqCombineFragmentCount );
		ret += String.format("%-30s %10d\n", "StateMachine Diagram", 	mStateDiagramCount       );
		ret += String.format("%-30s %10d\n", "    State", 				mSdStateCount            );
		ret += String.format("%-30s %10d\n", "    Transition", 			mSdTransitionCount       );
		return ret;
	}
	
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
			if (CommonUtils.isEmpty(data) == false) {
				mGateway.importTranslatedData(element, data);
				count(type);
			} else {
				Log.e(TAG, "importTranSlatedData() ERROR: Key is not included in input [" + tagValue + "]");
			}
		}
	}
	private String checkAndGetTag(INamedElement element, int inputType) {
		ITaggedValue iTag = mGateway.getTag(element);
		if (iTag != null) {
			String tagValue = iTag.getValue();
			if (tagValue == null || tagValue.isEmpty()) {
				Log.d(TAG, "ERROR: ddtag null or empty: [" + element.getName());
				return null;
			}
			
			int holderType = mHolder.getType(tagValue);
			if (holderType != inputType) {
				Log.d(TAG, "type mismatch DDTag:[" + tagValue + "] type: " + inputType + " holderType:" + holderType);
				return null;
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
	
	// ===========================================================================================
	//										import function
	// ===========================================================================================
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
		String tagValue = checkAndGetTag(iCmt, CommonUtils.ITEM_TYPE_COMMENT);
		if (tagValue != null) {
			String data = mHolder.getData(tagValue);
			mGateway.importTranslatedData(iCmt, data);
			count(CommonUtils.ITEM_TYPE_COMMENT);
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
			count(CommonUtils.ITEM_TYPE_SMC_TRANSITION);
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
	
	// ===========================================================================================
	//										run function
	// ===========================================================================================
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
			INamedElement proj = mGateway.getProject();
			if (packages != null) {
				for (int i = 0; i < packages.length; i++) {
					if (packages[i] == proj) {
						continue; // ignore project instance
					}
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
			String summaryMsg = summary();
			mGateway.showMessage("IMPORT... done!\n" + summaryMsg);
			Log.d(TAG, "import DONE\n" + summaryMsg);
		} else {
			mGateway.showMessage("ERROR: when reading your data. please check again");
		}
	}
}
