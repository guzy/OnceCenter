package oncecenter.maintabs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import oncecenter.util.FileUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class HomeTab extends OnceHomeTabItem {
	private Composite map;

	public HomeTab(CTabFolder arg0, int arg1, int arg2,VMTreeObject object) {
		super(arg0, arg1, arg2, object);
		setText("主页");
		Init();
	}
	
	public boolean Init(){
		FormData cp_browser = new FormData();  
        cp_browser.bottom = new FormAttachment(100, 0);  
        cp_browser.right = new FormAttachment(100, 0);  
        cp_browser.top = new FormAttachment(0, 0);  
        cp_browser.left = new FormAttachment(0, 0);
		composite = new Composite(folder, SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//composite.setBackground(new Color(null, 0,255,0));
		Image background = ImageRegistry.getImage(ImageRegistry.MAPBACK);
		composite.setBackgroundImage(background);
		map = new Composite(folder, SWT.NONE);
		map.setBackground(new Color(null, 0,0,255));
		map.setLayout(new FormLayout());
	
		new MapThread(PlatformUI.getWorkbench().getDisplay()).start();
		CLabel imageLable = new CLabel(composite, SWT.CENTER);
		imageLable.setBackground(new Color(null, 255, 255, 255));
		Image image = ImageRegistry.getImage(ImageRegistry.MAP);
		imageLable.setImage(image);
		imageLable.setLayoutData(new GridData(GridData.FILL_BOTH));
		//imageLable.setBackground(background);
		
		//Modified by wuheng09
//		imageLable.addMouseListener(new MouseListener() {
//			
//			@Override
//			public void mouseUp(MouseEvent e) {
//				action();
//			}
//			
//			@Override
//			public void mouseDown(MouseEvent e) {
//				action();
//			}
//			
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//				action();
//			}
//			void action(){
//				setControl(map);
//			}
//		});	
		setControl(composite);
		return true;
	}
	//获取地图
	class MapThread extends Thread{
		Display display;
		public MapThread(Display display) {
			this.display = display;
		}
		public void run(){
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	Browser browser = new Browser(map, SWT.NONE);
			        	FormData fd_browser = new FormData();  
			            fd_browser.bottom = new FormAttachment(100, 0);  
			            fd_browser.right = new FormAttachment(100, 0);  
			            fd_browser.top = new FormAttachment(0, 0);  
			            fd_browser.left = new FormAttachment(0, 0);
			            browser.setLayoutData(fd_browser);
						FileReader input;
						StringBuilder stringBuilder = null;
						try {
							File file = new File(FileUtil.getXenCenterRoot()+"/map1.html");
							input = new FileReader(file);
							stringBuilder = new StringBuilder();
							int bufLen = 1000;
							char[] buf = new char[bufLen];
							int len = 0;
							while((len = input.read(buf)) != -1){
								stringBuilder.append(buf, 0, len);
							}
						} catch (FileNotFoundException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						browser.setText(stringBuilder.toString());
			        	//browser.setUrl(FileUtil.getXenCenterRoot()+"/map1.html");
			        }
			    };
			    this.display.syncExec(runnable); 
			}
		}
	}
}
