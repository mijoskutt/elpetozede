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

public class AlbumProject implements IModell
{
  private String m_projectName;
  private Album m_album;
  private List<IAudioFile> m_audioFiles;
  private String m_filePath;

 
  public AlbumProject()
  {
    m_audioFiles = new ArrayList<IAudioFile>();
    m_album = new Album(this, "new Album", "Unknown", DvYear.getNullValue());
    m_projectName = "new Project";
  }
  
  public String getProjectName()
  {
    return m_projectName;
  }

  public void setProjectName(String projectName)
  {
    m_projectName = projectName;
  }

  public Album getAlbum()
  {
    return m_album;
  }

  public void setAlbum(Album album)
  {
    m_album = album;
    m_album.setParent(this);
  }

  public List<IAudioFile> getAudioFiles()
  {
    return m_audioFiles;
  }

  public void addAudioFile(IAudioFile newFile)
  {
    m_audioFiles.add(newFile);
  }
  
  public void removeAudioFile(IAudioFile removeFile)
  {
    removeFile.setParent(null);
    m_audioFiles.remove(removeFile);
  }

  @Override
  public String toString()
  {
    return getProjectName();
  }
  

  public IModell getParent()
  {
    return null;
  }
  
  public String getFilePath()
  {
    return m_filePath;
  }

  public void setFilePath(String filePath)
  {
    m_filePath = filePath;
  }
  
  public boolean moveTrack(Track moveTrack, boolean moveUp)
  {
	  boolean result = true;
	  IAudioFile audioFile = getAudioFileWithTrack(moveTrack);

	  if (audioFile != null)
	  {
		  result =  moveTrack(audioFile.getTrackList(), moveTrack, moveUp); 
	  }
	  
	  if (result)
	  {
		  List<Track> trackList = getAlbum().getTracks();
		  moveTrack(trackList, moveTrack, moveUp);
	  }
	  
	  return result;
  }
  
  public void removeTrack(Track deleteTrack) 
  {
	  IAudioFile audioFile = getAudioFileWithTrack(deleteTrack);

	  if (audioFile != null)
	  {
		  audioFile.getTrackList().remove(deleteTrack); 
	  }
	  
	  getAlbum().removeTrack(deleteTrack);
  }
	  
  
  public boolean moveTrack(List<Track> trackList, Track moveTrack, boolean moveUp)
  {
	  int selTrackIdx = trackList.indexOf(moveTrack);

	  boolean canMove = selTrackIdx < trackList.size() -1;
	  int offset = 1;

	  if (moveUp)
	  {
		  offset = -1;
		  canMove = selTrackIdx > 0;
	  }

	  if (canMove)
	  {
		  Collections.swap(trackList, selTrackIdx, selTrackIdx+offset);
	  }

	  return canMove;
  }
  
  public IAudioFile getAudioFileWithTrack(Track track)
  {
	 IAudioFile result = null;
	 
	 for (IAudioFile audioFile : m_audioFiles)
	 {
	   if (audioFile.getTrackList().contains(track))
	   {
		   result = audioFile;
	   }
	 }
	 
	 return result;
  }


}
