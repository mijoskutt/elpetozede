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

import java.util.Hashtable;

import org.musicbrainz1.JMBWSException;
import org.musicbrainz1.Query;
import org.musicbrainz1.model.Artist;
import org.musicbrainz1.model.Release;
import org.musicbrainz1.model.Track;
import org.musicbrainz1.model.User;
import org.musicbrainz1.webservice.WebService;
import org.musicbrainz1.webservice.filter.ArtistFilter;
import org.musicbrainz1.webservice.filter.ReleaseFilter;
import org.musicbrainz1.webservice.filter.TrackFilter;
import org.musicbrainz1.webservice.includes.ArtistIncludes;
import org.musicbrainz1.webservice.includes.ReleaseIncludes;
import org.musicbrainz1.webservice.includes.TrackIncludes;
import org.musicbrainz1.ws1xml.element.ArtistSearchResults;
import org.musicbrainz1.ws1xml.element.ReleaseSearchResults;
import org.musicbrainz1.ws1xml.element.TrackSearchResults;

public class QueryWrapper
{

  private Query m_query;

  public QueryWrapper()
  {
    m_query = new Query();
  }

  @Override
  public boolean equals(Object obj)
  {
    return m_query.equals(obj);
  }

  public Artist getArtistById(String arg0, ArtistIncludes arg1) throws JMBWSException
  {
    waitASecond();
    return m_query.getArtistById(arg0, arg1);
  }

  public ArtistSearchResults getArtists(ArtistFilter filter) throws JMBWSException
  {
    waitASecond();
    return m_query.getArtists(filter);
  }

  public Release getReleaseById(String arg0, ReleaseIncludes arg1) throws JMBWSException
  {
    Release rueckgabe = s_releaseCache.get(arg0);

    if (rueckgabe == null)
    {

      waitASecond();
      rueckgabe = m_query.getReleaseById(arg0, arg1);

      if (rueckgabe != null)
      {
        s_releaseCache.put(arg0, rueckgabe);
      }

    }
    return rueckgabe;
  }

  public ReleaseSearchResults getReleases(ReleaseFilter filter) throws JMBWSException
  {
    waitASecond();
    return m_query.getReleases(filter);
  }

  public Track getTrackById(String arg0, TrackIncludes arg1) throws JMBWSException
  {
    Track rueckgabe = s_trackCache.get(arg0);

    if (rueckgabe == null)
    {
      waitASecond();
      rueckgabe = m_query.getTrackById(arg0, arg1);

      if (rueckgabe != null)
      {
        s_trackCache.put(arg0, rueckgabe);
      }
    }

    return rueckgabe;

  }

  public TrackSearchResults getTracks(TrackFilter filter) throws JMBWSException
  {
    waitASecond();
    return m_query.getTracks(filter);
  }

  public User getUserByName(String name) throws JMBWSException
  {
    waitASecond();
    return m_query.getUserByName(name);
  }

  public WebService getWs()
  {
    return m_query.getWs();
  }

  @Override
  public int hashCode()
  {
    return m_query.hashCode();
  }

  public void setWs(WebService ws)
  {
    m_query.setWs(ws);
  }

  @Override
  public String toString()
  {
    return m_query.toString();
  }

  private void waitASecond()
  {
    try
    {
      // because we are only allowed to query the web service once in a second

      Thread.sleep(1100);
    }
    catch (InterruptedException e)
    {

    }
  }

  private static Hashtable<String, Release> s_releaseCache = new Hashtable<String, Release>();
  private static Hashtable<String, org.musicbrainz1.model.Track> s_trackCache = new Hashtable<String, org.musicbrainz1.model.Track>();

}
