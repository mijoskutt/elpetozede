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
package abz.kamirez.elpetozede.service.waveassign;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.ISampleData;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.IProgressReportingTask;
import abz.kamirez.elpetozede.domain.service.IWaveTrackCalibratorService;
import abz.kamirez.elpetozede.domain.service.ProgressInfo;
import abz.kamirez.elpetozede.domain.service.SilenceCalibrationParam;
import abz.kamirez.elpetozede.domain.service.SilenceFinderParameter;

public class WaveCalibratorService implements IWaveTrackCalibratorService, IProgressReportingTask
{
  private AudioFile m_sourceFile;
  private List<Track> m_trackList;
  private boolean m_multiThreaded;

  private List<IProgressListener> m_progressListeners;
  private ProgressInfo m_progInfo;
  private ILogger m_logger;

  public WaveCalibratorService(boolean multiThreaded, ILogger logger)
  {
    m_multiThreaded = multiThreaded;
    m_logger = logger;
  }

  @Override
  public void calibrateTracks(AudioFile sourceFile, List<Track> trackList, SilenceCalibrationParam param)
  {
    m_sourceFile = sourceFile;
    m_trackList = trackList;

    System.out.println("calibrateTracks() beginnt...");
    long timer = System.currentTimeMillis();

    m_progInfo = new ProgressInfo(this, 0, "Start calibrating...");
    reportProgress();

    int stepPercent = (int) floor((100.0 / (double) trackList.size()));

    for (int i = 0; i < m_trackList.size(); i++)
    {
      m_progInfo.setMessage("Calibrating Track \"" + m_trackList.get(i).getName() + "\"");
      reportProgress();
      int perc = m_progInfo.getPercentage();
      m_progInfo.setPercentage(perc + stepPercent);
      calibrateTrack(m_sourceFile, trackList, i, param);
    }

    m_progInfo.setPercentage(100);
    m_progInfo.setMessage("Calibrating finished.");

    timer = (System.currentTimeMillis() - timer);

    long seconds = timer / 1000;
    System.out.println("calibrateTracks() benötigte: " + String.valueOf(seconds) + " Sekunden");

  }

  private void calibrateTrack(AudioFile sourceFile, List<Track> trackList, int trackIndex,
      SilenceCalibrationParam param)
  {
    short secondsAround = 5;

    int endOfTrack = 0;
    int startOfTrack = getTotalLengthOfTracks(trackList, trackIndex - 1);

    endOfTrack = startOfTrack + trackList.get(trackIndex).getTime().getNumberOfSeconds();
    List<SearchResultStruct> results = new ArrayList<SearchResultStruct>();

    // Last Track? => reduce maxVolume  
    if (trackIndex + 1 == trackList.size())
    {
      double orgMaxVolume = param.getMaxVolumeValue();
      double lastTrackMaxVolume = orgMaxVolume * 0.7;
      param.setMaxVolumeValue(lastTrackMaxVolume);
    }

    if (m_multiThreaded)
    {
      findSilencePartsMultiThreaded(sourceFile, param, secondsAround, endOfTrack, results);
    }
    else
    {
      findSilencePartsSingleThreaded(sourceFile, param, secondsAround, endOfTrack, results);
    }

    Track calibrateTrack = trackList.get(trackIndex);

    if (results.size() > 0)
    {
      SearchResultStruct bestSilence = getBestSilence(sourceFile.getBitsPerSample(), results,
        param.getMinPauseBetweenTracks());
      int startOfTrackInSamples = startOfTrack * sourceFile.getBitsPerSample();
      int calcTrackLengthSamples = bestSilence.m_startSample + (bestSilence.m_silenceSamples / 2)
          - startOfTrackInSamples;
      int calcLengthSeconds = calcTrackLengthSamples / sourceFile.getBitsPerSample();

      if (calcLengthSeconds != calibrateTrack.getTime().getNumberOfSeconds())
      {
        DvTime calcTime = DvTime.valueOf(calcLengthSeconds);
        calibrateTrack.setTime(calcTime);

        m_logger.log("Track " + calibrateTrack.getName() + " seems to end after " + calcTime + ", Totaltime = "
            + DvTime.valueOf(getTotalLengthOfTracks(trackList, trackIndex)));
      }
    }
    else
    {
      m_logger.log("Track " + calibrateTrack.getName() + " couldn't be calibrated, no silence found.");
    }

  }

  protected static SearchResultStruct getBestSilence(int bitsPerSample, List<SearchResultStruct> results,
      double minPauseBetweenTracks)
  {
    Collections.sort(results);
    SearchResultStruct bestSilence = results.get(0);

    List<SearchResultStruct> resultsWithMinPause = new ArrayList<SearchResultStruct>();

    for (int i = 0; i < results.size(); i++)
    {
      SearchResultStruct tempResult = results.get(i);

      double silenceSeconds = (double) tempResult.m_silenceSamples / (double) bitsPerSample;
      if (silenceSeconds > minPauseBetweenTracks)
      {
        resultsWithMinPause.add(tempResult);
      }
      else
      {
        break;
      }
    }

    if (resultsWithMinPause.size() > 0)
    {

      Comparator<SearchResultStruct> silenceComparator = new Comparator<SearchResultStruct>()
      {
        @Override
        public int compare(SearchResultStruct o1, SearchResultStruct o2)
        {
          int result = 0;

          if (o1.m_maxVolumeValue > o2.m_maxVolumeValue)
          {
            result = 1;
          }
          else if (o1.m_maxVolumeValue < o2.m_maxVolumeValue)
          {
            result = -1;
          }
          else
          {
            result = o1.compareTo(o2);
          }

          return result;
        }

      };

      Collections.sort(resultsWithMinPause, silenceComparator);
      bestSilence = resultsWithMinPause.get(0);
    }

    return bestSilence;
  }

  private void findSilencePartsSingleThreaded(AudioFile sourceFile, SilenceCalibrationParam param, short secondsAround,
      int endOfTrack, List<SearchResultStruct> results)
  {
    for (int i = 0; i < param.getMaxVolumeValue(); i = i + 100)
    {
      SilenceFinderParameter tempParams = new SilenceFinderParameter(secondsAround, secondsAround, 1000, i);

      double minPause = param.getMinPauseBetweenTracks();

      for (int j = 0; j < 10; j++)
      {
        for (double p = 2.4; p > minPause; p = p - 0.2)
        {
          int pauseInSamples = (int) (sourceFile.getBitsPerSample() * p);
          results.addAll(findPauseSample(sourceFile, endOfTrack + j, pauseInSamples, tempParams));
          results.addAll(findPauseSample(sourceFile, endOfTrack - j, pauseInSamples, tempParams));
        }
      }
    }
  }

  private void findSilencePartsMultiThreaded(AudioFile sourceFile, SilenceCalibrationParam param, short secondsAround,
      int endOfTrack, List<SearchResultStruct> results)
  {
    int threads = Runtime.getRuntime().availableProcessors();

    int volumeRang = (int) Math.floor((double) param.getMaxVolumeValue() / (double) threads);

    List<PauseFinderThread> finderList = new ArrayList<PauseFinderThread>();
    List<Thread> threadList = new ArrayList<Thread>();

    double pauseBetweenTracks = param.getMinPauseBetweenTracks();
    int minVolume = 0;

    for (int i = 1; i <= threads; i++)
    {
      int maxVolume = minVolume + volumeRang;

      if (i == threads)
      {
        maxVolume = (int) param.getMaxVolumeValue();
      }

      PauseFinderThread finder1 = new PauseFinderThread("finder" + i, sourceFile, minVolume, maxVolume, secondsAround,
        pauseBetweenTracks, endOfTrack);
      Thread finderThread1 = new Thread(finder1);
      finderThread1.start();
      finderList.add(finder1);
      threadList.add(finderThread1);
      minVolume = maxVolume;
    }

    try
    {
      for (int t = 0; t < threadList.size(); t++)
      {
        Thread tempThread = threadList.get(t);
        tempThread.join();
      }

    }
    catch (InterruptedException irex)
    {
      irex.printStackTrace();
    }

    for (int t = 0; t < finderList.size(); t++)
    {
      PauseFinderThread tempFinder = finderList.get(t);
      results.addAll(tempFinder.getResults());
    }

  }

  /**
   * Find the start of a pause at startSearchSample 
   * @param startSearchSample the number of the sample to start the search
   * @param the duration of the pauses in samples 
   * @return
   */
  protected List<SearchResultStruct> findPauseSample(AudioFile sourceFile, int endOfTracksecond, int samples,
      SilenceFinderParameter params)
  {
    return findPauseSample(sourceFile, sourceFile, endOfTracksecond, samples, params);
  }

  /**
   * Find the start of a pause at startSearchSample 
   * @param startSearchSample the number of the sample to start the search
   * @param the duration of the pauses in samples 
   * @return
   */
  protected List<SearchResultStruct> findPauseSample(AudioFile sourceFile, ISampleData sampleData, int endOfTracksecond,
      int samples, SilenceFinderParameter params)
  {
    List<SearchResultStruct> rueckgabe = new ArrayList<SearchResultStruct>();

    int startSecond = max(endOfTracksecond - params.getSecondsBeforeTrackEnd(), 0);
    int startSearchSample = min(startSecond * sourceFile.getBitsPerSample(), (int) sourceFile.getSamples());

    int endSecond = params.getSecondsAfterTrackEnd();
    int endSample = min(((endOfTracksecond + endSecond) * sourceFile.getBitsPerSample()),
      (int) sourceFile.getSamples());

    int aktSample = startSearchSample;

    float aktMaxVal = -1.0f;

    // the maximum value for "silence" (0 would be complete silence)
    float maxSilenceVal = params.getMaxValue();

    // count of aktuals sequence of samples with a value < maxSilenceVal
    int silenceSamples = 0;

    int stepSize = 10;
    int samplesOverMaxVol = 0;
    int limitSamplesOverMaxVol = (sourceFile.getBitsPerSample() / 500) / stepSize;

    float aktVal = 0.0f;
    int channels = sourceFile.getChannels();

    SearchResultStruct aktResult = new SearchResultStruct();

    while (aktSample < endSample)
    {
      aktResult = new SearchResultStruct();
      aktResult.m_startSample = aktSample;

      for (int i = aktSample; i < endSample; i = i + stepSize)
      {
        aktVal = getMaxVal(sampleData, channels, i);
        aktMaxVal = max(aktVal, aktMaxVal);

        if (aktVal < maxSilenceVal)
        {
          silenceSamples = silenceSamples + stepSize;
          samplesOverMaxVol = 0;
          aktSample = aktSample + stepSize;
        }
        else
        {
          samplesOverMaxVol++;
          aktSample = aktSample + stepSize;

          if (samplesOverMaxVol > limitSamplesOverMaxVol)
          {
            aktSample = aktSample + silenceSamples;
            break;
          }
        }
      }

      if (silenceSamples > (samples - params.getTolerance()))
      {
        aktResult.m_silenceSamples = silenceSamples - samplesOverMaxVol;
        aktResult.m_maxVolumeValue = aktMaxVal;
        rueckgabe.add(aktResult);
      }

      aktMaxVal = -1.0f;
      silenceSamples = 0;
      samplesOverMaxVol = 0;

    }

    return rueckgabe;
  }

  private float getMaxVal(ISampleData file, int channels, int sample)
  {
    float rueckgabe = 0.0f;

    for (int i = 0; i < channels; i++)
    {
      rueckgabe = max(rueckgabe, abs(file.getValue(i, sample)));
    }

    return rueckgabe;
  }

  protected double findMaxVolume(ISampleData file, int startSample, int endSample)
  {
    float rueckgabe = 0.0f;
    int channels = file.getChannels();

    for (int i = startSample; i < endSample; i++)
    {
      rueckgabe = max(rueckgabe, getMaxVal(file, channels, i));
    }

    return rueckgabe;
  }

  public static int getTotalLengthOfTracks(List<Track> trackList, int toTrackIndex)
  {
    int rueckgabe = 0;

    for (int i = 0; i < toTrackIndex + 1; i++)
    {
      rueckgabe = rueckgabe + trackList.get(i).getTime().getNumberOfSeconds();
    }

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

  private class PauseFinderThread implements Runnable
  {
    private List<SearchResultStruct> m_results;

    private String m_treadName;
    private AudioFile m_file;
    private int m_minVolume;
    private int m_maxVolume;
    private short m_secondsAround;
    private int m_endOfTracksecond;
    private double m_pauseBetweenTracks;
    private short m_searchRange;

    private DataChunk m_data;

    public PauseFinderThread(String treadName, AudioFile sourceFile, int minVolume, int maxVolume, short secondsAround,
        double pauseBetweenTracks, int endOfTracksecond)
    {
      m_results = new ArrayList<SearchResultStruct>();
      m_treadName = treadName;
      m_file = sourceFile;
      m_minVolume = minVolume;
      m_maxVolume = maxVolume;
      m_secondsAround = secondsAround;
      m_pauseBetweenTracks = pauseBetweenTracks;
      m_endOfTracksecond = endOfTracksecond;

      m_searchRange = 10;
      createData();
    }

    private void createData()
    {
      int samplesPerSecond = m_file.getBitsPerSample();

      int startSample = (m_endOfTracksecond - m_searchRange - m_secondsAround) * samplesPerSecond;
      startSample = max(0, startSample);
      int endSample = (m_endOfTracksecond + m_searchRange + m_secondsAround) * samplesPerSecond;
      endSample = min(endSample, (int) m_file.getSamples());

      float[] valCopyChannel1 = m_file.getValueCopy(0, startSample, endSample);
      float[] valCopyChannel2 = m_file.getValueCopy(1, startSample, endSample);

      m_data = new DataChunk(startSample, valCopyChannel1, valCopyChannel2);
    }

    @Override
    public void run()
    {
      System.out.println("PauseFinderThread " + m_treadName + " started");
      long timer = System.currentTimeMillis();

      for (int i = m_minVolume; i < m_maxVolume; i = i + 100)
      {
        SilenceFinderParameter tempParams = new SilenceFinderParameter(m_secondsAround, m_secondsAround, 1000, i);

        for (int j = 0; j < m_searchRange; j++)
        {
          for (double p = 2.4; p > m_pauseBetweenTracks; p = p - 0.2)
          {
            int pauseInSamples = (int) (m_file.getBitsPerSample() * p);
            m_results.addAll(findPauseSample(m_file, m_data, m_endOfTracksecond + j, pauseInSamples, tempParams));
            m_results.addAll(findPauseSample(m_file, m_data, m_endOfTracksecond - j, pauseInSamples, tempParams));

          }
        }
      }
      timer = (System.currentTimeMillis() - timer);

      long seconds = timer / 1000;
      System.out
        .println("PauseFinderThread " + m_treadName + " finished after " + String.valueOf(seconds) + " Seconds");

    }

    public List<SearchResultStruct> getResults()
    {
      return m_results;
    }

  }
}
