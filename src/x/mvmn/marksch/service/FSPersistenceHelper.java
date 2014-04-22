package x.mvmn.marksch.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import x.mvmn.marksch.model.AnglesTableModel;
import x.mvmn.marksch.model.AngleDefinition;
import x.mvmn.marksch.model.AngleDefinitionReadOnly;

public class FSPersistenceHelper {

	public static void saveTableModel(final AnglesTableModel tableModel, final File file) throws Exception {
		StringBuilder content = new StringBuilder();
		synchronized (tableModel) {
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				AngleDefinitionReadOnly point = tableModel.getValue(i);
				content.append(point.getDegrees()).append(" ");
				content.append(point.getMinutes()).append(" ");
				content.append(point.getSeconds()).append("\n");
			}
		}
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		try {
			writer.write(content.toString());
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	public static AnglesTableModel loadTableModel(final File file) throws Exception {
		AnglesTableModel result = new AnglesTableModel();

		if (file.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file), 256);
				String line = reader.readLine();
				while (line != null) {
					String splits[] = line.trim().split("[ ]+");
					if (splits.length > 2) {
						result.add(new AngleDefinition(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2])));
					}
					line = reader.readLine();
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
					}
				}
			}

		}

		return result;
	}
}
