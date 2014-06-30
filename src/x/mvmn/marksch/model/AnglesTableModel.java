package x.mvmn.marksch.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import x.mvmn.marksch.model.AngleDefinitionReadOnly;

public class AnglesTableModel extends AbstractTableModel {

	public interface SumChangeListener {
		public void sumChanged(double newSum);
	}

	private static final long serialVersionUID = 8671339960785999368L;
	private List<AngleDefinition> data = new ArrayList<AngleDefinition>();

	private static final String[] COLUMN_NAMES = { "#", "º", "'", "\"", "Радиан" };

	private SumChangeListener sumChangeListener;

	private double sum = 0;

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col > 0;
	}

	@Override
	public Object getValueAt(int row, int col) {
		final Object result;
		switch (col) {
			case 1:
				result = data.get(row).getDegrees();
			break;
			case 2:
				result = data.get(row).getMinutes();
			break;
			case 3:
				result = data.get(row).getSeconds();
			break;
			case 4:
				result = String.format("%.8f", data.get(row).getRadians());
			break;
			default:
				result = row + 1;
		}
		return result;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (value != null) {
			String str = value.toString();
			Double val = null;
			try {
				val = Double.parseDouble(str.trim());
			} catch (NumberFormatException e) {
			}
			if (val != null) {
				AngleDefinition pDef = data.get(row);
				this.sum -= pDef.getRadians();
				switch (col) {
					case 1:
						pDef.setAngle((int) val.doubleValue(), pDef.getMinutes(), pDef.getSeconds());
						fireTableCellUpdated(row, col);
					break;
					case 2:
						pDef.setAngle(pDef.getDegrees(), (int) val.doubleValue(), pDef.getSeconds());
						fireTableCellUpdated(row, col);
					break;
					case 3:
						pDef.setAngle(pDef.getDegrees(), pDef.getMinutes(), (int) val.doubleValue());
						fireTableCellUpdated(row, col);
					break;
					case 4:
						pDef.setRadians(val);
						fireTableCellUpdated(row, col);
					break;
				}
				this.sum += pDef.getRadians();
				notifySumChange();
			}
		}
	}

	public synchronized void add(AngleDefinition pointDefinition) {
		data.add(pointDefinition);
		this.sum += pointDefinition.getRadians();
		notifySumChange();
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
	}

	public synchronized void delete(int rowIndex) {
		if (rowIndex < data.size()) {
			this.sum -= data.remove(rowIndex).getRadians();
			notifySumChange();
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
	}

	public AngleDefinitionReadOnly getValue(int row) {
		return data.get(row);
	}

	public void setValue(int row, AngleDefinition value) {
		this.sum -= data.set(row, value).getRadians();
		this.sum += value.getRadians();
		notifySumChange();
		this.fireTableRowsUpdated(row, row);
	}

	public double getSum() {
		return sum;
	}

	protected void notifySumChange() {
		if (sumChangeListener != null) {
			sumChangeListener.sumChanged(sum);
		}
	}

	public SumChangeListener getSumChangeListener() {
		return sumChangeListener;
	}

	public void setSumChangeListener(SumChangeListener sumChangeListener) {
		this.sumChangeListener = sumChangeListener;
	}
}
