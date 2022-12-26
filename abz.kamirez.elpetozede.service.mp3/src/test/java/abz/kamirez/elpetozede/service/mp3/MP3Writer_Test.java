package abz.kamirez.elpetozede.service.mp3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.Track;

public class MP3Writer_Test
{
  private String m_testAudioFileName = "test_data" + File.separator + "1_1_Badlands.wav";

  private String m_testOutputFileName = "test_output" + File.separator + "Badlands.mp3";

  private AlbumProject m_project;

  private AudioFile m_testAudioFile;

  @Before
  public void setUp() throws Exception
  {

    File outputFile = new File("m_testOutputFileName");

    if (outputFile.exists())
    {
      outputFile.delete();
    }

    m_project = new AlbumProject();

    m_project.setProjectName("Test-Projekt");
    Album album = m_project.getAlbum();
    album.setInterpret("Bruce Springsteen");
    album.setTitle("Badlands");

    m_testAudioFile = new AudioFile(m_project);
    m_testAudioFile.setFile(new File(m_testAudioFileName));
    m_project.addAudioFile(m_testAudioFile);

    List<Track> audioFileTracks = new ArrayList<Track>();
    Track tempTrack = new Track(album, "Badlands", album.getInterpret(), DvTime.valueOf(4, 6));
    album.addTrack(tempTrack);

    audioFileTracks.add(tempTrack);

    m_testAudioFile.setTrackList(audioFileTracks);

  }

  @Test
  public void test_speichern_als_mp3()
  {
    MP3Writer writer = new MP3Writer("/opt/csw/bin/lame");

    try
    {
      writer.writeTrackFiles("", m_testAudioFile, "test_output");
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
