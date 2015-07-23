package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FindEncodingException {
	private static List<String> fileNameList = new ArrayList<String>();
	/**  
     *  用getBytes(encoding)：返回字符串的一个byte数组  
     *  当b[0]为  63时，应该是转码错误  
     *  A、不乱码的汉字字符串：  
     *  1、encoding用GB2312时，每byte是负数；  
     *  2、encoding用ISO8859_1时，b[i]全是63。  
     *  B、乱码的汉字字符串：  
     *  1、encoding用ISO8859_1时，每byte也是负数；  
     *  2、encoding用GB2312时，b[i]大部分是63。  
     *  C、英文字符串  
     *  1、encoding用ISO8859_1和GB2312时，每byte都大于0；  
     *  <p/>  
     *  总结：给定一个字符串，用getBytes("iso8859_1")  
     *  1、如果b[i]有63，不用转码；  A-2  
     *  2、如果b[i]全大于0，那么为英文字符串，不用转码；  B-1  
     *  3、如果b[i]有小于0的，那么已经乱码，要转码。  C-1  
     */ 
	public static void readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				byte  b[];  
				try {  
					b = tempString.getBytes("UTF-8");  
					for (int  i  =  0;  i  <  b.length;  i++) {  
						byte  b1  =  b[i];  
						if  (b1  ==  63) {
							break;    //1
						} else if (b1  >  0) {
							continue;//2  
						} else if (b1 < 0) {        //不可能为0，0为字符串结束符  
							//retStr  =  new  String(b,  "GB2312");
							System.out.println(fileName + "----line " + line + ": " + tempString);
							break;  
						}  
					}  
               } catch(UnsupportedEncodingException  e)  {  
            	   e.printStackTrace();
               }  
               line++;
           }
           reader.close();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           if (reader != null) {
               try {
                   reader.close();
               } catch (IOException e1) {
            	   e1.printStackTrace();
               }
           }
       }
   }

   public static List<String> readfile(String filepath) throws FileNotFoundException, IOException {
	   try {
           File file = new File(filepath);
           if (!file.isDirectory()) {
//               System.out.println("文件");
//               System.out.println("path=" + file.getPath());
//               System.out.println("absolutepath=" + file.getAbsolutePath());
//               System.out.println("name=" + file.getName());
               fileNameList.add(file.getAbsolutePath());
           } else if (file.isDirectory()) {
//        	   System.out.println("文件夹");
			   String[] filelist = file.list();
			   for (int i = 0; i < filelist.length; i++) {
				   File readfile = new File(filepath + "\\" + filelist[i]);
				   if (!readfile.isDirectory()) {
	//			   		System.out.println("path=" + readfile.getPath());
	//			   		System.out.println("absolutepath=" + readfile.getAbsolutePath());
	//			   		System.out.println("name=" + readfile.getName());
				        fileNameList.add(readfile.getAbsolutePath());
				   } else if (readfile.isDirectory()) {
			           readfile(filepath + "\\" + filelist[i]);
		           }
			   }
           }
       } catch (FileNotFoundException e) {
       		System.out.println("readfile()   Exception:" + e.getMessage());
       }
       return fileNameList;
	}

	public static void main(String[] args) {
		String filepath = "F:\\workspace_gzy\\OnceCenter64\\OnceVM\\src";
		try {
			List<String> fileNameList = readfile(filepath);
			for(int i=0;i<fileNameList.size();i++){
				readFileByLines(fileNameList.get(i));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
