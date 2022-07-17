package com.upendra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.dejim.SecurePropertiesWrapper;

public class ChangeEnv {
	private String app_home = "";

	public final String ENCRYPTION_ACTION = "encrypt";
	public final String DECRYPTION_ACTION = "decrypt";

	private final Pattern encryptPattern = Pattern.compile("!\\[(.*)\\]");

	private final SecurePropertiesWrapper spw = new SecurePropertiesWrapper();

	public String processFile(String algorithm, String mode, String oldKey, String newKey, String inputFilePath,
			String outputFilePath) throws IOException {
		StringBuilder errorsFound = new StringBuilder();
		String line;
		List<String> opfileLines = new ArrayList<>();
		List<Integer> opfileIndexes = new ArrayList<Integer>();
		List<String> tempFileInpLines = new ArrayList<>();
		List<String> tempFileOpLines = new ArrayList<>();
		File inputFileObj = new File(inputFilePath);
		app_home = inputFileObj.getParent();
		String tempFileName = "tmp_" + inputFileObj.getName();

		System.out.println("Started Processing the input file");

		try {
			List<String> inputLines = Files.readAllLines(inputFileObj.toPath());
//			for (String line : Files.readAllLines(Paths.get(inputFilePath, new String[0]))) {
			for (int i = 0; i < inputLines.size(); i++) {
				line = inputLines.get(i);

				if (encryptPattern.matcher(line).find() && !line.trim().startsWith("#")) { // Do the process only if
																							// line has encrypted value
					opfileLines.add("");
					opfileIndexes.add(i);
					tempFileInpLines.add(line);
				} else {
					opfileLines.add(line);
				}
			}

			Files.write(Paths.get(app_home + File.separatorChar + tempFileName), tempFileInpLines);

			String decryError = spw.secureFile(app_home, DECRYPTION_ACTION, algorithm, mode, oldKey,
					app_home + File.separatorChar + tempFileName, "dec_" + tempFileName);
			if (decryError.startsWith("Invalid"))
				return decryError + " in Decryption";
			
			String encryError = spw.secureFile(app_home, ENCRYPTION_ACTION, algorithm, mode, newKey,
					app_home + File.separatorChar + "dec_" + tempFileName, "enc_" + tempFileName);
			if (encryError.startsWith("Invalid"))
				return encryError + " in Encryption";
			
			
			File tempFileOpObj = new File(app_home + File.separatorChar + "enc_" + tempFileName);
			tempFileOpLines = Files.readAllLines(tempFileOpObj.toPath());
			for (int i = 0; i < tempFileOpLines.size(); i++) {
				opfileLines.set(opfileIndexes.get(i), tempFileOpLines.get(i));
			}

			Files.write(Paths.get(outputFilePath), opfileLines);
			System.out.println("Ended Processing the input file");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			errorsFound.append(e.getMessage()).append(System.lineSeparator());
		}

		return errorsFound.toString();
	}

}
