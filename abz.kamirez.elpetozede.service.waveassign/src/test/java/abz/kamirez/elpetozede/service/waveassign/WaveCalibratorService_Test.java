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
package abz.kamirez.elpetozede.service.waveassign;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.IWaveTrackCalibratorService;
import abz.kamirez.elpetozede.domain.service.SilenceCalibrationParam;
import abz.kamirez.elpetozede.domain.service.SilenceFinderParameter;
import abz.kamirez.elpetozede.domain.service.SysoutLogger;
import junit.framework.TestCase;

public class WaveCalibratorService_Test extends TestCase
{
  private List<Track> m_beatlesTracks;

  private static AudioFile s_audioFile;

  @Override
  protected void setUp() throws Exception
  {
    m_beatlesTracks = new ArrayList<Track>();
    m_beatlesTracks.add(new Track(null, "I am the Walrus", "The Beatles", DvTime.valueOf(4, 37)));
    m_beatlesTracks.add(new Track(null, "Hello, Goodby", "The Beatles", DvTime.valueOf(3, 32)));
    m_beatlesTracks.add(new Track(null, "The Fool on the Hill", "The Beatles", DvTime.valueOf(3, 02)));
    m_beatlesTracks.add(new Track(null, "Magical Mystery Tour", "The Beatles", DvTime.valueOf(2, 51)));
    m_beatlesTracks.add(new Track(null, "Lady Madonna", "The Beatles", DvTime.valueOf(2, 21)));
    m_beatlesTracks.add(new Track(null, "Hey Jude", "The Beatles", DvTime.valueOf(7, 11)));
    m_beatlesTracks.add(new Track(null, "Revolution", "The Beatles", DvTime.valueOf(3, 26)));

    if (s_audioFile == null)
    {
      s_audioFile = new AudioFile(null);

      String testFileName = "test_data" + File.separator + "Beatles_2.wav";
      s_audioFile.setFile(new File(testFileName));
      s_audioFile.setTrackList(m_beatlesTracks);
    }
  }

  @Override
  protected void tearDown() throws Exception
  {
  }

  public void testCalibrateInTrack_NoSilence()
  {
    WaveCalibratorService calibrator = createCalibrator();

    double maxVolume = calibrator.findMaxVolume(s_audioFile, 0, (int) s_audioFile.getSamples());

    System.out.println("maxVolume = " + maxVolume);

    short secondsAround = 1;

    SilenceFinderParameter params = new SilenceFinderParameter(secondsAround, secondsAround, 1000, 1000.0f);

    int startOfSecondTrack = 281;
    int samplesPerSecond = s_audioFile.getBitsPerSample();

    List<SearchResultStruct> results = calibrator.findPauseSample(s_audioFile, startOfSecondTrack, samplesPerSecond,
      params);

    // there should be NO silence:
    assertEquals(0, results.size());

  }

  public void testCalibrateAtEndOfFirstTrack()
  {
    WaveCalibratorService calibrator = createCalibrator();

    short secondsAround = 5;

    SilenceFinderParameter params = new SilenceFinderParameter(secondsAround, secondsAround, 1000, 1300.0f);

    int endOfFirstTrack = m_beatlesTracks.get(0).getTime().getNumberOfSeconds();
    int samplesPerSecond = s_audioFile.getBitsPerSample();

    List<SearchResultStruct> results = calibrator.findPauseSample(s_audioFile, endOfFirstTrack, samplesPerSecond,
      params);

    assertTrue(results.size() > 0);

    results.clear();

    for (int i = 0; i < 1400; i = i + 100)
    {
      SilenceFinderParameter tempParams = new SilenceFinderParameter(secondsAround, secondsAround, 1000, i);
      results.addAll(
        calibrator.findPauseSample(s_audioFile, endOfFirstTrack, (int) ((double) samplesPerSecond * 1.5), tempParams));

    }

    Collections.sort(results);
    SearchResultStruct bestSilence = WaveCalibratorService.getBestSilence(44100, results, 0.7);

    System.out.println("Best result for \"I am the Walrus\":");
    System.out.println(bestSilence.toString());

    int pauseBegin = 12162610;
    int pauseEnd = 12363159;

    assertTrue(bestSilence.m_startSample >= pauseBegin);

    int resultPauseEnd = bestSilence.m_startSample + bestSilence.m_silenceSamples;
    assertTrue(resultPauseEnd <= pauseEnd);

  }

  public void testCalibrateAtEndOf_HeyJude()
  {
    WaveCalibratorService calibrator = createCalibrator();

    short secondsAround = 5;

    int endOfTrack = 0;

    for (int i = 0; i < 6; i++)
    {
      endOfTrack = endOfTrack + m_beatlesTracks.get(i).getTime().getNumberOfSeconds();
    }

    int pauseInSamples = s_audioFile.getBitsPerSample() * 2;

    List<SearchResultStruct> results = new ArrayList<SearchResultStruct>();

    for (int i = 0; i < 1000; i = i + 100)
    {
      SilenceFinderParameter tempParams = new SilenceFinderParameter(secondsAround, secondsAround, 1000, i);

      for (int j = 0; j < 10; j++)
      {
        results.addAll(calibrator.findPauseSample(s_audioFile, endOfTrack + j, pauseInSamples, tempParams));
        results.addAll(calibrator.findPauseSample(s_audioFile, endOfTrack - j, pauseInSamples, tempParams));
      }
    }

    assertTrue(results.size() > 0);

    Collections.sort(results);

    SearchResultStruct bestSilence = WaveCalibratorService.getBestSilence(44100, results, 0.7);

    //SearchResultStruct bestSilence = results.get(0);

    System.out.println("Best result for \"Hey Jude\":");
    System.out.println(bestSilence.toString());

    int pauseBegin = 61612000;
    int pauseEnd = 62110600;

    assertTrue(bestSilence.m_startSample >= pauseBegin);

    int resultPauseEnd = bestSilence.m_startSample + bestSilence.m_silenceSamples;
    assertTrue(resultPauseEnd <= pauseEnd);
  }

  public void testGetBestSilence()
  {
    List<SearchResultStruct> results = new ArrayList<SearchResultStruct>();

    SearchResultStruct struct1 = new SearchResultStruct();
    struct1.m_maxVolumeValue = 1200.0f;
    struct1.m_silenceSamples = 44200;

    SearchResultStruct struct2 = new SearchResultStruct();
    struct2.m_maxVolumeValue = 900.0f;
    struct2.m_silenceSamples = 38200;

    SearchResultStruct struct3 = new SearchResultStruct();
    struct3.m_maxVolumeValue = 600.0f;
    struct3.m_silenceSamples = 14200;

    results.add(struct1);
    results.add(struct2);
    results.add(struct3);

    SearchResultStruct bestSilence = struct2;

    SearchResultStruct bestSilenceResult = WaveCalibratorService.getBestSilence(44100, results, 0.7);

    assertEquals(bestSilence, bestSilenceResult);
  }

  public void testCalibrateAllTracks()
  {
    IWaveTrackCalibratorService calibrator = createCalibrator();

    int lengthOfTracksBefore = getLengthOfTracks(m_beatlesTracks);
    int lengthOfWaveFile = s_audioFile.getLengthInSeconds();

    SilenceCalibrationParam param = new SilenceCalibrationParam(1100.0, 0.9);

    calibrator.calibrateTracks(s_audioFile, m_beatlesTracks, param);

    int lengthOfTracksAfter = getLengthOfTracks(m_beatlesTracks);

    assertEquals(lengthOfWaveFile, lengthOfTracksAfter);

  }

  private int getLengthOfTracks(List<Track> trackList)
  {
    int rueckgabe = 0;

    for (int i = 0; i < trackList.size(); i++)
    {
      rueckgabe = rueckgabe + trackList.get(i).getTime().getNumberOfSeconds();
    }

    return rueckgabe;
  }

  private WaveCalibratorService createCalibrator()
  {
    return new WaveCalibratorService(true, new SysoutLogger());
  }

}
