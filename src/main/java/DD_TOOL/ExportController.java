package DD_TOOL;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class ExportController {
	private static String TAG = "ExportController";
	private AstahGateway mGateway = null;
	private FileWriter mWr = null;
	private String mDirPath = null;
	private int mTagId = 0;
	
	public ExportController (AstahGateway gw, String dirPath) {
		mGateway = gw;
		mDirPath = dirPath;
		Random rd = new Random();
		mTagId = rd.nextInt(1000000);
		
		initWriter();
	}
	
	private void initWriter () {
		if (mDirPath != null) {
			try {
				String fileName = mGateway.getProjectName() + "_Exported.txt";
				mWr = new FileWriter(new java.io.File(mDirPath + "\\" + fileName));
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
			// ignore
		}
	}
	
	private String genTag() {
		mTagId += 1;
		return "" + mTagId;
	}
	
	private void exportItem (INamedElement item, int type) {
		String name = item.getName();
		if (name == null || name.isEmpty()) {
			return;
		}
		// add tag
		String ddTag = genTag();
		boolean res = mGateway.addTag(item, ddTag);
		
		// export
		if (res == true) {
			write(ddTag + " " + type + " " + name);
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
		// add tag
		String ddTag = genTag();
		boolean res = mGateway.addTag(iCmt, ddTag);
		
		// export
		if (res == true) {
			String content = iCmt.getBody();
			write(ddTag + " " + CommonUtils.ITEM_TYPE_COMMENT + " " + content);
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
			String msg = ddTag + " " + CommonUtils.ITEM_TYPE_COMBINEFRAG + " [" + name + "] - {";
			IInteractionOperand[] frms = fm.getInteractionOperands();
			if (frms != null) {
				for (int j = 0; j < frms.length; j++) {
					msg += "[" + frms[j].getGuard() + "]";
				}
			}
			msg += "}";
			write(msg);
			Log.d(TAG, "export ICombinedFragment: '" + name + "'");
		}
	}
	private void export (ILifeline iLfl) {
		exportItem(iLfl, CommonUtils.ITEM_TYPE_LIFELINE);
	}
	private void export (IMessage iItr) {
		exportItem(iItr, CommonUtils.ITEM_TYPE_LIFELINEFUNC);
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
		// add tag
		String ddTag = genTag();
		mGateway.addTag(sq, ddTag);
		
		// export
		String name = sq.getName();
		write(ddTag + " " + CommonUtils.ITEM_TYPE_SEQ + " " + name);
		
		IPresentation[] psts = mGateway.getPresentation(sq);
		Log.d(TAG, "export ISequenceDiagram: " + name + " " + psts.length);
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
	
	public void run () {
		Log.d(TAG, "+++++++++++ Start export ++++++++++++++");
		
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
				Log.d(TAG, "class: " + classes[i].getName());
				export((IClass)classes[i]);
			}
		}
		
		// export all sequence diagram
		INamedElement[] dgs = mGateway.getSequenceDiagrams();
		if (dgs != null) {
			for (int i = 0; i < dgs.length; i++) {
				Log.d(TAG, "Diagram: " + dgs[i].getName());
				export((ISequenceDiagram)dgs[i]);
			}
		}
		
		finalizeWriter();
		mGateway.save();
		mGateway.showMessage("Export Done.");
	}
}
