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
package abz.kamirez.elpetozede.service.player.wave;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import abz.kamirez.elpetozede.domain.material.ClipStatusModel;
import abz.kamirez.elpetozede.domain.material.IAudioFile;
import abz.kamirez.elpetozede.domain.service.IAudioPlayer;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.IProgressReportingTask;
import abz.kamirez.elpetozede.domain.service.ProgressInfo;

public class SimpleWavePlayer implements IAudioPlayer, IProgressReportingTask
{
  private static final int EXTERNAL_BUFFER_SIZE = 65536;

  private ClipStatusModel m_clipStatusModel;
  private SourceDataLine m_clip;
  private PlayThread m_playerthread;

  private List<IProgressListener> m_progressListeners;
  private ProgressInfo m_progInfo;

  private AudioInputStream m_audioStream;

  public SimpleWavePlayer()
  {
    m_progressListeners = new ArrayList<IProgressListener>();
  }

  public void addProgressListener(IProgressListener progListener)
  {
    if (m_progressListeners.contains(progListener) == false)
    {
      m_progressListeners.add(progListener);
    }
  }

  public synchronized void removeProgressListener(IProgressListener progListener)
  {
    m_progressListeners.remove(progListener);
  }

  private synchronized void reportProgress()
  {
    List<IProgressListener> listeners = getListeners();

    for (IProgressListener tempListener : listeners)
    {
      tempListener.progressReported(m_progInfo);
    }
  }

  private List<IProgressListener> getListeners()
  {
    return m_progressListeners;
  }

  public void playAudioFile(IAudioFile file, ClipStatusModel clipStatusModel) throws IOException
  {
    m_clipStatusModel = clipStatusModel;
    String errorMsg = "";

    try
    {
      m_audioStream = AudioSystem.getAudioInputStream(file.getFile());
      AudioFormat format = m_audioStream.getFormat();

      DataLine.Info info = new DataLine.Info(SourceDataLine.class, m_audioStream.getFormat(),
        ((int) m_audioStream.getFrameLength() * format.getFrameSize()));

      m_progInfo = new PlayerProgressInfo(this, 0, "Start playing...");

      m_clip = (SourceDataLine) AudioSystem.getLine(info);
      m_clip.open(format, EXTERNAL_BUFFER_SIZE);

      m_clip.addLineListener(new LineListener()
      {
        public void update(LineEvent event)
        {
          System.out.println("Level: " + m_clip.getLevel());
          System.out.println("Active: " + m_clip.isActive());
          System.out.println("Frame-Position: " + m_clip.getFramePosition());
          System.out.println("Buffer-Size: " + m_clip.getBufferSize());
          System.out.println("Running: " + m_clip.isRunning());

          if (event.getType() == LineEvent.Type.STOP)
          {
            System.out.println("stop!");
            event.getLine().close();
            // System.exit(0);
          }
        }
      });

      m_playerthread = new PlayThread(m_clipStatusModel.getStartFrame() * format.getFrameSize(),
        m_clipStatusModel.getEndFrame() * format.getFrameSize());
      m_playerthread.start();

    }
    catch (UnsupportedAudioFileException e)
    {
      e.printStackTrace();
      errorMsg = e.getMessage();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      errorMsg = e.getMessage();
    }
    catch (LineUnavailableException e)
    {
      e.printStackTrace();
      errorMsg = e.getMessage();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      errorMsg = e.getMessage();
    }

    if (errorMsg.length() > 0)
    {
      throw new IOException(errorMsg);
    }
  }

  public void stop()
  {
    if (m_playerthread != null)
    {
      m_playerthread.stopClip();
      m_playerthread.interrupt();
    }
  }

  public boolean isPlaying()
  {
    return m_playerthread != null && m_playerthread.isAlive();
  }

  private class PlayThread extends Thread
  {
    private int m_startByte;
    private int m_lastByte;

    public PlayThread(int startByte, int lastByte)
    {
      super();
      m_startByte = startByte;
      m_lastByte = lastByte;
    }

    @Override
    public void run()
    {
      try
      {
        m_audioStream.skip(m_startByte);
        m_clip.start();

        int nBytesRead = 0;
        int pos = m_startByte;

        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        while (nBytesRead != -1 && pos < m_lastByte)
        {
          try
          {
            int len = abData.length;

            if (pos + abData.length > m_lastByte)
            {
              len = pos + abData.length - m_lastByte;
              m_clipStatusModel.setFinished(true);
            }

            nBytesRead = m_audioStream.read(abData, 0, len);

            pos = pos + len;
          }
          catch (InterruptedIOException intioex)
          {
            System.out.println("PlayThread interrupted");
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
          if (nBytesRead >= 0)
          {
            m_clip.write(abData, 0, nBytesRead);
            m_clipStatusModel
              .setCurrentFrame(m_startByte / m_clip.getFormat().getFrameSize() + m_clip.getFramePosition());
            reportProgress();
          }
        }

        // m_clip.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      finally
      {
        m_clip.drain();
        m_clip.close();
      }
    }

    public void stopClip()
    {
      if (m_clip != null)
      {
        if (m_clip.isRunning())
        {
          m_clip.stop();
          m_clip.close();
        }
      }
    }
  }
}
