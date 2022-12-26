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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import abz.kamirez.elpetozede.domain.service.ILogger;

public class ProgressDialog extends JDialog
{

  private JProgressBar m_progressBar;
  private JLabel m_progressLabel;
  private boolean m_isIndeterminate;
  private int m_width;
  private int m_height;

  public ProgressDialog(JFrame frame, String title, ILogger logger, boolean isIndeterminate)
  {
    super(frame);
    frame.setEnabled(false);
    m_width = 550;
    m_height = 150;
    m_isIndeterminate = isIndeterminate;
    createDialog(title);
  }

  @Override
  public void setSize(Dimension dim)
  {
    super.setSize(dim);
    m_width = dim.width;
    m_height = dim.height;
  }

  protected void createDialog(String title)
  {
    setLayout(new GridLayout(2, 1, 20, 20));

    setTitle(title);

    m_progressLabel = new JLabel();
    m_progressLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
    m_progressLabel.setAlignmentX(CENTER_ALIGNMENT);

    JPanel progressPanel = new JPanel();
    progressPanel.setLayout(new BorderLayout());

    m_progressBar = new JProgressBar(0, 100);
    m_progressBar.setValue(0);
    m_progressBar.setStringPainted(m_isIndeterminate == false);
    m_progressBar.setIndeterminate(m_isIndeterminate);
    progressPanel.add(m_progressBar, BorderLayout.NORTH);

    add(m_progressLabel);
    add(progressPanel);

  }

  public void open()
  {
    pack();
    setLocationRelativeTo(getOwner());
    setSize(m_width, m_height);
    setVisible(true);
  }

  public void close()
  {
    setVisible(false);
    getOwner().setEnabled(true);
  }

  public void setProgress(int progress)
  {
    m_progressBar.setIndeterminate(false);
    m_progressBar.setValue(progress);
  }

  public void setNote(String text)
  {
    m_progressLabel.setText(text);
  }
}
