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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import abz.kamirez.elpetozede.domain.service.IProgressInfo;
import ch.laoe.audio.AudioException;
import ch.laoe.audio.load.ALoad;
import ch.laoe.audio.load.ALoadFactory;
import ch.laoe.clip.AChannel;
import ch.laoe.clip.ALayer;

public class WaveData
{
  private ALayer m_layer;
  private int m_sampleWidth;
  private int m_bitsPerSample;
  private AudioFormat m_audioFormat;
  private IProgressInfo m_progInfo;

  public WaveData(File waveFile) throws UnsupportedAudioFileException, IOException
  {
    this(waveFile, null);
  }

  public WaveData(File waveFile, IProgressInfo progInfo) throws UnsupportedAudioFileException, IOException
  {
    read(waveFile, progInfo);
  }

  private void read(File waveFile, IProgressInfo progInfo) throws UnsupportedAudioFileException, IOException
  {
    try
    {
      ALoad waveData = ALoadFactory.create(waveFile);
      System.out.println("Channels: " + waveData.getChannels());
      System.out.println("Sample-Length: " + waveData.getSampleLength());
      System.out.println("Sample-Width: " + waveData.getSampleWidth());
      System.out.println("Sample-Rate: " + waveData.getSampleRate());
      m_sampleWidth = waveData.getSampleWidth();
      m_bitsPerSample = (int) waveData.getSampleRate();

      m_layer = new ALayer(waveData.getChannels(), waveData.getSampleLength());

      int s = 0;
      int d;

      while ((d = waveData.read(m_layer, s, 4000)) >= 0)
      {
        s += d;
      }

      waveData.close();

      // additional Infos direct from the Stream:
      AudioInputStream ais = AudioSystem.getAudioInputStream(waveFile);
      m_audioFormat = ais.getFormat();
      ais.close();
    }
    catch (AudioException audioEx)
    {
      throw new UnsupportedAudioFileException(audioEx.getMessage());
    }
  }

  public int getBitsPerSample()
  {
    return m_bitsPerSample;
  }

  public int getSampleWidth()
  {
    return m_sampleWidth;
  }

  public long getSamples()
  {
    return m_layer.getMaxSampleLength();
  }

  public int getChannels()
  {
    int rueckgabe = m_layer.getNumberOfChannels();
    return rueckgabe;
  }

  public float getValue(int channelIndex, int position)
  {
    AChannel channel = m_layer.getChannel(channelIndex);
    float rueckgabe = channel.getSample(position);
    return rueckgabe;
  }

  public AudioFormat getFormat()
  {
    return m_audioFormat;
  }

  public ALayer getLayer()
  {
    return m_layer;
  }

  protected float[] getValueCopy(int channelIndex, int startSample, int endSample)
  {
    AChannel channel = m_layer.getChannel(channelIndex);
    float[] rueckgabe = copyOfRange(channel.sample, startSample, endSample);
    return rueckgabe;
  }

  // erst ab Java 6
  public static float[] copyOfRange(float[] original, int from, int to)
  {
    int newLength = to - from;
    if (newLength < 0)
      throw new IllegalArgumentException(from + " > " + to);
    float[] copy = new float[newLength];
    System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
    return copy;
  }

}
