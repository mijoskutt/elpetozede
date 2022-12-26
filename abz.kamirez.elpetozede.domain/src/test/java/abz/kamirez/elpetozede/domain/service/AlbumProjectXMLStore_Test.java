/*******************************************************************************
 * Copyright (c) 2010 Michael Skutta.
 * 
 * This file is part of Elpetozede.
 * 
 * Elpetozede is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elpetozede is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elpetozede.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package abz.kamirez.elpetozede.domain.service;


import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.Track;

public class AlbumProjectXMLStore_Test
{
  private String m_testFileName = "test_data" + File.separator + "testproject.xml";
  private String m_testAudioFileName = "test_data" + File.separator + "test_audiofile.wav";
  
  private AlbumProject m_project;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
  }

  @Before
  public void setUp() throws Exception
  {
    m_project = new AlbumProject();
    
    m_project.setProjectName("JUnit Album-Project mät Ümlutenß");
    Album album = m_project.getAlbum();
    album.setInterpret("Kent Beck");
    album.setTitle("The test");
    
    AudioFile testAudioFile = new AudioFile(m_project);
    testAudioFile.setFile(new File(m_testAudioFileName));
    m_project.addAudioFile(testAudioFile);
    
    List<Track> audioFileTracks = new ArrayList<Track>();
    
    for (int i=0; i<8; i++)
    {
      Track tempTrack = new Track(album, "Test Track Nr " + (i+1), album.getInterpret(), DvTime.valueOf(i+1, 30+i));
      album.addTrack(tempTrack);
      
      if (i<4)
      {
        audioFileTracks.add(tempTrack);
      }
    }
    
    testAudioFile.setTrackList(audioFileTracks);
    
    File testFile = new File(m_testFileName);
    
    if (testFile.exists())
    {
      testFile.delete();
    }
  }
  
  @Test
  public void testSpeichern() throws Exception
  {
    AlbumProjectXMLStore store = new AlbumProjectXMLStore();
       
    store.saveProject(m_project, m_testFileName);
    File xmlFile  = new File(m_testFileName);
    
    assertTrue(xmlFile.exists());
   
    
    AlbumProject readProject = store.openProject(m_testFileName);
    
    assertEquals(m_project.getAlbum(), readProject.getAlbum());
  }

  @After
  public void tearDown() throws Exception
  {
  }

}
