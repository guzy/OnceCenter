package oncecenter.daemon;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

import oncecenter.Constants;
import oncecenter.action.pool.SwitchMasterAction;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
	
public class GetRecordTask extends TimerTask {
	
	private Display display=PlatformUI.getWorkbench().getDisplay();
	
	private VMTreeObjectRoot root;
	private VMTreeObjectPool rootPool;
	private VMTreeObjectHost rootHost;
	private Connection conn;
	private boolean isPool;
	
	Pool pool;
	Pool.Record poolRecord;
	
	//ArrayList<Host> hostList = new  ArrayList<Host>();
	ArrayList<Host.Record> hostRecordList = new ArrayList<Host.Record>();
	
	//ArrayList<VM> vmList = new  ArrayList<VM>();
	ArrayList<VM.Record> vmRecordList = new ArrayList<VM.Record>();
	
	//ArrayList<SR> srList = new  ArrayList<SR>();
	ArrayList<SR.Record> srRecordList = new ArrayList<SR.Record>();
	
	Map<Host,VMTreeObjectHost> hostMap;
	Map<VM,VMTreeObjectVM> vmMap;
//	Map<SR,VMTreeObjectSR> srMap;
	Map<VM,VMTreeObjectTemplate> templateMap;
	
	ArrayList<VMTreeObject> deleteObjects = new ArrayList<VMTreeObject>();
	
	public GetRecordTask(VMTreeObjectRoot root){
		this.root=root;
		this.conn=root.getConnection();
		this.hostMap = root.hostMap;
		this.vmMap = root.vmMap;
//		this.srMap = root.srMap;
		this.templateMap = root.templateMap;
		if(root instanceof VMTreeObjectPool){
			this.isPool=true;
			rootPool = (VMTreeObjectPool) root;
		}else{
			this.isPool=false;
			rootHost = (VMTreeObjectHost) root;
		}
	}
	public void clear(){
		pool = null;
		poolRecord = null;
		//hostList.clear();
		hostRecordList.clear();
		//vmList.clear();
		vmRecordList.clear();
		//srList.clear();
		srRecordList.clear();
		deleteObjects.clear();
	}
	
	public void getChildren(VMTreeObject root){
		for(VMTreeObject o :root.getChildrenList()){
			if(!o.getItemState().equals(ItemState.changing)){
				deleteObjects.add(o);
//				if(o instanceof VMTreeObjectSR)
//					System.out.println(this.root.getName()+" add delete sr:"+o.getName());
			}
			getChildren(o);
		}
	}
	
	public void getPoolHost(Host master,Host backup){
		for(Host.Record hostRecord:hostRecordList){
			boolean isMaster = false;
			boolean isBackup = false;
			Host host = Types.toHost(hostRecord.uuid);
			if(host.equals(master)){
				isMaster = true;
			}else if(host.equals(backup)){
				isBackup = true;
			}
			VMTreeObjectHost hostObject = hostMap.get(host);
			if(hostObject!=null){
				boolean isSuccess = hostObject.setRecord(hostRecord);
				if(isSuccess){
					hostObject.isMaster = isMaster;
					hostObject.isBackup = isBackup;
				}
				deleteObjects.remove(hostObject);
				//System.out.println("remove delete host:"+hostObject.getName());
			}else{
				hostObject = new VMTreeObjectHost(hostRecord.nameLabel,conn,host,hostRecord);
				hostObject.isMaster = isMaster;
				hostObject.isBackup = isBackup;
				root.addChild(hostObject);
				hostMap.put(host, hostObject);
			}
		}
	}
	
	public void getVM(boolean isPool){
		for(VM.Record vmRecord:vmRecordList){
			VM vm = Types.toVM(vmRecord.uuid);
			if(vmRecord.isATemplate){
				VMTreeObjectTemplate tempObject = templateMap.get(vm);
				VMTreeObjectVM vmObject = vmMap.get(vm);
				if(tempObject!=null){
					tempObject.setRecord(vmRecord);
					deleteObjects.remove(tempObject);
				}else if(vmObject==null){
					tempObject = new VMTreeObjectTemplate(vmRecord.nameLabel,conn,vm,vmRecord);
					root.addChild(tempObject);
					templateMap.put(vm, tempObject);
				}
			}else{
				final VMTreeObjectVM vmObject = vmMap.get(vm);
				VMTreeObjectTemplate tempObject = templateMap.get(vm);
				if(vmObject!=null){
					refreshVM(vmObject,vmRecord,isPool,root,display,hostMap);
					deleteObjects.remove(vmObject);
					//System.out.println("remove delete vm:"+vmObject.getName());
				}else if(tempObject==null){
					boolean isExist=false;
					for(VMTreeObject temporaryO : root.temporaryList){
						if((temporaryO.getName().equals(vmRecord.nameLabel))){
							isExist=true;
							break;
						}
					}
					if(!isExist){
						VMTreeObject hostObject = hostMap.get(vmRecord.residentOn);
						VMTreeObjectVM newVmObject = new VMTreeObjectVM(vmRecord.nameLabel,conn,vm,vmRecord);
						hostObject.addChild(newVmObject);
						vmMap.put(vm, newVmObject);
					}
				}
			}
			
		}
	}
	
	public static  void refreshVM(final VMTreeObjectVM vmObject,VM.Record vmRecord
			,boolean isPool,VMTreeObjectRoot root,Display display,Map<Host,VMTreeObjectHost> hostMap){
		VM.Record originalR = (VM.Record)vmObject.getRecord();
		if(!vmObject.getItemState().equals(ItemState.changing)){
			boolean isSuccess = vmObject.setRecord(vmRecord);
			if(isSuccess){
				if(isPool){
					if(!vmRecord.powerState.equals(Types.VmPowerState.RUNNING)){
						if(!(vmObject.getParent() instanceof VMTreeObjectPool)){
							vmObject.getParent().getChildrenList().remove(vmObject);
							root.addChild(vmObject);
						}
					}else if(vmRecord.powerState.equals(Types.VmPowerState.RUNNING)){
						if(!vmRecord.residentOn.equals(vmObject.getParent().getApiObject())){
							VMTreeObject hostObject = hostMap.get(vmRecord.residentOn);
							vmObject.getParent().getChildrenList().remove(vmObject);
							hostObject.addChild(vmObject);
						}
					}
				}
				
				if(!vmRecord.powerState.equals(Types.VmPowerState.RUNNING)
						&&originalR.powerState.equals(Types.VmPowerState.RUNNING)){
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	vmObject.shutVM();
					        }
						};
					    display.asyncExec(runnable); 
					}
				}else if(vmRecord.powerState.equals(Types.VmPowerState.RUNNING)
						&&!originalR.powerState.equals(Types.VmPowerState.RUNNING)){
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	vmObject.startVM();
					        }
						};
					    display.asyncExec(runnable); 
					}
				}
			}
		}
	}
	
	public static void refreshTree(Display display){
		if (!display.isDisposed()){
		    Runnable runnable = new Runnable(){
		        public void run( ){
		        	Constants.treeView.getViewer().expandAll();
		        	Constants.treeView.getViewer().refresh();
		        }
			};
		    display.asyncExec(runnable); 
		}
	}
	
	public void getSR(){
		for(SR.Record srRecord:srRecordList){
			if(TypeUtil.getAllSRTypes().contains(srRecord.type)){
				SR sr = Types.toSR(srRecord.uuid);
				VMTreeObjectSR srObject = root.srMap.get(sr);
				if(srObject!=null){
					deleteObjects.remove(srObject);
					boolean isSuccess = srObject.setRecord(srRecord);
					if(isSuccess){
						if(TypeUtil.localSrType.equals(srRecord.type)){
							if(srRecord.residentOn!=null
									&&!srRecord.residentOn.equals(srObject.getParent().getApiObject())){
								if(hostMap.get(srRecord.residentOn)!=null){
									srObject.getParent().getChildrenList().remove(srObject);
									hostMap.get(srRecord.residentOn).addChild(srObject);
								}							
							}
						}
					}
//					if(deleteObjects.remove(srObject)){
//						System.out.println("删除成功");
//					}else{
//						System.out.println("删除失败");
//					}
				}else{
					boolean isExist=false;
					for(VMTreeObject temporaryO : root.temporaryList){
						if((temporaryO.getName().equals(srRecord.nameLabel))){
							isExist=true;
							break;
						}
					}
					if(!isExist){
						srObject = new VMTreeObjectSR(srRecord.nameLabel,conn,sr,srRecord);
						root.addChild(srObject);
						root.srMap.put(sr, srObject);
					}
				}
			}
		}
	}
	public void run(){
			this.clear();
			try{
				pool = Constants.getPool(conn);
				//如果pool是空将如何？
				//Pool.Status status = Pool.getStatus(conn);
				hostRecordList.addAll(Host.getAllRecords(conn).values());
				vmRecordList.addAll(VM.getAllRecords(conn).values());
				srRecordList.addAll(SR.getAllRecords(conn).values());
			}catch(Exception e){
				e.printStackTrace();
				//处理高可用
				try {
					Connection c = new Connection("http://"+root.getIpAddress()+":9363",
							root.getUsername(),root.getPassword());
				} catch (Exception e1) {
					e1.printStackTrace();
					if(root instanceof VMTreeObjectPool){
						System.out.println("资源池连接失效");
						SwitchMasterAction action = new SwitchMasterAction((VMTreeObjectPool)root);
						action.run();
					}
				}
				return;
			}
			getChildren(root);
			if(isPool){
				if(pool!=null){ 
					try{
						poolRecord = pool.getRecord(conn);
					}catch(Exception e){
						e.printStackTrace();
					}
					rootPool.setRecord(poolRecord);
					getPoolHost(poolRecord.master,poolRecord.backup);
					getVM(isPool);
					getSR();
				}else{
					isPool = false;
					//待实现
				}
			}else{
				if(pool!=null){
					isPool = true;
					//待实现
				}else{
					if(hostRecordList!=null&&hostRecordList.size()>0)
						rootHost.setRecord(hostRecordList.get(0));
					getVM(isPool);
					getSR();
				}
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	long timestamp =0L;
//			        	if(hostRecordList.size()>0){
//			        		timestamp = hostRecordList.iterator().next().timestamp;
//			        	}
			        	for(VMTreeObject o:deleteObjects){
			        		if(o instanceof VMTreeObjectVM){
			        			VMTreeObjectVM vm = (VMTreeObjectVM)o;
			        			timestamp = vmRecordList.iterator().next().timestamp;
			        			if(vm.getRecord().timestamp>=timestamp)
			        				continue;
			        		}
			        		if(o instanceof VMTreeObjectTemplate){
			        			VMTreeObjectTemplate vm = (VMTreeObjectTemplate)o;
			        			timestamp = vmRecordList.iterator().next().timestamp;
			        			if(vm.getRecord().timestamp>=timestamp)
			        				continue;
			        		}
			        		if(o instanceof VMTreeObjectHost){
			        			VMTreeObjectHost vm = (VMTreeObjectHost)o;
			        			timestamp = hostRecordList.iterator().next().timestamp;
			        			if(vm.getRecord().timestamp>=timestamp)
			        				continue;
			        		}
			        		if(o instanceof VMTreeObjectSR){
			        			VMTreeObjectSR vm = (VMTreeObjectSR)o;
			        			timestamp = srRecordList.iterator().next().timestamp;
			        			if(vm.getRecord().timestamp>=timestamp)
			        				continue;
			        		}
			        		o.getParent().getChildrenList().remove(o);
			        		Constants.treeView.getViewer().remove(o);
			        		System.out.println(root.getName()+" remove object:"+o.getName());
			        	}
			        	Constants.treeView.getViewer().expandAll();
			        	Constants.treeView.getViewer().refresh();
			        }
				};
			    this.display.asyncExec(runnable); 
			}
			
			
	}
			
			
//			ArrayList<VMTreeObject> list=root.getChildrenList();
//			for(final VMTreeObject object:list){
//				try{
//					if(object.getItemState().equals(ItemState.able)&&object.getType().equals(Type.pool)){
//						Connection c = object.getConnection();
//						Host.getAll(c);
//				} catch (BadServerResponse e) {
//					e.printStackTrace();
//				} catch (XenAPIException e) {
//					e.printStackTrace();
//				} catch (XmlRpcException e) {
//					e.printStackTrace();
//					try {
//						Connection conn = new Connection("http://"+object.getIpAddress()+":9363",
//								object.getUsername(),object.getPassword());
//					} catch (BadServerResponse e1) {
//						e1.printStackTrace();
//					} catch (SessionAuthenticationFailed e1) {
//						e1.printStackTrace();
//					} catch (MalformedURLException e1) {
//						e1.printStackTrace();
//					} catch (XenAPIException e1) {
//						e1.printStackTrace();
//					} catch (XmlRpcException e1) {
//						e1.printStackTrace();
//						System.out.println("资源池链接失效");
//						SwitchMasterAction action = new SwitchMasterAction(object);
//						action.run();
//					}
//				} 
//			}
//			
//			for(VMTreeObject object:root.getChildrenList()){
//				if(object.getType().equals(Type.host)){
//						try { 
////				            Socket s = new Socket(object.getIpAddress(),8000);  
////				            
////				            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));  
////				            
////				            OutputStream out = s.getOutputStream();  
////				            
////				            StringBuffer sb = new StringBuffer("GET /15sec.xml HTTP/1.1\r\n");  
////				            
////				            out.write(sb.toString().getBytes());  
////				            
////				            String tmp;
////							URL url = new URL(" http://"+object.getIpAddress()+":15000/15sec.xml");
////							InputStream in = url.openStream();
////							BufferedReader bin = new BufferedReader(new InputStreamReader(in, "UTF-8"));
////							String s = null;
////				            File f = new File(Constant.workspace+"/"+object.getName()+"/15sec.xml");
////				            if(!f.exists()){
////				            	f.createNewFile();//不存在则创建
////				            }
////				            BufferedWriter output = new BufferedWriter(new FileWriter(f));
////				            while((s = bin.readLine())!=null){  
////				            	output.write(s);
////				            }
////				            
////				            output.close();
////				            bin.close();
////				           
////				            
////				            if(object.hostPerform==null){
////								object.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
////										,object.uuid);
////							}
////							object.hostPerform.getMetricsTimelines();
//							VMTreeParent parent = (VMTreeParent)object;
//							for(VMTreeObject o:parent.getChildren()){
//								if(o.getType().equals(Type.vm)){
//									VM vm = (VM)o.getApiObject();
//									if(o.uuid==null||o.uuid.length()==0){
//										o.uuid=vm.getUuid(o.getConnection());
//									}
//									o.powerState=vm.getPowerState(o.getConnection());
////									if(o.vmPerform==null){
////										o.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
////												,o.uuid);
////										o.vmPerform.getMetricsTimelines();
////									}
//									
//								}
//							}
//				              
//				        } catch (Exception e) {  
//				              
//				            e.printStackTrace();  
//				        }
//				}else if(object.getType().equals(Type.pool)){
//					VMTreeParent parent = (VMTreeParent)object;
//					for(VMTreeObject o:parent.getChildrenList()){
//						if(o.getType().equals(Type.host)){
//							try { 
////								URL url = new URL(" http://"+object.getIpAddress()+":15000/15sec.xml");
////								InputStream in = url.openStream();
////								BufferedReader bin = new BufferedReader(new InputStreamReader(in, "UTF-8"));
////								String s = null;
////					            File f = new File(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml");
////					            if(!f.exists()){
////					            	f.createNewFile();//不存在则创建
////					            }
////					            BufferedWriter output = new BufferedWriter(new FileWriter(f));
////					            while((s = bin.readLine())!=null){  
////					            	output.write(s);
////					            }
////					            
////					            output.close();
////					            bin.close(); 
////					            
////					            if(o.hostPerform==null){
////									o.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml"
////											,o.uuid);
////								}
////								o.hostPerform.getMetricsTimelines();
//								VMTreeParent p = (VMTreeParent)o;
//								for(VMTreeObject o1:p.getChildren()){
//									if(o1.getType().equals(Type.vm)){
//										if(o1.getItemState().equals(ItemState.able)){
//											VM vm = (VM)o1.getApiObject();
//											if(o1.uuid==null||o1.uuid.length()==0){
//												o1.uuid=vm.getUuid(o1.getConnection());
//											}
//											o.powerState=vm.getPowerState(o.getConnection());
//										}
//										
////										if(o1.vmPerform==null){
////											o1.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml"
////													,o1.uuid);
////											o1.vmPerform.getMetricsTimelines();
////										}
//										
//									}
//								}
//					              
//					        } catch (Exception e) {  
//					              
//					            e.printStackTrace();  
//					        }
//						}
//						
//					}
//				}
//			}
			
			
			

//			ArrayList<VMTreeObject> list=root.getChildrenList();
//			for(VMTreeObject object:list){
//				if(object.getType().equals(Type.host)){
//					try{
//						Host host=(Host)object.getApiObject();
//						if(object.getItemState().equals(ItemState.able)){
//							if(host.getEnabled(object.getConnection())){
//								ch.ethz.ssh2.Connection conn=new ch.ethz.ssh2.Connection(object.getIpAddress());
//								try {
//									conn.connect();
//									conn.authenticateWithPassword(object.getUsername(), object.getPassword());
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
//								HttpHost targetHost = new HttpHost(object.getIpAddress(), 80, "http");
//						        DefaultHttpClient httpclient = new DefaultHttpClient();
//						        
//						        httpclient.getCredentialsProvider().setCredentials(
//						                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//						                new UsernamePasswordCredentials(object.getUsername(), object.getPassword()));
//
//						        // Create AuthCache instance
//						        AuthCache authCache = new BasicAuthCache();
//						        // Generate BASIC scheme object and add it to the local
//						        // auth cache
//						        BasicScheme basicAuth = new BasicScheme();
//						        authCache.put(targetHost, basicAuth);
//
//						        // Add AuthCache to the execution context
//						        BasicHttpContext localcontext = new BasicHttpContext();
//						        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//						        HttpGet httpget = new HttpGet("/rrd_updates?host=true&start=" + (System.currentTimeMillis() / 1000 -400));
//						        //HttpGet httpget = new HttpGet("");
//						   
//						        
//						        HttpResponse response;
//								try {
//									response = httpclient.execute(targetHost, httpget, localcontext);				
//									HttpEntity entity = response.getEntity();
//									String rrdXportData = "";
//									if (entity != null) {	                	
//										String line = null;
//										rrdXportData = IOUtils.toString(new InputStreamReader(entity.getContent()));
//									}
//									
//									long startTime = 0;
//									int step = 0;
//									DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//									domFactory.setNamespaceAware(true);
//									DocumentBuilder builder = domFactory.newDocumentBuilder();
//									StringReader stringReader = new StringReader(rrdXportData);
//									InputSource inputSource = new InputSource(stringReader);
//									Document doc = builder.parse(inputSource);	         
//									save(doc,Constant.workspace+"/"+object.getName()+"/15sec.xml");
//									stringReader.close();
//									
//									
//									if(object.hostPerform==null){
//										object.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
//												,object.uuid);
//									}
//									object.hostPerform.getMetricsTimelines();
//									VMTreeParent parent = (VMTreeParent)object;
//									for(VMTreeObject o:parent.getChildren()){
//										if(o.getType().equals(Type.vm)){
//											VM vm = (VM)o.getApiObject();
//											if(o.uuid==null||o.uuid.length()==0){
//												o.uuid=vm.getUuid(o.getConnection());
//											}
//											if(o.vmPerform==null){
//												o.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
//														,o.uuid);
//												o.vmPerform.getMetricsTimelines();
//											}
//										}
//									}
//								}catch(Exception e){
//									e.printStackTrace();
//								}
////								SCPClient sc = new SCPClient(conn);
////								try {
////									sc.get(Constant.performpath, Constant.workspace+"/"+object.getName()+"/");
////									if(object.uuid==null||object.uuid.length()==0){
////										object.uuid=host.getUuid(object.getConnection());
////									}
////									if(object.hostPerform==null){
////										object.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
////												,object.uuid);
////									}
////									object.hostPerform.getMetricsTimelines();
////									VMTreeParent parent = (VMTreeParent)object;
////									for(VMTreeObject o:parent.getChildren()){
////										if(o.getType().equals(Type.vm)){
////											VM vm = (VM)o.getApiObject();
////											if(o.uuid==null||o.uuid.length()==0){
////												o.uuid=vm.getUuid(o.getConnection());
////											}
////											if(o.vmPerform==null){
////												o.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
////														,o.uuid);
////												o.vmPerform.getMetricsTimelines();
////											}
////										}
////									}
////								} catch (IOException e) {
////									e.printStackTrace();
////								}
////								conn.close();
//							}
//						}
//					}catch(Exception e){
//						e.printStackTrace();
//					}
//					
//				}
//				else if(object.getType().equals(Type.pool)){
//					try{
//						VMTreeParent parent=(VMTreeParent)object;
//						Pool pool=(Pool)object.getApiObject();
//						for(VMTreeObject o:parent.getChildren()){
//							if(o.getType().equals(Type.host)){
//								Host host=(Host)o.getApiObject();
//								ch.ethz.ssh2.Connection conn=new ch.ethz.ssh2.Connection(o.getIpAddress());
//								try {
//									conn.connect();
//									conn.authenticateWithPassword(object.getUsername(), object.getPassword());
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
//								HttpHost targetHost = new HttpHost(o.getIpAddress(), 80, "http");
//						        DefaultHttpClient httpclient = new DefaultHttpClient();
//						        
//						        httpclient.getCredentialsProvider().setCredentials(
//						                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//						                new UsernamePasswordCredentials(object.getUsername(), object.getPassword()));
//
//						        // Create AuthCache instance
//						        AuthCache authCache = new BasicAuthCache();
//						        // Generate BASIC scheme object and add it to the local
//						        // auth cache
//						        BasicScheme basicAuth = new BasicScheme();
//						        authCache.put(targetHost, basicAuth);
//
//						        // Add AuthCache to the execution context
//						        BasicHttpContext localcontext = new BasicHttpContext();
//						        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//						        HttpGet httpget = new HttpGet("/rrd_updates?host=true&start=" + (System.currentTimeMillis() / 1000 -400));
//						        //System.out.println("executing request: " + httpget.getRequestLine());
//						        //System.out.println("to target: " + targetHost);
//						        
//						        HttpResponse response;
//								try {
//									response = httpclient.execute(targetHost, httpget, localcontext);				
//									HttpEntity entity = response.getEntity();
//									String rrdXportData = "";
//									if (entity != null) {	                	
//										String line = null;
//										rrdXportData = IOUtils.toString(new InputStreamReader(entity.getContent()));
//									}
//									
//									long startTime = 0;
//									int step = 0;
//									DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//									domFactory.setNamespaceAware(true);
//									DocumentBuilder builder = domFactory.newDocumentBuilder();
//									StringReader stringReader = new StringReader(rrdXportData);
//									InputSource inputSource = new InputSource(stringReader);
//									Document doc = builder.parse(inputSource);	         
//									save(doc,Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml");
//									stringReader.close();
//									
//									
//									if(o.hostPerform==null){
//										o.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml"
//												,o.uuid);
//									}
//									o.hostPerform.getMetricsTimelines();
//									VMTreeParent p = (VMTreeParent)o;
//									for(VMTreeObject o1:p.getChildren()){
//										if(o1.getType().equals(Type.vm)){
//											VM vm = (VM)o1.getApiObject();
//											if(o1.uuid==null||o1.uuid.length()==0){
//												o1.uuid=vm.getUuid(o1.getConnection());
//											}
//											if(o1.vmPerform==null){
//												o1.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml"
//														,o1.uuid);
//												o1.vmPerform.getMetricsTimelines();
//											}
//										}
//									}
//									}catch(Exception e){
//										e.printStackTrace();
//									}
//							}
//
//							}
//						
//					}catch(Exception e){
//						e.printStackTrace();
//					}
//					
//				}
//			}
			
			
//			if(isStop)
//				break;
//			try {
//				sleep(15000);
//			} catch (InterruptedException e) {
//			
//				e.printStackTrace();
//			}
}
