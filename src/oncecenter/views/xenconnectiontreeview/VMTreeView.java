package oncecenter.views.xenconnectiontreeview;

import java.util.ArrayList;
import java.util.List;

import oncecenter.Constants;
import oncecenter.action.DisconnectAction;
import oncecenter.action.ItemDeleteAction;
import oncecenter.action.NewSRAction;
import oncecenter.action.NewServerAction;
import oncecenter.action.NewVMAction;
import oncecenter.action.RecoverVMAction;
import oncecenter.action.edit.PageBookViewState;
import oncecenter.action.host.AddHostDescription;
import oncecenter.action.vm.AddVMDescription;
import oncecenter.action.host.AddtoNewPoolAction;
import oncecenter.action.host.AddtoPoolAction;
import oncecenter.action.host.BondtoOvsAction;
import oncecenter.action.host.ConnectServerAction;
import oncecenter.action.host.EjectFromPoolAction;
import oncecenter.action.host.ManageHAAction;
import oncecenter.action.host.RenameHostAction;
import oncecenter.action.pool.AddHostToPoolAction;
import oncecenter.action.pool.RenamePoolAction;
import oncecenter.action.sr.DestroySRAction;
import oncecenter.action.sr.RenameSRAction;
import oncecenter.action.template.AddTemplateDescAction;
import oncecenter.action.template.ChangeToVmAction;
import oncecenter.action.template.DeleteTempAction;
import oncecenter.action.template.QuickCreateVMAction;
import oncecenter.action.template.RenameTempAction;
import oncecenter.action.template.TemplateBackupAction;
import oncecenter.action.template.TemplateRBAction;
import oncecenter.action.vm.AddVMDiskAction;
import oncecenter.action.vm.AdjustCpuAndMemoryAction;
import oncecenter.action.vm.ChangeIsoAction;
import oncecenter.action.vm.ChangetoTemplateAction;
import oncecenter.action.vm.DeleteVMAction;
import oncecenter.action.vm.DeployToolsAction;
import oncecenter.action.vm.EditVmDiskAction;
import oncecenter.action.vm.Export2OtherPoolAction;
import oncecenter.action.vm.ForceShutAction;
import oncecenter.action.vm.MigrateAction;
import oncecenter.action.vm.RebootAction;
import oncecenter.action.vm.RemoteBackupAction;
import oncecenter.action.vm.RenameVMAction;
import oncecenter.action.vm.ResumeAction;
import oncecenter.action.vm.ShutDownAction;
import oncecenter.action.vm.StartFromCDAction;
import oncecenter.action.vm.StartOnAction;
import oncecenter.action.vm.StartAction;
import oncecenter.action.vm.StartOnFromCDAction;
import oncecenter.action.vm.VMBackupAction;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;

public class VMTreeView extends ViewPart{

	public static final String ID = "oncecenter.tree.VMTreeView"; //$NON-NLS-1$

	private VMTreeViewer viewer;
	public TreeEditor editor;
	private VMTreeObject invisibleRoot;
	private DrillDownAdapter drillDownAdapter;
	
	public VMTreeView() {
		invisibleRoot = new VMTreeObjectDefault("");
	}
 
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(176, 196, 222));
		
		viewer = new VMTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new VMTreeContentProvider());
		viewer.setLabelProvider(new VMTreeLabelProvider());
		viewer.setInput(invisibleRoot);
		CreateContextMenu();
		drillDownAdapter.addNavigationActions(getViewSite().getActionBars().getToolBarManager());
		   
		viewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				final VMTreeObject selection=(VMTreeObject)GetSelectedObject();
				if(selection.getItemState().equals(ItemState.unable)&&selection instanceof VMTreeObjectRoot){
					ConnectServerAction action = new ConnectServerAction((VMTreeObjectRoot)selection);
					action.run();
					if(viewer != null && Constants.treeView != null )
						Constants.pageBookView.selectionChanged(Constants.treeView, viewer.getSelection());
					StructuredSelection select = (StructuredSelection)(viewer.getSelection());
					VMTreeObject element = (VMTreeObject)select.getFirstElement();
					setMenuBar(element);
				}
			}
		});
		
		viewer.getTree().addListener(SWT.Selection, new Listener() {   
			public void handleEvent(Event event) {   
//				if(editor!=null&&editor.getEditor()!=null){
//					editor.getEditor().dispose();
//				}
				VMTreeObject object=(VMTreeObject)GetSelectedObject();
				if(object==null||object.getItemState().equals(ItemState.changing)){
					return;
				}
				
				if(viewer != null && Constants.treeView != null )
					Constants.pageBookView.selectionChanged(Constants.treeView, viewer.getSelection());
				PageBookViewState.addState();
				
			}   
		});   
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				VMTreeObject object=(VMTreeObject)GetSelectedObject();
				if(object!=null){
					setMenuBar(object);
				}
				
			}
			
		});
		invisibleRoot.addChild(Constants.CONNECTIONS_TREE);
		
		viewer.expandAll();
		viewer.refresh();
		ISelection selection = new StructuredSelection(new Object[]{Constants.CONNECTIONS_TREE});
		viewer.setSelection(selection);
		editor = new TreeEditor(viewer.getTree());
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 20;
		Constants.treeView=this;
	}
	
	public void setMenuBar(VMTreeObject object)
	{
		for(IAction action:Constants.menuTypeMap.keySet()){
			if(!Constants.menuTypeMap.get(action).contains(Constants.ActionType.all))
				action.setEnabled(false);
		}
		if(object instanceof VMTreeObjectHost&&object.itemState.equals(ItemState.able)){
			for(IAction action:Constants.menuTypeMap.keySet()){
				if(Constants.menuTypeMap.get(action).contains(Constants.ActionType.host))
					action.setEnabled(true);
			}
		}else if(object instanceof VMTreeObjectPool&&object.itemState.equals(ItemState.able)){
			for(IAction action:Constants.menuTypeMap.keySet()){
				if(Constants.menuTypeMap.get(action).contains(Constants.ActionType.pool))
					action.setEnabled(true);
			}
		}else if(object instanceof VMTreeObjectSR){
			for(IAction action:Constants.menuTypeMap.keySet()){
				if(Constants.menuTypeMap.get(action).contains(Constants.ActionType.sr))
					action.setEnabled(true);
			}
		}else if(object instanceof VMTreeObjectTemplate){
			for(IAction action:Constants.menuTypeMap.keySet()){
				if(Constants.menuTypeMap.get(action).contains(Constants.ActionType.template))
					action.setEnabled(true);
			}
		}else if(object instanceof VMTreeObjectVM){
			VM.Record recordVM = ((VMTreeObjectVM)object).getRecord();
			if(recordVM.powerState.equals(Types.VmPowerState.RUNNING)){
				for(IAction action:Constants.menuTypeMap.keySet()){
					if(Constants.menuTypeMap.get(action).contains(Constants.ActionType.runningvm))
						action.setEnabled(true);
				}
			}else{
				for(IAction action:Constants.menuTypeMap.keySet()){
					if(Constants.menuTypeMap.get(action).contains(Constants.ActionType.haltedvm))
						action.setEnabled(true);
				}
			}
		}
	}
	
	@Override
	public void setFocus() {
		// Set the focus
	}
	
	public VMTreeObject GetSelectedObject(){
		StructuredSelection select = (StructuredSelection)viewer.getSelection();
		//return new ArrayList<VMTreeObject>(select.toList());
		VMTreeObject element = (VMTreeObject)select.getFirstElement();
		return element;
	}
	
	public List<VMTreeObject> GetSelectedObjectList(){
		StructuredSelection select = (StructuredSelection)viewer.getSelection();
		return new ArrayList<VMTreeObject>(select.toList());
//		VMTreeObject element = (VMTreeObject)select.getFirstElement;
//		return element;
	}
	
	public TreeItem GetSelectedItem(){
		return viewer.getTree().getSelection()[0];
	}
	
	public void RenameItem(TreeItem item){
		
	}
	
	private void CreateContextMenu() {
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				//VMTreeObject tmp = GetSelectedObject();
				List<VMTreeObject> selectedList = GetSelectedObjectList();
				if(selectedList==null||selectedList.size()==0){
					return;
				}else if (selectedList.size()==1){
					VMTreeObject selection = selectedList.get(0);
					if(selection.getItemState().equals(ItemState.unable)){
						VMTreeView.fillUnableMenu(manager,selection);
					}else if(selection.getItemState().equals(ItemState.changing)){
						return;
					}else if(selection.getName().equals("Xen")){
						VMTreeView.fillXenMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectHost){
						VMTreeView.fillServerMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectVM){
						VMTreeView.fillVMMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectPool){
						VMTreeView.fillPoolMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectTemplate){
						VMTreeView.fillTempMenu(manager,selection);
					}else if(selection instanceof VMTreeObjectSR){
						VMTreeView.fillSRMenu(manager,selection);
					}
				}else{
					boolean isAllVm = true;
					boolean isAllTemplate = true;
					boolean hasShutVm = false;
					boolean hasRunningVm = false;
					ArrayList<VMTreeObjectVM> vmList = new ArrayList<VMTreeObjectVM>();
					ArrayList<VMTreeObjectTemplate> templateList = new ArrayList<VMTreeObjectTemplate>();
					for(VMTreeObject selection:selectedList){
						if(!(selection instanceof VMTreeObjectVM)){
							isAllVm = false;
							break;
						}else{
							VMTreeObjectVM vm =  (VMTreeObjectVM)selection;
							VM.Record record = vm.getRecord();
							Types.VmPowerState state = record.powerState;
							if(state.equals(Types.VmPowerState.RUNNING)){
								hasRunningVm = true;
							}else{
								hasShutVm = true;
							}
							vmList.add(vm);
						}
					}
					for(VMTreeObject selection:selectedList){
						if(!(selection instanceof VMTreeObjectTemplate)){
							isAllTemplate = false;
							break;
						}else{
							VMTreeObjectTemplate vm =  (VMTreeObjectTemplate)selection;
							templateList.add(vm);
						}
					}
					if(isAllTemplate){
						VMTreeView.fillTemplateListMenu(manager,templateList);
					}else if(isAllVm){
						VMTreeView.fillVMListMenu(manager,vmList,hasShutVm,hasRunningVm);
					}
				}

			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	public static  void fillTemplateListMenu(IMenuManager manager,
			List<VMTreeObjectTemplate> templateList) {
			VMTreeObjectPool pool = null;
			ArrayList<VMTreeObjectSR> srList = new ArrayList<VMTreeObjectSR>();
			for(VMTreeObjectTemplate vm:templateList){
				if(vm.getParent() instanceof VMTreeObjectPool){
					if(pool == null){
						pool = (VMTreeObjectPool)vm.getParent();
					}else if(!pool.equals(vm.getParent())){
						pool = null;
						break;
					}
				}else{
					pool = null;
					break;
				}
				VMTreeObjectSR srObject = vm.getStorageObject();
				if(srObject == null){
					try{
						VDI vdi = VDI.getByVM(vm.getConnection(), (VM)vm.getApiObject()).iterator().next();
						vm.setVdi(vdi);
						SR sr = vdi.getSR(vm.getConnection());
						if(sr!=null){
							for(VMTreeObjectSR srObject1:pool.srMap.values()){
								if(srObject1.getApiObject().equals(sr)){
									srObject = srObject1;
									break;
								}
							}
						}
					}catch(Exception e){
						e.printStackTrace();
						srObject = null;
						break;
					}
				}
				if(srObject!=null){
					vm.setStorageObject(srObject);
					srList.add(srObject);
//					if(storageObject == null){
//						storageObject = srObject;
//					}else if(!srObject.equals(storageObject)){
//						storageObject = null;
//						break;
//					}
				}
			}
			MenuManager remoteBackupMenu = new MenuManager("异地备份到");
			if(pool!=null/*&&storageObject!=null*/){
				for(VMTreeObjectSR srObject:pool.srMap.values()){
					if(!srList.contains(srObject)
							/*&&srObject!=storageObject&&!srObject.equals(storageObject)*/
							&&TypeUtil.getDiskSRTypes().contains(srObject.getSrType())
							&&!srObject.getSrType().equals(TypeUtil.localSrType)
							/*&&srObject.getSrType().equals(storageObject.getSrType())*/){
						TemplateRBAction remoteBackupAction = new TemplateRBAction(templateList,srObject);
				        remoteBackupMenu.add(remoteBackupAction);
					}
				}
				manager.add(remoteBackupMenu);
			}
	}
	
	public static  void fillVMListMenu(IMenuManager manager,
			List<VMTreeObjectVM> selectedList, boolean hasShutVm,
			boolean hasRunningVm) {
		if(hasShutVm){
			manager.add(new StartAction(selectedList));
		}
		manager.add(new Separator());
		if(hasRunningVm){
			manager.add(new ShutDownAction(selectedList));
			//manager.add(new RebootAction(selectedList));
		}else{
			manager.add(new DeleteVMAction(selectedList));
			manager.add(new Separator());
			manager.add(new VMBackupAction(selectedList));
			VMTreeObjectPool pool = null;
			ArrayList<VMTreeObjectSR> srList = new ArrayList<VMTreeObjectSR>();
			for(VMTreeObjectVM vm:selectedList){
				if(vm.getParent() instanceof VMTreeObjectPool){
					if(pool == null){
						pool = (VMTreeObjectPool)vm.getParent();
					}else if(!pool.equals(vm.getParent())){
						pool = null;
						break;
					}
				}else{
					pool = null;
					break;
				}
				VMTreeObjectSR srObject = vm.getStorageObject();
				if(srObject == null){
					try{
						VDI vdi = VDI.getByVM(vm.getConnection(), (VM)vm.getApiObject()).iterator().next();
						vm.setVdi(vdi);
						SR sr = vdi.getSR(vm.getConnection());
						if(sr!=null){
							for(VMTreeObjectSR srObject1:pool.srMap.values()){
								if(srObject1.getApiObject().equals(sr)){
									srObject = srObject1;
									break;
								}
							}
						}
					}catch(Exception e){
						e.printStackTrace();
						srObject = null;
						break;
					}
				}
				if(srObject!=null){
					vm.setStorageObject(srObject);
					srList.add(srObject);
				}
			}
			MenuManager remoteBackupMenu = new MenuManager("异地备份到");
			if(pool!=null/*&&storageObject!=null*/){
				for(VMTreeObjectSR srObject:pool.srMap.values()){
					if(//storageObject!=null&&srObject!=null
							//&&srObject!=storageObject&&!srObject.equals(storageObject)
							//&&srObject.getSrType().equals(storageObject.getSrType())
							!srList.contains(srObject)
							&&TypeUtil.getDiskSRTypes().contains(srObject.getSrType())
							&&!srObject.getSrType().equals(TypeUtil.localSrType)){
						System.out.println("selectedList = " + selectedList.toString());
						System.out.println("srObject = " + srObject.getName());
						RemoteBackupAction remoteBackupAction = new RemoteBackupAction(selectedList,srObject);
				        remoteBackupMenu.add(remoteBackupAction);
					}
				}
				manager.add(remoteBackupMenu);
			}
		}
	}

	public static void fillUnableMenu(IMenuManager manager,VMTreeObject object){
		if(object instanceof VMTreeObjectRoot){
			manager.add(new ConnectServerAction((VMTreeObjectRoot)object));
		}
		manager.add(new ItemDeleteAction(object));
	}
	
	public static  void fillXenMenu(IMenuManager manager,VMTreeObject object){
		manager.add(new NewServerAction());
	}
	
	public static  void fillSRMenu(IMenuManager manager,VMTreeObject object){
		VMTreeObjectSR sr = (VMTreeObjectSR)object;
//		manager.add(new DetachSRAction(sr));
//		manager.add(new ForgetSRAction(sr));
		if(TypeUtil.getAllSRTypes().contains(sr.getSrType())
				&&!sr.getSrType().equals(TypeUtil.localSrType)){
			manager.add(new DestroySRAction(sr));
		}
		manager.add(new Separator());
		manager.add(new RenameSRAction(sr));
	}
	
	public static  void fillServerMenu(IMenuManager manager,VMTreeObject object){
		VMTreeObjectHost host = (VMTreeObjectHost)object;
		if(object.getItemState()!=null&&object.getItemState().equals(ItemState.able)){
			manager.add(new NewVMAction(host));
			manager.add(new NewSRAction(host));
			manager.add(new RenameHostAction(host));
			manager.add(new AddHostDescription(host));
			ManageHAAction manageHAAction = new ManageHAAction(host);
			manager.add(manageHAAction);
			manager.add(new Separator());
			if(object.getParent() instanceof VMTreeObjectDefault){
				MenuManager addtoPoolMenu = new MenuManager("添加到资源池...");
				for(VMTreeObject o:Constants.CONNECTIONS_TREE.getChildrenList()){
					if(o.getItemState().equals(ItemState.able)&&o instanceof VMTreeObjectPool){
						addtoPoolMenu.add(new AddtoPoolAction(host,(VMTreeObjectPool)o));
					}
				}
				addtoPoolMenu.add(new Separator());
				addtoPoolMenu.add(new AddtoNewPoolAction(host));
				manager.add(addtoPoolMenu);
			}
			manager.add(new Separator());
			//manager.add(new RenameHostAction(host));
			boolean hasVM = false;
			if(object.getParent() instanceof VMTreeObjectPool){
				VMTreeObjectPool poolObject = (VMTreeObjectPool)object.getParent();
				if(poolObject.hostMap.size()>1&&host.isMaster)
				{
					
				}else{
					if(!hasVM){
						manager.add(new Separator());
						manager.add(new EjectFromPoolAction(host));
					}
				}
//				for(VMTreeObjectVM vmObject :poolObject.vmMap.values()){
//					VM.Record record = (VM.Record)vmObject.getRecord();
//					if(record.residentOn.equals(object.getApiObject())){
//						hasVM = true;
//						break;
//					}
//				}
				
			}
			manager.add(new Separator());
			manager.add(new RecoverVMAction(host));
			manager.add(new Separator());
			manager.add(new BondtoOvsAction(host));
			if(!(object.getParent() instanceof VMTreeObjectPool))
			{	manager.add(new Separator());
				manager.add(new DisconnectAction(host));}
			manager.add(new Separator());
		}
		
	}
	
	public static  void fillTempMenu(IMenuManager manager,VMTreeObject object){
		VMTreeObjectTemplate template = (VMTreeObjectTemplate)object;
		//manager.add(new NewVMAction(template));	
		manager.add(new QuickCreateVMAction(template));
		manager.add(new ChangeToVmAction(template));
		manager.add(new Separator());
		manager.add(new RenameTempAction(template));
		manager.add(new AddTemplateDescAction(template));
		manager.add(new Separator());
		manager.add(new DeleteTempAction(object));
		manager.add(new Separator());
		manager.add(new TemplateBackupAction(template));
		if(object.getParent() instanceof VMTreeObjectPool){
			manager.add(new Separator());
			MenuManager remoteBackupMenu = new MenuManager("异地备份到");
			VMTreeObjectPool pool = (VMTreeObjectPool)object.getParent();
			VMTreeObjectSR storageObject = template.getStorageObject();
			if(storageObject == null){
				try{
					VDI vdi = VDI.getByVM(object.getConnection(), (VM)object.getApiObject()).iterator().next();
					SR sr = vdi.getSR(object.getConnection());
					if(sr!=null){
						for(VMTreeObjectSR srObject1:pool.srMap.values()){
							if(srObject1.getApiObject().equals(sr)){
								storageObject = srObject1;
								break;
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					storageObject = null;
				}
				if(storageObject!=null&&template!=null)
					template.setStorageObject(storageObject);
			}
			for(VMTreeObjectSR srObject:pool.srMap.values()){
				if(storageObject!=null&&srObject!=null&&srObject!=storageObject&&!srObject.equals(storageObject)
						//&&srObject.getSrType().equals(storageObject.getSrType())
						&&TypeUtil.getDiskSRTypes().contains(srObject.getSrType())
						&&!srObject.getSrType().equals(TypeUtil.localSrType)){
					TemplateRBAction remoteBackupAction = new TemplateRBAction(template,srObject);
			        remoteBackupMenu.add(remoteBackupAction);
				}
			}
			manager.add(remoteBackupMenu);
			
//			manager.add(new Separator());
//			MenuManager exportMenu = new MenuManager("导出到资源池...");
//			for(VMTreeObject child:Constants.CONNECTIONS_TREE.getChildren()){
//				if(child instanceof VMTreeObjectPool&&child.getItemState().equals(ItemState.able)
//						&&!child.equals(object.getParent())){
//					MenuManager poolMenu = new MenuManager(child.getName());
//					for(VMTreeObject poolChild:child.getChildren()){
//						if(poolChild instanceof VMTreeObjectSR){
//							VMTreeObjectSR srObject = (VMTreeObjectSR)poolChild;
//							if(//storageObject!=null&&srObject!=null
//									//&&srObject!=storageObject&&!srObject.equals(storageObject)
//									//&&srObject.getSrType().equals(storageObject.getSrType())
//									(srObject.getSrType().equals(TypeUtil.gpfsDiskType)
//											||srObject.getSrType().equals(TypeUtil.nfsZfsType))){
//								Export2OtherPoolAction action = new Export2OtherPoolAction(vm,srObject);
//								poolMenu.add(action);
//							}
//						}
//					}
//					exportMenu.add(poolMenu);
//				}
//			}
			
//			manager.add(exportMenu);
			
		}
	}

	public static void fillPoolMenu(IMenuManager manager,VMTreeObject object){
		VMTreeObjectPool pool = (VMTreeObjectPool)object;
		manager.add(new NewVMAction(pool));
		manager.add(new NewSRAction(pool));	
		MenuManager addMenu = new MenuManager("添加主机...");
		for(VMTreeObject host:Constants.CONNECTIONS_TREE.getChildrenList()){
			if(host instanceof VMTreeObjectHost
					&&host.getItemState().equals(ItemState.able)){
						addMenu.add(new AddHostToPoolAction((VMTreeObjectHost)host,pool));
			}
		}
//		addMenu.add(new Separator());
//		addMenu.add(new AddNewServerAction((VMTreeObjectPool)GetSelectedObject()));
		manager.add(addMenu);
		manager.add(new Separator());
		manager.add(new RenamePoolAction(pool));
		manager.add(new Separator());
		manager.add(new RecoverVMAction(pool));
		manager.add(new Separator());
		manager.add(new DisconnectAction(pool));
//		manager.add(new DeletePoolAction((VMTreeObject)GetSelectedObject()));
	}
	
	public static  void fillVMMenu(IMenuManager manager,VMTreeObject object){
		VMTreeObjectVM vm =  (VMTreeObjectVM)object;
		VM.Record record = vm.getRecord();
			Types.VmPowerState state = record.powerState;
			if(state.equals(Types.VmPowerState.RUNNING)){
				manager.add(new ShutDownAction(vm));
				manager.add(new RebootAction(vm));
				manager.add(new Separator());
				manager.add(new ForceShutAction(vm));
				manager.add(new RenameVMAction(vm));
				manager.add(new AddVMDescription(vm));
				if((object.getParent().getParent() instanceof VMTreeObjectPool) && record.isLocalVM != null && !record.isLocalVM){
					manager.add(new Separator());
					MenuManager migrateMenu = new MenuManager("迁移");
					VMTreeObjectPool pool = (VMTreeObjectPool)object.getParent().getParent();
					for(VMTreeObject host:pool.getChildren()){
						if(host instanceof VMTreeObjectHost){
							MigrateAction migrateAction = new MigrateAction(vm,(VMTreeObjectHost)host);
							//此处需要斟酌一下
							if(object.getParent().equals(host)){
								migrateAction.setEnabled(false);
							}
							VMTreeObjectHost hostObject = (VMTreeObjectHost)host;
							double vmMemory = vm.getMemoryTotalValue();
					        double hostFreeMemory = hostObject.getMemoryTotalValue() - hostObject.getMemoryUsageValue();
					        if(hostFreeMemory<vmMemory){
					        	migrateAction.setEnabled(false);
					        }
							migrateMenu.add(migrateAction);
						}
					}
					manager.add(migrateMenu);
					
				}
//				manager.add(new Separator());
//				manager.add(new DeployToolsAction(vm));
				manager.add(new Separator());
				manager.add(new VMBackupAction(vm));
			}else if(state.equals(Types.VmPowerState.SUSPENDED)){
				manager.add(new ResumeAction(vm));
				manager.add(new Separator());
				manager.add(new ForceShutAction(vm));
				manager.add(new Separator());
				manager.add(new AddVMDescription(vm));
				DeleteVMAction deleteAction = new DeleteVMAction(vm);
				manager.add(deleteAction);
				if(object.getParent() instanceof VMTreeObjectPool){
					manager.add(new Separator());
					manager.add(new VMBackupAction(vm));
				}
			}else{
				manager.add(new StartAction(vm));
				if(vm.getParent() instanceof VMTreeObjectPool&&!record.isLocalVM){
					MenuManager startonMenu = new MenuManager("启动到...");
					VMTreeObjectPool pool = (VMTreeObjectPool)object.getParent();
					for(VMTreeObject host:pool.getChildren()){
						if(host instanceof VMTreeObjectHost){
							StartOnAction startonAction = new StartOnAction(vm,(VMTreeObjectHost)host);
							//此处需要斟酌一下
							VMTreeObjectHost hostObject = (VMTreeObjectHost)host;
							double vmMemory = vm.getMemoryTotalValue();
					        double hostFreeMemory = hostObject.getMemoryTotalValue() - hostObject.getMemoryUsageValue();
					        if(hostFreeMemory<vmMemory){
					        	startonAction.setEnabled(false);
					        }
					        startonMenu.add(startonAction);
						}
					}
					manager.add(startonMenu);
				}
				manager.add(new Separator());
				
				manager.add(new StartFromCDAction(vm));
				if(vm.getParent() instanceof VMTreeObjectPool&&!record.isLocalVM){
					MenuManager startonMenu = new MenuManager("从光盘启动到...");
					VMTreeObjectPool pool = (VMTreeObjectPool)object.getParent();
					for(VMTreeObject host:pool.getChildren()){
						if(host instanceof VMTreeObjectHost){
							StartOnFromCDAction startonAction = new StartOnFromCDAction(vm,(VMTreeObjectHost)host);
							//此处需要斟酌一下
							VMTreeObjectHost hostObject = (VMTreeObjectHost)host;
							double vmMemory = vm.getMemoryTotalValue();
					        double hostFreeMemory = hostObject.getMemoryTotalValue() - hostObject.getMemoryUsageValue();
					        if(hostFreeMemory<vmMemory){
					        	startonAction.setEnabled(false);
					        }
					        startonMenu.add(startonAction);
						}
					}
					manager.add(startonMenu);
				}
				manager.add(new Separator());
				
				if(record.isLocalVM != null && !record.isLocalVM){
					ChangetoTemplateAction tempAction = new ChangetoTemplateAction(vm);
					manager.add(tempAction);
					manager.add(new Separator());
				}
				manager.add(new DeployToolsAction(vm));
				manager.add(new Separator());
				manager.add(new RenameVMAction(vm));
				manager.add(new AddVMDescription(vm));
				manager.add(new Separator());
				manager.add(new AdjustCpuAndMemoryAction(vm));
				manager.add(new EditVmDiskAction(vm));
//				manager.add(new Separator());
//				manager.add(new DeployToolsAction(vm));
				manager.add(new Separator());
				DeleteVMAction deleteAction = new DeleteVMAction(vm);
				//deleteAction.setEnabled(false);
				manager.add(deleteAction);
				manager.add(new Separator());
				//备份操作，修改单机也可进行备份操作
				manager.add(new VMBackupAction(vm));
				if(record.isLocalVM != null && !record.isLocalVM && object.getParent() instanceof VMTreeObjectPool)
				{
					manager.add(new Separator());
					MenuManager remoteBackupMenu = new MenuManager("异地备份到");
					VMTreeObjectPool parent = (VMTreeObjectPool)object.getParent();
					VMTreeObjectSR storageObject = vm.getStorageObject();
					if(storageObject == null){
						try{
							VDI vdi = VDI.getByVM(object.getConnection(), (VM)object.getApiObject()).iterator().next();
							SR sr = vdi.getSR(object.getConnection());
							if(sr!=null){
								for(VMTreeObjectSR srObject1:parent.srMap.values()){
									if(srObject1.getApiObject().equals(sr)){
										storageObject = srObject1;
										break;
									}
								}
							}
						}catch(Exception e){
							e.printStackTrace();
							storageObject = null;
						}
						if(storageObject!=null)
							vm.setStorageObject(storageObject);
					}
					if(storageObject!=null){
						for(VMTreeObjectSR srObject:parent.srMap.values()){
							if(//storageObject!=null&&srObject!=null
									srObject!=storageObject&&!srObject.equals(storageObject)
//									&&srObject.getSrType().equals(storageObject.getSrType())
									&&TypeUtil.getDiskSRTypes().contains(srObject.getSrType())
									&&!srObject.getSrType().equals(TypeUtil.localSrType)){
								RemoteBackupAction remoteBackupAction = new RemoteBackupAction(vm,srObject);
						        remoteBackupMenu.add(remoteBackupAction);
							}
						}
					}
					manager.add(remoteBackupMenu);
				}
				
				//导出到其他资源池
				if(object.getParent() instanceof VMTreeObjectPool)
				{
					VMTreeObjectPool parent = (VMTreeObjectPool)object.getParent();
					VMTreeObjectSR storageObject = vm.getStorageObject();
					if(storageObject == null){
						try{
							VDI vdi = VDI.getByVM(object.getConnection(), (VM)object.getApiObject()).iterator().next();
							SR sr = vdi.getSR(object.getConnection());
							if(sr!=null){
								for(VMTreeObjectSR srObject1:parent.srMap.values()){
									if(srObject1.getApiObject().equals(sr)){
										storageObject = srObject1;
										break;
									}
								}
							}
						}catch(Exception e){
							e.printStackTrace();
							storageObject = null;
						}
						if(storageObject!=null)
							vm.setStorageObject(storageObject);
					}
					manager.add(new Separator());
					MenuManager exportMenu = new MenuManager("导出到资源池...");
					for(VMTreeObject child:Constants.CONNECTIONS_TREE.getChildren()){
						if(child instanceof VMTreeObjectPool&&child.getItemState().equals(ItemState.able)
								&&!child.equals(object.getParent())){
							MenuManager poolMenu = new MenuManager(child.getName());
							for(VMTreeObject poolChild:child.getChildren()){
								if(poolChild instanceof VMTreeObjectSR){
									VMTreeObjectSR srObject = (VMTreeObjectSR)poolChild;
									if(//storageObject!=null&&srObject!=null
											//&&srObject!=storageObject&&!srObject.equals(storageObject)
											//&&srObject.getSrType().equals(storageObject.getSrType())
											TypeUtil.getDiskSRTypes().contains(srObject.getSrType())
											&&!srObject.getSrType().equals(TypeUtil.localSrType)){
										Export2OtherPoolAction action = new Export2OtherPoolAction(vm,srObject);
										poolMenu.add(action);
									}
								}
							}
							exportMenu.add(poolMenu);
						}
					}
					manager.add(exportMenu);
				}
			}
			manager.add(new Separator());
			manager.add(new ChangeIsoAction(vm));
			manager.add(new AddVMDiskAction(vm));
	}
	public VMTreeViewer getViewer() {
		return viewer;
	}
}
