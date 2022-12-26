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
package abz.kamirez.ui.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ModelChangeMediator implements IModelChangeMediator
{
  private PropertyChangeSupport m_propSupporter;
  private Object m_model;

  private final static String DEFAULT_PROPNAME = "Model";

  public ModelChangeMediator()
  {

  }

  @Override
  public void setModel(Object model)
  {
    boolean changed = m_model != model;
    m_model = model;

    PropertyChangeListener[] propertyChangeListeners = null;

    if (m_propSupporter != null)
    {
      propertyChangeListeners = m_propSupporter.getPropertyChangeListeners();
    }
    m_propSupporter = new PropertyChangeSupport(model);

    if (propertyChangeListeners != null)
    {
      for (int i = 0; i < propertyChangeListeners.length; i++)
      {
        PropertyChangeListener propertyChangeListener = propertyChangeListeners[i];
        m_propSupporter.addPropertyChangeListener(DEFAULT_PROPNAME, propertyChangeListener);

      }
    }

    if (changed && (m_model != null))
    {
      propertyChange(new PropertyChangeEvent(this, DEFAULT_PROPNAME, null, m_model));
    }
  }

  @Override
  public Object getModel()
  {
    return m_model;
  }

  @Override
  public void modelModified(Object model, Object modifier)
  {
    propertyChange(new PropertyChangeEvent(modifier, DEFAULT_PROPNAME, null, model));
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    m_propSupporter.addPropertyChangeListener(DEFAULT_PROPNAME, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    m_propSupporter.removePropertyChangeListener(listener);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    m_propSupporter.firePropertyChange(DEFAULT_PROPNAME, null, m_model);
  }

}
