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
package abz.kamirez.elpetozede.domain.service;

public class SilenceFinderParameter
{
  private short m_secondsBeforeTrackEnd;
  private short m_secondsAfterTrackEnd;
  private int m_tolerance;
  private float m_maxValue;
  
  public SilenceFinderParameter(short secondsBeforeTrackEnd, short secondsAfterTrackEnd, int tolerance, float maxValue)
  {
    super();
    m_secondsBeforeTrackEnd = secondsBeforeTrackEnd;
    m_secondsAfterTrackEnd = secondsAfterTrackEnd;
    m_tolerance = tolerance;
    m_maxValue = maxValue;
  }

  public int getSecondsBeforeTrackEnd()
  {
    return m_secondsBeforeTrackEnd;
  }

  public void setSecondsBeforeTrackEnd(short seconsBeforeTrackEnd)
  {
    m_secondsBeforeTrackEnd = seconsBeforeTrackEnd;
  }

  public short getSecondsAfterTrackEnd()
  {
    return m_secondsAfterTrackEnd;
  }

  public void setSecondsAfterTrackEnd(short maxSearchRange)
  {
    m_secondsAfterTrackEnd = maxSearchRange;
  }

  public float getMaxValue()
  {
    return m_maxValue;
  }

  public void setMaxValue(float maxValue)
  {
    m_maxValue = maxValue;
  }

  public int getTolerance()
  {
    return m_tolerance;
  }

  public void setTolerance(int tolerance)
  {
    m_tolerance = tolerance;
  }
  
  
  
  
}
