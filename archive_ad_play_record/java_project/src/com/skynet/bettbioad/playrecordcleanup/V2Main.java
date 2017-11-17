package com.skynet.bettbioad.playrecordcleanup;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class V2Main {

	static String usage = V2Main.class.getCanonicalName() + "　<config file>";

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println(usage);
			return;
		}
		String configFileName = args[0];//"./config/config.properties";
//		String configFileName = "./config/config.properties";
		Properties props = new Properties();
		InputStream cfgFileInputStream = new FileInputStream(configFileName);
		props.load(cfgFileInputStream);
		try {
			cfgFileInputStream.close();
		} catch (Exception e) {
			// ignore any exception when close
		}
		
		V2Cleaner cleaner = new V2Cleaner();
		try{
		cleaner.init(props);
		cleaner.doCleanup();
		}finally{
			cleaner.close();
		}
		
		
	}

}
