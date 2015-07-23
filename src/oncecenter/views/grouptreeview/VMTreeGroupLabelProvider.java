package oncecenter.views.grouptreeview;

import oncecenter.util.ImageRegistry;
import oncecenter.views.grouptreeview.elements.VMTreeObjectGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectHostinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectPoolinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.once.xenapi.Types;

public class VMTreeGroupLabelProvider extends LabelProvider{

	public String getText(Object obj) {
		//此处可以做点文章
		VMTreeObject vobj = (VMTreeObject)obj;
		
		if(vobj instanceof VMTreeObjectVM)
		{
			VMTreeObjectVM vmObject = (VMTreeObjectVM)vobj;
			if(vmObject.getRecord() != null && vmObject.getRecord().isLocalVM)
				return vobj.getName()+"(L)";
		}
		return vobj.getName();
	}
	
	public Image getImage(Object obj) {
		VMTreeObject object = (VMTreeObject)obj;
		if(object instanceof VMTreeObjectDefault){
			return ImageRegistry.getImage(ImageRegistry.DEFAULT);
		}else if (object instanceof VMTreeObjectGroup){
			return ImageRegistry.getImage(ImageRegistry.DEFAULT);
		}else if(object instanceof VMTreeObjectHostinGroup){
			return ImageRegistry.getImage(ImageRegistry.SERVERCONNECT);
		}else if(object instanceof VMTreeObjectPoolinGroup){
			return ImageRegistry.getImage(ImageRegistry.POOL);
		}else if(object instanceof VMTreeObjectVMinGroup){
			VMTreeObjectVM objectVM = ((VMTreeObjectVMinGroup)object).getVmObject();
			if(objectVM.getItemState().equals(ItemState.changing))
			{
				return ImageRegistry.getImage(ImageRegistry.VMCHANGING);
			}
			if(objectVM.getRecord()==null)
			{
				return ImageRegistry.getImage(ImageRegistry.VMOFF);
			}
				
			Types.VmPowerState state = objectVM.getRecord().powerState;
				
			if(state.equals(Types.VmPowerState.RUNNING)){
					return ImageRegistry.getImage(ImageRegistry.VMON);
			}else if(state.equals(Types.VmPowerState.SUSPENDED)){
					return ImageRegistry.getImage(ImageRegistry.VMSUSPEND);
			}else{
					return ImageRegistry.getImage(ImageRegistry.VMOFF);
			}
		}
		return ImageRegistry.getImage(ImageRegistry.DEFAULT);
	}

}
