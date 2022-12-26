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

import abz.kamirez.elpetozede.domain.material.ISampleData;

public class DataChunk implements ISampleData
{

  private int m_offSet;
  private float[][] m_data;

  public DataChunk(int offSet, float[] channel1, float[] channel2)
  {
    super();
    m_offSet = offSet;
    m_data = new float[][]
    { channel1, channel2 };
  }

  @Override
  public int getChannels()
  {
    return 2;
  }

  @Override
  public float getValue(int channelIndex, int position)
  {
    return m_data[channelIndex][position - m_offSet];
  }

}
