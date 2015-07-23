package oncecenter.action.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.action.OnceAction;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class ShutDownAction extends OnceAction {

	VMTreeObjectVM selection;
	List<VMTreeObjectVM> selectionList;
	
	public ShutDownAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
		
	}
	
	public ShutDownAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;
		
		setText("关机");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SHUTDOWN));
	}
	
	public ShutDownAction(List<VMTreeObjectVM> selectionList){
		super();
		this.selectionList=selectionList;
		
		setText("关机");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SHUTDOWN));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		List<VMTreeObjectVM> selectionList;
		Connection connection;
		VM vm;
		public VMJob(Display display, VMTreeView viewer,List<VMTreeObjectVM> selectionList){
			super("关闭虚拟机");
			this.viewer=viewer;
			this.display=display;
			this.selectionList = selectionList;
			this.connection = selectionList.get(0).getConnection();
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("正在关机 ...", 100); 
	        for(VMTreeObjectVM selection:selectionList){
	        	selection.setItemState(ItemState.changing);
	        }
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.getViewer().refresh();
			        	if(Constants.groupView!=null)
			        		Constants.groupView.getViewer().refresh();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
	        int step = 100/selectionList.size();
	        int i = 0;
	        for(final VMTreeObjectVM selection:selectionList){
	        	monitor.worked(step*i++);
	        	VM vm = (VM)selection.getApiObject();
	        	try {
		        	Types.VmPowerState state=vm.getPowerState(selection.getConnection());
					if(state.equals(Types.VmPowerState.HALTED)){
						selection.setRecord(vm.getRecord(connection));
						selection.setItemState(ItemState.able);
						if (!this.display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run(){
						        	viewer.getViewer().refresh();
						        	if(Constants.groupView!=null)
						        		Constants.groupView.getViewer().refresh();
						        }
						    };
						    this.display.asyncExec(runnable); 
						}
						continue;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					selection.setItemState(ItemState.able);
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	viewer.getViewer().refresh();
					        	if(Constants.groupView!=null)
					        		Constants.groupView.getViewer().refresh();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
					continue;
				}
		        if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	selection.shutVM();
				        	selection.refresh();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
				try {
					vm.cleanShutdown(connection);
					record = vm.getRecord(connection);
				} catch (Exception e) {
					e.printStackTrace();
					selection.setItemState(ItemState.able);
			        if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	viewer.getViewer().refresh();
					        	if(Constants.groupView!=null)
					        		Constants.groupView.getViewer().refresh();
					        }
					    };
					    this.display.syncExec(runnable); 
					}
					continue;
				}
				
				//将关机的虚拟机迁移到池下面
				if(selection.getParent().getParent() instanceof VMTreeObjectPool){
	        		selection.getParent().getChildrenList().remove(selection);
	        		VMTreeObject pool=selection.getParent().getParent();
					pool.addChild(selection);
	        	}
				
				selection.setRecord(record);
				
		        selection.setItemState(ItemState.able);
		        
//		        VMTreeObject vmInGroup = selection.getShadowObject();
//		        if(vmInGroup!=null){
//		        	 VMTreeObject haltedGroup = null;
//				        for(VMTreeObject group:vmInGroup.getParent().getParent().getChildrenList()){
//				        	if(group.getName().equals(Constants.HALTED_VM_GROUP_DEFAULT_NAME)){
//				        		haltedGroup = group;
//				        		break;
//				        	}
//				        }
//				        if(haltedGroup!=null){
//				        	vmInGroup.getParent().getChildrenList().remove(vmInGroup);
//				        	haltedGroup.addChild(vmInGroup);
//				        }
//		        }
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
							viewer.getViewer().refresh();
							if(Constants.groupView!=null)
				        		Constants.groupView.getViewer().refresh();
				        }
					};
				    this.display.syncExec(runnable); 
				}
	        }
	        
	        monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	public void run(){
		if(selection==null && selectionList ==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();	
		}
		if(selectionList != null){
			//记录日志暂时没写，回头补上
			VMTreeView viewer = Constants.treeView;
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer,selectionList);
			Constants.jobs.add(job);
			job.schedule();
		}else{
			VMTreeObject parent=selection;
			VMEvent event=new VMEvent();
			while(!parent.getName().equals("Xen")){
				event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("关闭 VM '"+selection.getName()+"'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				//event.setUser(selection.getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.SHUTDOWN));
				parent.events.add(event);
				parent=parent.getParent();
			}
			Constants.logView.logFresh(event);
			VMTreeView viewer = Constants.treeView;
			selectionList = new ArrayList<VMTreeObjectVM>();
			selectionList.add(selection);
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer,selectionList);
			Constants.jobs.add(job);
			job.schedule();
		}
	}
}
