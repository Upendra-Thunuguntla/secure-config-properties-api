package com.dejim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.upendra.ChangeEnv;

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
//				System.out.println(line);
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
//		System.out.println(toolString);
		try {

			process = Runtime.getRuntime().exec(String.format(toolString));
//			System.out.println("Started Process");
			process.waitFor();
//			System.out.println("Waiting for Process");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				line = line.replace(JDK_ERROR, "");
				System.out.println(line);
				response.append(line);
			}
//			System.out.println(response.toString());
			while ((line = error.readLine()) != null) {
				line = line.replace(JDK_ERROR, "");
				System.out.println(line);
				if (!response.toString().contains(line)) {
					response.append(line);
				}
			}
//			System.out.println(response.toString());
		} catch (IOException | InterruptedException  e1) {
			System.out.println(e1.getMessage());
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
//		System.out.println(toolString);
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

	public String changeEnvforFile(String fullsecure, String algorithm, String mode, String oldKey, String newKey, String inputFilePath, String outputFilePath) {
		try {
			if (Boolean.parseBoolean(fullsecure)) {
				StringBuilder errorsFound = new StringBuilder();
				File inputFileObj = new File(inputFilePath);
				File outputFileObj = new File(outputFilePath);
				String app_home = inputFileObj.getParent();
				String decryError = secureFile(app_home, "decrypt", algorithm, mode, oldKey, inputFilePath , "dec_" + inputFileObj.getName());
				if (decryError.startsWith("Invalid"))
					return decryError + " in Decryption";
				
				String encryError = secureFile(app_home, "encrypt" , algorithm, mode, newKey, app_home + File.separatorChar + "dec_" + inputFileObj.getName(), outputFileObj.getName() );
				if (encryError.startsWith("Invalid"))
					return encryError + " in Encryption";

//				errorsFound.append("Decryption - " +  );
//				errorsFound.append("Encryption - " + );			
				return errorsFound.toString();
			}else {
				ChangeEnv ce = new ChangeEnv();
				return ce.processFile(algorithm, mode, oldKey, newKey, inputFilePath, outputFilePath);
			}
		} catch (Exception e) {
			return (e.getMessage());
		}
//		return "";
	}

}
