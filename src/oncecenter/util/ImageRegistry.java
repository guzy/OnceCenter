package oncecenter.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import oncecenter.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageRegistry {
	private static ImageRegistry INSTANCE;

	public static ImageRegistry getRegistry() {
		if (INSTANCE == null) {
			synchronized (ImageRegistry.class) {
				if (INSTANCE == null) {
					INSTANCE = new ImageRegistry();
				}
			}
		}
		return INSTANCE;
	}

	/**
	 * The directory of the images
	 */
	private static String ICON_FOLDER = "icons"+File.separator+"console"+File.separator;

	
	
	public static String INFO = "info.png";
	public static String SETTING = "setting.png";
	public static String CONSOLE = "console.png";
	public static String CONSOLE_DISABLE = "console_disable.jpg";
	public static String PERFORM = "mchart.png";
	public static String DELETE = "icon"+File.separator+"delete.png";
	public static String TEMPLATES = "templates.png";
	
	public static String VMS = "vm.png";
	public static String DEFAULT = "default.png";
	public static String BOOT = "boot.png";
	public static String VM = "vm.png";
	public static String EMPTY = "snapshots"+File.separator+"empty.jpg";
	public static String PERIOD = "snapshots" + File.separator + "period.png";
	
	
	public static String NETWORK = "network.png";
	public static String SAVE = "save.png";
	public static String PRINT = "print.png";
	public static String REFRESH = "refresh.png";
	public static String BACK = "back.png";
	public static String FORWARD = "next.png";
	public static String APP = "application.png";
	public static String COMPONENT = "component.png";
	public static String BIGAPP = "bigapplications.png";
	public static String BIGCOMP = "bigcomponent.png";
	public static String arrow = "arrow.png";
	public static String LINE = "line.png";
	
	public static String WINDOWS = "windows.png";
	public static String CENTOS = "centos.png";
	public static String REDHAT = "redhat.jpg";
	
	public static String ADDITEM = "action" + File.separator + "add_item.png";
	public static String DELITEM = "action" + File.separator + "del_item.png";
	public static String ADDITEMUNABLE = "action" + File.separator + "add_item_unable.png";
	public static String DELITEMUNABLE = "action" + File.separator + "del_item_unable.png";
	public static String EDITDISK = "action" + File.separator + "editdisk.png";
	//toolbar
	public static String BACK_FT = "toolbar"+File.separator+"back.png";
	public static String FORWARD_FT = "toolbar"+File.separator+"forward.png";
	
	public static String ADDSERVER_FT = "toolbar"+File.separator+"add_server.png";
	public static String ADDPOOL_FT = "toolbar"+File.separator+"addpool.png";
	public static String ADDVM_FT = "toolbar"+File.separator+"add_vm.png";
	public static String ADDSTORAGE_FT = "toolbar"+File.separator+"addstorage.png";
	
	public static String SHUT_FT = "toolbar"+File.separator+"shutdown.png";
	public static String REBOOT_FT = "toolbar"+File.separator+"reboot.png";
	public static String SUSPEND_FT = "toolbar"+File.separator+"suspend.png";
	public static String START_FT = "toolbar"+File.separator+"start.png";
	public static String FRESH_FT = "toolbar"+File.separator+"refresh.png";
	public static String IMPORTPHY = "toolbar"+File.separator+"import_phy.png";
	
	public static String RECOVER = "toolbar"+File.separator+"recover.png";
	public static String UPGRADE = "toolbar"+ File.separator + "waitupdate.png";
	
	public static String UPLOAD = "toolbar"+ File.separator + "upload.png";
	
	//toolbar disable
	
	public static String BACK_FT_DIS = "toolbar"+File.separator+"back_disable.png";
	public static String FORWARD_FT_DIS = "toolbar"+File.separator+"forward_disable.png";
	
	public static String ADDVM_FT_DIS = "toolbar"+File.separator+"add_vm_disable.png";
	public static String ADDSTORAGE_FT_DIS = "toolbar"+File.separator+"addstorage_disable.png";
	
	public static String SHUT_FT_DIS = "toolbar"+File.separator+"shutdown_disable.png";
	public static String REBOOT_FT_DIS = "toolbar"+File.separator+"reboot_disable.png";
	public static String SUSPEND_FT_DIS = "toolbar"+File.separator+"suspend_disable.png";
	public static String START_FT_DIS = "toolbar"+File.separator+"start_disable.png";
	
	public static String UPGRADE_DIS = "toolbar"+ File.separator + "update.png";
	
	public static String UPLOAD_DIS = "toolbar"+ File.separator + "noupload.png";
	//action
	public static String STARTUP = "action"+File.separator+"start.png";
	public static String STARTUP_DISABLE = "action"+File.separator+"start_disable.jpg";
	public static String SHUTDOWN = "action"+File.separator+"shutdown.png";
	public static String SHUTDOWN_DISABLE = "action"+File.separator+"shutdown_disable.jpg";
	public static String REBOOT = "action"+File.separator+"reboot.png";
	public static String REBOOT_DISABLE = "action"+File.separator+"reboot_disable.jpg";
	public static String SUSPEND = "action"+File.separator+"suspend.png";
	public static String SUSPEND_DISABLE = "action"+File.separator+"suspeng_disable.jpg";
	
	public static String ADDSERVER = "action"+File.separator+"add_server.png";
	public static String ADDPOOL = "action"+File.separator+"add_pool.png";
	public static String ADDPOOLDISABLE = "action"+File.separator+"add_pool_disable.png";
	public static String ADDVM = "action"+File.separator+"add_vm.png";
	public static String ADDVMDISABLE = "action"+File.separator+"add_vm_disable.png";
	public static String ADDSTORAGE = "action"+File.separator+"add_storage.png";
	public static String ADDSTORAGEDISABLE = "action"+File.separator+"add_storage_disable.png";

	public static String SNAPSHOT = "action"+File.separator+"snap.png";
	
	public static String ADJUST_CPU_MEMORY = "action"+File.separator+"adjustCpuAndMemory.png";
	public static String REBOOT_OR_NOT = "action"+File.separator+"reboot_or_not.png";
	
	
	//tree
	public static String LOGO = "tree"+File.separator+"logo.png";
	public static String POOL = "tree"+File.separator+"pool.png";
	public static String POOL_DISABLE = "tree"+File.separator+"pool_middle.png";
	public static String SERVERCONNECT = "tree"+File.separator+"server_start.png";
	public static String SERVERCHANGING = "tree"+File.separator+"server_middle.png";
	public static String SERVERDISCON = "tree"+File.separator+"server_close.png";
	public static String SERVEROFF = "tree"+File.separator+"server_close.png";
	public static String VMON = "tree"+File.separator+"vm_start.png";
	public static String VMOFF = "tree"+File.separator+"vm_stop.png";
	public static String VMCHANGING = "tree"+File.separator+"vm_middle.png";
	public static String VMSUSPEND = "tree"+File.separator+"vm_suspend.png";
	public static String TEMPLATE = "tree"+File.separator+"template.png";
	public static String TEMPLATE_DISABLE = "tree"+File.separator+"template_disable.png";
	public static String STORAGE = "tree"+File.separator+"storage.png";
	public static String STORAGE_DISABLE = "tree"+File.separator+"storage_middle.png";
	
	//tray
	public static String TRAY = "logo"+File.separator+"logo16.png";
	
	//homepage
	public static String MAP = "home"+File.separator+"ditu.jpg";
	public static String MAPBACK = "home"+File.separator+"background.jpg";
	
	//虚拟机状态
	public static String FREE = "free1.png";
	public static String DISABLE = "disable1.gif";
	public static String BUSY = "busy1.gif";
	
	//主机评分
	public static String GRADE = "grade.png";
	
	//cpu绑定
	public static String VCPU = "cpu"+File.separator+"vcpu.png";
	public static String PCPU = "cpu"+File.separator+"pcpu.png";
	public static String CPUBIND = "cpu"+File.separator+"bind.jpg";
	
	//vlan
	public static String SWITCH = "vlan"+File.separator+"switch.png";
	public static String VMINVLAN = "vlan"+File.separator+"vm.png";
	
	public static String LOG = "log" + File.separator + "log.png";
	
	//pool常规
	public static String SRNEW = "sr_new.png";
	public static String REMOVEPOOL = "remove.png";
	
	public static String SERVICENOTOPEN = "servernotopen.png";
	public static String SUCCESS = "success.png";
	public static String FAILURE = "error.png";
	public static String RIGHT = "right.png";
	public static String ADDTOPOOLERROR = "add_to_pool_error.png";
	
	//快照操作
	public static String SNAPINFO = "snapshots" + File.separator+"info.png";
	
	//help
	public static String HELP = "help.png";
	
	//priority
	public static String HIGHPRIORITY = "priority" + File.separator+"high.png";
	public static String MIDDLEPRIORITY = "priority" + File.separator+"middle.png";
	public static String LOWPRIORITY = "priority" + File.separator+"low.png";
	
	public static String BACKPRIORITY = "priority" +File.separator + "back.jpg";
	
	public static String ARROW_RIGHT = "arrow_right.png";
	public static String ARROW_LEFT = "arrow_left.png";
	
	//空性能数据图
	public static String cpuChart = "perform" + File.separator + "cpu.png";
	public static String memoryChart = "perform" + File.separator + "memory.png";
	public static String diskChart = "perform" + File.separator + "disk.png";
	public static String netChart = "perform" + File.separator + "network.png";
	
	//主机的虚拟机图表
	public static String SERVERVIRTUALIZATION = "hostpage" + File.separator + "server.jpg";
	public static String VMONINCHART = "hostpage" + File.separator + "openvm.png";
	public static String VMOFFINCHART = "hostpage" + File.separator + "shutdownvm.png";
	
	/**
	 * Map used to store all of images
	 */
	private Map<String, Image> images;
	/**
	 * Map used to store all of imageDescriptors
	 */
	private Map<String, ImageDescriptor> imageDescriptors;

	/**
	 * Method used to find the Icons
	 * @return  The list of Icons' name
	 */
	public static List<String> findIconsList() {
		Field[] fields = ImageRegistry.class.getFields();
		List<String> list = new ArrayList<String>();
		for (Field field : fields) {
			int modifies = field.getModifiers();
			if (field.getType().equals(String.class)
					&& (modifies & Modifier.PUBLIC) != 0
					&& (modifies & Modifier.STATIC) != 0) {
				try {
					list.add(field.get(null).toString());
				} catch (Exception e) {
				}
			}
		}
		return list;
	}

	/**
	 * constructor
	 */
	private ImageRegistry() {
		List<String> list = findIconsList();
		images = new ConcurrentHashMap<String, Image>();
		imageDescriptors = new ConcurrentHashMap<String, ImageDescriptor>();


//		for (String name : list) {
//			
//			try {
//				URL url = new URL("file:"+Constant.workspace+ICON_FOLDER+name);
//				ImageDescriptor imageDescriptor = ImageDescriptor
//						.createFromURL(url);
//				imageDescriptors.put(name, imageDescriptor);
//				images.put(name, imageDescriptor.createImage());
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//			}
////			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
////					ICON_FOLDER+name);
////			imageDescriptors.put(name, imageDescriptor);
////			images.put(name, imageDescriptor.createImage());
//		}
	}

	/**
	 * Method used to get the path of image
	 * @param name   The name of the image
	 * @return       The path of the image
	 */
	public static String getImagePath(String name){
		return ICON_FOLDER+name;
	}
	
	/**
	 * Method used to get the image
	 * @param name   The name of image
	 * @return       The Image object
	 */
	public static Image getImage(String name) {
		if(!getRegistry().images.containsKey(name)){
			createImage(name);
		}
		return getRegistry().images.get(name);
	}

	/**
	 * Method used to get the imageDescriptor
	 * @param name   The name of image
	 * @return       The ImageDescriptor object
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		if(!getRegistry().imageDescriptors.containsKey(name)){
			createImage(name);
		}
		return getRegistry().imageDescriptors.get(name);
	}

	private static void createImage(String name) {
		ImageDescriptor ImgDes = null;
		ImgDes = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,getImagePath(name));
		if(ImgDes!=null){
			getRegistry().imageDescriptors.put(name, ImgDes);
			getRegistry().images.put(name, ImgDes.createImage());
		}
	}
}
