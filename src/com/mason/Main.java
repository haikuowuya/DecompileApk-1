package com.mason;
import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.beust.jcommander.JCommander;
import com.mason.library.FileUtil;
import com.mason.library.MyRuntime;
import com.mason.library.StringUtil;
import com.mason.library.ZipUtil;
/**
 * 混淆：
 * !shrink
 * !warning
 * !optimize
 * !com.mason.**
 * keep attribute annotation
 * @author liumeng
 */
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String projectDir = System.getProperty("user.dir");
		String mylibsDir = projectDir+"/mylibs";
		/*******************************************/
//		String apkPath = "/Users/liumeng/AndroidStudioProjects/eclipse_workspace2/BatchPackApk/output/eleme-eleme4_3_1.apk";
//		String outPath = projectDir+"/output";
//		boolean isLog = false;
		MyJCommander jct = new MyJCommander(args);
		String apkPath = jct.apkpath;
		String outPath = jct.output;
    	boolean isLog = jct.debug;
    	
		outPath = outPath+"/decompile"+System.currentTimeMillis();
		System.out.println("********************************************************");
		System.out.println("Processing...");
		/*******************************************/
		try {
			FileUtil.copyFile(new File(apkPath), new File(outPath+"/tem.apk"));
			MyRuntime myRuntime = MyRuntime.getMyRuntime();
			myRuntime.isLog = isLog;
			myRuntime.changeDir(mylibsDir+"/");
			//反编译res中的全部xml文件
			//myRuntime.exec("java -jar apktool_2.0.0.3.jar d -f tem.apk -o apk");
			//把apktool反编译的smali转成dex
			//myRuntime.exec("java -jar smali-2.0.3.jar apk/smali -o apk/classes.dex");
			
			//apk直接转成jar
			if(MyRuntime.isWindowsOS()){
				myRuntime.exec("dex2jar-0.0.9.15/d2j-dex2jar.bat -f -o "+outPath+"/classes_dex2jar.jar "+outPath+"/tem.apk");
			}else{
				myRuntime.exec("dex2jar-0.0.9.15/d2j-dex2jar.sh -f -o "+outPath+"/classes_dex2jar.jar "+outPath+"/tem.apk");
			}
			//检测签名
			String signerLog = myRuntime.exec("jarsigner -verify -verbose -certs "+outPath+"/tem.apk");
			StringUtil.string2File(signerLog, outPath+"/signlog.txt");
			//反编译AndroidManifest，apktool反编译的不靠谱
			ZipUtil.UnZip(new File(outPath+"/tem.apk"), outPath+"/apkunzip");
			String trueAndroidManifest = myRuntime.exec("java -jar AXMLPrinter2.jar "+outPath+"/apkunzip/AndroidManifest.xml");
			FileUtil.delete(new File(outPath+"/apkunzip/AndroidManifest.xml"));
			StringUtil.string2File(trueAndroidManifest, outPath+"/apkunzip/AndroidManifest.xml");
			//查看结果
			Result.handleResult(new String[]{outPath+"/apkunzip/AndroidManifest.xml",outPath+"/signlog.txt"});
			//调用jd-ui
			if(MyRuntime.isWindowsOS()){
				myRuntime.exec("jd-ui.exe "+outPath+"/classes_dex2jar.jar");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
		System.out.println("********************************************************");
	}
}