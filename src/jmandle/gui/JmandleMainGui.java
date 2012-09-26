package jmandle.gui;

import jmandle.engine.JmandleEngineThread;
import jmandle.queue.JmandleQueue;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class JmandleMainGui {

    private JPanel mainPanel;
    private JTextField inputTextField;
    private JTextField outputTextField;
    private JButton inputButton;
    private JButton outputButton;
    private JCheckBox grayScaleCheckBox;
    private JCheckBox cropBorderCheckBox;
    private JCheckBox splitLandscapeCheckBox;
    private JCheckBox scaleImagesCheckBox;
    private JCheckBox useOnly16ColoursCheckBox;
    private JRadioButton leftToRightRadioButton;
    private JRadioButton rightToLeftRadioButton;
    private ButtonGroup buttonGroup;
    private JCheckBox keepAspectRatioCheckBox;
    private JCheckBox addWhiteBordersCheckBox;
    private JProgressBar convertProgressBar;
    private JButton convertButton;
    private JComboBox presetImageSizeComboBox;
    private JCheckBox threadingCheckBox;
    private JCheckBox subFolderCheckBox;
    private JCheckBox removeNoiseCheckBox;

    public JmandleMainGui() {
        grayScaleCheckBox.setSelected(true);
        cropBorderCheckBox.setSelected(true);
        splitLandscapeCheckBox.setSelected(true);
        scaleImagesCheckBox.setSelected(true);
        useOnly16ColoursCheckBox.setSelected(true);
        leftToRightRadioButton.setSelected(true);
        keepAspectRatioCheckBox.setSelected(true);
        addWhiteBordersCheckBox.setSelected(true);
        threadingCheckBox.setSelected(true);
        subFolderCheckBox.setSelected(true);

        inputTextField.setEditable(false);
        outputTextField.setEditable(false);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(leftToRightRadioButton);
        buttonGroup.add(rightToLeftRadioButton);

        presetImageSizeComboBox.insertItemAt("Kindle 2, 3, 4, 5", 0);
        presetImageSizeComboBox.insertItemAt("Kindle DX", 1);
        presetImageSizeComboBox.insertItemAt("Kindle Paperwhite", 2);
        presetImageSizeComboBox.setSelectedIndex(0);

        convertButton.setEnabled(false);

        inputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int rc = fc.showOpenDialog(null);

                if (rc == JFileChooser.APPROVE_OPTION) {
                    inputTextField.setText(fc.getSelectedFile().getAbsolutePath());
                }

                if (inputTextField.getText() != null
                        && !inputTextField.getText().isEmpty()
                        && outputTextField.getText() != null
                        && !outputTextField.getText().isEmpty()) {

                    convertButton.setEnabled(true);
                }
            }
        });

        outputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int rc = fc.showOpenDialog(null);

                if (rc == JFileChooser.APPROVE_OPTION) {
                    outputTextField.setText(fc.getSelectedFile().getAbsolutePath());
                }

                if (inputTextField.getText() != null
                        && !inputTextField.getText().isEmpty()
                        && outputTextField.getText() != null
                        && !outputTextField.getText().isEmpty()) {

                    convertButton.setEnabled(true);
                }
            }
        });

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                convert();
            }
        });
    }

    public void display() {
        JFrame frame = new JFrame("JmandleMainGui");
        frame.setContentPane(new JmandleMainGui().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void convert() {
        final File root = new File(inputTextField.getText());
        final File output = new File(outputTextField.getText().equals(inputTextField.getText()) ? outputTextField.getText() + File.separatorChar + "jmandle" : outputTextField.getText());
        final Queue<File> subs = new LinkedList<File>();
        int fileCount = 0;

        subs.add(root);

        while (!subs.isEmpty()) {
            final File current = subs.poll();

            for (final File file : current.listFiles()) {
                if (subFolderCheckBox.isSelected() && file.isDirectory()) {
                    subs.offer(file);
                } else if (file.isFile()) {
                    try {
                        JmandleQueue.getInstance().put(file.getAbsolutePath());
                        fileCount++;
                    } catch (InterruptedException e) {}
                }
            }
        }

        convertProgressBar.setMinimum(0);
        convertProgressBar.setMaximum(fileCount);
        convertProgressBar.setValue(0);

        final JmandleEngineThread[] workers = new JmandleEngineThread[threadingCheckBox.isSelected() ? Runtime.getRuntime().availableProcessors() : 1];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new JmandleEngineThread(
                    grayScaleCheckBox.isSelected(),
                    cropBorderCheckBox.isSelected(),
                    splitLandscapeCheckBox.isSelected(),
                    scaleImagesCheckBox.isSelected(),
                    useOnly16ColoursCheckBox.isSelected(),
                    removeNoiseCheckBox.isSelected(),
                    leftToRightRadioButton.isSelected(),
                    keepAspectRatioCheckBox.isSelected(),
                    addWhiteBordersCheckBox.isSelected(),
                    output.getAbsolutePath()
            );

            switch (presetImageSizeComboBox.getSelectedIndex()) {
                case 0:
                    workers[i].setScaleWidth(600);
                    workers[i].setScaleHeight(800);
                    break;
                case 1:
                    workers[i].setScaleWidth(824);
                    workers[i].setScaleHeight(1200);
                    break;
                case 2:
                    workers[i].setScaleWidth(768);
                    workers[i].setScaleHeight(1024);
                    break;
            }

            workers[i].setProgressBar(convertProgressBar);
            workers[i].start();
        }
    }
}
