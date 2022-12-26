import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;


public class ClipTest
{
    public static void main(String[] args) throws Exception {

      // specify the sound to play
      // (assuming the sound can be played by the audio system)
      File soundFile = new File("./test_data/2_2_Banshee.wav");
      AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);

      // load the sound into memory (a Clip)
      DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
      Clip clip = (Clip) AudioSystem.getLine(info);
      clip.open(sound);

      // due to bug in Java Sound, explicitly exit the VM when
      // the sound has stopped.
      clip.addLineListener(new LineListener() {
        public void update(LineEvent event) {
          if (event.getType() == LineEvent.Type.STOP) {
            event.getLine().close();
            System.out.println("Stop");
            System.exit(0);
          }
        }
      });

      // play the sound clip
      clip.start();
      Thread.sleep(100);
      
      while (clip.isRunning())
      {
        Thread.sleep(100);
      }
      
      System.out.println("Ende");
    }
      
}
