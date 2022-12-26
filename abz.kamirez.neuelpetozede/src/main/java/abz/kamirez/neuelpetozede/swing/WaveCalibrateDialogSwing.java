/*******************************************************************************
 * Copyright 2017 Michael Skutta
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package abz.kamirez.neuelpetozede.swing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.elpetozede.domain.service.IProgressInfo;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.SilenceCalibrationParam;
import abz.kamirez.elpetozede.service.waveassign.WaveCalibratorService;

public class WaveCalibrateDialogSwing extends JDialog
{
  // private Combo m_maxValue;

  private JSlider m_maxValScale;
  private JLabel m_maxValLabel;
  private JSlider m_minPause;
  private JLabel m_minPauseLabel;

  private AudioFile m_sourceFile;
  private ILogger m_logger;
  private ResourceBundle m_messages;

  private boolean m_ergebnis;

  public WaveCalibrateDialogSwing(JFrame parentShell, ResourceBundle messages, ILogger logger)
  {
    super(parentShell);
    m_messages = messages;
    m_ergebnis = false;
    m_logger = logger;

  }

  public boolean open()
  {
    createDialog();
    setLocationRelativeTo(getOwner());
    setVisible(true);

    return m_ergebnis;
  }

  protected void createDialog()
  {
    setTitle(m_messages.getString("calibrate.dialog.title"));

    setLayout(new BorderLayout());

    JPanel sliderPanel = new JPanel();

    add(sliderPanel, BorderLayout.CENTER);
    sliderPanel.setLayout(new java.awt.GridLayout(2, 2));
    sliderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    m_maxValLabel = new JLabel("Maximum Volume:");

    m_maxValScale = new JSlider(JSlider.HORIZONTAL);
    m_maxValScale.setMaximum(1800);
    m_maxValScale.setMinimum(0);
    m_maxValScale.setMajorTickSpacing(300);
    m_maxValScale.setMinorTickSpacing(100);
    m_maxValScale.setPaintTicks(true);
    m_maxValScale.setPaintLabels(true);

    m_maxValScale.addChangeListener(new ChangeListener()
    {

      @Override
      public void stateChanged(ChangeEvent e)
      {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting())
        {
          int value = m_maxValScale.getValue();
          m_maxValLabel.setText("Max Volume: " + value);
        }
      }
    });

    m_minPauseLabel = new JLabel("");

    m_minPause = new JSlider(JSlider.HORIZONTAL);
    m_minPause.setMaximum(1600);
    m_minPause.setMinimum(0);
    m_minPause.setMajorTickSpacing(400);
    m_minPause.setMinorTickSpacing(100);
    m_minPause.setPaintTicks(true);
    m_minPause.setPaintLabels(true);

    m_minPause.addChangeListener(new ChangeListener()
    {

      @Override
      public void stateChanged(ChangeEvent e)
      {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting())
        {
          int value = m_minPause.getValue();
          m_minPauseLabel.setText("Minimum Pause: " + value + " milliseconds");
        }
      }
    });

    SilenceCalibrationParam param = new SilenceCalibrationParam(1250.0, 0.5);

    m_maxValScale.setValue((int) param.getMaxVolumeValue());
    m_maxValLabel.setText("Max Volume: " + m_maxValScale.getValue());

    // Pause

    int minPause = (int) (param.getMinPauseBetweenTracks() * 1000.0);
    m_minPause.setValue(minPause);
    m_minPauseLabel.setText("Minimum Pause: " + minPause + " milliseconds");

    JButton cancelButton = new JButton(m_messages.getString("calibrate.dialog.button_cancel"));

    cancelButton.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        setVisible(false);
        m_ergebnis = false;
      }
    });

    JButton calibrierenButton = new JButton(m_messages.getString("calibrate.dialog.button_start"));
    calibrierenButton.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        calibrate();
        setVisible(false);
        m_ergebnis = true;
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2));
    add(buttonPanel, BorderLayout.SOUTH);

    sliderPanel.add(m_maxValLabel);
    sliderPanel.add(m_maxValScale);
    sliderPanel.add(m_minPauseLabel);
    sliderPanel.add(m_minPause);
    buttonPanel.add(cancelButton);
    buttonPanel.add(calibrierenButton);
    pack();
    setSize(700, 250);
  }

  private void calibrate()
  {
    int maxVol = m_maxValScale.getValue();
    double minPause = m_minPause.getValue() / 1000.0;

    SilenceCalibrationParam param = new SilenceCalibrationParam((double) maxVol, minPause);

    try
    {
      JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
      ProgressDialog progMonDlg = new ProgressDialog(topFrame, m_messages.getString("calibrate.dialog.progresstitle"),
        m_logger, false);
      CalibrateProgress write = new CalibrateProgress(progMonDlg, m_sourceFile, param);

      write.execute();
    }
    catch (Exception ex)
    {
      Throwable cause = ex;

      if (ex.getCause() != null)
      {
        cause = ex.getCause();
      }

      cause.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error: " + cause.getMessage(),
        m_messages.getString("calibrate.dialog.errortitle"), JOptionPane.ERROR_MESSAGE);
    }
  }

  public AudioFile getSourceFile()
  {
    return m_sourceFile;
  }

  public void setSourceFile(AudioFile sourceFile)
  {
    m_sourceFile = sourceFile;
  }

  private class CalibrateProgress extends SwingWorker<Void, Void> implements IProgressListener
  {
    private ProgressDialog m_dlg;
    private WaveCalibratorService m_calibrator;
    private AudioFile m_selFile;
    private SilenceCalibrationParam m_param;

    private CalibrateProgress(ProgressDialog progDlg, final AudioFile selFile, final SilenceCalibrationParam param)
    {
      m_dlg = progDlg;
      m_selFile = selFile;
      m_param = param;
    }

    @Override
    public Void doInBackground()
    {
      m_calibrator = new WaveCalibratorService(true, m_logger);

      try
      {
        m_dlg.setNote(m_messages.getString("calibrate.dialog.progresstitle"));
        m_dlg.open();
        m_calibrator.addProgressListener(CalibrateProgress.this);
        m_calibrator.calibrateTracks(m_selFile, m_selFile.getTrackList(), m_param);

        m_calibrator.removeProgressListener(CalibrateProgress.this);

      }
      catch (Exception ex)
      {
        m_dlg.close();
        m_calibrator.removeProgressListener(CalibrateProgress.this);
        //throw new InvocationTargetException(ex);
      }

      return null;
    }

    @Override
    public void done()
    {
      m_calibrator.removeProgressListener(CalibrateProgress.this);
      m_dlg.close();
      m_logger.log("");
    }

    @Override
    public void progressReported(IProgressInfo progInfo)
    {
      final int newWork = progInfo.getPercentage();
      final String msg = progInfo.getMessage();

      m_dlg.setProgress(newWork);
      m_dlg.setNote(msg);
    }
  }

  private static void createAndShowGUI()
  {
    //Create and set up the window.
    JFrame frame = new JFrame("WaveCalibrateDialogSwing");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    WaveCalibrateDialogSwing dialog = new WaveCalibrateDialogSwing(frame, null, new ILogger()
    {

      @Override
      public void log(String message)
      {
        // TODO Auto-generated method stub

      }
    });
    //      newContentPane.setOpaque(true); //content panes must be opaque
    //      frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);

    dialog.setModal(true);
    dialog.setVisible(true);

  }

  public static void main(String[] args)
  {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        createAndShowGUI();
      }
    });
  }
}
