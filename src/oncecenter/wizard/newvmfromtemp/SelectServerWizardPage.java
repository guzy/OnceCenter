package oncecenter.wizard.newvmfromtemp;

import java.util.ArrayList;

import oncecenter.util.ImageRegistry;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.once.xenapi.Host;
import com.once.xenapi.SR;


public class SelectServerWizardPage extends NewVMPage {

	private VMTreeObject selection;
	private Table table;
	TableViewer tableViewer;
	
	private Button m_yesButton;
	private Button m_noButton;
	
	boolean isPool;
	VMTreeObject selectedHost;
	
	ArrayList<Server> servers = new ArrayList<Server>();
	
	public VMTreeObject getSelectedHost() {
		return selectedHost;
	}

	public void setSelectedHost(VMTreeObject selectedHost) {
		this.selectedHost = selectedHost;
	}

	ArrayList<VMTreeObjectHost> hosts;
	ArrayList<VMTreeObjectSR> srs;

	/**
	 * Create the wizard.
	 */
	public SelectServerWizardPage(VMTreeObject selection) {
		super("wizardPage");
		setTitle("选择一台主机");
		setDescription("您可以选择让虚拟机固定在某台主机上启动，或者在池内的任何主机上启动");
		this.selection = selection;
		
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		this.isPool = ((NewVmFTWizard)this.getWizard()).isPool;
		this.hosts = ((NewVmFTWizard)this.getWizard()).hosts;
		this.srs = ((NewVmFTWizard)this.getWizard()).srs;
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		


		m_yesButton = new Button(composite, SWT.RADIO);
		m_yesButton.setText("不指定主机");
		//m_yesButton.setSelection(true);

		m_noButton = new Button(composite, SWT.RADIO);
		m_noButton.setText("将这台虚拟机放置在本台主机上：");

		if(isPool){
			m_yesButton.setSelection(true);
		}else{
			m_noButton.setSelection(true);
			m_yesButton.setEnabled(false);
		}
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		GridData gridData = new GridData();
		gridData.heightHint = 200;
		gridData.widthHint = 460;
		table.setLayoutData(gridData);

		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		tc1.setText("主机");
		tc2.setText("存储空间");
		tc1.setWidth(180);
		tc2.setWidth(200);
		table.setHeaderVisible(false);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		for(VMTreeObjectHost h:hosts){
			Host host = (Host)h.getApiObject();
			VMTreeObjectSR srObject = null;
			for(VMTreeObjectSR sr:srs){
				if(sr.getSrType().equals(TypeUtil.localSrType)
						&&(sr.getRecord()!=null)
						&&host.equals(((SR.Record)sr.getRecord()).residentOn)){
					srObject = sr;
					break;
				}
			}
			if(srObject!=null){
				Server s = new Server(h,srObject,((SR.Record)srObject.getRecord()).physicalSize
						-((SR.Record)srObject.getRecord()).physicalUtilisation);
				servers.add(s);
			}
			else
			{
				Server s = new Server(h,null,-1);
				servers.add(s);
			}
//			TableItem item = new TableItem(table, SWT.NONE);
//			item.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//					ImageRegistry.getImagePath(ImageRegistry.SERVERDISCON)).createImage());
//			item.setText(new String[] { h.getName(), "     MB available" });
//			if(selectedHost!=null&&h.getName().equals(selectedHost.getName())){
//				table.setSelection(item);
//			}
		}
		
		tableViewer.setInput(servers);
		
		table.select(0);
		Listener yesOrNoRadioGroupListener = new Listener() {

			public void handleEvent(Event p_event) {

				Button button = (Button) p_event.widget;

				if (m_yesButton.equals(button)) {
					m_yesButton.setSelection(true);
					m_noButton.setSelection(false);
					table.setEnabled(false);
				} else {
					m_yesButton.setSelection(false);
					m_noButton.setSelection(true);
					table.setEnabled(true);
					// table.setSelection(0);
				}
			}
		};

		m_yesButton.addListener(SWT.Selection, yesOrNoRadioGroupListener);
		m_noButton.addListener(SWT.Selection, yesOrNoRadioGroupListener);

		// p_dbContext.bindValue(SWTObservables.observeSelection(this.getYesButton()),
		// p_modelProperty, null, null);

		if(isPool){
			table.setEnabled(false);
		}else{
			table.setEnabled(true);
		}
		table.pack();
		setControl(composite);
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof Server) {
			   switch(columnIndex) {
			   case 0:
			    return ImageRegistry.getImage(ImageRegistry.SERVERCONNECT);
			   
			   }
			  }
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Server) {
			  Server server=(Server)element;
		   switch(columnIndex) {
		   case 0:
		    return server.getObject().getName();
		   
		   case 1:
			   return server.getAvailableSize()/1024/1024+" MB available";
		  
		   }
		  }
		  
		  return null;
		 }
	}
	
	class Server {
		private VMTreeObjectHost object;
		private VMTreeObjectSR localSR;
		private long availableSize;
		public Server(VMTreeObjectHost object,VMTreeObjectSR localSR,long availableSize){
			this.object=object;
			this.localSR = localSR;
			this.availableSize = availableSize;
		}
		public VMTreeObjectHost getObject() {
			return object;
		}
		public void setObject(VMTreeObjectHost object) {
			this.object = object;
		}
		public long getAvailableSize() {
			return availableSize;
		}
		public void setAvailableSize(long availableSize) {
			this.availableSize = availableSize;
		}
		public VMTreeObjectSR getLocalSR() {
			return localSR;
		}
		public void setLocalSR(VMTreeObjectSR localSR) {
			this.localSR = localSR;
		}
	}

	@Override
	protected boolean nextButtonClick() {
		
		if(m_yesButton.getSelection()){
			((NewVmFTWizard)this.getWizard()).isAssignHost = false;
		}else{
			((NewVmFTWizard)this.getWizard()).isAssignHost = true;
			int index = table.getSelectionIndex();
			((NewVmFTWizard)this.getWizard()).selectedHost = servers.get(index).getObject();
			((NewVmFTWizard)this.getWizard()).selectedSR = servers.get(index).getLocalSR();
		}
		
		return true;
	}

}
