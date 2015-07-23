package oncecenter.views.xenconnectiontreeview;

import java.util.Timer;

import oncecenter.views.grouptreeview.VMTreeVMGroupView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public class VMTreeViewer extends TreeViewer {

	public VMTreeViewer(Composite parent, int style) {
		super(parent, style);
		
	}

	@Override
	public void remove(Object elementsOrTreePaths) {
		if(elementsOrTreePaths instanceof VMTreeObject){
			VMTreeObject child = (VMTreeObject)elementsOrTreePaths;
			cancelTimer(child);
			if(child.getParent()!=null){
				if(child.getParent() instanceof VMTreeObjectDefault){
					VMTreeVMGroupView.remove(child);
				}else{
					VMTreeObjectRoot root = child.getRoot();
					if(child instanceof VMTreeObjectVM){
						((VMTreeObjectVM) child).disconnectVNCClient();
						root.vmMap.remove(child);
						VMTreeVMGroupView.remove(child);
					}else if(child instanceof VMTreeObjectSR){
						root.srMap.remove(child);
					}else if(child instanceof VMTreeObjectHost){
						root.hostMap.remove(child);
					}else if(child instanceof VMTreeObjectTemplate){
						root.templateMap.remove(child);
					}
				}
			}			
		}
		super.remove(elementsOrTreePaths);
	}
	
	public void cancelTimer(VMTreeObject child){
		if(child instanceof VMTreeObjectRoot){
			VMTreeObjectRoot root = (VMTreeObjectRoot)child;
			cancelDeamon(root);
		}
		if(child.timerList!=null){
			for(Timer timer:child.timerList){
				timer.cancel();
			}
		}
		for(VMTreeObject o:child.getChildrenList()){
			cancelTimer(o);
		}
	}
	
	public void cancelDeamon(VMTreeObjectRoot root){
		if(root.getPerformTimer != null)
			root.getPerformTimer.cancel();
		if(root.getRecordTimer != null)
			root.getRecordTimer.cancel();
	}
}
