package oncecenter.views.grouptreeview;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.action.edit.PageBookViewState;
import oncecenter.action.group.AddGroupAction;
import oncecenter.action.group.AddtoGroupAction;
import oncecenter.action.group.DeleteGroupAction;
import oncecenter.action.group.ResetGroupAction;
import oncecenter.action.vm.RebootAction;
import oncecenter.action.vm.ShutDownAction;
import oncecenter.action.vm.StartAction;
import oncecenter.util.FileUtil;
import oncecenter.util.GroupUtil;
import oncecenter.views.grouptreeview.elements.VMTreeObjectGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectHostinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectPoolinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectRootinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;
import oncecenter.views.xenconnectiontreeview.VMTreeContentProvider;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class VMTreeVMGroupView extends ViewPart {

	public static final String ID = "oncecenter.tree.VMTreeVMGroupView";
	
	private TreeViewer viewer;
	private VMTreeObject invisibleRoot;
	private DrillDownAdapter drillDownAdapter;
	
	public VMTreeVMGroupView() {
		invisibleRoot = new VMTreeObjectDefault("");
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new VMTreeContentProvider());
		viewer.setLabelProvider(new VMTreeGroupLabelProvider());
		viewer.setInput(invisibleRoot);
		drillDownAdapter.addNavigationActions(getViewSite().getActionBars().getToolBarManager());
		
		viewer.getTree().addListener(SWT.Selection, new Listener() {   
			public void handleEvent(Event event) {   
				
				VMTreeObject object=(VMTreeObject)GetSelectedObject();
				VMTreeObject select = null;
				if(object instanceof VMTreeObjectVMinGroup){
					select = ((VMTreeObjectVMinGroup)object).getVmObject();
				}else if(object instanceof VMTreeObjectPoolinGroup){
					select = ((VMTreeObjectPoolinGroup)object).getPoolObject();
				}else if(object instanceof VMTreeObjectHostinGroup){
					select = ((VMTreeObjectHostinGroup)object).getHostObject();
				}
				if(select!=null&&!select.getItemState().equals(ItemState.changing)){
					ISelection selection = new StructuredSelection(new Object[]{select});
					if(Constants.treeView!=null&&Constants.treeView.getViewer()!=null){
						Constants.treeView.getViewer().setSelection(selection);
						Constants.pageBookView.selectionChanged(Constants.treeView, Constants.treeView.getViewer().getSelection());
						PageBookViewState.addState();
					}
				}
				
				
			}   
		});   
		
		invisibleRoot.addChild(Constants.VMGROUPS_TREE);
		
		CreateContextMenu();
		
		viewer.expandAll();
		viewer.refresh();
		ISelection selection = new StructuredSelection(new Object[]{Constants.VMGROUPS_TREE});
		viewer.setSelection(selection);
		
		Constants.groupView=this;
	}

	@Override
	public void setFocus() {
		
		
	}
	
	public VMTreeObject GetSelectedObject(){
		StructuredSelection select = (StructuredSelection)viewer.getSelection();
		VMTreeObject element = (VMTreeObject)select.getFirstElement();
		return element;
	}
	
	public List<VMTreeObject> GetSelectedObjectList(){
		StructuredSelection select = (StructuredSelection)viewer.getSelection();
		return new ArrayList<VMTreeObject>(select.toList());
	}
	
	
	private void CreateContextMenu() {
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				List<VMTreeObject> selectedList = GetSelectedObjectList();
				if(selectedList==null||selectedList.size()==0){
					return;
				}else if (selectedList.size()>1){
					ArrayList<VMTreeObjectVM> selectedVMList = new ArrayList<VMTreeObjectVM>();
					boolean isAllVm = true;
					boolean hasRunningVm = false;
					boolean hasShutVm = false;
					for(VMTreeObject o : selectedList){
						if(!(o instanceof VMTreeObjectVMinGroup)){
							isAllVm = false;
							break;
						}else{
							VMTreeObjectVM vm =  ((VMTreeObjectVMinGroup)o).getVmObject();
							VM.Record record = vm.getRecord();
							Types.VmPowerState state = record.powerState;
							if(state.equals(Types.VmPowerState.RUNNING)){
								hasRunningVm = true;
							}else{
								hasShutVm = true;
							}
							selectedVMList.add(vm);
						}
					}
					if(!isAllVm){
						return;
					}else{
						fillVmListMenu(manager,selectedList);
						manager.add(new Separator());
						VMTreeView.fillVMListMenu(manager,selectedVMList,hasShutVm,hasRunningVm);
					}
				}else{
					VMTreeObject selection = selectedList.get(0);
					if(selection instanceof VMTreeObjectGroup){
						fillGroupMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectVMinGroup){
						fillVMMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectRootinGroup){
						fillGroupRootMenu(manager,selection);
					}
				}

			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	public static void fillGroupRootMenu(IMenuManager manager,
			VMTreeObject selection) {
		manager.add(new AddGroupAction((VMTreeObjectRootinGroup)selection));
	}

	public static void fillVmListMenu(IMenuManager manager,List<VMTreeObject> list){
		List<VMTreeObjectVMinGroup> vmList = new ArrayList<VMTreeObjectVMinGroup>();
		boolean isDefault = true;
		boolean isNotDefault = true;
		for(VMTreeObject o : list){
			if(o.getParent().getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
				isNotDefault = false;
			}else{
				isDefault = false;
			}
			vmList.add((VMTreeObjectVMinGroup)o);
		}
		if(isDefault){
			MenuManager addtogroupMenu = new MenuManager("添加到...");
			for(VMTreeObject group:vmList.get(0).getParent().getParent().getChildren()){
				if(!group.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
					addtogroupMenu.add(new AddtoGroupAction(vmList,(VMTreeObjectGroup)group));
				}
			}
			manager.add(addtogroupMenu);
			return;
		}
		if(isNotDefault){
			manager.add(new ResetGroupAction(vmList));
		}
	}
	public static void fillVMMenu(IMenuManager manager, VMTreeObject selection) {
		
		VMTreeObjectVMinGroup vminGroup =  (VMTreeObjectVMinGroup)selection;
		if(selection.getParent().getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
			MenuManager addtogroupMenu = new MenuManager("添加到...");
			for(VMTreeObject group:vminGroup.getParent().getParent().getChildren()){
				if(!group.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
					addtogroupMenu.add(new AddtoGroupAction(vminGroup,(VMTreeObjectGroup)group));
				}
			}
			manager.add(addtogroupMenu);
		}else{
			manager.add(new ResetGroupAction(vminGroup));
		}
		manager.add(new Separator());
		VMTreeObjectVM vm = vminGroup.getVmObject();
		VM.Record record = vm.getRecord();
		Types.VmPowerState state = record.powerState;
		if(state.equals(Types.VmPowerState.RUNNING)){
			manager.add(new ShutDownAction(vm));
			manager.add(new RebootAction(vm));
		}else{
			manager.add(new StartAction(vm));
		}
	}

	
	public static void fillGroupMenu(IMenuManager manager,
			VMTreeObject selection) {
		
		if(!selection.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
			manager.add(new DeleteGroupAction((VMTreeObjectGroup)selection));
		}
	}
	
	
	public static boolean addServer(VMTreeObject object){
		VMTreeObjectRootinGroup rootinGroup = null;
		if(object instanceof VMTreeObjectHost){
			VMTreeObjectHost host = (VMTreeObjectHost)object;
			VMTreeObjectHostinGroup  hostInGroup = new VMTreeObjectHostinGroup(host);
			host.setShadowObject(hostInGroup);
			rootinGroup = hostInGroup;
		}else{
			VMTreeObjectPool pool = (VMTreeObjectPool)object;
			VMTreeObjectPoolinGroup  poolInGroup = new VMTreeObjectPoolinGroup(pool);
			pool.setShadowObject(poolInGroup);
			rootinGroup = poolInGroup;
		}
		rootinGroup.groupMap = GroupUtil.getGroupConfig(rootinGroup);
		ArrayList<VMTreeObjectVM> vmList = new ArrayList<VMTreeObjectVM>();
		for(VMTreeObjectVM vm:((VMTreeObjectRoot)object).vmMap.values()){
			vmList.add(vm);
		}
		VMTreeObject defaultGroup = null;
//		VMTreeObject haltedVmGroup = null;
//		VMTreeObject templateGroup = null;
		for(VMTreeObject o:rootinGroup.getChildren()){
			if(o.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
				defaultGroup  = o;
			}
//			else if(o.getName().equals(Constants.HALTED_VM_GROUP_DEFAULT_NAME)){
//				haltedVmGroup  = o;
//			}
//			else if(o.getName().equals(Constants.TEMPLATE_GROUP_DEFAULT_NAME)){
//				templateGroup  = o;
//			}
		}
		if(defaultGroup == null){
			defaultGroup = new VMTreeObjectGroup(Constants.VM_GROUP_DEFAULT_NAME);
			rootinGroup.addChild(defaultGroup);
		}
//		if(haltedVmGroup == null){
//			haltedVmGroup = new VMTreeObjectGroup(Constants.HALTED_VM_GROUP_DEFAULT_NAME);
//			rootinGroup.addChild(haltedVmGroup);
//		}
//		if(templateGroup == null){
//			templateGroup = new VMTreeObjectGroup(Constants.TEMPLATE_GROUP_DEFAULT_NAME);
//			rootinGroup.addChild(templateGroup);
//		}
		if(rootinGroup.groupMap!=null){
			ArrayList<VMTreeObjectVM> vminGroupList = new ArrayList<VMTreeObjectVM>();
			for(String groupName:rootinGroup.groupMap.keySet()){
				VMTreeObjectGroup group = new VMTreeObjectGroup(groupName);
				List<String> vminGroup = rootinGroup.groupMap.get(groupName);
				for(VMTreeObjectVM vm:vmList){					
					String vmUuid = vm.getApiObject().toWireString();
					if(vminGroup.contains(vmUuid)){
//						if(vm.getRecord()!=null
//								&&vm.getRecord().powerState!=null
//								&&vm.getRecord().powerState.equals(Types.VmPowerState.RUNNING)){
							VMTreeObjectVMinGroup vmInGroup = new VMTreeObjectVMinGroup(vm);
							vm.setShadowObject(vmInGroup);
							group.addChild(vmInGroup);
							vminGroupList.add(vm);
//						}
					}
				}
				rootinGroup.addChild(group);
			}
			
			vmList.removeAll(vminGroupList);
		}
		else{
			rootinGroup.groupMap = new HashMap<String,List<String>>();
			GroupUtil.addGroupConfig(rootinGroup);
		}
		for(VMTreeObjectVM vm:vmList){
			VMTreeObjectVMinGroup vmInGroup = new VMTreeObjectVMinGroup(vm);
			vm.setShadowObject(vmInGroup);
//			if(vm.getRecord()!=null
//					&&vm.getRecord().powerState!=null
//					&&vm.getRecord().powerState.equals(Types.VmPowerState.RUNNING)){
				defaultGroup.addChild(vmInGroup);
//			}else{
//				haltedVmGroup.addChild(vmInGroup);
//			}
		}
//		for(VMTreeObjectTemplate vm:((VMTreeObjectRoot)object).templateMap.values()){
//			VMTreeObjectTemplateinGroup vmInGroup = new VMTreeObjectTemplateinGroup(vm);
//			vm.setShadowObject(vmInGroup);
//			templateGroup.addChild(vmInGroup);
//		}
		Constants.VMGROUPS_TREE.addChild(rootinGroup);
		
		if(Constants.groupView!=null){
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						Constants.groupView.getViewer().refresh();	
					}
				};
				display.syncExec(runnable);
			}
		}
		return true;
	}
	
	public static void addVM(VMTreeObjectVM vm){
		VMTreeObjectRoot root = vm.getRoot();
		if(root.getShadowObject()!=null&&vm.getShadowObject()==null){
//			if(vm.getRecord()==null
//					||vm.getRecord().powerState==null
//					||!vm.getRecord().powerState.equals(Types.VmPowerState.RUNNING)){
//				for(VMTreeObject o:root.getShadowObject().getChildren()){
//					if(o.getName().equals(Constants.HALTED_VM_GROUP_DEFAULT_NAME)){
//						VMTreeObjectVMinGroup vmInGroup = new VMTreeObjectVMinGroup(vm);
//						vm.setShadowObject(vmInGroup);
//						o.addChild(vmInGroup);
//						break;
//					}
//				}
//			}else{
				for(VMTreeObject o:root.getShadowObject().getChildren()){
					if(o.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
						VMTreeObjectVMinGroup vmInGroup = new VMTreeObjectVMinGroup(vm);
						vm.setShadowObject(vmInGroup);
						o.addChild(vmInGroup);
						break;
					}
				}
//			}
			
		}
		if(Constants.groupView!=null){
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						Constants.groupView.getViewer().refresh();	
					}
				};
				display.syncExec(runnable);
			}
		}
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public static void remove(VMTreeObject o){
		final VMTreeObject shadow = o.getShadowObject();
		if(shadow==null){
			return;
		}
		if(shadow.getParent()!=null){
			shadow.getParent().getChildrenList().remove(shadow);
		}
		if(Constants.groupView!=null){
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						Constants.groupView.getViewer().remove(shadow);
						Constants.groupView.getViewer().refresh();	
					}
				};
				display.syncExec(runnable);
			}
		}
	}
}
