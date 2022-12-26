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
package abz.kamirez.elpetozede.service.mp3;

import java.io.File;
import java.io.IOException;

import abz.kamirez.elpetozede.domain.material.LAOEWaveWriter;
import abz.kamirez.elpetozede.domain.material.Track;

public class MP3Writer extends LAOEWaveWriter
{
  private String m_lameFullPath;
  private boolean m_deleteWavFile = false;

  public MP3Writer(String lameFullPath)
  {
    m_lameFullPath = lameFullPath;

    File lameFile = new File(lameFullPath);

    try
    {
      if (lameFile.isAbsolute() == false)
      {
        m_lameFullPath = lameFile.getCanonicalPath();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

  }

  public void setDeleteWavFile(boolean deleteWavFile)
  {
    m_deleteWavFile = deleteWavFile;
  }

  @Override
  protected void encode(Track track, String trackFileNameWave)
  {

    Konvertierer konverter = new Konvertierer(m_lameFullPath, track);
    konverter.setInputFileName(trackFileNameWave);

    konverter.setOutputFileName(trackFileNameWave.replace(".wav", ".mp3"));
    try
    {
      m_progInfo.setMessage("Konvertiere " + track.getName() + " nach MP3, bitte etwas Geduld...");
      reportProgress();
      Process konvProc = konverter.konvert();

      OutputParseThread stdOutThread = new OutputParseThread(konvProc.getInputStream(),
        new ProgInfoFortschrittsLogger());
      OutputParseThread stdErrThread = new OutputParseThread(konvProc.getErrorStream(),
        new ProgInfoFortschrittsLogger());
      stdErrThread.start();
      stdOutThread.start();
      //  System.out.println(konvProc.getErrorStream());
      int exitCode = konvProc.waitFor();

      if (exitCode == 0)
      {
        m_progInfo.setMessage("Konvertierung von " + track.getName() + "  beendet!");
      }
      else
      {
        m_progInfo.setMessage("Bei der Konvertierung von " + track.getName() + "  gab es leider einen Fehler!");
      }
      reportProgress();

      if (m_deleteWavFile && m_previousTrackFiles.contains(trackFileNameWave) == false)
      {
        File wavFile = new File(trackFileNameWave);
        wavFile.delete();
      }
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private class ProgInfoFortschrittsLogger implements IFortschrittsLogger
  {

    @Override
    public void log(int prozent, String fortschrittsStr)
    {
      if (prozent % 10 == 0)
      {
        m_progInfo.setMessage(fortschrittsStr);
        reportProgress();
      }
    }

  }

}
