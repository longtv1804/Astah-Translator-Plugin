package DD_TOOL;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Random;

import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class ExportController {
	private static String TAG = "ExportController";
	protected AstahGateway mGateway = null;
	//private FileWriter mWr = null;
	protected OutputStreamWriter mWr = null;
	protected String mDirPath = null;
	protected int mTagId = 0;
	
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
	
	HashMap<Integer, HashMap<String, String>> mDataMap = null;
	
	public ExportController (AstahGateway gw, String dirPath) {
		mGateway = gw;
		mDirPath = dirPath;
		Random rd = new Random();
		mTagId = rd.nextInt(CommonUtils.DD_TAG_MAX_ID_RND);
		
		mDataMap = new HashMap<>();
		initWriter();
	}
	
	protected void initWriter () {
		if (mDirPath != null) {
			try {
				String fileName = "[DDTOOL][Exported_Data]-" + mGateway.getProjectName() + ".txt";
				//mWr = new FileWriter(new java.io.File(mDirPath + "\\" + fileName));
				mWr = new OutputStreamWriter(new java.io.FileOutputStream(mDirPath + "\\" + fileName), Charset.forName("UTF-8"));
			} catch (IOException e) {
				mGateway.showMessage("ERROR: can not create FileWriter: " + e.getMessage());
			}
		}
	}
	
	protected void finalizeWriter () {
		if (mWr != null) {
			try {
				mWr.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	@Override
	protected void finalize() {
		finalizeWriter();
		mGateway.finalize();
	}
	
	protected void write (String line) {
		try {
			mWr.write(line);
			mWr.write("\n");
		} catch (Exception e) {
			Log.d(TAG, "exception: " + e.getMessage());
		}
	}
	
	protected String genTag() {
		mTagId += 1;
		return "" + mTagId;
	}
	
	protected String getExistTag (int type, String data) {
		String res = null;
		HashMap<String, String> lMap = mDataMap.get(type);
		if (lMap != null) {
			res = lMap.get(data);
		}
		return res;
	}
	
	void addNewDatatoCache (int type, String data, String ddTag) {
		HashMap<String, String> lMap = mDataMap.get(type);
		if (lMap == null) {
			lMap = new HashMap<>();
			mDataMap.put(type, lMap);
		}
		lMap.put(data, ddTag);
	}
	
	protected String addTag (INamedElement item, String data, int type) {
		boolean res = false;
		String ddTag = getExistTag(type, data);
		
		if (CommonUtils.isEmpty(ddTag) == true) {
			res = true;
			ddTag = genTag();
			addNewDatatoCache(type, data, ddTag);
		} else {
			Log.d(TAG, "ddTag exit: " + ddTag);
		}
		
		res = res & mGateway.addTag(item, ddTag);
		
		if (res == true) {
			return ddTag;
		}
		return null;
	}
	
	
	// ===========================================================================================
	//										export functions
	// ===========================================================================================
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
	
	protected void exportItem (INamedElement item, int type) {
		String name = item.getName();
		if (name == null || name.isEmpty()) {
			return;
		}
		
		String ddTag = addTag(item, name, type);
		
		// export data
		if (CommonUtils.isEmpty(ddTag) == false) {
			write(ddTag + " " + type + " 0");
			write(name);
			Log.d(TAG, "exportItem: [" + name + "] type: " + type);
			count(type);
		}
	}
	protected boolean isNeededToExport(IElement item) {
		boolean res = true;
		
		if (item instanceof ILifeline) {
			ILifeline iLfl = (ILifeline) item;
			IClass baseClass = iLfl.getBase();
			if (baseClass != null) {
				res = false;
				Log.d(TAG, "ignore ILifeline: " + iLfl.getName() + " " + baseClass.getName());
			}
			
		}
		else if (item instanceof IMessage) {
			IMessage msg = (IMessage) item;
			IOperation baseOp = msg.getOperation();
			if (baseOp != null) {
				res = false;
				Log.d(TAG, "ignore IMessage: " + msg.getName() + " hasOP: "+ baseOp.getName());
			}
		}
		
		return res;
	}
	
	protected void export (IPackage iPk) {
		exportItem(iPk, CommonUtils.ITEM_TYPE_PACKAGE);
	}
	protected void export (IOperation iOp) {
		exportItem(iOp, CommonUtils.ITEM_TYPE_CLASS_FUNC);
	}
	protected void export (IClass iclass) {
		exportItem(iclass, CommonUtils.ITEM_TYPE_CLASS);
			
		// export class function
		IOperation[] funcs = iclass.getOperations();
		if (funcs != null) {
			for (int i = 0; i < funcs.length; i++) {
				export(funcs[i]);
			}
		}
	}
	protected void export (IComment iCmt) {
		String content = iCmt.getBody();
		if (content == null || content.isEmpty()) {
			return;
		}
		
		String ddTag = addTag(iCmt, content, CommonUtils.ITEM_TYPE_COMMENT);
		
		// export
		if (CommonUtils.isEmpty(ddTag) == false) {
			String[] strArr = content.split("\n");
			write(ddTag + " " + CommonUtils.ITEM_TYPE_COMMENT + " " + strArr.length);
			for (int i = 0; i < strArr.length; i++) {
				write(strArr[i]);
			}
			Log.d(TAG, "export IComment: [" + content + "] type: " + CommonUtils.ITEM_TYPE_COMMENT);
			count(CommonUtils.ITEM_TYPE_COMMENT);
		}
	}
	protected void export (ICombinedFragment fm) {
		// add tag
		String ddTag = genTag(); 
		boolean res = mGateway.addTag(fm, ddTag);
		
		// export
		if (res == true) {
			String name = fm.getName();
			if (CommonUtils.isEmpty(name)) {
				name = CommonUtils.DEFAULT_VALUE_FOR_ITEM;
			}
			IInteractionOperand[] frms = fm.getInteractionOperands();
			int len = (frms == null ? 0 : frms.length); 
			len += 1; // for the name
			
			write( ddTag + " " + CommonUtils.ITEM_TYPE_COMBINEFRAG + " "  + len);
			write(name);

			if (frms != null) {
				for (int j = 0; j < frms.length; j++) {
					String guard = frms[j].getGuard();
					if (CommonUtils.isEmpty(guard)) {
						guard = CommonUtils.DEFAULT_VALUE_FOR_ITEM;
					}
					write(guard);
				}
			}
			Log.d(TAG, "export ICombinedFragment: '" + name + "'");
			count(CommonUtils.ITEM_TYPE_COMBINEFRAG);
		}
	}
	protected void export (ILifeline iLfl) {
		exportItem(iLfl, CommonUtils.ITEM_TYPE_LIFELINE);
	}
	protected void export (IMessage iMess) {
		exportItem(iMess, CommonUtils.ITEM_TYPE_LIFELINEFUNC);
	}
	protected void export (IInteractionUse iItr) {
		exportItem(iItr, CommonUtils.ITEM_TYPE_REFERENCESEQUENCE);
	}
	protected void export (ISequenceDiagram sq) {
		exportItem(sq, CommonUtils.ITEM_TYPE_SEQ);

		IPresentation[] psts = mGateway.getPresentation(sq);
		Log.d(TAG, "start export ISequenceDiagram: " + sq.getName() + " " + psts.length);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			if (isNeededToExport(item) == false) {
				continue;
			}
			// export comment
			if (item instanceof IComment) {
				export((IComment)item);
			}
			// export combinFragment
			else if (item instanceof ICombinedFragment) {
				export((ICombinedFragment)item);
			}
			// export life_line
			else if (item instanceof ILifeline) {
				export((ILifeline)item);
			}
			// export interaction
			else if (item instanceof IMessage) {
				export((IMessage)item);
			}
			else if (item instanceof IInteractionUse) {
				export((IInteractionUse)item);
			}
		}
	}
	protected void export(IState item) {
		exportItem(item, CommonUtils.ITEM_TYPE_SMC_STATE);
	}
	protected void export(ITransition item) {
		String trigger = item.getEvent();
		String guard = item.getGuard();
		String action = item.getAction(); 
		
		if (CommonUtils.isEmpty(trigger) && CommonUtils.isEmpty(guard) && CommonUtils.isEmpty(action)) {
			return;
		}
		
		// add tag
		String ddTag = genTag();
		boolean res = mGateway.addTag(item, ddTag);
		
		// export
		if (res == true) {
			Log.d(TAG, "export ITransition: [" + trigger + "] guard: [" + guard + "] action: [" + action + "]");
			
			if (CommonUtils.isEmpty(trigger)) trigger = CommonUtils.DEFAULT_VALUE_FOR_ITEM;
			if (CommonUtils.isEmpty(guard)) guard = CommonUtils.DEFAULT_VALUE_FOR_ITEM;
			if (CommonUtils.isEmpty(action)) action = CommonUtils.DEFAULT_VALUE_FOR_ITEM;
			
			String triggerArr[] = trigger.split("\n");
			String guardArr[] 	= guard.split("\n");
			String actionArr[] 	= action.split("\n");
			int numberOfline = (triggerArr.length << 16) | (guardArr.length << 8) | actionArr.length;
			
			write(ddTag + " " + CommonUtils.ITEM_TYPE_SMC_TRANSITION + " " + numberOfline);
			for (int i = 0; i < triggerArr.length; i++) {
				write(triggerArr[i]);
			}
			for (int i = 0; i < guardArr.length; i++) {
				write(guardArr[i]);
			}
			for (int i = 0; i < actionArr.length; i++) {
				write(actionArr[i]);
			}
			count(CommonUtils.ITEM_TYPE_SMC_TRANSITION);
		}
	}
	protected void export (IStateMachineDiagram dg) {
		exportItem(dg, CommonUtils.ITEM_TYPE_SEQ_STATE_MACHINE);
		
		IPresentation[] psts = mGateway.getPresentation(dg);
		Log.d(TAG, "start export IStateMachineDiagram: " + dg.getName() + " " + psts.length);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			// export comment
			if (item instanceof IComment) {
				export((IComment)item);
			}
			// export states
			else if (item instanceof IState) {
				export((IState)item);
			}
			// export transitions
			else if (item instanceof ITransition) {
				export((ITransition)item);
			}
		}
	}
	
	// ===========================================================================================
	//										run function
	// ===========================================================================================
	public void run () throws Exception {
		Log.d(TAG, "+++++++++++ Start Export ++++++++++++++");
		
		// export package
		INamedElement[] packages = mGateway.getPackages();
		if (packages != null) {
			for (int i = 0; i < packages.length; i++) {
				Log.d(TAG, "package: " + packages[i].getName());
				export((IPackage)packages[i]);
			}
		}
		
		// export class
		INamedElement[] classes = mGateway.getClasses();
		if (classes != null) {
			for (int i = 0; i < classes.length; i++) {
				if (classes[i] instanceof ISubsystem) {
					continue;
				}
				Log.d(TAG, "class: " + classes[i].getName());
				export((IClass)classes[i]);
			}
		}
		
		// export all sequence diagram
		INamedElement[] seqDgs = mGateway.getSequenceDiagrams();
		if (seqDgs != null) {
			for (int i = 0; i < seqDgs.length; i++) {
				Log.d(TAG, "Diagram: " + seqDgs[i].getName());
				export((ISequenceDiagram)seqDgs[i]);
			}
		}
		
		// export all sequence diagram
		INamedElement[] stateDgs = mGateway.getStateMachineDiagrams();
		if (stateDgs != null) {
			for (int i = 0; i < stateDgs.length; i++) {
				Log.d(TAG, "State Diagram: " + stateDgs[i].getName());
				export((IStateMachineDiagram)stateDgs[i]);
			}
		}
		
		finalizeWriter();
		mGateway.save();
		String summaryMsg = summary();
		mGateway.showMessage("Export Whole Project.. Done.\n" + summaryMsg);
		Log.d(TAG, "export DONE\n" + summaryMsg);
	}
}
