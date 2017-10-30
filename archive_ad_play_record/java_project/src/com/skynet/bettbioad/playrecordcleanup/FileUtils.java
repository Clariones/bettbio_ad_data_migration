package com.skynet.bettbioad.playrecordcleanup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileUtils {

	public static PrintStream createFileForWrite(File output) throws Exception {
		File folder = output.getParentFile();
		if (!folder.exists()){
			folder.mkdirs();
		}
		if (!output.exists()){
			output.createNewFile();
		}
		PrintStream ps = new PrintStream(new FileOutputStream(output));
		return ps;
	}

}
