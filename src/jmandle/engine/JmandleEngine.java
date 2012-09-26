package jmandle.engine;

import marvin.gui.MarvinAttributesPanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.util.MarvinAttributes;
import java.io.File;

/**
 *
 */
public class JmandleEngine  extends MarvinAbstractImagePlugin {

    // global
    private boolean doScale = true;
    private int scaleWidth = -1;
    private int scaleHeight = -1;
    private boolean scaleToRatio = true;
    private boolean scaleWithBorders = true;

    private boolean doGrayScale = true;
    private boolean doCropBorder = true;
    private boolean doLandscapeSplit = true;

    private String outputUriPath = "jmandle";
    private String imageName;
    private String imageExtension;

    // gray scale
    private int[] grayScale16 = new int[] {0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255};
    private boolean doScale16;

    // crop border
    private int cropBorderThreshold = 221;

    // landscape split
    private boolean isLeftToRight = true;
    private MarvinImage imageLeft;
    private MarvinImage imageRight;

    public JmandleEngine() {}

    public void setDoScale(final boolean enable) {
        doScale = enable;
    }

    public void setDoGrayScale(final boolean enable) {
        doGrayScale = enable;
    }

    public void setDoCropBorder(final boolean enable) {
        doCropBorder = enable;
    }

    public void setDoLandscapeSplit(final boolean enable) {
        doLandscapeSplit = enable;
    }

    public void setOutputUri(final String outputUri) {
        outputUriPath = outputUri;
    }

    public void setScaleWidth(final int width) {
        scaleWidth = width;
    }

    public void setScaleHeight(final int height) {
        scaleHeight = height;
    }

    public void setScaleToRatio(final boolean enable) {
        scaleToRatio = enable;
    }

    public void setScaleWithBorders(final boolean enable) {
        scaleWithBorders = enable;
    }

    public void setImageUri(final String imageUri) {
        imageName = imageUri.substring(imageUri.lastIndexOf(File.separator) + 1, imageUri.lastIndexOf("."));
        imageExtension = imageUri.substring(imageUri.lastIndexOf("."), imageUri.length());
    }

    public void setDoScale16(final boolean enable) {
        doScale16 = enable;
    }

    public void setRemoveNoise(final boolean enable) {
        if (enable) {
            grayScale16[13] = 255;
            grayScale16[14] = 255;
        } else {
            grayScale16[13] = 221;
            grayScale16[14] = 238;
        }
    }

    public void setLeftToRight(final boolean enable) {
        isLeftToRight = enable;
    }

    @Override
    public void process(final MarvinImage imageIn,
                        final MarvinImage imageOut,
                        final MarvinAttributes attributes,
                        final MarvinImageMask imageMask,
                        boolean preview) {

        MarvinImage image = imageIn.clone();

        // STEP 1 -- Gray Scale
        if (doGrayScale) {
            image = doGrayScale(image);
        }

        // STEP 2 -- Remove Border
        if (doCropBorder) {
            image = doCropBorder(image);
        }

        // STEP 3 -- Landscape Split
        if (doLandscapeSplit) {
            image = doLandscapeSplit(image);
        }

        // STEP 4 -- Scale
        if (doScale) {
            if (doLandscapeSplit && imageLeft != null && imageRight != null) {
                imageLeft = doScale(imageLeft, scaleWidth, scaleHeight);
                imageRight = doScale(imageRight, scaleWidth, scaleHeight);
            } else {
                image = doScale(image, scaleWidth, scaleHeight);
            }
        }

        if (doLandscapeSplit && imageLeft != null && imageRight != null) {
            MarvinImageIO.saveImage(isLeftToRight ? imageLeft : imageRight, outputUriPath + File.separator + imageName + "_0" + imageExtension);
            MarvinImageIO.saveImage(isLeftToRight ? imageRight : imageLeft, outputUriPath + File.separator + imageName + "_1" + imageExtension);
            imageLeft = null;
            imageRight = null;
        } else {
            MarvinImageIO.saveImage(image, outputUriPath + File.separator + imageName + imageExtension);
        }
    }

    @Override
    public MarvinAttributesPanel getAttributesPanel() {
        return null;
    }

    @Override
    public void load() {
        // do nothing
    }

    private MarvinImage doGrayScale(final MarvinImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int r, g, b, f;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                r = image.getIntComponent0(x, y);
                g = image.getIntComponent1(x, y);
                b = image.getIntComponent2(x, y);
                f = (int) ((r * 0.3) + (g * 0.59) + (b * 0.11));

                if (doScale16) {
                    f = grayScale16[f / 16];
                }

                image.setIntColor(x, y, f, f, f);
            }
        }

        return image;
    }

    private MarvinImage doCropBorder(final MarvinImage image) {
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        int r, g, b, f;

        // columns - left to right
        columnLeft:
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                r = image.getIntComponent0(x, y);
                g = image.getIntComponent1(x, y);
                b = image.getIntComponent2(x, y);
                f = (r + g + b) / 3;

                if (f > cropBorderThreshold) {
                    x1 = x;
                } else {
                    break columnLeft;
                }
            }
        }

        // columns - right to left
        columnRight:
        for (int x = image.getWidth() - 1; x >= 0; x--) {
            for (int y = image.getHeight() - 1; y >= 0; y--) {
                r = image.getIntComponent0(x, y);
                g = image.getIntComponent1(x, y);
                b = image.getIntComponent2(x, y);
                f = (r + g + b) / 3;

                if (f > cropBorderThreshold) {
                    x2 = x;
                } else {
                    break columnRight;
                }
            }
        }

        // rows - top to bottom
        rowTop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                r = image.getIntComponent0(x, y);
                g = image.getIntComponent1(x, y);
                b = image.getIntComponent2(x, y);
                f = (r + g + b) / 3;

                if (f > cropBorderThreshold) {
                    y1 = y;
                } else {
                    break rowTop;
                }
            }
        }

        // rows - bottom to top
        rowBottom:
        for (int y = image.getHeight() - 1; y >= 0; y--) {
            for (int x = image.getWidth() - 1; x >= 0; x--) {
                r = image.getIntComponent0(x, y);
                g = image.getIntComponent1(x, y);
                b = image.getIntComponent2(x, y);
                f = (r + g + b) / 3;

                if (f > cropBorderThreshold) {
                    y2 = y;
                } else {
                    break rowBottom;
                }
            }
        }


        if (!(x1 < x2 && y1 < y2)) {
            return image;
        }

        x1 = x1 > 0 ? x1 - 1 : 0;
        x2 = x2 < image.getWidth() - 1 ? x2 + 1 : image.getWidth() - 1;
        y1 = y1 > 0 ? y1 - 1 : 0;
        y2 = y2 < image.getHeight() - 1 ? y2 + 1 : image.getHeight() - 1;

        final MarvinImage imageOut = new MarvinImage((x2 - x1), (y2 - y1));

        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                imageOut.setIntColor(x - x1, y - y1, image.getIntColor(x, y));
            }
        }

        return imageOut;
    }

    private MarvinImage doLandscapeSplit(final MarvinImage image) {
        if (image.getWidth() > image.getHeight()) {
            final int width = image.getWidth() / 2;
            final int height = image.getHeight();

            imageLeft = new MarvinImage(width, height);
            imageRight = new MarvinImage(width, height);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    imageLeft.setIntColor(x, y, image.getIntColor(x, y));
                }
            }

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    imageRight.setIntColor(x, y, image.getIntColor(x + width, y));
                }
            }
        }

        return image;
    }

    private MarvinImage doScale(final MarvinImage image, int width, int height) {
        final MarvinImage imageOut = new MarvinImage(1, 1);
        final double wRatio = image.getWidth() / (double) width;
        final double hRatio = image.getHeight() / (double) height;
        final int widthOriginal = width;
        final int heightOriginal = height;

        if (scaleToRatio) {
            if (wRatio < hRatio) {
                width = (int) (image.getWidth() / hRatio);
            } else {
                height = (int) (image.getHeight() / wRatio);
            }
        }

        imageOut.setDimension(width, height);

        final int xRatio = (image.getWidth() << 16) / width;
        final int yRatio = (image.getHeight() << 16) / height;

        int x2, y2;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                x2 = (x * xRatio) >> 16;
                y2 = (y * yRatio) >> 16;

                imageOut.setIntColor(x, y, image.getIntColor(x2, y2));
            }
        }

        if (scaleWithBorders && scaleToRatio) {
            final MarvinImage imageOut2 = new MarvinImage(widthOriginal, heightOriginal);
            int top = 0;
            int bottom = 0;
            int left = 0;
            int right = 0;

            if (imageOut.getWidth() != widthOriginal) {
                left = (widthOriginal - imageOut.getWidth()) / 2;
                right = left;

                if ((widthOriginal - imageOut.getWidth()) % 2 == 1) {
                    left++;
                }
            }

            if (imageOut.getHeight() != heightOriginal) {
                top = (heightOriginal - imageOut.getHeight()) / 2;
                bottom = top;

                if ((heightOriginal - imageOut.getHeight()) % 2 == 1) {
                    top++;
                }
            }

            // add border top
            for (int x = 0; x < widthOriginal; x++) {
                for (int y = 0; y < top; y++) {
                    imageOut2.setIntColor(x, y, 255, 255, 255);
                }
            }

            // add border bottom
            for (int x = 0; x < widthOriginal; x++) {
                for (int y = heightOriginal - 1; y > (heightOriginal - bottom); y--) {
                    imageOut2.setIntColor(x, y, 255, 255, 255);
                }
            }

            // add border left
            for (int x = 0; x < left; x++) {
                for (int y = 0; y < heightOriginal; y++) {
                    imageOut2.setIntColor(x, y, 255, 255, 255);
                }
            }

            // add border right
            for (int x = widthOriginal - 1; x > (widthOriginal - right); x--) {
                for (int y = 0; y < heightOriginal; y++) {
                    imageOut2.setIntColor(x, y, 255, 255, 255);
                }
            }

            // add image
            for (int x = left; x < (widthOriginal - right); x++) {
                for (int y = top; y < (heightOriginal - top); y++) {
                    imageOut2.setIntColor(x, y, imageOut.getIntColor(x - left, y - top));
                }
            }

            return imageOut2;
        }

        return imageOut;
    }
}
