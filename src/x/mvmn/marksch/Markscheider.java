package x.mvmn.marksch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import x.mvmn.marksch.AngleInputDialog.AngleInputCallback;
import x.mvmn.marksch.model.AnglesTableModel;
import x.mvmn.marksch.model.AngleDefinition;
import x.mvmn.marksch.model.AngleDefinitionReadOnly;
import x.mvmn.marksch.model.AnglesTableModel.SumChangeListener;
import x.mvmn.marksch.service.FSPersistenceHelper;

public class Markscheider implements AngleInputCallback, SumChangeListener {

	private final JTable mainTable;
	private AnglesTableModel tableModel;
	private final JFrame mainFrame;
	private final JButton btnAdd;
	private final JButton btnRemove;
	private final JButton btnEqualize;

	private final AngleInputDialog angleInputDialog;

	// private double sum = 0;

	private final JLabel sumLabel = new JLabel("Сумма = 0");

	private final JMenuBar mainMenu;
	private final JMenu menuFile;
	private final JMenuItem menuItemLoad;
	private final JMenuItem menuItemSave;

	public Markscheider() {
		mainMenu = new JMenuBar();
		menuFile = new JMenu("Файл");
		mainMenu.add(menuFile);
		menuItemLoad = new JMenuItem("Загрузить...");
		menuItemSave = new JMenuItem("Сохранить...");
		menuFile.add(menuItemLoad);
		menuFile.add(menuItemSave);

		menuItemLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvt) {
				final JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.setMultiSelectionEnabled(false);
				if (jfc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
					new Thread() {
						public void run() {
							try {
								AnglesTableModel tableModel = FSPersistenceHelper.loadTableModel(jfc.getSelectedFile());
								tableModel.setSumChangeListener(Markscheider.this);
								Markscheider.this.tableModel = tableModel;
								Markscheider.this.mainTable.setModel(tableModel);
								Markscheider.this.updateSum();
							} catch (final Exception e) {
								e.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										JOptionPane.showMessageDialog(mainFrame, "Error occurred: " + e.getClass().getName() + " " + e.getMessage());
									}
								});
							}
						}
					}.start();
				}
			}
		});
		menuItemSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvt) {
				final JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.setMultiSelectionEnabled(false);
				if (jfc.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
					new Thread() {
						public void run() {
							try {
								FSPersistenceHelper.saveTableModel(tableModel, jfc.getSelectedFile());
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										JOptionPane.showMessageDialog(mainFrame, "Save complete: " + jfc.getSelectedFile().getAbsolutePath());
									}
								});
							} catch (final Exception e) {
								e.printStackTrace();
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										JOptionPane.showMessageDialog(mainFrame, "Error occurred: " + e.getClass().getName() + " " + e.getMessage());
									}
								});
							}
						}
					}.start();
				}
			}
		});

		mainFrame = new JFrame("Angl");
		mainFrame.setJMenuBar(mainMenu);
		tableModel = new AnglesTableModel();
		tableModel.setSumChangeListener(this);
		mainTable = new JTable(tableModel);
		angleInputDialog = new AngleInputDialog(mainFrame, this, this);
		Container contentPane = mainFrame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new JScrollPane(mainTable), BorderLayout.CENTER);
		btnAdd = new JButton("+");
		btnRemove = new JButton("-");
		btnEqualize = new JButton("=");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(btnAdd, BorderLayout.WEST);
		panel.add(btnEqualize, BorderLayout.CENTER);
		panel.add(btnRemove, BorderLayout.EAST);
		contentPane.add(panel, BorderLayout.NORTH);
		contentPane.add(sumLabel, BorderLayout.SOUTH);
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				angleInputDialog.setVisible(true);
			}
		});
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				int[] rows = mainTable.getSelectedRows();
				Arrays.sort(rows);
				for (int i = rows.length - 1; i >= 0; i--) {
					tableModel.delete(rows[i]);
				}
				updateSum();
			}
		});

		btnEqualize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized (tableModel) {
					int rowCount = Markscheider.this.tableModel.getRowCount();
					if (rowCount > 0) {
						double totalDelta = Markscheider.this.tableModel.getSum();
						if (totalDelta > Math.PI * 2) {
							totalDelta -= Math.PI * 2;
						}
						if ((int) (totalDelta * 100000) > 0) {
							double delta = totalDelta / ((double) rowCount);
							for (int i = 0; i < rowCount; i++) {
								AngleDefinitionReadOnly point = Markscheider.this.tableModel.getValue(i);
								Markscheider.this.tableModel.setValue(i, new AngleDefinition(point.getRadians() - delta));
							}
						}
					}
				}
				updateSum();
			}
		});
		//
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	public static void main(String args[]) {
		new Markscheider();
		// PointDefinition point = new PointDefinition();
		// point.setAngle(45, 15, 15);
		// System.out.println(point);
		// point.setRadians(point.getRadians());
		// System.out.println(point);
	}

	@Override
	public void callback(int degrees, int minutes, int seconds, boolean cancelled) {
		angleInputDialog.setVisible(false);
		if (!cancelled) {
			AngleDefinition newPoint = new AngleDefinition(degrees, minutes, seconds);
			tableModel.add(newPoint);
			updateSum();
		}
	}

	public void sumChanged(final double newSum) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sumLabel.setText("Сумма = " + new AngleDefinition(newSum).toString());
			}
		});
	}

	protected void updateSum() {
		// sumLabel.setText("Sum = " + new AngleDefinition(tableModel.getSum()).toString());
		sumChanged(tableModel.getSum());
	}
}
