package abz.kamirez.elpetozede.service.mp3;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.regex.Pattern;

public class OutputParseThread extends Thread
{
  protected InputStream m_processStream;
  protected Scanner m_sc;
  protected double m_progress;
  private IFortschrittsLogger m_logger;

  public OutputParseThread(InputStream procStream, IFortschrittsLogger logger)
  {
    m_processStream = procStream;
    if (logger == null)
    {
      logger = new IFortschrittsLogger()
      {

        @Override
        public void log(int prozent, String fortschrittsStr)
        {
          System.out.println(fortschrittsStr);
        }
      };
    }

    m_logger = logger;
  }

  @Override
  public void run()
  {
    m_sc = new Scanner(m_processStream);
    logProgress();
    m_sc.close();
  }

  protected void logProgress()
  {
    Pattern timePattern = Pattern.compile("\\(\\d{1,3}\\%\\)");
    String progressStr;
    while (null != (progressStr = m_sc.findWithinHorizon(timePattern, 0)))
    {
      String progressIntStr = progressStr.replace("(", "");
      progressIntStr = progressIntStr.replace("%)", "");

      int prozent = Integer.valueOf(progressIntStr);

      if (prozent <= 100)
      {
        m_logger.log(prozent, "Fortschritt: " + prozent + "%");
      }

    }
  }

  @Override
  protected void finalize() throws Throwable
  {
    m_sc.close();
  }

  protected int getSeconds(String timeStr) throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Date date = sdf.parse(timeStr);

    Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
    calendar.setTime(date); // assigns calendar to given date 
    int hour = calendar.get(Calendar.HOUR);
    int minutes = calendar.get(Calendar.MINUTE);
    int seconds = calendar.get(Calendar.SECOND);

    int rueckgabe = hour * 3600 + minutes * 60 + seconds;
    return rueckgabe;
  }
}