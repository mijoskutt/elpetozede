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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.ui.common.IModelChangeMediator;
import abz.kamirez.ui.common.ModelChangeMediatorProvider;

public abstract class AbstraktElpetozedePanel extends JPanel implements PropertyChangeListener
{
  protected AlbumProject m_albumProject;
  protected ILogger m_logger;
  protected ResourceBundle m_messages;

  public AbstraktElpetozedePanel(AlbumProject albumProject, ResourceBundle messages, ILogger logger)
  {
    m_albumProject = albumProject;
    m_messages = messages;
    m_logger = logger;

    getMediator().addPropertyChangeListener(this);
  }

  protected abstract void initGui();

  protected abstract void updatePanel();

  protected Album getAlbum()
  {
    return m_albumProject.getAlbum();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (getMediator().getModel() != m_albumProject)
    {
      m_albumProject = (AlbumProject) getMediator().getModel();
    }
    updatePanel();
  }

  protected void notifyAlbumChange()
  {
    getMediator().modelModified(m_albumProject, this);
  }

  protected IModelChangeMediator getMediator()
  {
    return ModelChangeMediatorProvider.getMediator();
  }

  public void setModel(AlbumProject newProject)
  {
    if (newProject != m_albumProject)
    {
      m_albumProject = newProject;
      updatePanel();
    }
  }
}
