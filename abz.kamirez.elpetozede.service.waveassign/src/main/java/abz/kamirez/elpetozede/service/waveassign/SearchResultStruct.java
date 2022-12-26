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
package abz.kamirez.elpetozede.service.waveassign;

public class SearchResultStruct implements Comparable<SearchResultStruct>
{
  public int m_startSample;
  public int m_silenceSamples;
  public float m_maxVolumeValue;

  public SearchResultStruct()
  {
    m_startSample = -1;
    m_silenceSamples = -1;
    m_maxVolumeValue = -1.0f;
  }

  @Override
  public int compareTo(SearchResultStruct o)
  {
    int result = 0;
    int longerSilence = (-1) * ((Integer) m_silenceSamples).compareTo(o.m_silenceSamples);
    result = longerSilence;

    return result;

  }

  @Override
  public String toString()
  {
    String rueckgabe = "m_startFrame=" + m_startSample + "\n" + "m_silenceSamples=" + m_silenceSamples + "\n"
        + "m_maxValue=" + m_maxVolumeValue;
    return rueckgabe;
  }
}
