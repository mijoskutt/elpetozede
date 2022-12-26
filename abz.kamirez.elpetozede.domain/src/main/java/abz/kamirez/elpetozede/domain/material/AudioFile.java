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

import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFile implements IAudioFile, IModellChild, ISampleData
{
  private IModell m_parent;
  private File m_file;
  private WaveData m_waveData;
  private List<Track> m_trackList;
  private int m_startSample;

  public AudioFile(IModell parent)
  {
    m_parent = parent;
    m_trackList = new ArrayList<Track>();
    m_startSample = 0;
  }

  public IModell getParent()
  {
    return m_parent;
  }

  public void setParent(IModell parentModell)
  {
    m_parent = parentModell;
  }

  public void setFile(File file) throws UnsupportedAudioFileException, IOException
  {
    m_file = file;
    m_waveData = new WaveData(file);
  }

  public File getFile()
  {
    return m_file;
  }

  public String getName()
  {
    String rueckgabe = "no file";

    if (m_file != null)
    {
      rueckgabe = m_file.getName();
    }

    return rueckgabe;
  }

  @Override
  public String toString()
  {
    return getName();
  }

  public int getBitsPerSample()
  {
    return m_waveData.getBitsPerSample();
  }

  public int getSampleWidth()
  {
    return m_waveData.getSampleWidth();
  }

  public long getSamples()
  {
    return m_waveData.getSamples();
  }

  public int getChannels()
  {
    int rueckgabe = m_waveData.getChannels();
    return rueckgabe;
  }

  public float getValue(int channelIndex, int position)
  {
    return m_waveData.getValue(channelIndex, position);
  }

  public int getLengthInSeconds()
  {
    int rueckgabe = 0;
    int samples = (int) getSamples();

    rueckgabe = samples / getBitsPerSample();
    return rueckgabe;
  }

  public List<Track> getTrackList()
  {
    return m_trackList;
  }

  public void setTrackList(List<Track> trackList)
  {
    m_trackList = trackList;
  }

  public Track getTrack(int second)
  {
    Track rueckgabe = null;
    int secondCounter = 0;

    for (int i = 0; i < m_trackList.size() && rueckgabe == null; i++)
    {
      Track tempTrack = m_trackList.get(i);
      int tempTrackDuration = tempTrack.getTime().getNumberOfSeconds();

      if (second < secondCounter + tempTrackDuration)
      {
        rueckgabe = tempTrack;
      }
      else
      {
        secondCounter = secondCounter + tempTrackDuration;
      }
    }

    return rueckgabe;
  }

  public DvTime getTotalTime(Track track)
  {
    DvTime rueckgabe = DvTime.getNullValue();

    int trackIdx = getTrackList().indexOf(track);

    if (trackIdx > -1)
    {
      int totalSeconds = 0;

      for (int i = 0; i <= trackIdx; i++)
      {
        Track tempTrack = getTrackList().get(i);
        totalSeconds = totalSeconds + tempTrack.getTime().getNumberOfSeconds();
      }

      rueckgabe = DvTime.valueOf(totalSeconds);
    }

    return rueckgabe;
  }

  public int getStartSample()
  {
    return m_startSample;
  }

  public void setStartSample(int startSample)
  {
    m_startSample = startSample;
  }

  public WaveData getWaveData()
  {
    return m_waveData;
  }

  public float[] getValueCopy(int channelIndex, int startSample, int endSample)
  {
    return m_waveData.getValueCopy(channelIndex, startSample, endSample);
  }
}
