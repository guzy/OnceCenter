//package oncecenter.action.vm;
//
//import java.util.Date;
//
//import oncecenter.Constants;
//import oncecenter.tool.ImageRegistry;
//import oncecenter.tree.VMEvent;
//import oncecenter.tree.VMTreeObject;
//import oncecenter.tree.VMTreeObjectPool;
//import oncecenter.tree.VMTreeObjectVM;
//import oncecenter.tree.VMEvent.eventType;
//import oncecenter.tree.VMTreeObject.ItemState;
//import oncecenter.tree.group.VMTreeView;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.core.runtime.jobs.Job;
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.PlatformUI;
//
//import com.xensource.xenapi.Connection;
//import com.xensource.xenapi.Types;
//import com.xensource.xenapi.VM;
//
//public class SuspendAction extends Action {
//	
//	VMTreeObjectVM selection;
//	Connection connection;
//	VM vm;
//	
//	//toolbar
//	public SuspendAction(){
//		super();
//		
//		setText("挂起");		
//		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SUSPEND_FT));
//		setDisabledImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SUSPEND_FT_DIS));
//	}
//	
//	public SuspendAction(VMTreeObjectVM selection){
//		super();
//		this.selection=selection;
//		setText("挂起");		
//		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SUSPEND));
//	}
//	
//	class VMJob extends Job{
//		VMTreeView viewer;
//		Display display;
//		VM.Record record;
//		public VMJob(Display display,VMTreeView viewer){
//			super("挂起虚拟机"+selection.getName());
//			this.viewer=viewer;
//			this.display=display;
//		}
//		@Override 
//	    protected IStatus run(IProgressMonitor monitor) { 
//	        monitor.beginTask("Suspending ...", 100); 
//	        try {
//	        	Types.VmPowerState state=vm.getPowerState(selection.getConnection());
//	        	//检测当前虚拟机状态，如果不是运行状态则不能挂起
//				if(!state.equals(Types.VmPowerState.RUNNING)){
//					selection.setRecord(vm.getRecord(connection));
//					if (!this.display.isDisposed()){
//					    Runnable runnable = new Runnable(){
//					        public void run(){
//					        	viewer.getViewer().refresh();
//					        }
//					    };
//					    this.display.syncExec(runnable); 
//					}
//					monitor.done();
//					Constants.jobs.remove(this);
//					return Status.CANCEL_STATUS;
//				}
//			} catch (Exception e1) {
//				
//				e1.printStackTrace();
//				monitor.done();
//				Constants.jobs.remove(this);
//				return Status.CANCEL_STATUS;
//			}
//	        selection.setItemState(ItemState.changing);
//	        if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	viewer.getViewer().refresh();
//			        	selection.shutVM();
//			        	selection.refresh();
//						
//			        }
//			    };
//			    this.display.syncExec(runnable); 
//			}
//			try {
//				vm.suspend(connection);
//				record = vm.getRecord(connection);
//			} catch (Exception e) {
//				e.printStackTrace();
//				selection.setItemState(ItemState.able);
//		        if (!this.display.isDisposed()){
//				    Runnable runnable = new Runnable(){
//				        public void run(){
//				        	viewer.getViewer().refresh();
//				        }
//				    };
//				    this.display.syncExec(runnable); 
//				}
//				monitor.done();
//				Constants.jobs.remove(this);
//		        return Status.CANCEL_STATUS;
//			}
//			if(selection.getParent().getParent() instanceof VMTreeObjectPool){
//        		selection.getParent().getChildrenList().remove(selection);
//        		VMTreeObject pool=selection.getParent().getParent();
//				pool.addChild(selection);
//        	}
//			selection.setRecord(record);
//	        selection.setItemState(ItemState.able);
//			if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
////			        	if(reference!=null){
////			        		MainView console=(MainView)reference.getView(false);
////			        		console.startVM();
//////			    			while(!parent.getName().equals("Xen")){
//////			    				IViewReference reference1=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//////			    						.findViewReference(MainView.ID, parent.getConsoleID()+"");
//////			    				if(reference1!=null){
//////			    					MainView console1=(MainView)reference1.getView(false);
//////				    				console1.getLogItem().logFresh();
//////			    				}
//////			    				parent=parent.getParent();
//////			    			}
////						}
//						viewer.getViewer().refresh();
//			        }
//				};
//			    this.display.syncExec(runnable); 
//			}
//			
//	        monitor.done();
//	        Constants.jobs.remove(this);
//	        return Status.OK_STATUS; 
//	    } 
//	}
//	
//	public void run(){
//		if(selection==null){
//			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
//			selection = (VMTreeObjectVM)select.getFirstElement();		
//		}
//		vm = (VM)selection.getApiObject();
//		connection=selection.getConnection();
//		try {
//			//记录日志
//			VMTreeObject parent=selection;
//			VMEvent event=new VMEvent();
//			while(!parent.getName().equals("Xen")){
//				event=new VMEvent();
//				event.setDatetime(new Date());
//				event.setDescription("挂起VM '"+selection.getName()+"'");
//				event.setTarget(selection);
//				event.setTask("");
//				event.setType(eventType.info);
//				//event.setUser(selection.getUsername());
//				event.setImage(ImageRegistry.getImage(ImageRegistry.BOOT));
//				Constants.logView.logFresh(event);
//				parent.events.add(event);
//				parent=parent.getParent();
//			}
//			Constants.logView.logFresh(event);
//			VMTreeView viewer = Constants.treeView;
//			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
//			Constants.jobs.add(job);
//			job.schedule();
//
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
//	}
//}
