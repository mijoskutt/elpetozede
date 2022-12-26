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
package abz.kamirez.elpetozede.domain.service;

import abz.kamirez.elpetozede.domain.material.Track;

public class WriterUtil
{
  public static String createNormalizedFileName(Track track)
  {
    String rueckgabe = "";
    StringBuilder builder = new StringBuilder();

    String trackName = track.getName();

    for (int i = 0; i < trackName.length(); i++)
    {
      Character tempChar = trackName.charAt(i);

      if (Character.isLetterOrDigit(tempChar))
      {
        builder.append(replaceUmlaut(tempChar));
      }
      else
      {
        builder.append('_');
      }
    }

    rueckgabe = builder.toString();

    return rueckgabe;
  }

  protected static String replaceUmlaut(Character tempChar)
  {
    String rueckgabe = tempChar + "";

    boolean isUpperCase = Character.isUpperCase(tempChar);

    Character lcChar = tempChar;

    if (isUpperCase)
    {
      lcChar = Character.toLowerCase(tempChar);
    }

    if (lcChar.equals('ä'))
    {
      rueckgabe = "ae";
    }
    else if (lcChar.equals('ü'))
    {
      rueckgabe = "ue";
    }
    else if (lcChar.equals('ö'))
    {
      rueckgabe = "oe";
    }
    else if (lcChar.equals('ß'))
    {
      rueckgabe = "ss";
    }

    if (isUpperCase && rueckgabe.length() == 2)
    {
      rueckgabe = Character.toUpperCase(rueckgabe.charAt(0)) + rueckgabe.charAt(1) + "";
    }

    return rueckgabe;
  }

}
