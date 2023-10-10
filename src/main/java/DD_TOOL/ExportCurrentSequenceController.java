package DD_TOOL;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Random;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IComment;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInteractionUse;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.ISubsystem;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class ExportCurrentSequenceController extends ExportController{
	private static String TAG = "ExportCurrentSequenceController";
	
	public ExportCurrentSequenceController(AstahGateway gw, String ouputPath) {
		super(gw, ouputPath);
		int startOfTagId = mGateway.getTagIdStartFromProject();
		if (startOfTagId != -1) {
			mTagId = startOfTagId;
			Log.d(TAG, "ExportCurrentSequenceController() found start of TAG ID: " + mTagId);
		} else {
			Random rd = new Random();
			mTagId = rd.nextInt(CommonUtils.DD_TAG_MAX_ID_RND) + 5 * CommonUtils.DD_TAG_MAX_ID_RND; // > 5Mil
			Log.d(TAG, "ExportCurrentSequenceController() NOT found start of TAG ID -> gen new tag: " + mTagId);
		}
	}
	
	@Override
	protected void initWriter () {
		if (mDirPath != null) {
			try {
				IDiagram dg = mGateway.getOpenedDiagram();
				String fileName = "[DDTOOL][Export Diagram]-" + dg.getName() + ".txt";
				mWr = new OutputStreamWriter(new java.io.FileOutputStream(mDirPath + "\\" + fileName), Charset.forName("UTF-8"));
			} catch (IOException e) {
				mGateway.showMessage("ERROR: can not create FileWriter: " + e.getMessage());
			}
		}
	}
	
	@Override
	public void run() throws Exception {
		Log.d(TAG, "+++++++++++ Start Export Diagram ++++++++++++++");
		IDiagram dg = mGateway.getOpenedDiagram();
		if (dg != null) {
			if (dg instanceof ISequenceDiagram) {
				export((ISequenceDiagram)dg);
			} 
			else if (dg instanceof IStateMachineDiagram) {
				export((IStateMachineDiagram)dg);
			} 
			else {
				Log.d(TAG, "Opened diagram is not a kind of StateMachineDiagram or SequenceDiagram!");
				throw new Exception("Opened diagram MUST be kind of StateMachineDiagram or SequenceDiagram!");
			}
			mGateway.setLastTagId("" + mTagId);
			mGateway.save();
			String summaryMsg = summary();
			mGateway.showMessage("Export Sequence [" + dg.getName() + "].... Done\n" + summaryMsg);
			Log.d(TAG, "export Sequence DONE\n" + summaryMsg);
		} else {
			Log.d(TAG, "don't find out any diagram");
			mGateway.showMessage("you need open a diagram to use this function!");
		}
		finalizeWriter();
	}
	
	@Override
	protected String addTag (INamedElement item, String data, int type) {
		boolean res = false;
		String ddTag = getExistTag(type, data);
		
		if (CommonUtils.isEmpty(ddTag) == true) {
			res = true;
			ddTag = genTag();
			addNewDatatoCache(type, data, ddTag);
		} else {
			Log.d(TAG, "addTag() ddTag exit in cached: " + ddTag);
		}
		
		res = res & mGateway.addTag(item, ddTag);
		
		if (res == true) {
			return ddTag;
		}
		return null;
	}
	
	@Override
	protected void export (ISequenceDiagram sq) {
		exportItem(sq, CommonUtils.ITEM_TYPE_SEQ);

		IPresentation[] psts = mGateway.getPresentation(sq);
		Log.d(TAG, "start export ISequenceDiagram: " + sq.getName() + " " + psts.length);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
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
				ILifeline iLfl = (ILifeline) item;
				IClass baseClass = iLfl.getBase();
				if (baseClass != null) {
					if (baseClass instanceof ISubsystem == false) {
						export((IClass)baseClass);
					} else {
						// ignore if it is ISubsystem
					}
				} else {
					export((ILifeline)item);
				}
			}
			// export interaction
			else if (item instanceof IMessage) {
				IMessage msg = (IMessage) item;
				IOperation baseOp = msg.getOperation();
				if (baseOp != null) {
					// ignore because it will be translated as ILifeline's base class
				} else {
					export((IMessage)item);
				}
			}
			else if (item instanceof IInteractionUse) {
				export((IInteractionUse)item);
			}
		}
	}
}
