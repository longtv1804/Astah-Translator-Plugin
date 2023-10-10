package DD_TOOL;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IComment;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInteractionUse;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.ISubsystem;
import com.change_vision.jude.api.inf.model.ITaggedValue;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class CleanTranslatedDataController {
	private static String TAG = "CleanTranslatedDataController";
	AstahGateway mGateway = null;
	
	public CleanTranslatedDataController (AstahGateway gw) {
		mGateway = gw;
	}

	void run () throws Exception {
		// export package
		INamedElement[] packages = mGateway.getPackages();
		if (packages != null) {
			for (int i = 0; i < packages.length; i++) {
				clean(packages[i]);
			}
		}
		
		// export class
		INamedElement[] classes = mGateway.getClasses();
		if (classes != null) {
			for (int i = 0; i < classes.length; i++) {
				if (classes[i] instanceof ISubsystem) {
					continue;
				}
				clean((IClass)classes[i]);
			}
		}
		
		// export all sequence diagram
		INamedElement[] seqDgs = mGateway.getSequenceDiagrams();
		if (seqDgs != null) {
			for (int i = 0; i < seqDgs.length; i++) {
				clean((ISequenceDiagram)seqDgs[i]);
			}
		}
		
		// export all sequence diagram
		INamedElement[] stateDgs = mGateway.getStateMachineDiagrams();
		if (stateDgs != null) {
			for (int i = 0; i < stateDgs.length; i++) {
				clean((IStateMachineDiagram)stateDgs[i]);
			}
		}
		
		mGateway.save();
		mGateway.showMessage("Export Done.");
	}
	
	private void clean (IElement element) {
		ITaggedValue tag = mGateway.getTag(element);
		if (tag != null) {
			mGateway.deleteTag(tag);
		}
	}
	
	private void clean (IClass iClass) {
		clean((IElement)iClass);
		
		// clean class function
		IOperation[] funcs = iClass.getOperations();
		if (funcs != null) {
			for (int i = 0; i < funcs.length; i++) {
				clean(funcs[i]);
			}
		}
	}
	
	private void clean (ISequenceDiagram iSeq) {
		clean((IElement)iSeq);

		IPresentation[] psts = mGateway.getPresentation(iSeq);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			
			// clean comment
			if (item instanceof IComment) {
				clean(item);
			}
			// clean combinFragment
			else if (item instanceof ICombinedFragment) {
				clean(item);
			}
			// clean life_line
			else if (item instanceof ILifeline) {
				clean(item);
			}
			// clean interaction
			else if (item instanceof IMessage) {
				clean(item);
			}
			else if (item instanceof IInteractionUse) {
				clean(item);
			}
		}
	}
	
	private void clean (IStateMachineDiagram iStm) {
		clean((INamedElement)iStm);
		
		IPresentation[] psts = mGateway.getPresentation(iStm);
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			// clean comment
			if (item instanceof IComment) {
				clean(item);
			}
			// clean states
			else if (item instanceof IState) {
				clean(item);
			}
			// clean transitions
			else if (item instanceof ITransition) {
				clean(item);
			}
		}
	}
	
}
