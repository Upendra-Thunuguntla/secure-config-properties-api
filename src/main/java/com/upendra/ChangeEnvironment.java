package com.upendra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
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
	private static final char COMMENTS = '#';
	private static final char VALUE_DELIMITER = '"';
	private static final char VALUE_ESCAPED_CHARACTER = '\\';
	private static final String VALUE_ESCAPED_DOUBLE_QUOTE = "\\\"";
	private static final Pattern encryptPattern = Pattern.compile("!\\[(.*)\\]");
	
	private static String app_home = "";

	private static SecurePropertiesWrapper spw = new SecurePropertiesWrapper();	
	
	
	private static String removeComment(String line) {
		StringBuilder result = new StringBuilder();
		boolean opened = false;
		char previous = Character.MIN_VALUE;
		for (char c : line.toCharArray()) {
			if (c == COMMENTS && !opened)
				return result.toString();
			result.append(c);
			if (c == VALUE_DELIMITER && previous != VALUE_ESCAPED_CHARACTER) {
				if (opened)
					return result.toString();
				opened = true;
			}
			previous = c;
		}
		return result.toString();
	}

	private static String getComment(String line) {
		String result = "";
		boolean commentStarted = false;
		boolean valueStarted = false;
		for (char c : line.toCharArray()) {
			if (c == COMMENTS && !valueStarted)
				commentStarted = true;
			if (c == VALUE_DELIMITER)
				valueStarted = !valueStarted;
			if (commentStarted)
				result = result + c;
		}
		return result;
	}

	private static String decryptAndEncryptValue(String algorithm, String mode, String oldKey, String newKey,
			boolean useRandomIVs, String value) throws MuleEncryptionException {
		try {
			Matcher m = encryptPattern.matcher(value);
			if (m.find()) {
//				String decryptedValue = SecurePropertiesTool.applyOverString(DECRYPTION_ACTION, algorithm, mode, oldKey,useRandomIVs, value.substring(2, value.length() - 1));
//				String ret = SecurePropertiesTool.applyOverString(ENCRYPTION_ACTION, algorithm, mode, newKey,useRandomIVs, decryptedValue);
				String decryptedValue = spw.secureString(app_home, DECRYPTION_ACTION, algorithm, mode, oldKey, value.substring(2, value.length() - 1));
						
						
						//(DECRYPTION_ACTION, algorithm, mode, oldKey,useRandomIVs, value.substring(2, value.length() - 1));
				String ret = spw.secureString(app_home, ENCRYPTION_ACTION, algorithm, mode, newKey, decryptedValue); 
						
						//SecurePropertiesTool.applyOverString(ENCRYPTION_ACTION, algorithm, mode, newKey,useRandomIVs, decryptedValue);
				return "![" + ret + "]";
			}
		} catch (Exception e) {
			throw new MuleEncryptionException(e.getMessage(), e);
		}
		return value;
	}

	private static void processFileLine(String line, BufferedWriter writer, String algorithm, String mode,
			String oldKey, String newKey, boolean useRandomIVs, String separator, boolean space)
			throws IOException, MuleEncryptionException {
		String comments = getComment(line);
		line = removeComment(line);
//		if (!line.contains(separator)) {
//			writer.write(comments);
//			return;
//		}
		String lineKey = line.split(separator)[0];
		writer.write(lineKey + separator);
		String value = line.substring(lineKey.length() + 1).trim();
		if (value.length() > 0) {
			if (space)
				writer.write(" ");
			(new String[1])[0] = value;
			String[] splitted = value.contains(VALUE_ESCAPED_DOUBLE_QUOTE) ? new String[1] : value.split("\"");
			if (splitted.length == 0) {
				writer.write("\"" + decryptAndEncryptValue(algorithm, mode, oldKey, newKey, useRandomIVs, "") + '"');
			} else if (splitted.length == 1) {
				writer.write(decryptAndEncryptValue(algorithm, mode, oldKey, newKey, useRandomIVs, value));
			} else {
				writer.write("\"" + decryptAndEncryptValue(algorithm, mode, oldKey, newKey, useRandomIVs, splitted[1])
						+ '"');
			}
		}
		if (comments.length() > 0)
			writer.write(" " + comments);
	}

	// This function will be used to migrate one environment into another another
	// environment
	public static void processFile(String algorithm, String mode, String oldKey, String newKey, boolean useRandomIVs,
			String inputFilePath, String outputFilePath) throws IOException {
		StringBuilder errorsFound = new StringBuilder();
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath, new String[0]),
				new java.nio.file.OpenOption[0]);
		app_home = new File(inputFilePath).getParent();
	//	System.out.println(app_home);
		try {
			for (String line : Files.readAllLines(Paths.get(inputFilePath, new String[0]))) {
				try {
					if (encryptPattern.matcher(line).find()) { // Do the process only if line has encrypted value
						if (inputFilePath.endsWith(YAML_FILE)) {
							processFileLine(line, writer, algorithm, mode, oldKey, newKey, useRandomIVs, YAML_SEPARATOR,
									true);
						} else {
							processFileLine(line, writer, algorithm, mode, oldKey, newKey, useRandomIVs,
									PROPERTIES_FILE_SEPARATOR, false);
						}
					} else {
						writer.write(line);
					}
					writer.write("\n");
				} catch (MuleEncryptionException e) {
					System.out.println(e.getMessage());
					errorsFound.append(e.getCause().getMessage()).append(System.lineSeparator());
				} catch (Exception e) {
					System.out.println(e.getMessage());
					errorsFound.append(e.getMessage()).append(System.lineSeparator());
				}
			}
			if (writer != null)
				writer.close();
		} catch (Throwable throwable) {
			if (writer != null)
				try {
					writer.close();
				} catch (Throwable throwable1) {
					throwable.addSuppressed(throwable1);
				}
			throw throwable;
		}
		if (errorsFound.length() != 0)
			throw new IOException(errorsFound.toString());
	}

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
