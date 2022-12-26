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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.ClipStatusModel;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.IAudioFile;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.IAudioPlayer;
import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.elpetozede.domain.service.IProgressInfo;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.IProgressReportingTask;
import abz.kamirez.elpetozede.service.player.wave.SimpleWavePlayer;

public class WavePanel extends AbstraktElpetozedePanel
{

  private AudioFile m_audioFile;
  private Point2D m_origin;
  private JScrollPane m_canvasScrollPane;
  private WaveCanvas m_canvas;
  private List<TrackInfo> m_trackInfoList;

  private Hashtable<TrackInfo, Rectangle2D> m_trackPosMap;

  private JButton m_adjustTracksBtn;
  private JButton m_undoAdjustTracksBtn;
  private JLabel m_zoomLabel;
  private JComboBox<String> m_zoomCombo;

  private Color[] m_channelCols;

  private boolean m_selecting = false;
  private ClipStatusModel m_clipStatus;

  private int m_zoom;

  private JButton m_playBtn;
  private JButton m_stopBtn;
  private JLabel m_selectionLabel;

  private IAudioPlayer m_player;
  private WavePlayerProgressListener m_playerListener;

  public WavePanel(AlbumProject albumProject, ResourceBundle messages, ILogger logger)
  {
    super(albumProject, messages, logger);
    m_clipStatus = new ClipStatusModel();
    m_zoom = 100;
    m_trackPosMap = new Hashtable<TrackInfo, Rectangle2D>();
    m_playerListener = new WavePlayerProgressListener();

    m_channelCols = new Color[4];

    m_channelCols[0] = new Color(255, 100, 100);
    m_channelCols[1] = new Color(100, 255, 100);
    m_channelCols[2] = new Color(100, 100, 255);
    m_channelCols[3] = new Color(255, 255, 255);

    m_player = new SimpleWavePlayer();

    initGui();
  }

  @Override
  protected void initGui()
  {
    setLayout(new BorderLayout());
    m_canvas = new WaveCanvas();
    m_canvasScrollPane = new JScrollPane(m_canvas);

    add(m_canvasScrollPane, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2));

    JPanel calibrateButtonPanel = createCalibrationButtonPanel();
    JPanel playButtonPanel = createPlayButtonPanel();

    buttonPanel.add(calibrateButtonPanel);
    buttonPanel.add(playButtonPanel);

    add(buttonPanel, BorderLayout.SOUTH);
  }

  private JPanel createCalibrationButtonPanel()
  {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    m_zoomLabel = new JLabel("Zoom: ");
    m_zoomCombo = new JComboBox<String>();

    for (int i = 1; i <= 8; i++)
    {
      m_zoomCombo.addItem(i * 100 + "%");
    }

    m_zoomCombo.setSelectedIndex(0);
    m_zoomCombo.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        zoomSelected();
      }
    });

    m_zoomCombo.setEnabled(false);

    m_adjustTracksBtn = new JButton();
    m_adjustTracksBtn.setText(m_messages.getString("button.liederGrenzenJustieren"));
    m_adjustTracksBtn.setEnabled(false);

    m_adjustTracksBtn.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        adjustTracks();
      }
    });

    m_undoAdjustTracksBtn = new JButton();
    m_undoAdjustTracksBtn.setText(m_messages.getString("button.undoLiederGrenzenJustieren"));
    m_undoAdjustTracksBtn.setEnabled(false);

    m_undoAdjustTracksBtn.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        undoAdjustTracks();
      }
    });

    buttonPanel.add(m_zoomLabel);
    buttonPanel.add(m_zoomCombo);
    buttonPanel.add(m_adjustTracksBtn);
    buttonPanel.add(m_undoAdjustTracksBtn);

    return buttonPanel;
  }

  private JPanel createPlayButtonPanel()
  {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    m_playBtn = new JButton();
    m_playBtn.setText(m_messages.getString("button.auswahlAbspielen"));
    m_playBtn.setEnabled(false);

    m_playBtn.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        playWav();
      }
    });

    m_stopBtn = new JButton();
    m_stopBtn.setText("Stop");
    m_stopBtn.setEnabled(false);

    m_stopBtn.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        stopWav();
      }
    });

    m_selectionLabel = new JLabel();
    m_selectionLabel.setText(m_messages.getString("label.auswahlKeine"));

    buttonPanel.add(m_playBtn);
    buttonPanel.add(m_stopBtn);
    buttonPanel.add(m_selectionLabel);

    return buttonPanel;
  }

  public IAudioFile getAudioFile()
  {
    return m_audioFile;
  }

  public void setAudioFile(IAudioFile audioFile)
  {
    getMediator().addPropertyChangeListener(this);
    m_audioFile = (AudioFile) audioFile;
    clearFrameSelection();

    if (m_audioFile != null)
    {
      m_adjustTracksBtn.setEnabled(true);
      m_zoomCombo.setEnabled(true);
      initOriginalTimeList();
    }

    m_canvas.repaint();

  }

  @Override
  protected void updatePanel()
  {
    m_canvas.repaint();
  }

  private void zoomSelected()
  {
    int zoomIdx = m_zoomCombo.getSelectedIndex();

    if (zoomIdx > -1)
    {
      m_zoom = 100 * (zoomIdx + 1);
    }

    m_canvas.repaint();
  }

  private int getZoomFactor()
  {
    int rueckgabe = m_zoom / 100;
    return rueckgabe;
  }

  private int getCanvasOffset()
  {
    return m_canvasScrollPane.getHorizontalScrollBar().getValue();
  }

  private String getTitleInfo(int second)
  {
    String rueckgabe = "";

    Track track = m_audioFile.getTrack(second);

    if (track != null)
    {
      rueckgabe = track.getName();
    }

    return rueckgabe;
  }

  private double getValue(double scale, int second, int channel)
  {
    double rueckgabe = 0.0;

    if (second < m_audioFile.getLengthInSeconds() / scale)
    {

      int bitsPerSecond = m_audioFile.getBitsPerSample();
      int posBegin = second * (int) (scale * (double) bitsPerSecond);
      int posEnd = Math.min((posBegin + (int) (scale * (double) bitsPerSecond)), (int) m_audioFile.getSamples());

      double sumX = 0.0;
      int n = 0;
      double val = 0.0;

      for (int i = posBegin; i < posEnd; i = i + 100)
      {
        val = m_audioFile.getValue(channel, i);

        sumX = sumX + val * val;
        n++;
      }

      rueckgabe = Math.sqrt(sumX / (double) n);
    }
    else
    {
      rueckgabe = 0.0;
    }

    return rueckgabe;
  }

  private int getFrameAtSecond(double scale, int second)
  {
    int bitsPerSecond = m_audioFile.getBitsPerSample();
    int frame = second * (int) (scale * (double) bitsPerSecond);
    return frame;

  }

  private DvTime getTimeAtFrame(int frame)
  {
    int bitsPerSecond = m_audioFile.getBitsPerSample();
    int second = frame / bitsPerSecond;
    DvTime result = DvTime.valueOf(second);
    return result;
  }

  private int getFrameAtPixel(int pixelPos)
  {
    int second = getCanvasOffset() + pixelPos;
    int frame = getFrameAtSecond(100.0 / (double) m_zoom, second);
    return frame;
  }

  private int getPixelAtFrame(int frame)
  {
    int scrollOffSet = getCanvasOffset();
    int bitsPerSecond = m_audioFile.getBitsPerSample();

    int result = (((m_zoom / 100) * frame) / bitsPerSecond) - scrollOffSet;
    return result;
  }

  private Rectangle getWaveImageBounds()
  {
    Rectangle canvRect = m_canvas.getBounds();

    if (m_audioFile != null)
    {
      canvRect.width = m_audioFile.getLengthInSeconds() * (m_zoom / 100);
    }
    return canvRect;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (m_audioFile != null)
    {
      if (m_audioFile.getTrackList().size() != m_trackInfoList.size())
      {
        initOriginalTimeList();
      }
    }

    m_canvas.repaint();

  }

  private void initOriginalTimeList()
  {
    m_trackInfoList = new ArrayList<TrackInfo>();

    List<Track> trackList = m_audioFile.getTrackList();

    for (int i = 0; i < trackList.size(); i++)
    {
      Track tempTrack = trackList.get(i);
      DvTime trackTime = tempTrack.getTime();
      TrackInfo tempInfo = new TrackInfo(tempTrack, trackTime);
      m_trackInfoList.add(tempInfo);
    }
  }

  private void adjustTracks()
  {
    initOriginalTimeList();

    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    WaveCalibrateDialogSwing calDlg = new WaveCalibrateDialogSwing(topFrame, m_messages, m_logger);
    calDlg.setSourceFile(m_audioFile);

    if (calDlg.open())
    {
      notifyAlbumChange();
      m_canvas.repaint();
      m_undoAdjustTracksBtn.setEnabled(true);
    }

  }

  private void undoAdjustTracks()
  {
    List<Track> trackList = m_audioFile.getTrackList();

    for (int i = 0; i < trackList.size(); i++)
    {
      DvTime orgTime = m_trackInfoList.get(i).m_orgTime;

      Track track = trackList.get(i);
      track.setTime(orgTime);
    }
    m_undoAdjustTracksBtn.setEnabled(false);
    notifyAlbumChange();
    m_canvas.repaint();

  }

  protected void playWav()
  {
    try
    {
      m_clipStatus.setFinished(false);
      m_player.playAudioFile(m_audioFile, m_clipStatus);
      m_stopBtn.setEnabled(true);
      m_playBtn.setEnabled(false);
      ((IProgressReportingTask) m_player).addProgressListener(m_playerListener);
    }
    catch (IOException ioex)
    {
      JOptionPane.showMessageDialog(this, "Error: " + ioex.getMessage(), "Error playing File",
        JOptionPane.ERROR_MESSAGE);

    }
  }

  protected void stopWav()
  {
    m_player.stop();
    m_stopBtn.setEnabled(false);
    m_playBtn.setEnabled(true);
    ((IProgressReportingTask) m_player).removeProgressListener(m_playerListener);
    m_clipStatus.setCurrentFrame(0);

    m_canvas.repaint();
  }

  private void startFrameSelection(int mouseX)
  {
    int startFrame = getFrameAtPixel(mouseX);

    if (m_audioFile.getSamples() > startFrame)
    {
      m_selecting = true;
      m_clipStatus.setStartFrame(startFrame);
      m_clipStatus.setEndFrame(getFrameAtPixel(mouseX + 1));

      m_playBtn.setEnabled(true);
      setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }
  }

  private void clearFrameSelection()
  {
    m_selecting = false;
    m_clipStatus.reset();
    m_playBtn.setEnabled(false);
    m_canvas.repaint();
    m_selectionLabel.setText(m_messages.getString("label.auswahlKeine"));

  }

  private void editFrameSelection(int x)
  {
    int endFrame = getFrameAtPixel(x);
    endFrame = (int) Math.min(endFrame, m_audioFile.getSamples());

    if (endFrame < m_clipStatus.getStartFrame())
    {
      m_clipStatus.setStartFrame(Math.min(m_clipStatus.getStartFrame(), endFrame));
      m_clipStatus.setEndFrame(Math.max(m_clipStatus.getStartFrame(), m_clipStatus.getEndFrame()));
    }
    else
    {
      m_clipStatus.setEndFrame(endFrame);
    }

    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    m_canvas.repaint();

    String text = getTimeAtFrame(m_clipStatus.getStartFrame()) + " - " + getTimeAtFrame(m_clipStatus.getEndFrame());
    m_selectionLabel.setText(text);
  }

  private class TrackInfo
  {
    public TrackInfo(Track track, int startPos, int endPos, Rectangle endArea, DvTime orgTime)
    {
      super();
      m_track = track;
      m_startPos = startPos;
      m_endPos = endPos;
      m_endArea = endArea;
      m_orgTime = orgTime;
    }

    public TrackInfo(Track track, DvTime orgTime)
    {
      this(track, 0, 0, new Rectangle(0, 0, 0, 0), orgTime);
    }

    private Track m_track;
    private int m_startPos;
    private int m_endPos;
    private Rectangle m_endArea;
    private DvTime m_orgTime;
  }

  private class WavePlayerProgressListener implements IProgressListener
  {
    @Override
    public void progressReported(IProgressInfo progInfo)
    {
      //   final PlayerProgressInfo playerProgress = (PlayerProgressInfo) progInfo;

      SwingUtilities.invokeLater(new Runnable()
      {

        @Override
        public void run()
        {
          //m_canvas.drawPlayerProgress();
          int y1 = 0;
          int y2 = getHeight();

          int beginX = getPixelAtFrame(m_clipStatus.getStartFrame());
          int endX = getPixelAtFrame(m_clipStatus.getEndFrame());

          m_canvas.repaint(beginX, y1, endX - beginX, y2);

          if (m_clipStatus.isFinished())
          {
            stopWav();
          }
        };

      });

    }

    public void progressReportedThreaded(IProgressInfo progInfo)
    {
      //   final PlayerProgressInfo playerProgress = (PlayerProgressInfo) progInfo;

      Thread progDrawThread = new Thread(new Runnable()
      {

        @Override
        public void run()
        {
          SwingUtilities.invokeLater(new Runnable()
          {

            @Override
            public void run()
            {
              m_canvas.drawPlayerProgress();

              if (m_clipStatus.isFinished())
              {
                stopWav();
              }
            };

          });

        };
      });

      progDrawThread.start();
    }
  }

  private class ProgressLineBackground
  {
    int m_xPos;
    int m_yPos;

    Image m_backgroundImage;
  }

  private class WaveCanvas extends JComponent
  {

    private BufferedImage m_waveImage;

    public WaveCanvas()
    {
      m_waveImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
      FrameSelectionListener selListener = new FrameSelectionListener();
      addMouseListener(selListener);
      addMouseMotionListener(selListener);
    }

    @Override
    public Dimension getPreferredSize()
    {
      Rectangle imageBounds = getWaveImageBounds();
      return new Dimension(imageBounds.width, m_canvasScrollPane.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g)
    {
      drawWave((Graphics2D) g);
      m_canvasScrollPane.revalidate();
    }

    private void drawWave(Graphics2D gc)
    {

      if (m_audioFile != null)
      {
        int maxSampleVal = 255;

        int beginX = m_canvasScrollPane.getHorizontalScrollBar().getValue();
        int endX = getWaveImageBounds().width;

        int canvasHeight = m_canvasScrollPane.getHeight();

        if (m_canvasScrollPane.getHorizontalScrollBar().isVisible())
        {
          canvasHeight = canvasHeight - m_canvasScrollPane.getHorizontalScrollBar().getHeight();
        }

        m_waveImage = new BufferedImage(endX, canvasHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D imageGC = m_waveImage.createGraphics();
        imageGC.setBackground(Color.BLACK);

        if (m_audioFile.getBitsPerSample() > 8)
        {
          maxSampleVal = 32767;
        }

        int channels = m_audioFile.getChannels();

        double sampleData = 0.0;
        double maxY = 100.0;
        double center = (canvasHeight - 40) / 2;
        double dy1 = 100.0;
        double dy2 = 100.0;

        for (int c = 0; c < (Math.min(channels, 4)); c++)
        {
          imageGC.setColor(m_channelCols[c]);

          for (int i = beginX; i < endX; i++)
          {
            sampleData = getValue(100.0 / (double) m_zoom, i, c);

            sampleData = sampleData / (double) maxSampleVal;

            maxY = 200.0;
            //    center = 100.0;
            dy2 = 100.0;

            if (sampleData > 0)
            {
              dy1 = center + (maxY * sampleData);
              dy2 = center - (maxY * (Math.abs(sampleData)));
            }

            imageGC.drawLine(i - beginX, (int) Math.round(dy1), (i + 1) - beginX, (int) Math.round(dy2));
          }
        }

        // and some TrackInfos:
        drawTrackInfos(imageGC, beginX, endX);

        // and the Time-Scale
        drawTimeScale(imageGC, beginX, endX);

        if (m_clipStatus != null)
        {
          drawSelection(imageGC);
        }

        if (m_clipStatus.isSelection())
        {
          drawPlayerProgress(imageGC);
        }

        gc.drawImage(m_waveImage, 0, 0, Color.BLACK, null);

      }

    }

    private void drawTrackInfos(Graphics2D gc, int beginX, int endX)
    {
      int nextTrackX = 0;
      m_trackPosMap.clear();

      List<Track> trackList = m_audioFile.getTrackList();
      for (int i = 0; i < trackList.size(); i++)
      {
        Track tempTrack = trackList.get(i);
        TrackInfo tempInfo = m_trackInfoList.get(i);

        if ((nextTrackX >= beginX) && (nextTrackX < endX))
        {
          int x1 = nextTrackX - beginX;

          int y1 = 0;
          int y2 = getHeight();

          gc.setColor(Color.CYAN);

          Rectangle lineRect = new Rectangle(x1, y1, 1, y2);
          gc.drawLine(x1, y1, x1 + 1, y2);

          gc.drawString(tempTrack.getName(), x1 + 5, y2 - 30);
          m_trackPosMap.put(tempInfo, lineRect);

          tempInfo.m_startPos = lineRect.x - (tempTrack.getTime().getNumberOfSeconds() * getZoomFactor());
          tempInfo.m_endArea = lineRect;

        }
        nextTrackX = nextTrackX + (tempTrack.getTime().getNumberOfSeconds() * getZoomFactor());

        // beim letzten Stueck noch das Ende Zeichnen
        if (tempTrack == trackList.get(trackList.size() - 1))
        {
          int x1 = nextTrackX - beginX;

          int y1 = 0;
          int y2 = getHeight();

          gc.setColor(Color.CYAN);

          gc.drawLine(x1, y1, x1 + 1, y2);
        }
      }
    }

    private void drawTimeScale(Graphics2D gc, int beginX, int endX)
    {
      int zoomFactor = getZoomFactor();
      int lastSecondDrawn = -1;
      int lastMinuteDrawn = -1;

      for (int i = 0; i < endX; i++)
      {
        boolean isMinute = false;
        boolean drawSecond = false;
        gc.setColor(Color.WHITE);

        DvTime time = getTimeAtFrame(getFrameAtPixel(i));
        int second = time.getSeconds();

        int y1 = 4;
        int y2 = 0;

        gc.drawLine(i, y1, i + 1, y1 + 1);

        if (second % 10 == 0)
        {
          y2 = 8;
        }

        if ((lastSecondDrawn != second) && (second % 20 == 0 && zoomFactor > 2))
        {
          lastSecondDrawn = second;
          drawSecond = true;
        }

        if (second % 60 == 0)
        {
          y2 = 12;

          if (lastMinuteDrawn != time.getMinutes())
          {
            isMinute = true;
            lastMinuteDrawn = time.getMinutes();
          }
        }

        if (y2 > 0)
        {
          gc.drawLine(i, y1, i, y2);
        }

        if (isMinute || drawSecond)
        {
          gc.drawString(time.toString(), i, y2 + 16);

        }

      }
    }

    private void drawSelection(Graphics2D gc)
    {
      int y1 = 0;
      int y2 = getHeight();

      Color col = new Color(130, 150, 200);

      int beginX = getPixelAtFrame(m_clipStatus.getStartFrame());
      int endX = getPixelAtFrame(m_clipStatus.getEndFrame());

      gc.setXORMode(col);
      // gc.setBackground(col);?
      gc.fillRect(beginX, y1, endX - beginX, y2);
    }

    private void drawPlayerProgress(Graphics2D gc)
    {
      int x1 = getPixelAtFrame(m_clipStatus.getCurrentFrame());

      int y1 = 0;
      int y2 = getHeight();

      // gc.setXORMode(Color.BLACK);
      gc.setColor(Color.WHITE);

      // System.out.println("Zeichne an x1=" + x1 + " von " + y1 + " bis " + y2);
      gc.drawLine(x1, y1, x1, y2);
    }

    private void drawPlayerProgress()
    {
      Graphics2D gc = m_waveImage.createGraphics();

      drawPlayerProgress(gc);

    }

  }

  private class FrameSelectionListener extends MouseAdapter implements MouseMotionListener
  {

    @Override
    public void mousePressed(MouseEvent e)
    {
      if (e.getButton() == 1 && m_audioFile != null)
      {
        if (m_player.isPlaying() == false)
        {
          startFrameSelection(e.getPoint().x);
        }
      }
      else
      {
        clearFrameSelection();
      }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      m_selecting = false;
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
      if (m_selecting)
      {
        editFrameSelection(e.getPoint().x);

      }
    }

  }
}
