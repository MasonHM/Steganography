import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

public class PictureFrame extends Component {
	private static final long serialVersionUID = 1L;
	private BufferedImage img;
	private BufferedImage origImg;
	private int preferredSize = 100;

	public PictureFrame() {
	}

	public PictureFrame(String filename) {
		try {
			img = ImageIO.read(new File(filename));
		} catch (IOException e) {
		}
		origImg = PictureUtils.copyImage(img);
	}

	public void setImage(BufferedImage newImage) {
		img = PictureUtils.copyImage(newImage);
		origImg = PictureUtils.copyImage(newImage);
	}

	public void setImage(String filename) {
		try {
			img = ImageIO.read(new File(filename));
		} catch (IOException e) {
		}
		origImg = PictureUtils.copyImage(img);
	}

	public void modifyImage(BufferedImage modifiedImage) {
		img = PictureUtils.copyImage(modifiedImage);
	}

	public BufferedImage getImage() {
		return img;
	}

	public void paint(Graphics g) {
		Image scaledImg = null;

		if (img != null) {
			if (img.getWidth() > img.getHeight()) {
				scaledImg = img.getScaledInstance(preferredSize, -1,
						Image.SCALE_SMOOTH);

			} else {
				scaledImg = img.getScaledInstance(-1, preferredSize,
						Image.SCALE_SMOOTH);
			}
		}

		int x = 0;
		int y = 0;
		if (scaledImg != null) {
			x = (this.getWidth() - scaledImg.getWidth(null)) / 2;
			y = (this.getHeight() - scaledImg.getHeight(null)) / 2;
		}

		g.drawImage(scaledImg, x, y, this);
	}

	public void setPreferredSize(int size) {
		preferredSize = size;
	}

	public void resetImage() {
		img = PictureUtils.copyImage(origImg);
	}

	public Dimension getPreferredSize() {
		if (img == null) {
			return new Dimension(100, 100);
		} else {
			Image scaledImg = null;
			if (img.getWidth() > img.getHeight()) {
				scaledImg = img.getScaledInstance(preferredSize, -1, Image.SCALE_SMOOTH);
			} else {
				scaledImg = img.getScaledInstance(-1, preferredSize, Image.SCALE_SMOOTH);
			}
			return new Dimension(scaledImg.getWidth(null), scaledImg.getHeight(null));
		}
	}

}
