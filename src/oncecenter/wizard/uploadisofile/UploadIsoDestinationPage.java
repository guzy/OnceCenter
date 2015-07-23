package oncecenter.wizard.uploadisofile;

import oncecenter.Constants;
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

public class UploadIsoDestinationPage extends NewVMPage {

	private VMTreeObjectPool selectedPool;
	private VMTreeObjectSR selectedSR;
	private CLabel poolCLabel;
	private Combo poolCombo;
	private CLabel srCLabel;
	private Combo srCombo;
	
	protected UploadIsoDestinationPage(String pageName) {
		super(pageName);
		this.setTitle("选择目标资源池及目标存储");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NONE);
		poolCLabel = new CLabel(composite,SWT.NULL);
		poolCLabel.setText("选取目标资源池");
		poolCLabel.setImage(ImageRegistry.getImage(ImageRegistry.POOL));
		poolCLabel.setBounds(20, 30, 300, 25);
		
		poolCombo = new Combo(composite,SWT.DROP_DOWN|SWT.READ_ONLY);
		poolCombo.setBounds(50, 70, 400, 30);
		
		srCLabel = new CLabel(composite,SWT.LEFT);
		srCLabel.setText("选取目标存储");
		srCLabel.setImage(ImageRegistry.getImage(ImageRegistry.STORAGE));
		srCLabel.setBounds(20, 110, 300, 25);
		
		srCombo = new Combo(composite,SWT.SINGLE|SWT.V_SCROLL|SWT.READ_ONLY);
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
				if(selectedPool!=null&&selectedSR!=null)
					setPageComplete(true);
				else
					setPageComplete(false);
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
				if(selectedPool!=null&&selectedSR!=null)
					setPageComplete(true);
				else
					setPageComplete(false);
			}
			
		});
		
		if(poolCombo.getItemCount()>0){
			poolCombo.select(0);
			poolSelectionChange();
		}
		if(srCombo.getItemCount()>0){
			srCombo.select(0);
			srSelectionChange();
		}
		
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
				return;
			}
		}
	}
	
	public void refreshSRList(){
		srCombo.removeAll();
		for(VMTreeObject child:selectedPool.getChildren()){
			if(child instanceof VMTreeObjectSR && ((VMTreeObjectSR)child).getSrType().contains(TypeUtil.isoSign)){
				srCombo.add(child.getName());
			}
		}
		srCombo.redraw();
	}
	
	public void srSelectionChange(){
		for(VMTreeObject child:selectedPool.getChildren()){
			if(child instanceof VMTreeObjectSR&&child.getName().equals(srCombo.getText())){
				selectedSR = (VMTreeObjectSR)child;
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
//		if(selectedPool!=null&&selectedSR!=null)
//			return true;
//		else
			return false;
	}

}
