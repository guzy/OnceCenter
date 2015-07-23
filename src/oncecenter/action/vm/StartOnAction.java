package oncecenter.action.vm;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class StartOnAction extends Action {
	
	VMTreeObjectVM selection;
	VMTreeObjectHost hostObject;
	Connection connection;
	VM vm;
	
	public StartOnAction(VMTreeObjectVM selection,VMTreeObjectHost hostObject){
		super();
		this.selection=selection;
		this.hostObject = hostObject;
		setText(hostObject.getName());		
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		public VMJob(Display display, VMTreeView viewer){
			super("���������"+selection.getName()+"��"+hostObject.getName());
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("������ ...", 100); 

			selection.setItemState(ItemState.changing);
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.getViewer().refresh();
			        }
			    };
			    this.display.asyncExec(runnable); 
			}
	        //�жϵ�ǰ�����״̬
			try {
				Types.VmPowerState state=vm.getPowerState(connection);
				//��ǰ������Ѿ�����
				if(state.equals(Types.VmPowerState.RUNNING)){
					System.out.println("��������Ѿ�����");
					selection.setItemState(ItemState.able);
					selection.setRecord(vm.getRecord(connection));
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
					monitor.done();
					Constants.jobs.remove(this);
					return Status.CANCEL_STATUS;
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
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
	        try {
	        	//���������
	        	vm.setBootOrder(connection, "cd");
	        	Host host=(Host)hostObject.getApiObject();
	        	vm.startOn(connection, host, false, true);
	        	record = vm.getRecord(connection);
	        }catch (Exception e) {
				e.printStackTrace();
				if(e.toString().startsWith("MEMORY_NOT_ENOUGH")){
					//boolean notEnough = true;
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
								messageBox.setText("����");
								messageBox.setMessage("�����" + selection.getName() + "��Դ���㣬����ʧ��");
								messageBox.open();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
				}
				else if(e.toString().startsWith("DISK_IMG_DOES_NOT_EXIST"))
				{
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
								messageBox.setText("��Ϣ");
								messageBox.setMessage("�����" + selection.getName() + "�Ҳ��������ļ�������ʧ��");
								messageBox.open();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
				}
				else if(e.toString().startsWith("FIBER_IN_USE"))
				{
					String [] tmp = e.toString().split(":");
					final String crush_vm = tmp[tmp.length-1];
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
								messageBox.setText("��Ϣ");
								messageBox.setMessage("�����" + selection.getName() + "���˴洢�����������"+crush_vm+"��ͻ, ����ʧ��");
								messageBox.open();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
				}
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
				monitor.done();
				Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
			}
			selection.getParent().getChildrenList().remove(selection);
			hostObject.addChild(selection);
	        selection.setRecord(record);
	        selection.setItemState(ItemState.able);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	selection.startVM();
			        	selection.refresh();
						viewer.getViewer().refresh();
			        }
				};
			    this.display.asyncExec(runnable); 
			}
			
	        monitor.done();
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	
	public void run(){
		if(selection==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();		
		}
		vm = (VM)selection.getApiObject();
		connection=selection.getConnection();
		try {
			//��¼��־
			VMTreeObject parent=selection;
			VMEvent event=new VMEvent();
			while(!parent.getName().equals("Xen")){
				event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("����VM '"+selection.getName()+"'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				//event.setUser(selection.getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.STARTUP));
				parent.events.add(event);
				
				parent=parent.getParent();
			}
			Constants.logView.logFresh(event);
			VMTreeView viewer = Constants.treeView;
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
			Constants.jobs.add(job);
			job.schedule();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}
