package com.upendra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.dejim.SecurePropertiesWrapper;

public class ChangeEnvironment {

	public ChangeEnvironment() {
	}

	
	public static final String ENCRYPTION_ACTION = "encrypt";
	public static final String DECRYPTION_ACTION = "decrypt";

	private static final String YAML_FILE = ".yaml";
	private static final String PROPERTIES_FILE_SEPARATOR = "=";
	private static final String YAML_SEPARATOR = ":";
//	private static final String PROPERTIES_FILE = ".properties";

	private static final Pattern encryptPattern = Pattern.compile("!\\[(.*)\\]");
	
	private static String app_home = "";

	private static SecurePropertiesWrapper spw = new SecurePropertiesWrapper();	
	private static ChangeEnvironment ce = new ChangeEnvironment();
	
	private static Process process;
	private static ExecutorService executor = Executors.newFixedThreadPool(10); 
	
	public List<String> oplist = Collections.synchronizedList(new ArrayList<String>());
	
	
	//Functions Start
	
	
	
	
	// This function will be used to migrate one environment into another another
	// environment
	public static void processFile(String algorithm, String mode, String oldKey, String newKey, String inputFilePath, String outputFilePath) throws IOException {
		StringBuilder errorsFound = new StringBuilder();
		String line;
		
//		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath, new String[0]),
//				new java.nio.file.OpenOption[0]);
		app_home = new File(inputFilePath).getParent();
		System.out.println("Started Processing the input file");
		try {
			List<String> inputLines = Files.readAllLines(Paths.get(inputFilePath));
//			for (String line : Files.readAllLines(Paths.get(inputFilePath, new String[0]))) {
			for (int i=0;i<inputLines.size();i++) {
				line = inputLines.get(i);
			try {
					if (encryptPattern.matcher(line).find() && !line.trim().startsWith("#")) { // Do the process only if line has encrypted value
						if (inputFilePath.endsWith(YAML_FILE)) {
							executor.execute(new ProcessLine(spw, ce, process, app_home, algorithm, mode, line, i, oldKey, newKey, YAML_SEPARATOR, true));
//							processFileLine(line,algorithm, mode, oldKey, newKey, YAML_SEPARATOR,true);
						} else {
							executor.execute(new ProcessLine(spw, ce, process, app_home, algorithm, mode, line, i, oldKey, newKey, PROPERTIES_FILE_SEPARATOR, false));
//							processFileLine(line,algorithm, mode, oldKey, newKey,PROPERTIES_FILE_SEPARATOR, false);
						}
						ce.oplist.add("");
					} else {
						ce.oplist.add(line);
//						writer.write(line);
					}
									
//					writer.write("\n");
//				} 
//				catch (MuleEncryptionException e) {
//					System.out.println(e.getMessage());
//					errorsFound.append(e.getCause().getMessage()).append(System.lineSeparator());
				} catch (Exception e) {
					System.out.println(e.getMessage());
					errorsFound.append(e.getMessage()).append(System.lineSeparator());
				}
			}
			System.out.println(ce.oplist.size());
			for (String l: ce.oplist)
				System.out.println("before " + l);
			
			executor.shutdown();
			while (!executor.isTerminated()) {}
			for (String l: ce.oplist)
				System.out.println("After " + l);
			
			Files.write(Paths.get(outputFilePath), ce.oplist);
//			if (writer != null)
//				writer.close();
			System.out.println("Ended Processing the input file");


		} catch (Throwable throwable) {
//			if (writer != null)
//				try {
//					writer.close();
//				} catch (Throwable throwable1) {
//					throwable.addSuppressed(throwable1);
//				}
			throw throwable;
		}
		if (errorsFound.length() != 0)
			throw new IOException(errorsFound.toString());
	}
	


//	private static String decryptAndEncryptValue(String algorithm, String mode, String oldKey, String newKey, String value) throws MuleEncryptionException {
//		try {
//			Matcher m = encryptPattern.matcher(value);
//			if (m.find()) {
////				String decryptedValue = SecurePropertiesTool.applyOverString(DECRYPTION_ACTION, algorithm, mode, oldKey,useRandomIVs, value.substring(2, value.length() - 1));
////				String ret = SecurePropertiesTool.applyOverString(ENCRYPTION_ACTION, algorithm, mode, newKey,useRandomIVs, decryptedValue);
//				String decryptedValue = secureString(app_home, DECRYPTION_ACTION, algorithm, mode, oldKey, value.substring(2, value.length() - 1).replace("%", "%%"));
//						
//						
//						//(DECRYPTION_ACTION, algorithm, mode, oldKey,useRandomIVs, value.substring(2, value.length() - 1));
//				String ret = spw.secureString(app_home, ENCRYPTION_ACTION, algorithm, mode, newKey, decryptedValue.replace("%", "%%")); 
//						
//						//SecurePropertiesTool.applyOverString(ENCRYPTION_ACTION, algorithm, mode, newKey,useRandomIVs, decryptedValue);
//				return "![" + ret + "]";
//			}
//		} catch (Exception e) {
//			throw new MuleEncryptionException(e.getMessage(), e);
//		}
//		return value;
//	}

}


class MuleEncryptionException extends Exception {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	public MuleEncryptionException(String message) {
		super(message);
	}

	public MuleEncryptionException(String message, Exception cause) {
		super(message, cause);
	}
}