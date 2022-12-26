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

public class DvYear
{
  private int m_year;

  protected DvYear(int year)
  {
    super();
    m_year = year;
  }

  public static DvYear valueOf(int year)
  {
    assert year == 0 || (year > 1900 && year < 2100) : "year == 0 || (year > 1900 && year < 2100)";
    return new DvYear(year);
  }
  
  public static DvYear getNullValue()
  {
    return valueOf(0);
  }
  
  public boolean isNullValue()
  {
    return m_year == 0;
  }
  

  public int getYear()
  {
    return m_year;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_year;
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
    DvYear other = (DvYear) obj;
    if (m_year != other.m_year)
      return false;
    return true;
  }

}
