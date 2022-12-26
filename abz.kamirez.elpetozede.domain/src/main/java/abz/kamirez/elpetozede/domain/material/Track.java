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

public class Track implements IModellChild
{
  private IModell m_parent;
  private String m_name;
  private String m_artist;
  private DvTime m_time;
  private String m_fileName;

  public Track(IModell parent, String name, String artist, DvTime time)
  {
    super();
    m_name = name;
    m_artist = artist;
    m_parent = parent;
    m_time = time;
    m_fileName = "";
  }
  
 
  public IModell getParent()
  {
    return m_parent;
  }
  

  public void setParent(IModell parentModell)
  {
    m_parent = parentModell;
  }

  public String getName()
  {
    return m_name;
  }

  public void setName(String name)
  {
    m_name = name.trim();
  }

  public String getArtist()
  {
    return m_artist;
  }

  public void setArtist(String artist)
  {
    m_artist = artist.trim();
  }

  public DvTime getTime()
  {
    return m_time;
  }

  public void setTime(DvTime time)
  {
    m_time = time;
  }

  @Override
  public String toString()
  {
    return getName() + " " + getTime().toString();
  }
  
  public String getFileName()
  {
    return m_fileName;
  }
  
  public void setFileName(String fileName)
  {
    m_fileName = fileName;
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_artist == null) ? 0 : m_artist.hashCode());
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result + ((m_time == null) ? 0 : m_time.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Track other = (Track) obj;
    if (m_artist == null)
    {
      if (other.m_artist != null)
        return false;
    }
    else if (!m_artist.equals(other.m_artist))
      return false;
    if (m_name == null)
    {
      if (other.m_name != null)
        return false;
    }
    else if (!m_name.equals(other.m_name))
      return false;
    if (m_time == null)
    {
      if (other.m_time != null)
        return false;
    }
    else if (!m_time.equals(other.m_time))
      return false;
    return true;
  }

}
