package jmandle.engine;

import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import jmandle.queue.JmandleQueue;

import javax.swing.*;

/**
 *
 */
public class JmandleEngineThread extends Thread {

    private final JmandleEngine engine = new JmandleEngine();
    private JProgressBar progressBar;

    public JmandleEngineThread(final boolean doGrayScale,
                               final boolean doCropBorder,
                               final boolean doLandscapeSplit,
                               final boolean doScale,
                               final boolean doGrayScale16,
                               final boolean doRemoveNoise,
                               final boolean landscapeLeftToRight,
                               final boolean scaleToRatio,
                               final boolean scaleWithBorders,
                               final String outputUri) {

        engine.setDoGrayScale(doGrayScale);
        engine.setDoCropBorder(doCropBorder);
        engine.setDoLandscapeSplit(doLandscapeSplit);
        engine.setDoScale(doScale);
        engine.setScaleToRatio(scaleToRatio);
        engine.setScaleWithBorders(scaleWithBorders);
        engine.setDoScale16(doGrayScale16);
        engine.setRemoveNoise(doRemoveNoise);
        engine.setLeftToRight(landscapeLeftToRight);
        engine.setOutputUri(outputUri);
    }

    public void setScaleWidth(final int width) {
        engine.setScaleWidth(width);
    }

    public void setScaleHeight(final int height) {
        engine.setScaleHeight(height);
    }

    public void setProgressBar(final JProgressBar bar) {
        progressBar = bar;
    }

    @Override
    public void run() {
        String imageUri;

        while ((imageUri = JmandleQueue.getInstance().poll()) != null) {
            final MarvinImage image = MarvinImageIO.loadImage(imageUri);

            if (image != null) {
                engine.setImageUri(imageUri);
                engine.process(image, null);
                progressBar.setValue(progressBar.getValue() + 1);
            }
        }
    }
}
