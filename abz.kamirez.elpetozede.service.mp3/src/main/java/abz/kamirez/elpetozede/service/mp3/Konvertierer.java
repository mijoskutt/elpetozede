package abz.kamirez.elpetozede.service.mp3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.Track;

public class Konvertierer
{

  private String m_inputFileName = "";
  private String m_outputFileName = "";
  private String m_converterExeStr = "";

  public final static String FORMAT_MP3 = "MP3";
  public final static String FORMAT_OGG = "OGG";
  private String m_commandStr;
  private Track m_track;
  private String m_albumName;

  public Konvertierer(String commandStr, Track track)
  {
    m_commandStr = commandStr;
    m_converterExeStr = commandStr;
    m_track = track;
    Album album = (Album) track.getParent();
    m_albumName = album.getTitle();
  }

  public void setConverterExeStr(String converterExeStr)
  {
    m_converterExeStr = converterExeStr;
  }

  public void setInputFileName(String inputFileName)
  {
    if (inputFileName == null)
    {
      inputFileName = "";
    }
    inputFileName = inputFileName.trim();

    m_inputFileName = inputFileName;
  }

  public String getInputFileName()
  {
    return m_inputFileName;
  }

  public void setOutputFileName(String outputFileName)
  {
    if (outputFileName == null)
    {
      outputFileName = "";
    }
    outputFileName = outputFileName.trim();
    m_outputFileName = outputFileName;
  }

  public String getOutputFileName()
  {
    return m_outputFileName;
  }

  public List<String> createLameKonvertCommand()
  {
    List<String> rueckgabe = new ArrayList<String>();
    rueckgabe.add(m_converterExeStr);
    rueckgabe.add("-q");
    rueckgabe.add("2");
    rueckgabe.add("-m");
    rueckgabe.add("j");
    rueckgabe.add("--tt");
    rueckgabe.add(m_track.getName());
    rueckgabe.add("--ta");
    rueckgabe.add(m_track.getArtist());
    rueckgabe.add("--tl");
    rueckgabe.add(m_albumName);

    rueckgabe.add(m_inputFileName);

    rueckgabe.add(m_outputFileName);

    return rueckgabe;
  }

  public Process konvert() throws IOException, InterruptedException
  {
    List<String> konvertCommand = createLameKonvertCommand();

    ProcessBuilder procBuilder = new ProcessBuilder(konvertCommand);

    String commandStr = "";

    for (String tempStr : konvertCommand)
    {
      commandStr = commandStr + " " + tempStr;
    }

    final Process p = procBuilder.start();
    return p;
  }

}
