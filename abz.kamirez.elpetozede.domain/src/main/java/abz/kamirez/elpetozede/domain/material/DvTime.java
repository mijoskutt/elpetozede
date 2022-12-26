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
package abz.kamirez.elpetozede.domain.material;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DvTime
{

  private int m_minutes;
  private int m_seconds;
  
  private static Time24HoursValidator s_validator = new Time24HoursValidator();

  protected DvTime(int minutes, int seconds)
  {
    assert minutes >= 0 && seconds >= 0 : "minutes >= 0 && seconds >= 0";
    m_minutes = minutes;
    m_seconds = seconds;
  }

  public static DvTime valueOf(int minutes, int seconds)
  {
    return new DvTime(minutes, seconds);
  }

  public static DvTime valueOf(int seconds)
  {
    int mins = seconds / 60;
    int secs = seconds % 60;

    return new DvTime(mins, secs);
  }

  public static DvTime valueOf(long millis)
  {
    int inSeconds = (int) Math.round((double) millis / 1000.0);

    int minutes = inSeconds / 60;
    int seconds = inSeconds % 60;

    return new DvTime(minutes, seconds);
  }

  public static DvTime getNullValue()
  {
    return valueOf(0, 0);
  }

  public boolean isNullValue()
  {
    return m_minutes == 0 && m_seconds == 0;
  }

  public int getMinutes()
  {
    return m_minutes;
  }

  public int getSeconds()
  {
    return m_seconds;
  }

  public int getNumberOfSeconds()
  {
    int rueckgabe = getMinutes() * 60 + getSeconds();
    return rueckgabe;
  }

  @Override
  public String toString()
  {
    String rueckgabe = "";

    if (isNullValue() == false)
    {
      String secondStr = getSeconds() + "";
      if (secondStr.length() < 2)
      {
        secondStr = "0" + secondStr;
      }

      rueckgabe = getMinutes() + ":" + secondStr;
    }
    return rueckgabe;
  }

  public DvTime add(DvTime otherTime)
  {
    DvTime rueckgabe = DvTime.valueOf(getNumberOfSeconds() + otherTime.getNumberOfSeconds());
    return rueckgabe;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_minutes;
    result = prime * result + m_seconds;
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DvTime other = (DvTime) obj;
    if (m_minutes != other.m_minutes)
      return false;
    if (m_seconds != other.m_seconds)
      return false;
    return true;
  }

  public static DvTime valueOf(String timeStr)
  {
    if (s_validator.validate(timeStr))
    {
    
    DvTime result = getNullValue();

    if (timeStr != null && timeStr.trim().length() > 0)
    {
      Calendar cal = convertString(timeStr.trim());

      if (cal != null)
      {
        if (cal != null)
        {
          int minute = cal.get(Calendar.MINUTE);
          int second = cal.get(Calendar.SECOND);
          result = valueOf(minute, second);
        }
      }
    }

    return result;
    }
    else
    {
      throw new IllegalArgumentException("Illegal String for DvTime: " + timeStr);
    }
  }
  
  public static boolean isValidTimeString(String timeStr)
  {
    return s_validator.validate(timeStr);
  }

  /**
   * Wandelt einen String der Form "HH:mm" in einen Calendar-Wert um.
   * Wenn das nicht möglich ist, wird null zurueckgegeben
   *
   * @require zpString != null
   */
  private static Calendar convertString(String zpString)
  {
    Calendar rueckgabe = Calendar.getInstance();

    SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    formatter.setLenient(false);
    ParsePosition pos = new ParsePosition(0);

    Date zpDate = formatter.parse(zpString.trim(), pos);

    if (zpDate != null)
    {
      rueckgabe.setTime(zpDate);
    }
    else
    {
      rueckgabe = null;
    }

    return rueckgabe;
  }

  public static class Time24HoursValidator
  {

    private Pattern pattern;
    private Matcher matcher;

    private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

    public Time24HoursValidator()
    {
      pattern = Pattern.compile(TIME24HOURS_PATTERN);
    }

    /**
     * Validate time in 24 hours format with regular expression
     * @param time time address for validation
     * @return true valid time fromat, false invalid time format
     */
    public boolean validate(final String time)
    {
      boolean rueckgabe = false;
      
      if (time != null) {
        matcher = pattern.matcher(time);
        rueckgabe = matcher.matches();
      }
      
      return rueckgabe;
    }
  }

}
