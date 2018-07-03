import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SteganographyApplet {

	private final String DECODE_BUTTON = "Decode Picture (D)";
	private final String ENCODE_BUTTON = "Encode Picture (E)";
	private JFrame frame;

	public SteganographyApplet() {
		frame = new JFrame("Steganography");
		frame.setBackground(Color.DARK_GRAY);
		frame.setPreferredSize(new Dimension(500, 600));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		JButton encodeButton = new JButton(ENCODE_BUTTON);
		encodeButton.setPreferredSize(new Dimension(100, 100));
		encodeButton.addActionListener(new MyButtonListener());
		encodeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton decodeButton = new JButton(DECODE_BUTTON);
		decodeButton.setPreferredSize(new Dimension(100, 100));
		decodeButton.addActionListener(new MyButtonListener());

		buttonPanel.add(encodeButton);
		buttonPanel.add(new PictureButton("images/strawberry.jpg", 400, 400));
		buttonPanel.add(decodeButton);
		frame.add(buttonPanel, BorderLayout.CENTER);

		decodeButton.addKeyListener(new MyKeyListener());
		encodeButton.addKeyListener(new MyKeyListener());
		buttonPanel.addKeyListener(new MyKeyListener());
		frame.addKeyListener(new MyKeyListener());

		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		SteganographyApplet app = new SteganographyApplet();
	}

	private class MyButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals(DECODE_BUTTON)) {
				new DecodingGUI();
			} else {
				new EncodingGUI();
			}
		}
	}

	private class MyKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				frame.dispose();
			} else if (e.getKeyCode() == KeyEvent.VK_E) {
				new EncodingGUI();
			} else if (e.getKeyCode() == KeyEvent.VK_D) {
				new DecodingGUI();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
}
