package oncecenter.views.xenconnectiontreeview;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectConnectionRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.once.xenapi.Types;

public class VMTreeLabelProvider extends LabelProvider{

	public String getText(Object obj) {
		//此处可以做点文章
		VMTreeObject vobj = (VMTreeObject)obj;
		if(vobj instanceof VMTreeObjectHost){
			VMTreeObjectHost hostObject = (VMTreeObjectHost)vobj;
			if(hostObject.isMaster)
				return vobj.getName()+"(M)";
			else if(hostObject.isBackup)
				return vobj.getName()+"(B)";
		}
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
		if(object.getItemState().equals(ItemState.unable)){
			return ImageRegistry.getImage(ImageRegistry.SERVERDISCON);
		}
		if(object instanceof VMTreeObjectConnectionRoot){
			return ImageRegistry.getImage(ImageRegistry.LOGO);
		}else if(object instanceof VMTreeObjectTemplate){
			if(object.getItemState().equals(ItemState.able)){
				return ImageRegistry.getImage(ImageRegistry.TEMPLATE);
			}else{
				return ImageRegistry.getImage(ImageRegistry.TEMPLATE_DISABLE);
			}
		}else if(object instanceof VMTreeObjectHost){
			if(object.getItemState().equals(ItemState.changing))
			{
				return ImageRegistry.getImage(ImageRegistry.SERVERCHANGING);
			}
			return ImageRegistry.getImage(ImageRegistry.SERVERCONNECT);
		}else if(object instanceof VMTreeObjectPool){
			if(object.getItemState().equals(ItemState.able)){
				return ImageRegistry.getImage(ImageRegistry.POOL);
			}else{
				return ImageRegistry.getImage(ImageRegistry.POOL_DISABLE);
			}
		}else if(object instanceof VMTreeObjectVM){
			VMTreeObjectVM objectVM = (VMTreeObjectVM)object;
			if(object.getItemState().equals(ItemState.changing))
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
		}else if(object instanceof VMTreeObjectSR){
			if(object.getItemState().equals(ItemState.able)){
				return ImageRegistry.getImage(ImageRegistry.STORAGE);
			}else{
				return ImageRegistry.getImage(ImageRegistry.STORAGE_DISABLE);
			}
		}
		return ImageRegistry.getImage(ImageRegistry.DEFAULT);
	}
}
