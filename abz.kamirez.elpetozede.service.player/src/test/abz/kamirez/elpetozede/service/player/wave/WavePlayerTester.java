package abz.kamirez.elpetozede.service.player.wave;

import java.io.File;

import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.ClipStatusModel;

public class WavePlayerTester
{
  public static void main(String[] args)
  {
    SimpleWavePlayer player = new SimpleWavePlayer();

    AudioFile testWav = new AudioFile(null);

    String testFileName = "test_data" + File.separator + "2_2_Banshee.wav";
    //String testFileName = "test_data" + File.separator + "1-welcome.wav";

    try
    {
      testWav.setFile(new File(testFileName));
      ClipStatusModel model = new ClipStatusModel();
      model.setStartFrame((int) testWav.getSamples() / 4);
      model.setEndFrame((int) testWav.getSamples() / 3);
      player.playAudioFile(testWav, model);

      for (int i = 0; i < 5; i++)
      {
        Thread.sleep(1000);
        System.out.println("WavePlayerTester: warte...");
      }
      player.stop();
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
