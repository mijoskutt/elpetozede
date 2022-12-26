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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.DvYear;
import abz.kamirez.elpetozede.domain.material.IAudioFile;
import abz.kamirez.elpetozede.domain.material.Track;

public class AlbumProjectXMLStore implements IAlbumProjectPersistence
{
  public final static String DOCUMENT_ELEM = "DOCUMENT";
  public final static String ALBUM_PROJECT_ELEM = "ALBUM_PROJECT";
  public final static String AUDIOFILE_ELEM = "AUDIO_FILE";
  public final static String ALBUM_ELEM = "ALBUM";
  public final static String TRACK_ELEM = "TRACK";

  public final static String NAME = "NAME";
  public final static String INTERPRET = "INTERPRET";
  public final static String TIME = "TIME";
  public final static String TRACK_NAME = "TRACK_NAME";

  public final static String FILE = "FILE";

  private Element m_docElement;

  public AlbumProject openProject(String filename) throws IOException
  {
    AlbumProject rueckgabe = null;
    InputStream stream = new FileInputStream(new File(filename));
    SAXReader parser = new SAXReader();

    try
    {
      Document doc = parser.read(stream);
      Element rootElement = doc.getRootElement();

      Element albumProjectElem = rootElement.element(ALBUM_PROJECT_ELEM);

      rueckgabe = readProjectElem(albumProjectElem);

    }
    catch (DocumentException docEx)
    {
      docEx.printStackTrace();

      throw new IOException(docEx.getMessage());
    }

    stream.close();

    return rueckgabe;
  }

  public void saveProject(AlbumProject project, String filename) throws IOException
  {
    m_docElement = DocumentHelper.createDocument().addElement(DOCUMENT_ELEM);

    writeAlbumProjectElem(m_docElement, project);

    File file = new File(filename);

    OutputFormat format = new OutputFormat();
    format.setExpandEmptyElements(true);
    format.setIndent(true);
    format.setNewlines(true);
    format.setNewLineAfterDeclaration(true);

    String encoding = Charset.defaultCharset().displayName();
    System.out.println("Encoding:" + Charset.defaultCharset().displayName());

    format.setEncoding(encoding);

    XMLWriter writer = new XMLWriter(new FileWriter(file), format);
    writer.write(m_docElement.getDocument());
    writer.close();

  }

  protected void writeAlbumProjectElem(Element parentElem, AlbumProject project)
  {
    Element albProjElem = parentElem.addElement(ALBUM_PROJECT_ELEM);

    Element projName = albProjElem.addElement(NAME);
    projName.setText(project.getProjectName());
    List<IAudioFile> audioFiles = project.getAudioFiles();

    for (int i = 0; i < audioFiles.size(); i++)
    {
      IAudioFile tempFile = audioFiles.get(i);
      writeAudioFileElem(albProjElem, tempFile);
    }

    writeAlbumElem(albProjElem, project.getAlbum());
  }

  protected AlbumProject readProjectElem(Element projectElem) throws IOException
  {
    AlbumProject rueckgabe = new AlbumProject();
    String title = projectElem.element(NAME).getText();

    rueckgabe.setProjectName(title);
    Album album = readAlbum(projectElem.element(ALBUM_ELEM));
    rueckgabe.setAlbum(album);

    List<Element> elemList = projectElem.elements(AUDIOFILE_ELEM);
    List<IAudioFile> audioFiles = readAudioFiles(elemList, album);

    for (int i = 0; i < audioFiles.size(); i++)
    {
      rueckgabe.addAudioFile(audioFiles.get(i));
    }

    return rueckgabe;
  }

  protected void writeAudioFileElem(Element parentElem, IAudioFile audioFile)
  {
    Element audioFileElem = parentElem.addElement(AUDIOFILE_ELEM);

    Element filenameElem = audioFileElem.addElement(FILE);
    filenameElem.setText(audioFile.getFile().getAbsolutePath());

    List<Track> trackList = audioFile.getTrackList();

    for (Track tempTrack : trackList)
    {
      Element trackName = audioFileElem.addElement(TRACK_NAME);
      trackName.setText(tempTrack.getName());
    }

  }

  protected List<IAudioFile> readAudioFiles(List<Element> elemList, Album album) throws IOException
  {
    List<IAudioFile> rueckgabe = new ArrayList<IAudioFile>();

    try
    {

      for (int i = 0; i < elemList.size(); i++)
      {
        Element tempAudioElem = elemList.get(i);
        String fileName = tempAudioElem.element(FILE).getText();

        File tempFile = new File(fileName);

        if (tempFile.exists())
        {
          AudioFile audioFile = new AudioFile(null);

          audioFile.setFile(tempFile);

          List<Element> trackNameElemList = tempAudioElem.elements(TRACK_NAME);
          List<Track> trackList = new ArrayList<Track>();

          for (Element temptrackNameElem : trackNameElemList)
          {
            String tempName = temptrackNameElem.getText();

            Track tempTrack = findTrack(album, tempName);

            if (tempTrack != null)
            {
              trackList.add(tempTrack);
            }
          }
          audioFile.setTrackList(trackList);
          rueckgabe.add(audioFile);
        }
      }
    }
    catch (UnsupportedAudioFileException audioEx)
    {
      throw new IOException(audioEx.getMessage());
    }

    return rueckgabe;
  }

  private Track findTrack(Album album, String trackName)
  {
    Track rueckgabe = null;
    List<Track> trackList = album.getTracks();

    for (int i = 0; i < trackList.size() && rueckgabe == null; i++)
    {
      Track tempTrack = trackList.get(i);
      if (tempTrack.getName().equals(trackName))
      {
        rueckgabe = tempTrack;
      }
    }

    return rueckgabe;
  }

  protected void writeAlbumElem(Element parentElem, Album album)
  {
    Element albumElem = parentElem.addElement(ALBUM_ELEM);
    Element albumName = albumElem.addElement(NAME);
    albumName.setText(album.getTitle());

    Element albumInterpret = albumElem.addElement(INTERPRET);
    albumInterpret.setText(album.getInterpret());

    List<Track> trackList = album.getTracks();

    for (Track tempTrack : trackList)
    {
      writeTrackElem(albumElem, tempTrack);
    }
  }

  private Album readAlbum(Element albumElement)
  {
    Album rueckgabe = new Album(null, "", "", DvYear.getNullValue());
    Element albumNameElem = albumElement.element(NAME);
    Element interpretElem = albumElement.element(INTERPRET);

    List<Element> trackElemList = albumElement.elements(TRACK_ELEM);

    List<Track> trackList = readTrackList(trackElemList);
    rueckgabe.setTitle(albumNameElem.getText());
    rueckgabe.setInterpret(interpretElem.getText());

    for (Track tempTrack : trackList)
    {
      rueckgabe.addTrack(tempTrack);
    }

    return rueckgabe;
  }

  private List<Track> readTrackList(List<Element> elemList)
  {
    List<Track> rueckgabe = new ArrayList<Track>();

    for (Element tempTrackElem : elemList)
    {
      Element trackInterpretElem = tempTrackElem.element(INTERPRET);
      Element trackNameElem = tempTrackElem.element(NAME);
      Element trackTimeElem = tempTrackElem.element(TIME);

      String name = trackNameElem.getText();
      String interpret = trackInterpretElem.getText();
      String timeStr = trackTimeElem.getText();

      DvTime time = DvTime.getNullValue();

      if (timeStr.trim().length() > 0)
      {
        try
        {
          int seconds = Integer.parseInt(timeStr);
          time = DvTime.valueOf(seconds);
        }
        catch (NumberFormatException nfex)
        {

        }
      }

      Track tempTrack = new Track(null, name, interpret, time);
      rueckgabe.add(tempTrack);
    }

    return rueckgabe;
  }

  protected void writeTrackElem(Element parentElem, Track track)
  {
    Element trackElem = parentElem.addElement(TRACK_ELEM);

    Element trackInterpret = trackElem.addElement(INTERPRET);
    trackInterpret.setText(track.getArtist());

    Element trackName = trackElem.addElement(NAME);
    trackName.setText(track.getName());

    Element trackTime = trackElem.addElement(TIME);
    trackTime.setText(track.getTime().getNumberOfSeconds() + "");
  }

}
