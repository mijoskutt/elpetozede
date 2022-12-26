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
package abz.kamirez.elpetozede.domain.material;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.IWaveWriter;
import abz.kamirez.elpetozede.domain.service.ProgressInfo;
import abz.kamirez.elpetozede.domain.service.WriterUtil;
import ch.laoe.audio.save.ASave;
import ch.laoe.audio.save.ASavePcmSigned16BitBigEndian;
import ch.laoe.audio.save.ASavePcmSigned16BitLittleEndian;
import ch.laoe.audio.save.ASavePcmSigned8Bit;
import ch.laoe.audio.save.ASavePcmUnsigned16BitBigEndian;
import ch.laoe.audio.save.ASavePcmUnsigned16BitLittleEndian;
import ch.laoe.audio.save.ASavePcmUnsigned8Bit;
import ch.laoe.audio.save.ASaveUlaw8Bit;
import ch.laoe.clip.AChannel;
import ch.laoe.clip.ALayer;

/**
 * Writer based on the LAOE-Project
 * @author michael
 *
 */
public class LAOEWaveWriter implements IWaveWriter
{
  private List<ASave> m_writers;
  protected List<String> m_previousTrackFiles;

  public LAOEWaveWriter()
  {
    m_previousTrackFiles = new ArrayList<String>();
  }

  public void writeTrackFiles(String prefix, IAudioFile sourcefile, String path) throws IOException
  {
    List<Track> trackList = sourcefile.getTrackList();
    WaveData data = ((AudioFile) sourcefile).getWaveData();

    int startSecond = 0;
    m_progInfo = new ProgressInfo(this, 0, "Start writing Track-Files...");
    reportProgress();

    int progressPart = (100 / trackList.size()) - 1;

    for (int i = 0; i < trackList.size(); i++)
    {
      Track tempTrack = trackList.get(i);
      int newPerc = m_progInfo.getPercentage() + progressPart;
      m_progInfo.setPercentage(newPerc);
      m_progInfo.setMessage("Writing Track \"" + tempTrack.getName() + "\"");

      reportProgress();

      writeTrackFile(prefix + String.format("%02d", i + 1) + "_", startSecond, tempTrack, data, path);
      System.gc();
      startSecond = startSecond + tempTrack.getTime().getNumberOfSeconds();
    }

  }

  protected void writeTrackFile(String prefix, int startSecond, Track track, WaveData sourceData, String path)
      throws IOException
  {
    ASave writer = createASave(sourceData);
    int trackDuration = track.getTime().getNumberOfSeconds();
    int numChannels = sourceData.getChannels();

    int trackBeginSample = getSampleNumber(startSecond, sourceData);
    int trackEndSample = getSampleNumber(startSecond + trackDuration, sourceData);
    int numTrackSamples = trackEndSample - trackBeginSample;

    ALayer trackLayer = new ALayer(numChannels, numTrackSamples);

    for (int i = 0; i < numChannels; i++)
    {
      AChannel tempTrackChn = trackLayer.getChannel(i);
      AChannel sourceChannel = sourceData.getLayer().getChannel(i);
      int tempPos = 0;
      float tempSourceVal = 0.0f;

      for (int n = 0; n < numTrackSamples; n++)
      {
        tempPos = n + trackBeginSample;
        tempSourceVal = sourceChannel.getSample(tempPos);

        tempTrackChn.sample[n] = tempSourceVal;
      }
    }

    writer.setLayer(trackLayer);

    String trackFileName = path + File.separator + prefix + createNormalizedFileName(track) + ".wav";

    File trackFile = new File(trackFileName);

    if (trackFile.exists())
    {
      m_previousTrackFiles.add(trackFileName);
    }

    writer.setFile(trackFile);
    writer.setAudioFileFormat(new AudioFileFormat(AudioFileFormat.Type.WAVE, sourceData.getFormat(), numTrackSamples));
    writer.write();
    writer.close();

    encode(track, trackFileName);

    m_writers = null;
    writer = null;
    trackLayer = null;
    trackFile = null;

  }

  protected void encode(Track track, String trackFileNameWave)
  {

  }

  protected String createNormalizedFileName(Track track)
  {
    String rueckgabe = WriterUtil.createNormalizedFileName(track);
    return rueckgabe;
  }

  protected ASave createASave(WaveData sourceData)
  {
    ASave rueckgabe = null;
    AudioFormat af = sourceData.getFormat();

    List<ASave> writers = getWriters();

    for (int i = 0; i < writers.size() && rueckgabe == null; i++)
    {
      ASave l = writers.get(i);
      if (l.supports(af))
      {
        rueckgabe = l.duplicate();
      }
    }

    return rueckgabe;
  }

  protected int getSampleNumber(int second, WaveData sourceData)
  {
    int rueckgabe = 0;
    rueckgabe = second * sourceData.getBitsPerSample();

    return rueckgabe;
  }

  protected List<ASave> getWriters()
  {
    if (m_writers == null)
    {
      m_writers = new ArrayList<ASave>();
      m_writers.add(new ASavePcmUnsigned8Bit());
      m_writers.add(new ASavePcmSigned8Bit());
      m_writers.add(new ASavePcmUnsigned16BitLittleEndian());
      m_writers.add(new ASavePcmUnsigned16BitBigEndian());
      m_writers.add(new ASavePcmSigned16BitLittleEndian());
      m_writers.add(new ASavePcmSigned16BitBigEndian());
      m_writers.add(new ASaveUlaw8Bit());
    }

    return m_writers;
  }

  protected void reportProgress()
  {
    List<IProgressListener> listeners = getListeners();

    for (IProgressListener tempListener : listeners)
    {
      tempListener.progressReported(m_progInfo);
    }
  }

  public void addProgressListener(IProgressListener progListener)
  {
    if (getListeners().contains(progListener) == false)
    {
      getListeners().add(progListener);
    }

  }

  public void removeProgressListener(IProgressListener progListener)
  {
    getListeners().remove(progListener);
  }

  private List<IProgressListener> getListeners()
  {
    if (m_progressListeners == null)
    {
      m_progressListeners = new ArrayList<IProgressListener>();
    }

    return m_progressListeners;
  }

  protected List<IProgressListener> m_progressListeners;
  protected ProgressInfo m_progInfo;

}
