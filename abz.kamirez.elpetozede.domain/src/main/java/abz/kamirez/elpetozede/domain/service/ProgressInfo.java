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

public class ProgressInfo implements IProgressInfo
{
  private IProgressReportingTask m_task;
  private int m_percentage;
  private String m_message;
  
  public ProgressInfo(IProgressReportingTask task, int percentage, String message)
  {
    super();
    m_task = task;
    m_percentage = percentage;
    m_message = message;
  }

  public String getMessage()
  {
    return m_message;
  }
  
  public void setMessage(String message)
  {
    m_message = message;
  }

  public int getPercentage()
  {
    return m_percentage;
  }
  
  public void setPercentage(int percentage)
  {
    if (percentage > 100)
    {
      percentage = 100;
    }
    m_percentage = percentage;
  }

 
  public IProgressReportingTask getTask()
  {
    return m_task;
  }
  


}
