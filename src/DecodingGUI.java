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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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

public class DecodingGUI {
	private static final int DEFAULT_HIDDEN_BITS_PER_COLOR = 2;

	private JFrame frame;
	private JPanel sourcePanel;
	private JPanel decodedPanel;
	private JSpinner spinner;
	private int inputLimit = 1; // from 0 to 8
	private JTextField alphaHiddenInput = new JTextField(1);
	private JTextField redHiddenInput = new JTextField(1);
	private JTextField greenHiddenInput = new JTextField(1);
	private JTextField blueHiddenInput = new JTextField(1);
	private PictureFrame sourcePic, decodedDecoy, decodedHidden;

	public DecodingGUI() {
		initInputs();

		frame = new JFrame("Steganography Decoder");
		frame.setMinimumSize(new Dimension(1200, 600));

		frame.setJMenuBar(makeMenuBar());
		frame.add(makeSourcePanel(), BorderLayout.WEST);
		frame.add(makeArrowPanel(), BorderLayout.CENTER);
		frame.add(makeDecodedPanel(), BorderLayout.EAST);
		KeyboardFocusManager manager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());

		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void setSourcePicture(String filename) {
		sourcePic.setImage(filename);
	}

	private JPanel makeArrowPanel() {
		JPanel arrowPanel = new JPanel(new BorderLayout());
		arrowPanel.setPreferredSize(new Dimension(100, 600));
		PictureFrame arrowPic = new PictureFrame("images/rightArrow.png");
		arrowPanel.add(arrowPic, BorderLayout.CENTER);
		return arrowPanel;
	}

	private JPanel makeSourcePanel() {
		sourcePanel = new JPanel();
		sourcePanel.setPreferredSize(new Dimension(600, 600));
		sourcePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		sourcePanel.setLayout(new BorderLayout());
		sourcePanel.addHierarchyBoundsListener(new FrameBoundsListener());

		sourcePic = new PictureFrame();
		JPanel sourcePicPanel = new JPanel(new BorderLayout());
		sourcePicPanel
				.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		sourcePicPanel.add(new JLabel("Source Picture:", JLabel.CENTER),
				BorderLayout.NORTH);
		sourcePicPanel.add(sourcePic, BorderLayout.CENTER);

		sourcePanel.add(sourcePicPanel, BorderLayout.CENTER);

		JPanel levelInput = new JPanel();
		levelInput.setLayout(new GridLayout(2, 9));
		levelInput.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		spinner = new JSpinner(new SpinnerNumberModel(DEFAULT_HIDDEN_BITS_PER_COLOR, 0, 8, 1));
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
		levelInput.add(alphaHiddenInput);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(redHiddenInput);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(greenHiddenInput);
		levelInput.add(Box.createRigidArea(new Dimension(1, 0)));
		levelInput.add(blueHiddenInput);

		JButton decodeButton = new JButton("Decode");
		decodeButton.addActionListener(new DecodeButtonActionListener());

		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.add(new JLabel("Hidden Bits:"), BorderLayout.WEST);
		inputPanel.add(levelInput, BorderLayout.CENTER);
		inputPanel.add(decodeButton, BorderLayout.EAST);
		sourcePanel.add(inputPanel, BorderLayout.SOUTH);

		return sourcePanel;
	}

	private void setDecodedDecoy(BufferedImage img) {
		decodedDecoy.setImage(img);
	}

	private void setDecodedHidden(BufferedImage img) {
		decodedHidden.setImage(img);
	}

	private JPanel makeDecodedPanel() {
		decodedPanel = new JPanel();
		decodedPanel.setPreferredSize(new Dimension(500, 600));
		decodedPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		decodedPanel.setLayout(new GridLayout(2, 0));
		decodedPanel.addHierarchyBoundsListener(new FrameBoundsListener());
		decodedDecoy = new PictureFrame();
		decodedHidden = new PictureFrame();

		JPanel decoyPanel = new JPanel(new BorderLayout());
		decoyPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 1, 2,
				Color.BLACK));
		decoyPanel.add(new JLabel("Decoy Picture:", JLabel.CENTER),
				BorderLayout.NORTH);
		decoyPanel.add(decodedDecoy, BorderLayout.CENTER);

		JPanel hiddenPanel = new JPanel(new BorderLayout());
		hiddenPanel.setBorder(BorderFactory.createMatteBorder(1, 2, 2, 2,
				Color.BLACK));
		hiddenPanel.add(new JLabel("Hidden Picture:", JLabel.CENTER),
				BorderLayout.NORTH);
		hiddenPanel.add(decodedHidden, BorderLayout.CENTER);

		decodedPanel.add(decoyPanel);
		decodedPanel.add(hiddenPanel);
		return decodedPanel;
	}

	private JMenuBar makeMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("File Menu");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Open Source Image...",
				KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.META_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open an image");
		menuItem.addActionListener(new MyDecoyFileListener());
		menu.add(menuItem);
		return menuBar;
	}

	public class DecodeButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			decodePic();
		}
	}

	private void decodePic() {
		int alphaDecoy = 8 - Integer.parseInt(alphaHiddenInput.getText());
		int redDecoy = 8 - Integer.parseInt(redHiddenInput.getText());
		int greenDecoy = 8 - Integer.parseInt(greenHiddenInput.getText());
		int blueDecoy = 8 - Integer.parseInt(blueHiddenInput.getText());
		BufferedImage[] imageArray = PictureUtils
				.decodePictureFromDecoyPicture(sourcePic.getImage(), alphaDecoy,
						redDecoy, greenDecoy, blueDecoy);
		setDecodedHidden(imageArray[0]);
		setDecodedDecoy(imageArray[1]);
		decodedHidden.setPreferredSize(Math.min(500,
				decodedPanel.getHeight() / 2));
		decodedDecoy.setPreferredSize(Math.min(500,
				decodedPanel.getHeight() / 2));
		frame.pack();
		frame.repaint();
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
				System.out.println("Source file: "
						+ chooser.getSelectedFile().getAbsolutePath());
			}
			setSourcePicture(chooser.getSelectedFile().getAbsolutePath());
			sourcePic.setPreferredSize(Math.min(500, sourcePanel.getWidth()));
			frame.pack();
			frame.repaint();
		}
	}

	public class FrameBoundsListener implements HierarchyBoundsListener {
		@Override
		public void ancestorMoved(HierarchyEvent e) {
		}

		@Override
		public void ancestorResized(HierarchyEvent e) {
			sourcePic.setPreferredSize(sourcePanel.getWidth() / 2);
		}
	}

	private void initInputs() {
		alphaHiddenInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		alphaHiddenInput.setText(Integer.toString(DEFAULT_HIDDEN_BITS_PER_COLOR));
		redHiddenInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		redHiddenInput.setText(Integer.toString(DEFAULT_HIDDEN_BITS_PER_COLOR));
		greenHiddenInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		greenHiddenInput.setText(Integer.toString(DEFAULT_HIDDEN_BITS_PER_COLOR));
		blueHiddenInput.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (getLength() + str.length() <= inputLimit)
					super.insertString(offs, str, a);
			}
		});
		blueHiddenInput.setText(Integer.toString(DEFAULT_HIDDEN_BITS_PER_COLOR));
	}

	public class MyChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent ce) {
			Integer value = (Integer) spinner.getValue();
			alphaHiddenInput.setText(Integer.toString(value));
			redHiddenInput.setText(Integer.toString(value));
			greenHiddenInput.setText(Integer.toString(value));
			blueHiddenInput.setText(Integer.toString(value));
			frame.repaint();
		}
	}

	private class MyDispatcher implements KeyEventDispatcher {
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					decodePic();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.dispose();
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (spinner.getModel().getNextValue() != null) {
						spinner.setValue((Integer) spinner.getValue() + 1);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (spinner.getModel().getPreviousValue() != null) {
						spinner.setValue((Integer) spinner.getValue() - 1);
					}
				}

			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
			} else if (e.getID() == KeyEvent.KEY_TYPED) {
			}
			return false;
		}
	}
}
