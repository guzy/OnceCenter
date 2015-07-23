package oncecenter.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Vector;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * This class mainly aims at manipulating the SSH protocal. SSH protocal is the
 * basis of the communication bettween console and agent. Console deploys the
 * agent tool, configuration and script file to the agent through SSH. In
 * addition, agent start its RMI server by the shell command through SSH.
 */
public class Ssh {

	private static final long TIME_OUT = 5000;
	/**
	 * The host name of the agent, in form of IP address
	 */
	private String hostname;
	/**
	 * The user name of the agent
	 */
	private String username;
	/**
	 * The password of the agent, corresponds to the username
	 */
	private String password;
	/**
	 * SSH connection bettween the console and agent
	 */
	private Connection conn;
	/**
	 * The session bettween the console and agent
	 */
	private Session sess;
	
	private Integer exitCode = -1;
	
	public Integer getExitCode() {
		return exitCode;
	}

	private String charset = Charset.defaultCharset().toString(); 

	/**
	 * The constructor
	 * 
	 * @param hostname
	 *            The name of agent's IP address.
	 * @param username
	 *            The name of agent's username.
	 * @param password
	 *            The password of the agent.
	 */
	public Ssh(String hostname, String username, String password) {
		this.hostname = hostname;
		this.username = username;
		this.password = password;
	}

	/**
	 * This method is used to connect the agent.
	 * 
	 * @return whether the console has been connected to the agent.
	 */
	public boolean Connect() {
		boolean isAuthenticated = false;
		conn = new Connection(hostname);
		try {
			conn.connect();
			isAuthenticated = conn.authenticateWithPassword(username, password);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isAuthenticated;
	}

	/**
	 * This method is used to make shell command to agent by console.
	 * 
	 * @param commandLine
	 *            The content of shell command.
	 * @throws IOException
	 */
	public String Command(String commandLine) throws Exception {
		sess = conn.openSession();
		sess.execCommand(commandLine);
        InputStream stdOut = new StreamGobbler(sess.getStdout());
        String outStr = processStream(stdOut, charset); 
        sess.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT); 
//		InputStream is = sess.getStdout();
//		int i = -1;
//		byte[] b = new byte[1024];
//		StringBuffer sb = new StringBuffer();
//		while ((i = is.read(b)) != -1) {
//			sb.append(new String(b, 0, i));
//		}
//		String content = sb.toString();
//		System.out.println(content);
        InputStream stdout = new StreamGobbler(sess.getStdout());  
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));  
        while (true)  
        {  
            String line = br.readLine();  
            if (line == null)  
                break;  
            System.out.println(line);  
        }  
        exitCode = sess.getExitStatus();
//        System.out.println(exitCode);
        
		stdOut.close();
		if(exitCode != 0) {
			throw new Exception();
		}
		sess.close();
		return outStr;
	}
	
    /** *//**
     * @param in
     * @param charset
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private String processStream(InputStream in, String charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }
        return sb.toString();
    } 

	/**
	 * This method is used to copy file from local to remote directory
	 * 
	 * @param localFile
	 *            The path of local file
	 * @param remoteDir
	 *            The path of remote directory
	 */
	public void ScpFile(String localFile, String remoteDir) {
		SCPClient sc = new SCPClient(conn);
//		TransferThread transferThread = new TransferThread(sc, localFile, remoteDir);
//		transferThread.run();
//		long transThreadId = transferThread.getId();
//		return transThreadId;
		try {
			sc.put(localFile, remoteDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class TransferThread extends Thread {
		private SCPClient sc;
		private String localFile;
		private String remoteDir;

		public TransferThread(SCPClient sc, String localFile, String remoteDir) {
			super();
			this.sc = sc;
			this.localFile = localFile;
			this.remoteDir = remoteDir;
		}

		public void run() {
			try {
				sc.put(localFile, remoteDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean rmFile(String toDelFileName) {
		boolean rmSuccess = true;
		try {
			SFTPv3Client sftpClient = new SFTPv3Client(conn);
			sftpClient.rm(toDelFileName);
		} catch (IOException e) {
			rmSuccess = false;
			e.printStackTrace();
		} 
		return rmSuccess;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<SFTPv3DirectoryEntry> getFileNameInDir(String dirName) {
		Vector<SFTPv3DirectoryEntry> fileNameInDir = new Vector<SFTPv3DirectoryEntry>();
		try {
			SFTPv3Client sftpClient = new SFTPv3Client(conn);
			fileNameInDir = sftpClient.ls(dirName);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return fileNameInDir;
	}

	/**
	 * Method used to colose SSH connection
	 */
	public void CloseSsh() {
		conn.close();
	}

	/**
	 * Method to display the SSH information
	 */
	public String toString() {
		return hostname + "/" + username + "/" + password;
	}

	/**
	 * Console uses this method to get remote file to local file system. It's
	 * mainly used for collecting the final testing result file.
	 * 
	 * @param remoteFile
	 *            The path of remote file
	 * @param localDir
	 *            The path of local directory
	 */
	public void GetFile(String remoteFile, String localDir) {
		SCPClient sc = new SCPClient(conn);
		try {
			sc.get(remoteFile, localDir);
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}

	
	public boolean MakeDir(String dir){
		try {		
			this.Command("test -d "+dir+" || mkdir "+dir);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}
}
