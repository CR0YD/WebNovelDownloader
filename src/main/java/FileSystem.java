package main.java;

import java.io.File;
import java.util.Arrays;

public class FileSystem {

	public enum AdjustmentType {
		PREFIX, SUFFIX
	};

	public static File createDirectory(String path) throws Exception {
		File directory = new File(path);

		if (!directory.exists()) {
			directory.mkdir();
		}

		return directory;
	}

	public static void adjustFileNameLength(File directory, String adjustment, AdjustmentType adjustmentType) {
		if (!directory.isDirectory()) {
			return;
		}

		int maxFileNameLength = Arrays.stream(directory.listFiles()).mapToInt(file -> {
			return (int) file.getName().split("\\.")[0].length();
		}).max().getAsInt();

		if (adjustmentType == AdjustmentType.PREFIX) {
			Arrays.stream(directory.listFiles()).forEach(file -> {
				file.renameTo(new File(directory.getAbsolutePath() + "\\"
						+ adjustment.repeat(
								maxFileNameLength - file.getName().substring(0, file.getName().indexOf(".")).length())
						+ file.getName()));
			});
			return;
		}
		if (adjustmentType == AdjustmentType.SUFFIX) {
			Arrays.stream(directory.listFiles()).forEach(file -> {
				file.renameTo(new File(directory.getAbsolutePath() + "\\"
						+ adjustment.repeat(
								maxFileNameLength - file.getName().substring(0, file.getName().indexOf(".")).length())
						+ file.getName()));
			});
			return;
		}
		throw new IllegalArgumentException();
	}
}
