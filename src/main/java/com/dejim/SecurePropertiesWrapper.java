package com.dejim;

import java.io.*;

import com.upendra.ChangeEnvironment;

public class SecurePropertiesWrapper {

	public SecurePropertiesWrapper() {

	}

	public final String JAVA_CMD = "java -cp ";
	public final String JAR_CMD = "/secure-properties-tool.jar com.mulesoft.tools.SecurePropertiesTool ";
	public final String JDK_ERROR = "NOTE: Picked up JDK_JAVA_OPTIONS: --add-opens java.base/sun.net.www.protocol.file=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.desktop/java.beans=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED";

	public String secureString(String appHome, String operation, String algorithm, String mode, String key,
			String value) {
		StringBuffer response = new StringBuffer();
		String line;
		Process process;

		String toolString = JAVA_CMD + appHome + JAR_CMD + "string " + operation + " " + algorithm + " " + mode + " "
				+ key + " ";
//		System.out.print(toolString + "\n");
		try {

			value = value.replace("%", "%%");
			process = Runtime.getRuntime().exec(String.format(toolString + value));
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
//			System.out.println(response);
			while ((line = error.readLine()) != null) {
				response.append(line);
			}
//			System.out.println(response);
		} catch (IOException e1) {
		} catch (InterruptedException e2) {
		}
		return response.toString().replace(JDK_ERROR, "");
	}

	public String secureFile(String appHome, String operation, String algorithm, String mode, String key,
			String inputFileLocation, String outputFile) {
		StringBuffer response = new StringBuffer();
		String line;
		Process process;

		String toolString = JAVA_CMD + appHome + JAR_CMD + "file " + operation + " " + algorithm + " " + mode + " "
				+ key + " " + inputFileLocation + " " + appHome + "/" + outputFile;
		try {

			process = Runtime.getRuntime().exec(String.format(toolString));
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			while ((line = error.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException e1) {
		} catch (InterruptedException e2) {
		}
		return response.toString().replace(JDK_ERROR, "");
	}

	public String secureFileLevel(String appHome, String operation, String algorithm, String mode, String key,
			String inputFileLocation, String outputFile) {
		StringBuffer response = new StringBuffer();
		String line;
		Process process;

		String toolString = JAVA_CMD + appHome + JAR_CMD + "file-level " + operation + " " + algorithm + " " + mode
				+ " " + key + " " + inputFileLocation + " " + appHome + "/" + outputFile;
		try {

			process = Runtime.getRuntime().exec(String.format(toolString));
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			while ((line = error.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException e1) {
		} catch (InterruptedException e2) {
		}
		return response.toString().replace(JDK_ERROR, "");
	}

	public String changeEnvforFile(String algorithm, String mode, String oldKey, String newKey, String inputFilePath,
			String outputFilePath) {
		try {
			ChangeEnvironment.processFile(algorithm, mode, oldKey, newKey, false, inputFilePath, outputFilePath);
		} catch (Exception e) {
			return (e.getMessage());
		}
		return "";
	}

}
