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
package abz.kamirez.elpetozede.service.albumsearch.ws2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.model.LabelInfoListWs2;
import org.musicbrainz.model.LabelInfoWs2;
import org.musicbrainz.model.MediumListWs2;
import org.musicbrainz.model.MediumWs2;
import org.musicbrainz.model.TrackWs2;
import org.musicbrainz.model.entity.RecordingWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseResultWs2;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.DvYear;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.IAlbumSearchService;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.ProgressInfo;
import abz.kamirez.elpetozede.domain.service.SearchServiceException;

public class MBrainzWS2ServiceImpl implements IAlbumSearchService
{

  private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
  private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
  private static final String REPLACEMENT_STRING = "\\\\$0";

  private List<IProgressListener> m_progressListeners;
  private ProgressInfo m_progInfo;
  private QueryWrapperWS2 m_queryProxy;

  public MBrainzWS2ServiceImpl()
  {
    m_queryProxy = new QueryWrapperWS2();
  }

  public List<Album> findAlbums(String interpret, String title, String releaseType) throws SearchServiceException
  {
    //ClasspathUtil.dumpClasspath(this.getClass());
    m_progInfo = new ProgressInfo(this, 0, "Start searching...");
    reportProgress();

    List<Album> rueckgabe = new ArrayList<Album>();

    String mbReleaseType = "";

    if ((releaseType.equals(RELEASE_LIVE)) || (releaseType.equals(RELEASE_BOOTLEG)))
    {
      mbReleaseType = QueryWrapperWS2.QUERY_RELEASETYPE_LIVE;
    }
    else if (releaseType.equals(RELEASE_COMPILATION))
    {
      mbReleaseType = QueryWrapperWS2.QUERY_RELEASETYPE_COMPILATION;
    }

    try
    {
      List<Album> normalList = getAlbumList(interpret, title, mbReleaseType);
      rueckgabe.addAll(normalList);

      //      List<Album> zusatzList = new ArrayList<Album>();
      //      List<Album> liveAlbumList = getAlbumList(interpret, title, ReleaseWs2.TYPE_LIVE);
      //      zusatzList.addAll(liveAlbumList);
      //
      //      List<Album> compilationAlbumList = getAlbumList(interpret, title, ReleaseWs2.TYPE_COMPILATION);
      //      zusatzList.addAll(compilationAlbumList);
      //
      //      for (Album tempAlbum : zusatzList)
      //      {
      //        if (rueckgabe.contains(tempAlbum) == false)
      //        {
      //          rueckgabe.add(tempAlbum);
      //        }
      //      }
    }
    catch (MBWS2Exception wsex)
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

  public void addProgressListener(IProgressListener progListener)
  {
    if (getListeners().contains(progListener) == false)
    {
      getListeners().add(progListener);
    }

  }

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

  protected List<Album> getAlbumList(String interpret, String title, String releaseType) throws MBWS2Exception
  {
    List<Album> rueckgabe = new ArrayList<Album>();
    List<ReleaseResultWs2> releaseResList = m_queryProxy.searchReleases(interpret, title, releaseType);

    m_progInfo.setMessage("Found " + releaseResList.size() + " Releases");
    m_progInfo.setPercentage(10);
    reportProgress();

    int ergebnisse = releaseResList.size();

    int progressStep = 90 / (ergebnisse + 1);

    for (int i = 0; i < ergebnisse; i++)
    {
      ReleaseWs2 tempRelease = releaseResList.get(i).getRelease();
      String tempTitle = tempRelease.getTitle().toLowerCase();

      if ((tempTitle.startsWith(title.toLowerCase())) || (tempTitle.endsWith(title.toLowerCase())))
      {
        int perc = m_progInfo.getPercentage();
        m_progInfo.setPercentage(perc + progressStep);
        m_progInfo.setMessage(
          "Collecting Informations for Release \"" + tempRelease.getTitle() + "\", " + (i + 1) + "/" + ergebnisse);
        reportProgress();

        ReleaseWs2 tempReleaseWithTracks = m_queryProxy.getReleaseData(tempRelease);

        rueckgabe.add(createAlbum(tempReleaseWithTracks));
      }

    }

    return rueckgabe;
  }

  protected Album createAlbum(ReleaseWs2 release) throws MBWS2Exception
  {
    String earliestRelDate = "";

    try
    {
      Date relDate = release.getDate();

      if (relDate != null)
      {
        Calendar cal = Calendar.getInstance();
        cal.setTime(relDate);
        earliestRelDate = "" + cal.get(Calendar.YEAR);
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

    MediumListWs2 mediumList = release.getMediumList();
    List<TrackWs2> mbTracks = mediumList.getCompleteTrackList();

    Album rueckgabe = new Album(null, release.getTitle(), release.getArtistCreditString(), year);
    String labelStr = "";

    LabelInfoListWs2 labelInfoList = release.getLabelInfoList();

    if (labelInfoList != null)
    {
      List<LabelInfoWs2> labelInfos = labelInfoList.getLabelInfos();

      if (labelInfos != null && labelInfos.size() > 0)
      {
        labelStr = labelInfos.get(0).getLabelName();
      }
    }

    rueckgabe.setLabel(labelStr);

    String mediumStr = "";
    List<MediumWs2> medList = mediumList.getMedia();

    for (MediumWs2 mediumWs2 : medList)
    {
      if (mediumStr.length() > 0)
      {
        mediumStr = mediumStr + ", ";
      }

      mediumStr = mediumStr + mediumWs2.getFormat();
    }

    rueckgabe.setMedium(mediumStr);

    if (mbTracks != null)
    {

      for (int i = 0; i < mbTracks.size(); i++)
      {
        TrackWs2 tempMBTrack = mbTracks.get(i);
        RecordingWs2 tempRecording = tempMBTrack.getRecording();

        //        int perc = m_progInfo.getPercentage();
        //        m_progInfo.setPercentage(perc + 2);
        //        m_progInfo.setMessage("Collecting Informations for Track \"" + tempRecording.getTitle() + "\"");
        //        reportProgress();

        DvTime time = DvTime.getNullValue();
        Long duration = tempMBTrack.getDurationInMillis();

        if (duration != null)
        {
          time = DvTime.valueOf((long) tempMBTrack.getDurationInMillis());
        }

        Track newTrack = new Track(rueckgabe, tempRecording.getTitle(), rueckgabe.getInterpret(), time);
        rueckgabe.addTrack(newTrack);
      }
    }

    return rueckgabe;
  }

}
