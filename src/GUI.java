
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private GUI gui;

	public static void main(String[] args) {
		new GUI();
	}

	private JLabel inputURLLabel, inputTitleLabel, inputPathLabel, progressLabel, inputTypeLabel;
	private JTextField inputURL, inputTitle, inputPath, inputType;
	private JButton startButton, cancelButton;
	private JTextArea progressTextArea;
	private Font standardFont;
	private JRadioButton oneFileRadioButton;

	public GUI() {
		gui = this;
		setTitle("WebNovelEctractor");
		standardFont = new Font(Font.MONOSPACED, Font.PLAIN, 15);
		setResizable(false);
		setBounds(300, 300, 470 + 16, 600 - 17);
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void initComponents() {
		setLayout(null);

		inputTitleLabel = new JLabel("Enter title     ->");
		inputTitleLabel.setFont(standardFont);
		inputTitleLabel.setBounds(50, 50, 200, 20);
		add(inputTitleLabel);

		inputTitle = new JTextField();
		inputTitle.setFont(standardFont);
		inputTitle.setBounds(inputTitleLabel.getX() + 170, inputTitleLabel.getY(), 200, 23);
		add(inputTitle);

		inputURLLabel = new JLabel("Enter URL       ->");
		inputURLLabel.setFont(standardFont);
		inputURLLabel.setBounds(inputTitleLabel.getX(), inputTitleLabel.getY() + 50, 200, 20);
		add(inputURLLabel);

		inputURL = new JTextField();
		inputURL.setFont(standardFont);
		inputURL.setBounds(inputURLLabel.getX() + 170, inputURLLabel.getY(), 200, 23);
		add(inputURL);

		inputPathLabel = new JLabel("Enter save path ->");
		inputPathLabel.setFont(standardFont);
		inputPathLabel.setBounds(inputURLLabel.getX(), inputURLLabel.getY() + 50, 200, 20);
		add(inputPathLabel);

		inputPath = new JTextField();
		inputPath.setFont(standardFont);
		inputPath.setBounds(inputPathLabel.getX() + 170, inputPathLabel.getY(), 200, 23);
		add(inputPath);

		inputTypeLabel = new JLabel("Enter extension ->");
		inputTypeLabel.setFont(standardFont);
		inputTypeLabel.setBounds(inputPathLabel.getX(), inputPathLabel.getY() + 50, 200, 20);
		add(inputTypeLabel);

		inputType = new JTextField();
		inputType.setFont(standardFont);
		inputType.setBounds(inputTypeLabel.getX() + 170, inputTypeLabel.getY(), 200, 23);
		add(inputType);

		startButton = new JButton("Start");
		startButton.setBounds(inputTypeLabel.getX(), inputTypeLabel.getY() + 50, 100, 20);
		startButton.setFocusable(false);
		add(startButton);

		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (inputURL.getText().length() != 0 && inputTitle.getText().length() != 0
						&& inputPath.getText().length() != 0) {
					ReaderThread reader = new ReaderThread(gui, inputURL.getText(), inputTitle.getText(),
							inputPath.getText(), inputType.getText(), oneFileRadioButton.isSelected());
					reader.start();
					return;
				}
				addProgressTextAreaText("Error: Please input a URL, a title and a path!");
			}
		});

		oneFileRadioButton = new JRadioButton("Single file");
		oneFileRadioButton.setFont(standardFont);
		oneFileRadioButton.setBounds(startButton.getX() + 125, startButton.getY(), 130, 20);
		oneFileRadioButton.setFocusable(false);
		add(oneFileRadioButton);

		cancelButton = new JButton("Cancel");
		cancelButton.setBounds(startButton.getX() + 270, startButton.getY(), 100, 20);
		cancelButton.setFocusable(false);
		add(cancelButton);

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		progressLabel = new JLabel("Progress:");
		progressLabel.setFont(standardFont);
		progressLabel.setBounds(startButton.getX(), startButton.getY() + 50, 200, 20);
		add(progressLabel);

		progressTextArea = new JTextArea();
		progressTextArea.setFont(standardFont);
		progressTextArea.setBounds(progressLabel.getX(), progressLabel.getY() + 30, 370, 150);
		progressTextArea.setLineWrap(true);
		progressTextArea.setEnabled(false);
		add(progressTextArea);
	}

	public synchronized void addProgressTextAreaText(String text) {
		progressTextArea.setText("- " + text + "\n" + progressTextArea.getText());
	}

}
