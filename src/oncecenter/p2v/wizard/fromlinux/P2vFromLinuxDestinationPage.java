package oncecenter.p2v.wizard.fromlinux;

import oncecenter.Constants;
import oncecenter.p2v.wizard.fromwindows.P2vFromWindowsWizard;
import oncecenter.util.ImageRegistry;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class P2vFromLinuxDestinationPage extends NewVMPage {

	private VMTreeObjectPool selectedPool;
	private VMTreeObjectSR selectedSR;
	private CLabel poolCLabel;
	private Combo poolCombo;
	private CLabel srCLabel;
	private Combo srCombo;
	public P2vFromLinuxDestinationPage(String pageName) {
		super(pageName);
		this.setTitle("选择要转化到的目标资源池及目标存储");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NONE);
		poolCLabel = new CLabel(composite,SWT.NULL);
		poolCLabel.setText("选取目标资源池");
		poolCLabel.setImage(ImageRegistry.getImage(ImageRegistry.POOL));
		poolCLabel.setBounds(20, 30, 300, 25);
		
		poolCombo = new Combo(composite,SWT.DROP_DOWN);
		poolCombo.setBounds(50, 70, 400, 30);;
		
		srCLabel = new CLabel(composite,SWT.LEFT);
		srCLabel.setText("选取目标存储");
		srCLabel.setImage(ImageRegistry.getImage(ImageRegistry.STORAGE));
		srCLabel.setBounds(20, 110, 300, 25);
		
		srCombo = new Combo(composite,SWT.SINGLE|SWT.V_SCROLL);
		srCombo.setBounds(50, 150, 400, 90);
		for(VMTreeObject treeObject:Constants.CONNECTIONS_TREE.getChildren()){
			if(treeObject.getItemState().equals(ItemState.able)&&treeObject instanceof VMTreeObjectPool){
				poolCombo.add(treeObject.getName());
				VMTreeObjectPool poolObject = (VMTreeObjectPool)treeObject;
				for(VMTreeObject child:poolObject.getChildren()){
					if(child instanceof VMTreeObjectSR){
						srCombo.removeAll();
						srCombo.add(child.getName());
					}
				}
			}
		}
		
		
		poolCombo.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				

				poolSelectionChange();
				srCombo.select(0);
				srSelectionChange();
			}
			
		});
		
		srCombo.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				srSelectionChange();
			}
			
		});
		
		poolCombo.select(0);
		poolSelectionChange();
		srCombo.select(0);
		srSelectionChange();
		
		this.setControl(composite);
		
	}
	
	public void poolSelectionChange(){
		refreshSelectedPool();
		refreshSRList();
	}
	
	public void refreshSelectedPool(){
		for(VMTreeObject treeObject:Constants.CONNECTIONS_TREE.getChildren()){
			if(treeObject.getName().equals(poolCombo.getText())){
				selectedPool = (VMTreeObjectPool)treeObject;
				if(this.getWizard() instanceof P2vFromWindowsWizard){
					((P2vFromWindowsWizard)this.getWizard()).setSelectedPool(selectedPool);
				}
				return;
			}
		}
	}
	
	public void refreshSRList(){
		srCombo.removeAll();
		for(VMTreeObject child:selectedPool.getChildren()){
			if(child instanceof VMTreeObjectSR && ((VMTreeObjectSR)child).getSrType().equals(TypeUtil.nfsZfsType)){
				srCombo.add(child.getName());
			}
		}
		srCombo.redraw();
	}
	
	public void srSelectionChange(){
		for(VMTreeObject child:selectedPool.getChildren()){
			if(child instanceof VMTreeObjectSR&&child.getName().equals(srCombo.getText())){
				selectedSR = (VMTreeObjectSR)child;
				if(this.getWizard() instanceof P2vFromWindowsWizard){
					((P2vFromWindowsWizard)this.getWizard()).setSelectedSR(selectedSR);
				}
				return;
			}
		}
	}

	public VMTreeObjectPool getSelectedPool() {
		return selectedPool;
	}

	public void setSelectedPool(VMTreeObjectPool selectedPool) {
		this.selectedPool = selectedPool;
	}

	public VMTreeObjectSR getSelectedSR() {
		return selectedSR;
	}

	public void setSelectedSR(VMTreeObjectSR selectedSR) {
		this.selectedSR = selectedSR;
	}

	@Override
	public boolean canFlipToNextPage() {
		if(selectedPool!=null&&selectedSR!=null)
			return true;
		else
			return false;
	}

	@Override
	protected boolean nextButtonClick() {
		
		return true;
	}
}
