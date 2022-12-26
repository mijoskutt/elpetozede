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

public class SilenceCalibrationParam
{
  private double m_maxVolumeValue;
  private double m_minPauseBetweenTracks;

  public SilenceCalibrationParam(double maxVolumeValue, double minPauseBetweenTracks)
  {
    super();
    m_maxVolumeValue = maxVolumeValue;
    m_minPauseBetweenTracks = minPauseBetweenTracks;
  }

  public double getMaxVolumeValue()
  {
    return m_maxVolumeValue;
  }

  public void setMaxVolumeValue(double maxVolumeValue)
  {
    m_maxVolumeValue = maxVolumeValue;
  }

  public double getMinPauseBetweenTracks()
  {
    return m_minPauseBetweenTracks;
  }

  public void setMinPauseBetweenTracks(double minPauseBetweenTracks)
  {
    m_minPauseBetweenTracks = minPauseBetweenTracks;
  }

}
