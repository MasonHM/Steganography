import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class EncodingGUI {
	private static final int DEFAULT_DECOY_BITS_PER_COLOR = 7;
	private static final int inputLimit = 1;
	private static final int ACTUAL_SIZE = 0;
	private static final int STRETCH = 1;
	private static final int SCALE = 2;
	private static final int TILE = 3;

	private JFrame frame;
	private JSpinner spinner;
	private ButtonGroup radioGroup;
	private JTextField alphaDecoyInput = new JTextField(1);
	private JTextField redDecoyInput = new JTextField(1);
	private JTextField greenDecoyInput = new JTextField(1);
	private JTextField blueDecoyInput = new JTextField(1);
	private JLabel decoySize, hiddenSize, resultSize;
	private JPanel picturePanel, decoyPanel, hiddenPanel, resultPanel;
	private PictureFrame decoyPic, hiddenPic, resultPic;
	private String[] hiddenPictureOptions = { "Actual size", "Stretch to fill",
			"Scale to fill", "Tile to fill" };
	private boolean decoyInitialized = false;
	private boolean hiddenInitialized = false;

	public EncodingGUI() {
		frame = new JFrame("Steganography Encoder");
		frame.setMinimumSize(new Dimension(1100, 500));

		initInputs();

		JPanel sliderPanel = new JPanel();
		JLabel label = new JLabel("Bits per color in decoy:");
		sliderPanel.add(label, BorderLayout.NORTH);
		sliderPanel.add(makeLevelPanel(), BorderLayout.WEST);
		sliderPanel.add(makeButtonPane(), BorderLayout.EAST);

		frame.setJMenuBar(makeMenuBar());
		frame.add(makePicturePanel(), BorderLayout.CENTER);
		frame.add(sliderPanel, BorderLayout.SOUTH);
		KeyboardFocusManager manager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());

		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private JPanel makeButtonPane() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

		JButton button = new JButton();
		button.setText("Hide Picture");
		button.addActionListener(new HidePictureActionListener());
		buttonPane.add(Box.createRigidArea(new Dimension(30, 0)));
		buttonPane.add(button);

		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel,
				BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("Hidden Picture:");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		radioButtonPanel.add(label);
		
		JPanel radioDividedPanel = new JPanel();
		radioDividedPanel.setLayout(new BoxLayout(radioDividedPanel,
				BoxLayout.LINE_AXIS));
		JPanel radioLeftPanel = new JPanel();
		radioLeftPanel.setLayout(new BoxLayout(radioLeftPanel,
				BoxLayout.PAGE_AXIS));
		JPanel radioRightPanel = new JPanel();
		radioRightPanel.setLayout(new BoxLayout(radioRightPanel,
				BoxLayout.PAGE_AXIS));
		radioGroup = new ButtonGroup();
		JRadioButton[] radioBtns = new JRadioButton[hiddenPictureOptions.length];
		for (int i = 0; i < radioBtns.length; i++) {
			radioBtns[i] = new JRadioButton(hiddenPictureOptions[i]);
			radioBtns[i].setActionCommand(hiddenPictureOptions[i]);
			radioGroup.add(radioBtns[i]);
			if (i < radioBtns.length/2) {
				radioLeftPanel.add(radioBtns[i]);
			} else {
				radioRightPanel.add(radioBtns[i]);
			}
		}
		radioDividedPanel.add(radioLeftPanel);
		radioDividedPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		radioDividedPanel.add(radioRightPanel);
		radioButtonPanel.add(radioDividedPanel);
		radioBtns[0].setSelected(true);
		buttonPane.add(Box.createRigidArea(new Dimension(50, 0)));
		buttonPane.add(radioButtonPanel);

		return buttonPane;
	}

	private Component makeLevelPanel() {
		JPanel levelInput = new JPanel();
		levelInput.setLayout(new GridLayout(2, 9));
		levelInput.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		spinner = new JSpinner(new SpinnerNumberModel(
				DEFAULT_DECOY_BITS_PER_COLOR, 0, 8, 1));
		if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
			JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
					.getEditor();
			editor.getTextField().setEnabled(true);
			editor.getTextField().setEditable(false);
		}
		spinner.addChangeListener(new MyChangeListener());
		Component[] comps = spinner.getEditor().getComponents();
		for (Component component : comps) {
			component.setFocusable(false);
		}

		levelInput.add(new JLabel("All:"));
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(new JLabel("Alpha:"));
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(new JLabel("Red:"));
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(new JLabel("Green:"));
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(new JLabel("Blue:"));
		levelInput.add(spinner);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(alphaDecoyInput);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(redDecoyInput);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(greenDecoyInput);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(blueDecoyInput);
		return levelInput;
	}

	private JPanel makePicturePanel() {
		picturePanel = new JPanel();
		picturePanel
				.setLayout(new BoxLayout(picturePanel, BoxLayout.LINE_AXIS));
		picturePanel.addHierarchyBoundsListener(new FrameBoundsListener());

		decoyPic = new PictureFrame();
		decoyPanel = new JPanel(new BorderLayout());
		decoyPanel.setPreferredSize(new Dimension(frame.getSize().width / 3,
				frame.getSize().height / 3));
		decoyPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		decoyPanel.add(new JLabel("Decoy Picture: ", JLabel.CENTER),
				BorderLayout.NORTH);
		decoyPanel.add(decoyPic, BorderLayout.CENTER);
		decoySize = new JLabel("", JLabel.CENTER);
		decoyPanel.add(decoySize, BorderLayout.SOUTH);

		hiddenPic = new PictureFrame();
		hiddenPanel = new JPanel(new BorderLayout());
		hiddenPanel.setPreferredSize(new Dimension(frame.getSize().width / 3,
				frame.getSize().height / 3));
		hiddenPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		hiddenPanel.add(new JLabel("Hidden Picture: ", JLabel.CENTER),
				BorderLayout.NORTH);
		hiddenPanel.add(hiddenPic, BorderLayout.CENTER);
		hiddenSize = new JLabel("", JLabel.CENTER);
		hiddenPanel.add(hiddenSize, BorderLayout.SOUTH);

		resultPic = new PictureFrame();
		resultPanel = new JPanel(new BorderLayout());
		resultPanel.setPreferredSize(new Dimension(frame.getSize().width / 3,
				frame.getSize().height / 3));
		resultPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		resultPanel.add(new JLabel("Result Picture: ", JLabel.CENTER),
				BorderLayout.NORTH);
		resultPanel.add(resultPic, BorderLayout.CENTER);
		resultSize = new JLabel("", JLabel.CENTER);
		resultPanel.add(resultSize, BorderLayout.SOUTH);

		picturePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		picturePanel.add(decoyPanel);
		picturePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		picturePanel.add(hiddenPanel);
		picturePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		picturePanel.add(resultPanel);
		picturePanel.add(Box.createRigidArea(new Dimension(10, 0)));

		return picturePanel;
	}

	private void setDecoyPicture(String filename) {
		decoyPic.setImage(filename);
	}

	private void setHiddenPicture(String filename) {
		hiddenPic.setImage(filename);
	}

	private JMenuBar makeMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("File Menu");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Select Decoy Image...",
				KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.META_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open an image");
		menuItem.addActionListener(new MyDecoyFileListener());
		menu.add(menuItem);

		menuItem = new JMenuItem("Select Hidden Image...", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.META_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open an image");
		menuItem.addActionListener(new MyHiddenFileListener());
		menu.add(menuItem);

		menuItem = new JMenuItem("Save Result Image...", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.META_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Save your image");
		menuItem.addActionListener(new MySaveFileListener());
		menu.add(menuItem);
		return menuBar;
	}

	private void refreshDecoyPic() {
		decoyPic.resetImage();
		decoyPic.modifyImage(PictureUtils.lowerPictureQuality(
				decoyPic.getImage(),
				Integer.parseInt(alphaDecoyInput.getText()),
				Integer.parseInt(redDecoyInput.getText()),
				Integer.parseInt(greenDecoyInput.getText()),
				Integer.parseInt(blueDecoyInput.getText())));
		frame.pack();
		frame.repaint();
	}

	private void refreshHiddenPic() {
		hiddenPic.resetImage();
		hiddenPic.modifyImage(PictureUtils.lowerPictureQuality(
				hiddenPic.getImage(),
				8 - Integer.parseInt(alphaDecoyInput.getText()),
				8 - Integer.parseInt(redDecoyInput.getText()),
				8 - Integer.parseInt(greenDecoyInput.getText()),
				8 - Integer.parseInt(blueDecoyInput.getText())));
		frame.pack();
		frame.repaint();
	}

	public class FrameBoundsListener implements HierarchyBoundsListener {
		@Override
		public void ancestorMoved(HierarchyEvent e) {
		}

		@Override
		public void ancestorResized(HierarchyEvent e) {
			decoyPic.setPreferredSize(decoyPanel.getWidth());
			hiddenPic.setPreferredSize(hiddenPanel.getWidth());
			resultPic.setPreferredSize(resultPanel.getWidth());
		}
	}

	public class HidePictureActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.print("Hiding picture in decoy... ");
			String hiddenPictureOption = radioGroup.getSelection()
					.getActionCommand();
			int hiddenPictureOp = ACTUAL_SIZE;
			if (hiddenPictureOption.equals(hiddenPictureOptions[STRETCH])) {
				hiddenPictureOp = STRETCH;
			} else if (hiddenPictureOption.equals(hiddenPictureOptions[SCALE])) {
				hiddenPictureOp = SCALE;
			} else if (hiddenPictureOption.equals(hiddenPictureOptions[TILE])) {
				hiddenPictureOp = TILE;
			}
			BufferedImage resultImage = PictureUtils
					.hidePictureInDecoyPicture(decoyPic.getImage(),
							hiddenPic.getImage(),
							Integer.parseInt(alphaDecoyInput.getText()),
							Integer.parseInt(redDecoyInput.getText()),
							Integer.parseInt(greenDecoyInput.getText()),
							Integer.parseInt(blueDecoyInput.getText()),
							hiddenPictureOp);
			resultPic.setImage(resultImage);
			if (resultPic.getImage().getHeight() < resultPic.getImage()
					.getWidth()) {
				resultPic.setPreferredSize(resultPanel.getWidth());
			} else {
				resultPic.setPreferredSize(resultPanel.getHeight()
						- resultSize.getHeight() * 2);
			}

			frame.repaint();
			System.out.println("Done.");
		}
	}

	public class MyChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent ce) {
			Integer value = (Integer) spinner.getValue();
			alphaDecoyInput.setText(Integer.toString(value));
			redDecoyInput.setText(Integer.toString(value));
			greenDecoyInput.setText(Integer.toString(value));
			blueDecoyInput.setText(Integer.toString(value));
			if (decoyInitialized) {
				refreshDecoyPic();
			}
			if (hiddenInitialized) {
				refreshHiddenPic();
			}
		}
	}

	public class MyDecoyFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Images", "jpg", "jpeg", "gif", "png");
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(new File("./images/"));
			int returnVal = chooser.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Decoy Picture: "
						+ chooser.getSelectedFile().getAbsolutePath());
				setDecoyPicture(chooser.getSelectedFile().getAbsolutePath());
				decoySize.setText(decoyPic.getImage().getWidth() + " x "
						+ decoyPic.getImage().getHeight());
				resultSize.setText(decoyPic.getImage().getWidth() + " x "
						+ decoyPic.getImage().getHeight());
				if (decoyPic.getImage().getHeight() < decoyPic.getImage()
						.getWidth()) {
					decoyPic.setPreferredSize(decoyPanel.getWidth());
				} else {
					decoyPic.setPreferredSize(decoyPanel.getHeight()
							- decoySize.getHeight() * 2);
				}
				decoyInitialized = true;
				refreshDecoyPic();
			}
		}
	}

	public class MyHiddenFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Images", "jpg", "jpeg", "gif", "png");
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(new File("./images/"));
			int returnVal = chooser.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Hidden Picture: "
						+ chooser.getSelectedFile().getAbsolutePath());
				setHiddenPicture(chooser.getSelectedFile().getAbsolutePath());
				hiddenSize.setText(hiddenPic.getImage().getWidth() + " x "
						+ hiddenPic.getImage().getHeight());
				if (hiddenPic.getImage().getHeight() < hiddenPic.getImage()
						.getWidth()) {
					hiddenPic.setPreferredSize(hiddenPanel.getWidth());
				} else {
					hiddenPic.setPreferredSize(hiddenPanel.getHeight()
							- hiddenSize.getHeight() * 2);
				}
				hiddenInitialized = true;
				refreshHiddenPic();
			}
		}
	}

	private class MySaveFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("./images/"));
			int returnVal = chooser.showSaveDialog(frame);
			String filename = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				filename = chooser.getSelectedFile().getName();
				if (!filename.endsWith(".png"))
					filename += ".png";
				System.out.println("You named your file: " + filename);
				try {
					File outputfile = new File("images/" + filename);
					ImageIO.write(resultPic.getImage(), "png", outputfile);
				} catch (IOException e) {
				}
			}
		}
	}

	private void initInputs() {
		alphaDecoyInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		alphaDecoyInput.setText(Integer.toString(DEFAULT_DECOY_BITS_PER_COLOR));
		redDecoyInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		redDecoyInput.setText(Integer.toString(DEFAULT_DECOY_BITS_PER_COLOR));
		greenDecoyInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		greenDecoyInput.setText(Integer.toString(DEFAULT_DECOY_BITS_PER_COLOR));
		blueDecoyInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		blueDecoyInput.setText(Integer.toString(DEFAULT_DECOY_BITS_PER_COLOR));
	}

	private class MyDispatcher implements KeyEventDispatcher {
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
					decoyPic.setImage(PictureUtils.invertImage(decoyPic
							.getImage()));
					frame.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
					hiddenPic.setImage(PictureUtils.invertImage(hiddenPic
							.getImage()));
					frame.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.dispose();
				} else if (e.getKeyCode() == KeyEvent.VK_P) {
					// PictureUtils.printPixelInfo(decoyPic.getImage());
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (spinner.getModel().getNextValue() != null) {
						spinner.setValue((Integer) spinner.getValue() + 1);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (spinner.getModel().getPreviousValue() != null) {
						spinner.setValue((Integer) spinner.getValue() - 1);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (decoyInitialized) {
						refreshDecoyPic();
					}
					if (hiddenInitialized) {
						refreshHiddenPic();
					}
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
			} else if (e.getID() == KeyEvent.KEY_TYPED) {
			}
			return false;
		}
	}
}
