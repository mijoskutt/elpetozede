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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Album implements IModellChild
{
  private IModell m_parent;
  private List<Track> m_tracks;
  private String m_title;
  private String m_interpret;
  private DvYear m_publishYear;
  private String m_medium;
  private String m_label;

  public Album(IModell parent, String title, String interpret, DvYear year)
  {
    this(parent, new ArrayList<Track>(), title, interpret, year);
  }

  public Album(IModell parent, List<Track> tracks, String title, String interpret, DvYear year)
  {
    super();
    m_tracks = tracks;
    m_title = title;
    m_interpret = interpret;
    m_publishYear = year;
    m_parent = parent;
    m_medium = "";
    m_label = "";
  }

  public IModell getParent()
  {
    return m_parent;
  }

  public void setParent(IModell parentModell)
  {
    m_parent = parentModell;
  }

  public List<Track> getTracks()
  {
    return m_tracks;
  }

  public void addTrack(Track newTrack)
  {
    assert newTrack != null : "newTrack != null";

    if (m_tracks.contains(newTrack) == false)
    {
      m_tracks.add(newTrack);
    }
    newTrack.setParent(this);
  }

  public void removeTrack(Track removedTrack)
  {
    m_tracks.remove(removedTrack);
  }

  public void insertTrack(int index, Track newTrack)
  {
    assert newTrack != null : "newTrack != null";

    if (m_tracks.contains(newTrack) == false)
    {
      m_tracks.add(index, newTrack);
    }
    newTrack.setParent(this);
  }

  public boolean moveTrack(Track moveTrack, boolean moveUp)
  {
    List<Track> trackList = getTracks();
    int selTrackIdx = trackList.indexOf(moveTrack);

    boolean canMove = selTrackIdx < trackList.size() - 1;
    int offset = 1;

    if (moveUp)
    {
      offset = -1;
      canMove = selTrackIdx > 0;
    }

    if (canMove)
    {
      Collections.swap(trackList, selTrackIdx, selTrackIdx + offset);

    }

    return canMove;
  }

  public boolean moveTrack(List<Track> trackList, Track moveTrack, boolean moveUp)
  {
    int selTrackIdx = trackList.indexOf(moveTrack);

    boolean canMove = selTrackIdx < trackList.size() - 1;
    int offset = 1;

    if (moveUp)
    {
      offset = -1;
      canMove = selTrackIdx > 0;
    }

    if (canMove)
    {
      Collections.swap(trackList, selTrackIdx, selTrackIdx + offset);

    }

    return canMove;
  }

  public int getTrackCount()
  {
    return m_tracks.size();
  }

  public String getTitle()
  {
    return m_title;
  }

  public void setTitle(String title)
  {
    m_title = title.trim();
  }

  public DvYear getPublishYear()
  {
    return m_publishYear;
  }

  public void setPublishYear(DvYear publishYear)
  {
    m_publishYear = publishYear;
  }

  public String getInterpret()
  {
    return m_interpret;
  }

  public void setInterpret(String interpret)
  {
    m_interpret = interpret.trim();
  }

  @Override
  public String toString()
  {
    String mediumLabelStr = "";
    
    if (getLabel().length() > 0)
    {
      mediumLabelStr = "(" + getLabel();
    }
    
    if (getMedium().length() > 0)
    {
      if (mediumLabelStr.length() > 0)
      {
        mediumLabelStr = mediumLabelStr + ", " + getMedium();
      }
      else
      {
        mediumLabelStr = getMedium();
      }
    }
    
    if (mediumLabelStr.length() > 0)
    {
      mediumLabelStr = mediumLabelStr + ")";
    }
    
    String rueckgabe = getInterpret() + ": " + getTitle() + " " + mediumLabelStr;
    return rueckgabe;
  }

  public String getMedium()
  {
    return m_medium;
  }

  public void setMedium(String medium)
  {
    if (medium == null || (medium.equals("null")))
    {
      medium = "";
    }
    m_medium = medium.trim();
  }

  public String getLabel()
  {
    return m_label;
  }

  public void setLabel(String label)
  {
    if (label == null || (label.equals("null")))
    {
      label = "";
    }

    m_label = label.trim();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_interpret == null) ? 0 : m_interpret.hashCode());
    result = prime * result + ((m_label == null) ? 0 : m_label.hashCode());
    result = prime * result + ((m_medium == null) ? 0 : m_medium.hashCode());
    result = prime * result + ((m_parent == null) ? 0 : m_parent.hashCode());
    result = prime * result + ((m_publishYear == null) ? 0 : m_publishYear.hashCode());
    result = prime * result + ((m_title == null) ? 0 : m_title.hashCode());
    result = prime * result + ((m_tracks == null) ? 0 : m_tracks.hashCode());
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
    Album other = (Album) obj;
    if (m_interpret == null)
    {
      if (other.m_interpret != null)
        return false;
    }
    else if (!m_interpret.equals(other.m_interpret))
      return false;
    if (m_label == null)
    {
      if (other.m_label != null)
        return false;
    }
    else if (!m_label.equals(other.m_label))
      return false;
    if (m_medium == null)
    {
      if (other.m_medium != null)
        return false;
    }
    else if (!m_medium.equals(other.m_medium))
      return false;
    if (m_parent == null)
    {
      if (other.m_parent != null)
        return false;
    }
    else if (!m_parent.equals(other.m_parent))
      return false;
    if (m_publishYear == null)
    {
      if (other.m_publishYear != null)
        return false;
    }
    else if (!m_publishYear.equals(other.m_publishYear))
      return false;
    if (m_title == null)
    {
      if (other.m_title != null)
        return false;
    }
    else if (!m_title.equals(other.m_title))
      return false;
    if (m_tracks == null)
    {
      if (other.m_tracks != null)
        return false;
    }
    else if (!m_tracks.equals(other.m_tracks))
      return false;
    return true;
  }

}
