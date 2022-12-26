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

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.elpetozede.domain.service.IProgressInfo;
import abz.kamirez.elpetozede.domain.service.IProgressListener;

public class LoadProgressSwing<T> extends SwingWorker<T, Void> implements IProgressListener
{
  private ProgressDialog m_dlg;
  private ILogger m_logger;
  private IFileLoader<T> m_loader;
  private T m_result;
  private String m_message;
  private Exception m_exception;
  private IExceptionHandler m_exHandler;

  public LoadProgressSwing(ProgressDialog progDlg, String message, ILogger logger, IFileLoader<T> loader,
      IExceptionHandler exHandler)
  {
    m_dlg = progDlg;
    m_message = message;
    m_logger = logger;
    m_loader = loader;
    m_exHandler = exHandler;
    m_exception = null;
  }

  @Override
  public T doInBackground()
  {
    try
    {
      m_dlg.setNote(m_message);
      m_dlg.open();
      m_result = m_loader.load();
    }
    catch (Exception ex)
    {
      m_dlg.close();
      m_exception = ex;
    }

    return m_result;
  }

  @Override
  protected void done()
  {
    try
    {
      get();
    }
    catch (final InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
    catch (final ExecutionException ex)
    {
      throw new RuntimeException(ex.getCause());
    }
    m_dlg.close();

    if (m_exception != null)
    {
      m_exHandler.handleException(m_exception);
    }
  }

  @Override
  public void progressReported(IProgressInfo progInfo)
  {
    final int newWork = progInfo.getPercentage();
    final String msg = progInfo.getMessage();
    m_dlg.setProgress(newWork);
    m_dlg.setNote(msg);
    m_logger.log(msg);
  }

}
