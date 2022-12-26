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
package abz.kamirez.neuelpetozede.swing;

import java.awt.Component;

import javax.swing.JOptionPane;

public class MessageBoxExceptionHandler implements IExceptionHandler
{

  private String m_fehlerMeldung;
  private Component m_parentComp;

  public MessageBoxExceptionHandler(Component parent, String fehlerMeldung)
  {
    m_parentComp = parent;
    m_fehlerMeldung = fehlerMeldung;

  }

  @Override
  public void handleException(Exception ex)
  {
    ex.printStackTrace();
    String msg = m_fehlerMeldung + ": " + ex.getMessage();
    JOptionPane.showMessageDialog(m_parentComp, msg, "File Problem", JOptionPane.ERROR_MESSAGE);
  }

}
