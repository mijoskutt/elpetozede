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

import java.util.Hashtable;
import java.util.List;

import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Release;
import org.musicbrainz.filter.searchfilter.ReleaseSearchFilterWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.TrackWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseResultWs2;

public class QueryWrapperWS2
{

  public final static String QUERY_RELEASETYPE_LIVE = "live";
  public final static String QUERY_RELEASETYPE_COMPILATION = "compilation";
  public final static String QUERY_RELEASETYPE_BOOTLEG = "bootleg";

  public QueryWrapperWS2()
  {

  }

  public List<ReleaseResultWs2> searchReleases(String interpret, String title, String releaseType)
  {
    Release query = new Release();
    ReleaseSearchFilterWs2 searchFilter = query.getSearchFilter();
    searchFilter.setLimit((long) 20);
    searchFilter.setMinScore((long) 60);

    String escInterpret = getEscapedString(interpret);
    String escTitle = getEscapedString(title);

    String releaseTypeQueryStr = "";

    if (releaseType.isEmpty() == false)
    {
      releaseTypeQueryStr = " AND secondarytype:" + releaseType;
    }

    query.search(escTitle + " AND artist:" + escInterpret + releaseTypeQueryStr);
    List<ReleaseResultWs2> releaseResList = query.getFirstSearchResultPage();

    return releaseResList;
  }

  public ReleaseWs2 getReleaseData(ReleaseWs2 release) throws MBWS2Exception
  {
    Release query = new Release();
    query.getSearchFilter().setLimit((long) 20);
    String releaseID = release.getId();

    ReleaseWs2 rueckgabe = s_releaseCache.get(releaseID);

    if (rueckgabe == null)
    {
      ReleaseIncludesWs2 includes = query.getIncludes();
      includes.setMedia(true);
      includes.setLabel(true);
      includes.setRecordings(true);
      includes.setReleaseRelations(false);
      rueckgabe = query.getComplete(releaseID);
      s_releaseCache.put(releaseID, rueckgabe);
    }

    return rueckgabe;
  }

  protected String getEscapedString(String str)
  {
    String rueckgabe = str;

    rueckgabe = str.replace("/", "\\/");
    //rueckgabe = LUCENE_PATTERN.matcher(rueckgabe).replaceAll(REPLACEMENT_STRING);

    return rueckgabe;
  }

  private void waitABit()
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

  private static Hashtable<String, ReleaseWs2> s_releaseCache = new Hashtable<String, ReleaseWs2>();
  private static Hashtable<String, TrackWs2> s_trackCache = new Hashtable<String, TrackWs2>();

}
