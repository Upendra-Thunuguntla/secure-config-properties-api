package com.upendra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dejim.SecurePropertiesWrapper;

class ProcessLine implements Runnable{
	
	
	private static final char COMMENTS = '#';
	private static final char VALUE_DELIMITER = '"';
	private static final char VALUE_ESCAPED_CHARACTER = '\\';
	private static final String VALUE_ESCAPED_DOUBLE_QUOTE = "\\\"";
	
	SecurePropertiesWrapper spw;
	ChangeEnvironment ce;
	Process process;
	String appHome, algorithm, mode,line, oldKey,  newKey,  separator;
	 //algorithm,  mode, value
	
	boolean space;
	int index;



	public ProcessLine(SecurePropertiesWrapper spw, ChangeEnvironment ce, Process process, String appHome,
			String algorithm, String mode, String line, int index, String oldKey, String newKey, String separator,
			boolean space) {
		super();
		this.spw = spw;
		this.ce = ce;
		this.process = process;
		this.appHome = appHome;
		this.algorithm = algorithm;
		this.mode = mode;
		this.line = line;
		this.index = index;
		this.oldKey = oldKey;
		this.newKey = newKey;
		this.separator = separator;
		this.space = space;
	}


	public void run() {
//		secureString(appHome, algorithm, mode, key, value);
		processFileLine(line, algorithm, mode, oldKey, newKey, separator, space);
	}
	
	
	private String secureString(String appHome, String algorithm, String mode, String oldkey, String newKey, String value) {

		String convertedValue = "";
//		System.out.println(value);
//		System.out.println( value.substring(2, value.length() - 1));
		String encryToolString = spw.JAVA_CMD + appHome + spw.JAR_CMD + "string encrypt " + algorithm + " " + mode + " "
				+ newKey + " " ;
		
		String decryToolString = spw.JAVA_CMD + appHome + spw.JAR_CMD + "string decrypt " + algorithm + " " + mode + " "
				+ oldKey + " \"" + value.substring(2, value.length() - 1) + "\"";
//		System.out.print(toolString + "\n");

		convertedValue = "![" + doConversion(encryToolString + "\"" + doConversion(decryToolString) + "\"") + "]";
		
		return convertedValue;
	}
	
	public String doConversion(String commandString) {
		StringBuffer response = new StringBuffer();
		String line;
		System.out.println("Command given " + commandString);
		try {

//			value = value.replace("%", "%%");
			process = Runtime.getRuntime().exec(String.format(commandString));
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
//			System.out.println(response);
			while ((line = error.readLine()) != null) {
				if (!line.replace(spw.JDK_ERROR, "").equals("")) {
					response.append(line);
					System.out.println(line);
				}
			}
//			System.out.println(response);
		} catch (IOException e1) {
		} catch (InterruptedException e2) {
		}
		return response.toString().replace(spw.JDK_ERROR, "");

	}
	
	private void processFileLine(String line, String algorithm, String mode, String oldKey, String newKey, String separator, boolean space) {
		String comments = getComment(line);
		line = removeComment(line);
		String lineKey = line.split(separator)[0];
		String retLine = "";
//		writer.write
		retLine += (lineKey + separator);
		String value = line.substring(lineKey.length() + 1).trim();
		System.out.println("initial value : " + line);
		if (value.length() > 0) {
			if (space)
//				writer.write
				retLine += (" ");
//				retLine += secureString(appHome, algorithm, mode, oldKey, newKey, value);
				
			(new String[1])[0] = value;
			String[] splitted = value.contains(VALUE_ESCAPED_DOUBLE_QUOTE) ? new String[1] : value.split("\"");
			
			if (splitted.length == 0) {
//				writer.write
				retLine += ("\"" + secureString(appHome, algorithm, mode, oldKey, newKey, "") + '"');
			} else if (splitted.length == 1) {
//				writer.write
				retLine += (secureString(appHome,algorithm, mode, oldKey, newKey, value));
			} else {
//				writer.write
				retLine += ("\"" + secureString(appHome,algorithm, mode, oldKey, newKey,splitted[1])
						+ '"');
			}
				
		}
		if (comments.length() > 0)
			retLine += (" " + comments);
		System.out.println("Final value : " + retLine);
		ce.oplist.set(index,retLine);
	}
	
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
}
