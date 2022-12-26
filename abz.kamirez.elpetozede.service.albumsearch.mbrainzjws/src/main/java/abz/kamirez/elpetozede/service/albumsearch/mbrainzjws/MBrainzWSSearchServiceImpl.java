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
package abz.kamirez.elpetozede.service.albumsearch.mbrainzjws;

import java.util.ArrayList;
import java.util.List;

import org.musicbrainz1.JMBWSException;
import org.musicbrainz1.model.Artist;
import org.musicbrainz1.model.Release;
import org.musicbrainz1.webservice.filter.ArtistFilter;
import org.musicbrainz1.webservice.filter.ReleaseFilter;
import org.musicbrainz1.webservice.includes.ReleaseIncludes;
import org.musicbrainz1.webservice.includes.TrackIncludes;
import org.musicbrainz1.ws1xml.element.ArtistResult;
import org.musicbrainz1.ws1xml.element.ArtistSearchResults;
import org.musicbrainz1.ws1xml.element.ReleaseResult;
import org.musicbrainz1.ws1xml.element.ReleaseSearchResults;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.DvYear;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.ClasspathUtil;
import abz.kamirez.elpetozede.domain.service.IAlbumSearchService;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.ProgressInfo;
import abz.kamirez.elpetozede.domain.service.SearchServiceException;

public class MBrainzWSSearchServiceImpl implements IAlbumSearchService
{

  private List<IProgressListener> m_progressListeners;
  private ProgressInfo m_progInfo;

  @Override
  public List<Album> findAlbums(String interpret, String title, String releaseType) throws SearchServiceException
  {
    ClasspathUtil.dumpClasspath(this.getClass());
    m_progInfo = new ProgressInfo(this, 0, "Start searching...");
    reportProgress();

    List<Album> rueckgabe = new ArrayList<Album>();

    List<String> releaseTypes = new ArrayList<>();

    if (releaseType.equals(RELEASE_REGULAR))
    {
      releaseTypes.add(Release.TYPE_OFFICIAL);
      releaseTypes.add(Release.TYPE_ALBUM);
    }
    if (releaseType.equals(RELEASE_LIVE))
    {
      releaseTypes.add(Release.TYPE_LIVE);
      releaseTypes.add(Release.TYPE_ALBUM);
    }
    else if (releaseType.equals(RELEASE_COMPILATION))
    {
      releaseTypes.add(Release.TYPE_COMPILATION);
    }
    else if (releaseType.equals(RELEASE_BOOTLEG))
    {
      releaseTypes.add(Release.TYPE_BOOTLEG);
      releaseTypes.add(Release.TYPE_LIVE);
    }

    try
    {
      List<Album> normalList = getAlbumList(interpret, title, releaseTypes.toArray(new String[0]));
      rueckgabe.addAll(normalList);
    }
    catch (JMBWSException wsex)
    {
      wsex.printStackTrace();

      m_progInfo.setPercentage(100);
      m_progInfo.setMessage("Error occured");
      reportProgress();
      throw new SearchServiceException(wsex.getMessage());
    }

    m_progInfo.setPercentage(100);
    m_progInfo.setMessage("Search completed");
    reportProgress();

    return rueckgabe;
  }

  @Override
  public void addProgressListener(IProgressListener progListener)
  {
    if (getListeners().contains(progListener) == false)
    {
      getListeners().add(progListener);
    }

  }

  @Override
  public void removeProgressListener(IProgressListener progListener)
  {
    getListeners().remove(progListener);
  }

  private void reportProgress()
  {
    List<IProgressListener> listeners = getListeners();

    for (IProgressListener tempListener : listeners)
    {
      tempListener.progressReported(m_progInfo);
    }
  }

  private List<IProgressListener> getListeners()
  {
    if (m_progressListeners == null)
    {
      m_progressListeners = new ArrayList<IProgressListener>();
    }

    return m_progressListeners;
  }

  protected List<Album> getAlbumList(String artist, String title, String[] releaseTypes) throws JMBWSException
  {
    List<Album> rueckgabe = new ArrayList<Album>();
    QueryWrapper q = new QueryWrapper();

    ReleaseFilter f = new ReleaseFilter();
    f.setArtistName(artist);
    f.setReleaseTypesStr(releaseTypes);
    f.setLimit((long) 10);

    ReleaseSearchResults releaseResults = q.getReleases(f);

    List<ReleaseResult> releaseResList = releaseResults.getReleaseResults();

    m_progInfo.setMessage("Found " + releaseResList.size() + " Releases of Type " + getTypeString(releaseTypes));
    m_progInfo.setPercentage(10);
    reportProgress();

    for (int i = 0; i < releaseResList.size(); i++)
    {
      Release tempRelease = releaseResList.get(i).getRelease();
      String tempTitle = tempRelease.getTitle().toLowerCase();

      if ((tempTitle.startsWith(title.toLowerCase())) || (tempTitle.endsWith(title.toLowerCase())))
      {
        int perc = m_progInfo.getPercentage();
        m_progInfo.setPercentage(perc + 5);
        m_progInfo.setMessage("Collecting Informations for Release \"" + tempRelease.getTitle() + "\"");
        reportProgress();

        Release tempReleaseWithTracks = getReleaseData(tempRelease.getId());

        rueckgabe.add(createAlbum(tempReleaseWithTracks));
      }

    }

    return rueckgabe;
  }

  private Release getReleaseData(String id) throws JMBWSException
  {
    QueryWrapper q = new QueryWrapper();
    ReleaseIncludes includes = new ReleaseIncludes();
    includes.setArtist(true);
    includes.setReleaseEvents(true);
    includes.setDiscs(true);
    includes.setTracks(true);

    if (id.startsWith("http"))
    {
      id = extractID(id);
    }

    Release rueckgabe = q.getReleaseById(id, includes);

    return rueckgabe;
  }

  private org.musicbrainz1.model.Track getTrackData(org.musicbrainz1.model.Track mbTrack) throws JMBWSException
  {
    org.musicbrainz1.model.Track rueckgabe = null;

    QueryWrapper q = new QueryWrapper();
    TrackIncludes includes = new TrackIncludes();
    includes.setArtist(true);

    String id = mbTrack.getId();

    if (id.startsWith("http"))
    {
      id = extractID(id);
    }

    rueckgabe = q.getTrackById(id, includes);

    return rueckgabe;
  }

  protected Album createAlbum(Release release) throws JMBWSException
  {
    String earliestRelDate = "";

    try
    {
      earliestRelDate = release.getEarliestReleaseDate();

      if (earliestRelDate == null)
      {
        earliestRelDate = "";
      }
    }
    catch (NullPointerException npex)
    {
      System.out.println("Fehler in libmusicbrainz: " + npex.getMessage());
    }

    DvYear year = DvYear.getNullValue();

    if (earliestRelDate.length() >= 4)
    {
      int yearInt = Integer.parseInt(earliestRelDate.substring(0, 4));
      year = DvYear.valueOf(yearInt);
    }

    Album rueckgabe = new Album(null, release.getTitle(), release.getArtist().getName(), year);

    List<org.musicbrainz1.model.Track> mbTracks = release.getTrackList().getTracks();

    if (mbTracks != null)
    {

      for (int i = 0; i < mbTracks.size(); i++)
      {
        org.musicbrainz1.model.Track tempMBTrack = mbTracks.get(i);

        int perc = m_progInfo.getPercentage();
        m_progInfo.setPercentage(perc + 2);
        m_progInfo.setMessage("Collecting Informations for Track \"" + tempMBTrack.getTitle() + "\"");
        reportProgress();

        org.musicbrainz1.model.Track tempTrackData = getTrackData(tempMBTrack);

        DvTime time = DvTime.getNullValue();
        Long duration = tempTrackData.getDuration();

        if (duration != null)
        {
          time = DvTime.valueOf((long) tempTrackData.getDuration());
        }

        Track newTrack = new Track(rueckgabe, tempTrackData.getTitle(), tempTrackData.getArtist().getName(), time);
        rueckgabe.addTrack(newTrack);
      }
    }

    return rueckgabe;
  }

  protected List<String> getArtistIDs(String artistName) throws JMBWSException
  {
    List<String> rueckgabe = new ArrayList<String>();
    ArtistFilter f = new ArtistFilter(artistName);
    QueryWrapper q = new QueryWrapper();

    // Limit the results to the 5 best matches.
    f.setLimit(5L);
    ArtistSearchResults artistResults = q.getArtists(f);

    for (ArtistResult ar : artistResults.getArtistResults())
    {
      Artist artist = ar.getArtist();
      String artistID = artist.getId();
      debug("[artist] " + artist.getId() + " - " + artist.getName() + " (" + artist.getBeginDate() + " - "
          + artist.getEndDate() + ")");

      if (artistName.equals(artist.getName()))
      {
        if (artistID != null)
        {
          if (rueckgabe.contains(artistID) == false)
          {
            rueckgabe.add(artistID);
          }
        }
      }
    }

    return rueckgabe;
  }

  private String extractID(String urlID)
  {
    String rueckgabe = urlID;

    int idBeginnIdx = urlID.lastIndexOf('/');

    if (idBeginnIdx > -1)
    {
      rueckgabe = urlID.substring(idBeginnIdx + 1);
    }
    return rueckgabe;
  }

  private void debug(String string)
  {
    System.out.println(string);
  }

  private String getTypeString(String[] relTypes)
  {
    String rueckgabe = "";

    for (int i = 0; i < relTypes.length; i++)
    {
      if (rueckgabe.length() > 0)
      {
        rueckgabe = rueckgabe + ", ";
      }

      String tempTyp = relTypes[i];
      int typIdx = tempTyp.lastIndexOf('#');

      String albumTyp = tempTyp.substring(typIdx + 1);

      rueckgabe = rueckgabe + albumTyp;
    }

    return rueckgabe;
  }

}
