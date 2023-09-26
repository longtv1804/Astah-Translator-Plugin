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
	private AstahGateway mGateway = null;
	//private FileWriter mWr = null;
	private OutputStreamWriter mWr = null;
	private String mDirPath = null;
	private int mTagId = 0;
	
	HashMap<Integer, HashMap<String, String>> mDataMap = null;
	
	public ExportController (AstahGateway gw, String dirPath) {
		mGateway = gw;
		mDirPath = dirPath;
		Random rd = new Random();
		mTagId = rd.nextInt(1000000);
		
		mDataMap = new HashMap<>();
		initWriter();
	}
	
	private void initWriter () {
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
	
	private void finalizeWriter () {
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
	
	private void write (String line) {
		try {
			mWr.write(line);
			mWr.write("\n");
		} catch (Exception e) {
			Log.d(TAG, "exception: " + e.getMessage());
		}
	}
	
	private String genTag() {
		mTagId += 1;
		return "" + mTagId;
	}
	
	String getExistTag (int type, String data) {
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
	
	private String addTag (INamedElement item, String data, int type) {
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
	
	private void exportItem (INamedElement item, int type) {
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
		}
	}

	private void export (IPackage iPk) {
		exportItem(iPk, CommonUtils.ITEM_TYPE_PACKAGE);
	}
	private void export (IOperation iOp) {
		exportItem(iOp, CommonUtils.ITEM_TYPE_CLASS_FUNC);
	}
	private void export (IClass iclass) {
		exportItem(iclass, CommonUtils.ITEM_TYPE_CLASS);
			
		// export class function
		IOperation[] funcs = iclass.getOperations();
		if (funcs != null) {
			for (int i = 0; i < funcs.length; i++) {
				export(funcs[i]);
			}
		}
	}
	private void export (IComment iCmt) {
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
		}
	}
	private void export (ICombinedFragment fm) {
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
		}
	}
	private void export (ILifeline iLfl) {
		exportItem(iLfl, CommonUtils.ITEM_TYPE_LIFELINE);
	}
	private void export (IMessage iMess) {
		exportItem(iMess, CommonUtils.ITEM_TYPE_LIFELINEFUNC);
	}
	private void export (IInteractionUse iItr) {
		exportItem(iItr, CommonUtils.ITEM_TYPE_REFERENCESEQUENCE);
	}
	private boolean isNeededToExport(IElement item) {
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
	private void export (ISequenceDiagram sq) {
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
	private void export(IState item) {
		exportItem(item, CommonUtils.ITEM_TYPE_SMC_STATE);
	}
	private void export(ITransition item) {
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
			Log.d(TAG, "export ITransition: [" + trigger + "] guard: [" + guard + " action: [" + action + "]");
			
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
		}
	}
	private void export (IStateMachineDiagram dg) {
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
		mGateway.showMessage("Export Done.");
	}
}
