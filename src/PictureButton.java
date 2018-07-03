import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class PictureButton extends JComponent implements MouseListener {
	private static final long serialVersionUID = 1L;

	private int spacing = 4;
	private BufferedImage buttonPic;
	private Dimension preferredSize = new Dimension(36, 36);
	private Dimension arc = new Dimension((int) Math.sqrt(preferredSize.width),
			(int) Math.sqrt(preferredSize.height));
	private boolean mouseInside, mousePressed;

	public PictureButton(String filename, int width, int height) {
		super();
		setPicture(filename);
		this.setAlignmentX(Component.CENTER_ALIGNMENT);
		setPreferredSize(width, height);
		enableInputMethods(true);
		addMouseListener(this);
	}

	private AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(type, alpha));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.DARK_GRAY);
		g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc.width, arc.height);
		
		if (buttonPic != null) {
			while (mousePressed && this.getMousePosition() != null) {
				double xScale = (double)this.getMousePosition().x/(double)this.getWidth();
				double yScale = (double)this.getMousePosition().y/(double)this.getHeight();
				int xVal = (int)(buttonPic.getWidth()*xScale);
				int yVal = (int)(buttonPic.getHeight()*yScale);
				buttonPic.setRGB(xVal, yVal, PictureUtils.compileRGBAInfo(0, 255, 0, 0));
			}
			Image scaledImg = buttonPic.getScaledInstance(preferredSize.width,
					-1, Image.SCALE_SMOOTH);
			int x = (this.getWidth() - scaledImg.getWidth(null)) / 2;
			int y = (this.getHeight() - scaledImg.getHeight(null)) / 2;
			g.drawImage(scaledImg, x, y, this);
		}

		g2d.setColor(Color.BLACK);
		g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc.width,
				arc.height);
		g2d.drawRoundRect(spacing, spacing, getWidth() - spacing * 2 - 1,
				getHeight() - spacing * 2 - 1, arc.width, arc.height);

		if (mouseInside) {
			Composite originalComposite = g2d.getComposite();

			g2d.setComposite(makeComposite(0.5F));

			if (mousePressed) {
				g2d.setColor(Color.RED);
			} else {
				g2d.setColor(Color.GREEN);
			}
			g2d.fillRoundRect(spacing, spacing, getWidth() - spacing * 2,
					getHeight() - spacing * 2, arc.width, arc.height);
			g2d.setComposite(originalComposite);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseInside = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseInside = false;
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressed = true;
		System.out.println("Mouse X: " + this.getMousePosition().x + "\tMouse Y: " + this.getMousePosition().y);
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mousePressed = false;
		repaint();
	}

	public void setPicture(String filename) {
		try {
			buttonPic = ImageIO.read(new File(filename));
		} catch (IOException e) {
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(preferredSize.width, preferredSize.height);
	}

	public void setPreferredSize(int width, int height) {
		preferredSize.setSize(width, height);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

}
