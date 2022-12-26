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
package abz.kamirez.elpetozede.service.albumsearch.impl;

import java.util.ArrayList;
import java.util.List;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.DvYear;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.IAlbumSearchService;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.ProgressInfo;

public class AlbumSearchServiceDummyImpl implements IAlbumSearchService
{
  private int m_sleepMillis;
  private List<IProgressListener> m_progressListeners;
  private ProgressInfo m_progInfo;

  public AlbumSearchServiceDummyImpl()
  {
    this(0);
  }

  public AlbumSearchServiceDummyImpl(int sleepMillis)
  {
    m_sleepMillis = sleepMillis;

  }

  @Override
  public List<Album> findAlbums(String interpret, String title, String releaseType)
  {
    List<Album> rueckgabe = new ArrayList<Album>();
    m_progInfo = new ProgressInfo(this, 0, "Start searching...");

    String artist1 = "The Dummies";
    String artist2 = "Dummy and the Geeks";

    Album dummyAlbum1 = new Album(null, "Dummy-Album 1", artist1, DvYear.valueOf(1975));
    Album dummyAlbum2 = new Album(null, "Dummy-Album 2", artist2, DvYear.valueOf(1984));

    m_progInfo.setMessage("Found 2 Releases...");
    reportProgress();

    int songs = 10;

    for (int i = 0; i < songs; i++)
    {
      try
      {
        Thread.sleep(m_sleepMillis);
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Track tempTrack1 = new Track(dummyAlbum1, "Dummy-Track Nr " + (i + 1), artist1, DvTime.valueOf(3, 25));
      dummyAlbum1.addTrack(tempTrack1);

      Track tempTrack2 = new Track(dummyAlbum2, "Another Dummy-Song Nr " + (i + 1), artist2, DvTime.valueOf(3, 25));
      dummyAlbum2.addTrack(tempTrack2);

      int perc = (100 / songs) * i;
      m_progInfo.setPercentage(perc);
      m_progInfo.setMessage((i + 1) + " Songs gefunden...");
      reportProgress();

      //throw new NullPointerException("Nur zum Testen der Exceptions!");
    }

    rueckgabe.add(dummyAlbum1);
    rueckgabe.add(dummyAlbum2);

    m_progInfo.setPercentage(100);
    m_progInfo.setMessage("Supi! Ganz tolle Songs gefunden!");
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

}
