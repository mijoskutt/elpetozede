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

public class ClipStatusModel
{

  private int m_startFrame;
  private int m_endFrame;
  private int m_currentFrame;
  private boolean m_isFinished = false;

  public ClipStatusModel()
  {
    reset();
  }
  
  public void setCurrentFrame(int currentFrame)
  {
    m_currentFrame = currentFrame;
  }

  public int getCurrentFrame()
  {
    return m_currentFrame;
  }
  
  public void setFinished(boolean isFinished)
  {
    m_isFinished = isFinished;
  }
  
  public boolean isFinished()
  {
    return m_isFinished;
  }
    
  public void setStartFrame(int startFrame)
  {
    m_startFrame = startFrame;
  }

  public int getStartFrame()
  {
    return m_startFrame;
  }

  public void setEndFrame(int endFrame)
  {
    m_endFrame = endFrame;
  }

  public int getEndFrame()
  {
    return m_endFrame;
  }
  
  public boolean isSelection()
  {
    return m_endFrame != 0;
  }
  
  public void reset()
  {
    m_startFrame = 0;
    m_endFrame = 0;
    
    m_currentFrame = 0;
    m_isFinished = false;
  }
}
