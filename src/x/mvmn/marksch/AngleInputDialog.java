package x.mvmn.marksch;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class AngleInputDialog extends JDialog {
	private static final long serialVersionUID = 3074502064523624574L;

	private final JComboBox comboDegrees = new JComboBox();
	private final JComboBox comboMinutes = new JComboBox();
	private final JComboBox comboSeconds = new JComboBox();

	private final JButton btnOk = new JButton("Ok");
	private final JButton btnCancel = new JButton("Cancel");

	private void init(final AngleInputCallback okCallback, final AngleInputCallback cancelCallback) {
		for (int i = 0; i < 360; i++) {
			comboDegrees.addItem(i);
		}
		for (int i = 0; i < 60; i++) {
			comboMinutes.addItem(i);
			comboSeconds.addItem(i);
		}
		this.getContentPane().setLayout(new BorderLayout());
		{
			JPanel panel = new JPanel();
			panel.add(comboDegrees);
			panel.add(comboMinutes);
			panel.add(comboSeconds);
			this.getContentPane().add(panel, BorderLayout.CENTER);
		}
		{
			JPanel panel = new JPanel();
			panel.add(btnOk);
			panel.add(btnCancel);
			this.getContentPane().add(panel, BorderLayout.SOUTH);
		}

		ActionListener actListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actPerformed) {
				AngleInputDialog.this.setVisible(false);
				AngleInputCallback callback = null;
				final boolean cancelled;
				if (actPerformed.getSource() == btnCancel) {
					callback = cancelCallback;
					cancelled = true;
				} else if (actPerformed.getSource() == btnOk) {
					callback = okCallback;
					cancelled = false;
				} else {
					cancelled = true;
				}
				if (callback != null) {
					new Thread() {
						public void run() {
							cancelCallback.callback((Integer) comboDegrees.getSelectedItem(), (Integer) comboMinutes.getSelectedItem(),
									(Integer) comboSeconds.getSelectedItem(), cancelled);
						}
					}.start();
				}
			}
		};
		btnCancel.addActionListener(actListener);
		btnOk.addActionListener(actListener);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setModal(true);
		this.pack();
	}

	public static interface AngleInputCallback {
		public void callback(int degrees, int minutes, int seconds, boolean cancelled);
	}

	public AngleInputDialog(final AngleInputCallback okCallback, final AngleInputCallback cancelCallback) {
		super();
		init(okCallback, cancelCallback);
	}

	public AngleInputDialog(final Frame frame, final AngleInputCallback okCallback, final AngleInputCallback cancelCallback) {
		super(frame);
		init(okCallback, cancelCallback);
	}
}
