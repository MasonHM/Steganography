import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class PictureUtils {
	private static final int ALPHA = 0;
	private static final int RED = 1;
	private static final int GREEN = 2;
	private static final int BLUE = 3;
	private static final int ACTUAL_SIZE = 0;
	private static final int STRETCH = 1;
	private static final int SCALE = 2;
	private static final int TILE = 3;

	public static void printPixelInfo(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();

		int[] dataBuffInt = img.getRGB(0, 0, w, h, null, 0, w);
		int count = 0;
		for (int rgb : dataBuffInt) {
			System.out.print("Pixel: " + count++);
			int alpha = (rgb >> 24) & 0xFF;
			int red = (rgb >> 16) & 0xFF;
			int green = (rgb >> 8) & 0xFF;
			int blue = rgb & 0xFF;
			System.out.print("\tRed: " + red);
			System.out.print("\tGreen: " + green);
			System.out.print("\tBlue: " + blue);
			System.out.println("\tAlpha: " + alpha);
		}
	}

	public static BufferedImage invertImage(BufferedImage img) {
		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				img.setRGB(i, j, img.getRGB(i, j) ^ 0xFFFFFFFF);
			}
		}

		return img;
	}

	/**
	 * Lowers the quality of a picture by setting the least significant bits in
	 * each color level to 0.
	 * 
	 * @param img
	 *            - Image to lower quality
	 * @param alphaBits
	 *            - Number of bits representing alpha in img
	 * @param redBits
	 *            - Number of bits representing red in img
	 * @param greenBits
	 *            - Number of bits representing green in img
	 * @param blueBits
	 *            - Number of bits representing blue in img
	 * @return The image with the quality lowered
	 */
	public static BufferedImage lowerPictureQuality(BufferedImage img,
			int alphaBits, int redBits, int greenBits, int blueBits) {
		BufferedImage resultPic = copyImage(img);
		int[] RGBA;
		int newRGBA;

		int roundNums[] = getScaleFactors(8 - alphaBits, 8 - redBits,
				8 - greenBits, 8 - blueBits, 1);

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				RGBA = getRGBAInfo(img, i, j);

				RGBA[ALPHA] = alphaBits > 0 ? roundToNearestMultiple(
						RGBA[ALPHA], roundNums[ALPHA]) : 0;
				RGBA[RED] = redBits > 0 ? roundToNearestMultiple(RGBA[RED],
						roundNums[RED]) : 0;
				RGBA[GREEN] = greenBits > 0 ? roundToNearestMultiple(
						RGBA[GREEN], roundNums[GREEN]) : 0;
				RGBA[BLUE] = blueBits > 0 ? roundToNearestMultiple(RGBA[BLUE],
						roundNums[BLUE]) : 0;

				newRGBA = compileRGBAInfo(RGBA[ALPHA], RGBA[RED], RGBA[GREEN],
						RGBA[BLUE]);

				resultPic.setRGB(i, j, newRGBA);
			}
		}
		return resultPic;
	}

	/**
	 * Hide a hidden picture in the less significant bits of the decoy image.
	 * 
	 * @param decoy
	 *            - Picture to hide the concealed image
	 * @param hidden
	 *            - Concealed image
	 * @param alphaBitsDecoy
	 *            - Number of bits representing alpha in decoy. 8 - alphaBits is
	 *            the number of bits representing alpha in hidden.
	 * @param redBitsDecoy
	 *            - Number of bits representing red in decoy. 8 - redBits is the
	 *            number of bits representing red in hidden.
	 * @param greenBitsDecoy
	 *            - Number of bits representing green in decoy. 8 - greenBits is
	 *            the number of bits representing green in hidden.
	 * @param blueBitsDecoy
	 *            - Number of bits representing blue in decoy. 8 - blueBits is
	 *            the number of bits representing blue in hidden.
	 * @return
	 */
	public static BufferedImage hidePictureInDecoyPicture(BufferedImage decoy,
			BufferedImage hidden, int alphaBitsDecoy, int redBitsDecoy,
			int greenBitsDecoy, int blueBitsDecoy, int hiddenPictureOption) {
		BufferedImage resultPic = copyImage(decoy);
		int[] decoyRGBA;
		int[] hiddenRGBA = new int[4];
		int newRGBA;

		int roundNums[] = getScaleFactors(alphaBitsDecoy, redBitsDecoy,
				greenBitsDecoy, blueBitsDecoy, 1);

		String[] masks = createDecoyStringMasks(alphaBitsDecoy, redBitsDecoy,
				greenBitsDecoy, blueBitsDecoy);

		if (hiddenPictureOption == STRETCH) {
			Image hiddenImage = hidden.getScaledInstance(decoy.getWidth(),
					decoy.getHeight(), Image.SCALE_SMOOTH);
			hidden = new BufferedImage(decoy.getWidth(), decoy.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D bGr = hidden.createGraphics();
			bGr.drawImage(hiddenImage, 0, 0, null);
			bGr.dispose();
		} else if (hiddenPictureOption == SCALE) {
			Image hiddenImage = null;
			int startX = 0;
			int startY = 0;
			if (hidden.getWidth() > hidden.getHeight()) {
				hiddenImage = hidden.getScaledInstance(decoy.getWidth(), -1,
						Image.SCALE_SMOOTH);
				startY = (decoy.getHeight() - hiddenImage.getHeight(null))/2;
			} else {
				hiddenImage = hidden.getScaledInstance(-1, decoy.getHeight(),
						Image.SCALE_SMOOTH);
				startX = (decoy.getWidth() - hiddenImage.getWidth(null))/2;
			}
			hidden = new BufferedImage(decoy.getWidth(), decoy.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D bGr = hidden.createGraphics();
			bGr.drawImage(hiddenImage, startX, startY, null);
			bGr.dispose();
		}

		for (int i = 0; i < decoy.getWidth(); i++) {
			for (int j = 0; j < decoy.getHeight(); j++) {
				decoyRGBA = getRGBAInfo(decoy, i, j);
				if (hiddenPictureOption == ACTUAL_SIZE) {
					if (i < hidden.getWidth() && j < hidden.getHeight()) {
						hiddenRGBA = getRGBAInfo(hidden, i, j);
					} else {
						hiddenRGBA[ALPHA] = 0;
						hiddenRGBA[RED] = 0;
						hiddenRGBA[GREEN] = 0;
						hiddenRGBA[BLUE] = 0;
					}
				} else if (hiddenPictureOption == STRETCH) {
					hiddenRGBA = getRGBAInfo(hidden, i, j);
				} else if (hiddenPictureOption == SCALE) {
					hiddenRGBA = getRGBAInfo(hidden, i, j);
				} else if (hiddenPictureOption == TILE) {
					hiddenRGBA = getRGBAInfo(hidden, i % hidden.getWidth(), j
							% hidden.getHeight());
				}

				if (alphaBitsDecoy == 0)
					decoyRGBA[ALPHA] = 0;
				if (redBitsDecoy == 0)
					decoyRGBA[RED] = 0;
				if (greenBitsDecoy == 0)
					decoyRGBA[GREEN] = 0;
				if (blueBitsDecoy == 0)
					decoyRGBA[BLUE] = 0;
				if (8 - alphaBitsDecoy == 0) {
					hiddenRGBA[ALPHA] = 0;
				}
				if (8 - redBitsDecoy == 0) {
					hiddenRGBA[RED] = 0;
				}
				if (8 - greenBitsDecoy == 0) {
					hiddenRGBA[GREEN] = 0;
				}
				if (8 - blueBitsDecoy == 0) {
					hiddenRGBA[BLUE] = 0;
				}

				hiddenRGBA[ALPHA] = roundToNearestMultiple(hiddenRGBA[ALPHA],
						roundNums[ALPHA]) / roundNums[ALPHA];
				hiddenRGBA[RED] = roundToNearestMultiple(hiddenRGBA[RED],
						roundNums[RED]) / roundNums[RED];
				hiddenRGBA[GREEN] = roundToNearestMultiple(hiddenRGBA[GREEN],
						roundNums[GREEN]) / roundNums[GREEN];
				hiddenRGBA[BLUE] = roundToNearestMultiple(hiddenRGBA[BLUE],
						roundNums[BLUE]) / roundNums[BLUE];

				hiddenRGBA[ALPHA] = (decoyRGBA[ALPHA] & Integer.parseInt(
						masks[ALPHA], 2)) | hiddenRGBA[ALPHA];
				hiddenRGBA[RED] = (decoyRGBA[RED] & Integer.parseInt(
						masks[RED], 2)) | hiddenRGBA[RED];
				hiddenRGBA[GREEN] = (decoyRGBA[GREEN] & Integer.parseInt(
						masks[GREEN], 2)) | hiddenRGBA[GREEN];
				hiddenRGBA[BLUE] = (decoyRGBA[BLUE] & Integer.parseInt(
						masks[BLUE], 2)) | hiddenRGBA[BLUE];

				newRGBA = compileRGBAInfo(hiddenRGBA[ALPHA], hiddenRGBA[RED],
						hiddenRGBA[GREEN], hiddenRGBA[BLUE]);
				resultPic.setRGB(i, j, newRGBA);
			}
		}
		return resultPic;
	}

	private static int[] getScaleFactors(int alphaBitsDecoy, int redBitsDecoy,
			int greenBitsDecoy, int blueBitsDecoy, int alternativeNum) {
		int scaleFactors[] = new int[4];
		scaleFactors[ALPHA] = 8 - alphaBitsDecoy > 0 ? 255 / ((int) Math.pow(2,
				8 - alphaBitsDecoy) - 1) : alternativeNum;
		scaleFactors[RED] = 8 - redBitsDecoy > 0 ? 255 / ((int) Math.pow(2,
				8 - redBitsDecoy) - 1) : alternativeNum;
		scaleFactors[GREEN] = 8 - greenBitsDecoy > 0 ? 255 / ((int) Math.pow(2,
				8 - greenBitsDecoy) - 1) : alternativeNum;
		scaleFactors[BLUE] = 8 - blueBitsDecoy > 0 ? 255 / ((int) Math.pow(2,
				8 - blueBitsDecoy) - 1) : alternativeNum;
		return scaleFactors;
	}

	/**
	 * Decode a hidden image from the less significant bits of a decoy image.
	 * 
	 * @param img
	 *            - The decoy image within which a picture is hidden.
	 * @param alphaBitsDecoy
	 *            - Number of bits representing alpha in decoy. 8 - alphaBits is
	 *            the number of bits representing alpha in hidden.
	 * @param redBitsDecoy
	 *            - Number of bits representing red in decoy. 8 - redBits is the
	 *            number of bits representing red in hidden.
	 * @param greenBitsDecoy
	 *            - Number of bits representing green in decoy. 8 - greenBits is
	 *            the number of bits representing green in hidden.
	 * @param blueBitsDecoy
	 *            - Number of bits representing blue in decoy. 8 - blueBits is
	 *            the number of bits representing blue in hidden.
	 * @return
	 */
	public static BufferedImage[] decodePictureFromDecoyPicture(
			BufferedImage img, int alphaBitsDecoy, int redBitsDecoy,
			int greenBitsDecoy, int blueBitsDecoy) {
		int newHiddenRGB, newDecoyRGB;
		int[] decoyRGBA = new int[4];
		int[] hiddenRGBA = new int[4];
		int[] sourceRGBA;

		// Copy the image to create the resulting images.
		BufferedImage resultHiddenPic = copyImage(img);
		BufferedImage resultDecoyPic = copyImage(img);

		// Create String mask for the hidden picture. It is simply a string of
		// 1's that is as long as the number of bits representing that channel.
		String[] hiddenStringMaskArray = createHiddenStringMasks(
				alphaBitsDecoy, redBitsDecoy, greenBitsDecoy, blueBitsDecoy);

		// Create String mask for the decoy picture. Consists of 8 bits, higher
		// order 1's and as many trailing 0's as the hidden picture uses for the
		// channel.
		String[] decoyStringMaskArray = createDecoyStringMasks(alphaBitsDecoy,
				redBitsDecoy, greenBitsDecoy, blueBitsDecoy);

		// Get the normalization multiplier for each pixel channel. If zero bits
		// are being used for a channel, the multiplier is zero.
		int[] multipliers = getScaleFactors(alphaBitsDecoy, redBitsDecoy,
				greenBitsDecoy, blueBitsDecoy, 0);

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				// Get RGBA values from the source picture
				sourceRGBA = getRGBAInfo(img, i, j);

				// Get RGBA values for the hidden picture by taking the RGB
				// values from the source picture and ANDing them with a String
				// mask
				hiddenRGBA[ALPHA] = sourceRGBA[ALPHA]
						& Integer.parseInt(hiddenStringMaskArray[ALPHA], 2);
				hiddenRGBA[RED] = sourceRGBA[RED]
						& Integer.parseInt(hiddenStringMaskArray[RED], 2);
				hiddenRGBA[GREEN] = sourceRGBA[GREEN]
						& Integer.parseInt(hiddenStringMaskArray[GREEN], 2);
				hiddenRGBA[BLUE] = sourceRGBA[BLUE]
						& Integer.parseInt(hiddenStringMaskArray[BLUE], 2);

				// Get RGBA values for the decoy picture by taking the RGB
				// values from the source picture and ANDing them with a String
				// mask
				decoyRGBA[ALPHA] = sourceRGBA[ALPHA]
						& Integer.parseInt(decoyStringMaskArray[ALPHA], 2);
				decoyRGBA[RED] = sourceRGBA[RED]
						& Integer.parseInt(decoyStringMaskArray[RED], 2);
				decoyRGBA[GREEN] = sourceRGBA[GREEN]
						& Integer.parseInt(decoyStringMaskArray[GREEN], 2);
				decoyRGBA[BLUE] = sourceRGBA[BLUE]
						& Integer.parseInt(decoyStringMaskArray[BLUE], 2);

				// Compile RGBA values into one integer value and set the decoy
				// picture's pixel info
				newDecoyRGB = compileRGBAInfo(decoyRGBA[ALPHA], decoyRGBA[RED],
						decoyRGBA[GREEN], decoyRGBA[BLUE]);
				resultDecoyPic.setRGB(i, j, newDecoyRGB);

				// Normalize the hidden picture's RGBA values
				hiddenRGBA[ALPHA] = hiddenRGBA[ALPHA] * multipliers[ALPHA];
				hiddenRGBA[RED] = hiddenRGBA[RED] * multipliers[RED];
				hiddenRGBA[GREEN] = hiddenRGBA[GREEN] * multipliers[GREEN];
				hiddenRGBA[BLUE] = hiddenRGBA[BLUE] * multipliers[BLUE];

				// Compile RGBA values into one integer value and set the hidden
				// picture's pixel info
				newHiddenRGB = compileRGBAInfo(hiddenRGBA[ALPHA],
						hiddenRGBA[RED], hiddenRGBA[GREEN], hiddenRGBA[BLUE]);
				resultHiddenPic.setRGB(i, j, newHiddenRGB);
			}
		}
		return new BufferedImage[] { resultHiddenPic, resultDecoyPic };
	}

	public static int[] getRGBAInfo(BufferedImage img, int x, int y) {
		int sourceRGB = img.getRGB(x, y);
		int alpha = (sourceRGB >> 24) & 0xFF;
		int red = (sourceRGB >> 16) & 0xFF;
		int green = (sourceRGB >> 8) & 0xFF;
		int blue = sourceRGB & 0xFF;
		return new int[] { alpha, red, green, blue };
	}

	public static int compileRGBAInfo(int alpha, int red, int green, int blue) {
		int rgb = alpha;
		rgb = (rgb << 8) + red;
		rgb = (rgb << 8) + green;
		rgb = (rgb << 8) + blue;
		return rgb;
	}

	private static String[] createHiddenStringMasks(int alphaBitsDecoy,
			int redBitsDecoy, int greenBitsDecoy, int blueBitsDecoy) {
		char[] aArray = new char[8 - alphaBitsDecoy];
		char[] rArray = new char[8 - redBitsDecoy];
		char[] gArray = new char[8 - greenBitsDecoy];
		char[] bArray = new char[8 - blueBitsDecoy];
		Arrays.fill(aArray, '1');
		Arrays.fill(rArray, '1');
		Arrays.fill(gArray, '1');
		Arrays.fill(bArray, '1');
		String[] returnArray = new String[4];
		returnArray[ALPHA] = 8 - alphaBitsDecoy > 0 ? new String(aArray)
				: new String("0");
		returnArray[RED] = 8 - redBitsDecoy > 0 ? new String(rArray)
				: new String("0");
		returnArray[GREEN] = 8 - greenBitsDecoy > 0 ? new String(gArray)
				: new String("0");
		returnArray[BLUE] = 8 - blueBitsDecoy > 0 ? new String(bArray)
				: new String("0");
		// System.out.println("HIDDEN STRING MASKS:");
		// System.out.println(returnArray[ALPHA]);
		// System.out.println(returnArray[RED]);
		// System.out.println(returnArray[GREEN]);
		// System.out.println(returnArray[BLUE]);
		return returnArray;
	}

	private static String[] createDecoyStringMasks(int alphaBitsDecoy,
			int redBitsDecoy, int greenBitsDecoy, int blueBitsDecoy) {
		char[] aArray = new char[8];
		char[] rArray = new char[8];
		char[] gArray = new char[8];
		char[] bArray = new char[8];
		Arrays.fill(aArray, '1');
		Arrays.fill(rArray, '1');
		Arrays.fill(gArray, '1');
		Arrays.fill(bArray, '1');
		Arrays.fill(aArray, alphaBitsDecoy, 8, '0');
		Arrays.fill(rArray, redBitsDecoy, 8, '0');
		Arrays.fill(gArray, greenBitsDecoy, 8, '0');
		Arrays.fill(bArray, blueBitsDecoy, 8, '0');
		String[] returnArray = new String[4];
		returnArray[ALPHA] = new String(aArray);
		returnArray[RED] = new String(rArray);
		returnArray[GREEN] = new String(gArray);
		returnArray[BLUE] = new String(bArray);
		// System.out.println("\nDECOY STRING MASKS:");
		// System.out.println(returnArray[ALPHA]);
		// System.out.println(returnArray[RED]);
		// System.out.println(returnArray[GREEN]);
		// System.out.println(returnArray[BLUE]);
		return returnArray;
	}

	public static int roundToNearestMultiple(int number, int roundNum) {
		int remainder = number % roundNum;
		return remainder < (double) roundNum / 2 ? number - remainder : Math
				.min(255, (number - remainder + roundNum));
	}

	public static BufferedImage copyImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
