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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.service.AlbumProjectXMLStore;
import abz.kamirez.elpetozede.domain.service.IAlbumProjectPersistence;
import abz.kamirez.elpetozede.service.albumsearch.ws2.MBrainzWS2ServiceImpl;
import abz.kamirez.ui.common.IModelChangeMediator;
import abz.kamirez.ui.common.ModelChangeMediatorProvider;

public class ElpetozedeMain extends javax.swing.JFrame implements PropertyChangeListener
{
  private static Color NAVITEM_FONTCOLOR_SELECTED = new Color(96, 35, 32);
  private static Color NAVITEM_FONTCOLOR_UNSELECTED = Color.WHITE;

  private JMenuBar m_hauptMenu;
  private AlbumProject m_albumProject;
  private JProgressBar m_memoryBar;
  private ResourceBundle m_messages;
  private StatusLineLoggerSwing m_logger;
  private Properties m_elpetozedeProps;
  private AlbumSuchPanelSwing m_albumPanel;
  private JPanel m_navPanel;
  private JPanel m_contentPanel;

  private List<NavItem> m_navItems;
  private JLabel m_statusLabel;
  private AlbumEditorPanelSwing m_editorPanel;
  private ExportPanelSwing m_exportPanel;
  private JLabel m_memoryLabel;

  public ElpetozedeMain()
  {
    try
    {
      m_elpetozedeProps = loadProperties();
    }
    catch (IOException ioex)
    {

    }
    m_navItems = new ArrayList<>();

    Locale locale = Locale.getDefault();

    if (locale.getCountry().equals(Locale.GERMANY) == false)
    {
      locale = new Locale("en", "US");
    }

    m_messages = ResourceBundle.getBundle("MessageBundle", locale);

    initModel();
    JPanel statusPanel = createStatusPanel();

    getContentPane().setLayout(new BorderLayout(4, 10));
    // NavigationsPanel
    m_navPanel = createNavPanel();
    getContentPane().add(m_navPanel, BorderLayout.NORTH);
    m_navPanel.setPreferredSize(new Dimension(0, 60));

    //ContentPanel
    m_contentPanel = new JPanel();
    m_contentPanel.setLayout(new GridLayout(1, 1));
    m_contentPanel.setBackground(Color.WHITE);
    getContentPane().add(m_contentPanel, BorderLayout.CENTER);

    m_logger = new StatusLineLoggerSwing(m_statusLabel);

    m_albumPanel = new AlbumSuchPanelSwing(m_albumProject, new MBrainzWS2ServiceImpl(), m_messages, m_logger);
    NavItem suchItem = addPanelToNav("Nach Album suchen", m_albumPanel, true);

    // bearbeiten/zuordnen/zuschneiden
    JPanel editWavePanel = new JPanel();
    editWavePanel.setLayout(new GridLayout(2, 1));

    m_editorPanel = new AlbumEditorPanelSwing(m_albumProject, m_messages, m_logger);
    addPanelToNav("Zuordnen und Zuschneiden", m_editorPanel, true);

    m_exportPanel = new ExportPanelSwing(m_albumProject, m_messages, m_logger, m_elpetozedeProps);
    addPanelToNav("Exportieren", m_exportPanel, false);

    handleNavEvent(suchItem);

    getContentPane().add(statusPanel, BorderLayout.SOUTH);

    initMenus();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setSize(1200, 800);
    setTitle("Elpetozede: " + m_albumProject.getAlbum().toString());
    updateMemInfo(m_memoryBar);

    URL iconURL = getClass().getResource("/resources/elpetozede_24.png");
    // iconURL is null when not found
    ImageIcon icon = new ImageIcon("./resources/elpetozede_24.png");
    setIconImage(icon.getImage());

    getContentPane().repaint();

    SplashScreen splashScreen = SplashScreen.getSplashScreen();

    if (splashScreen != null && splashScreen.isVisible())
    {
      splashScreen.close();
    }

  }

  private JPanel createNavPanel()
  {
    JPanel navPanel = new JPanel();
    navPanel.setLayout(new GridLayout(1, 3));

    return navPanel;
  }

  private JPanel createStatusPanel()
  {
    JPanel statusPanel = new JPanel();
    statusPanel.setBorder(new EmptyBorder(4, 10, 4, 10));
    statusPanel.setLayout(new GridLayout(1, 2));
    m_statusLabel = new JLabel("Status");

    JPanel memoryStatPanel = new JPanel();
    memoryStatPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    m_memoryLabel = new JLabel(m_messages.getString("label.memory_usage"));
    m_memoryBar = new JProgressBar();

    memoryStatPanel.add(m_memoryLabel);
    memoryStatPanel.add(m_memoryBar);

    statusPanel.add(m_statusLabel);
    statusPanel.add(memoryStatPanel);

    return statusPanel;
  }

  private NavItem addPanelToNav(String titleText, JPanel panel, boolean withNextStep)
  {
    NavItem newNavItem = new NavItem(titleText, panel, withNextStep);
    m_navItems.add(newNavItem);
    return newNavItem;
  }

  private void initModel()
  {
    m_albumProject = new AlbumProject();

    String defaultDirName = m_elpetozedeProps.getProperty("default.directory", "");

    if (defaultDirName.startsWith("[USER_HOME]"))
    {
      defaultDirName = defaultDirName.replace("[USER_HOME]", "");
      String userHomeDir = System.getProperty("user.home");
      defaultDirName = userHomeDir + defaultDirName;
    }

    File defaultDir = new File(defaultDirName);

    if (defaultDir.exists())
    {
      try
      {
        m_albumProject.setFilePath(defaultDir.getCanonicalPath());
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    IModelChangeMediator mediator = ModelChangeMediatorProvider.getMediator();
    mediator.setModel(m_albumProject);
    ModelChangeMediatorProvider.getMediator().modelModified(m_albumProject, this);
    mediator.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    setTitle("Elpetozede: " + m_albumProject.getAlbum().toString());
    updateMemInfo(m_memoryBar);
  }

  private void updateMemInfo(JProgressBar progbar)
  {
    long totalMem = Runtime.getRuntime().totalMemory();

    long freeMem = Runtime.getRuntime().freeMemory();

    int mb = 1024 * 1024;

    long totalMemMB = totalMem / mb;
    long freeMemMB = freeMem / mb;

    progbar.setMaximum((int) totalMemMB);
    progbar.setValue((int) (totalMemMB - freeMemMB));

    String memoryLabelText = m_messages.getString("label.memory_usage") + " (" + (totalMemMB - freeMemMB) + "MB / "
        + totalMemMB + "MB)";
    m_memoryLabel.setText(memoryLabelText);
  }

  private void initMenus()
  {
    JMenuBar menubar = new JMenuBar();

    JMenu fileMenu = new JMenu(m_messages.getString("menu.file"));
    menubar.add(fileMenu);

    JMenuItem openItem = new JMenuItem(m_messages.getString("menu.file.open"));
    JMenuItem saveItem = new JMenuItem(m_messages.getString("menu.file.save"));
    JMenuItem saveAsItem = new JMenuItem(m_messages.getString("menu.file.save_as"));
    JMenuItem exitItem = new JMenuItem(m_messages.getString("menu.file.exit"));

    fileMenu.add(openItem);

    openItem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        openProject();
      }

    });

    // fileMenu.add(saveItem);

    saveAsItem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveProject();
      }
    });

    exitItem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        exit();
      }
    });

    fileMenu.add(saveAsItem);
    fileMenu.add(exitItem);

    setJMenuBar(menubar);

  }

  protected void exit()
  {
    int antwort = JOptionPane.showConfirmDialog(this, m_messages.getString("menu.file.exit_msg"),
      m_messages.getString("menu.file.exit"), JOptionPane.OK_CANCEL_OPTION);

    if (antwort == JOptionPane.OK_OPTION)
    {
      System.exit(0);
    }

  }

  private void saveProject()
  {
    JFileChooser chooser = new JFileChooser(m_albumProject.getFilePath());
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Elpetozede-Dateien", "*.xml");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File fileSelected = chooser.getSelectedFile();
      try
      {
        String selFileName = fileSelected.getCanonicalPath();

        if (selFileName.toLowerCase().endsWith(".xml") == false)
        {
          fileSelected = new File(selFileName + ".xml");
        }

        IAlbumProjectPersistence persistenceService = new AlbumProjectXMLStore();

        persistenceService.saveProject(m_albumProject, fileSelected.getAbsolutePath());
      }
      catch (IOException ioex)
      {
        ioex.printStackTrace();
        String msg = m_messages.getString("menu.file.saveerror_msg") + ": " + ioex.getMessage();
        JOptionPane.showMessageDialog(ElpetozedeMain.this, msg, "File Problem", JOptionPane.ERROR_MESSAGE);

      }
    }
  }

  private void openProject()
  {
    JFileChooser chooser = new JFileChooser(m_albumProject.getFilePath());
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Elpetozede-Dateien", "xml");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile();

      try
      {
        String fullName = file.getCanonicalPath();

        Cursor cursorDefault = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        load(fullName);
        setCursor(cursorDefault);
      }
      catch (IOException e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }

  }

  protected void load(final String selected) throws IOException
  {
    String openMessage = m_messages.getString("menu.file.openprogress_msg") + " " + selected + "...";

    IExceptionHandler exhandler = new MessageBoxExceptionHandler(this, m_messages.getString("menu.file.openerror_msg"));

    ProgressDialog progDlg = new ProgressDialog(this, m_messages.getString("albumeditor.loadprogress.title"), m_logger,
      true);

    LoadProgressSwing<AlbumProject> loadProgress = new LoadProgressSwing<AlbumProject>(progDlg, openMessage, m_logger,
      new IFileLoader<AlbumProject>()
      {

        @Override
        public AlbumProject load()
        {
          IAlbumProjectPersistence persistenceService = new AlbumProjectXMLStore();
          AlbumProject newproj = null;
          try
          {
            newproj = persistenceService.openProject(selected);

            if (newproj != null)
            {

              if (newproj.getFilePath() == null)
              {
                File projFile = new File(selected);
                String projPath = projFile.getParent();

                newproj.setFilePath(projPath);
                m_albumProject = newproj;
              }

              ModelChangeMediatorProvider.getMediator().setModel(m_albumProject);

              m_albumPanel.setModel(m_albumProject);
              m_editorPanel.setModel(m_albumProject);
              m_exportPanel.setModel(m_albumProject);
            }
          }
          catch (IOException e)
          {
            exhandler.handleException(e);
          }
          return newproj;
        }
      }, exhandler);

    loadProgress.execute();

  }

  public Properties loadProperties() throws IOException
  {
    Properties props = new Properties();
    String propFile = System.getProperty("PROP_FILE", "./resources/elpetozede.properties");

    FileInputStream fis = new FileInputStream(propFile);
    props.load(fis);
    fis.close();

    return props;
  }

  private void handleNavEvent(NavItem navItem)
  {

    for (NavItem tempNavItem : m_navItems)
    {
      tempNavItem.setSelected(tempNavItem == navItem);
    }

    m_contentPanel.removeAll();
    JPanel contentPanel = navItem.getContentPanel();
    m_contentPanel.add(contentPanel);

    if (contentPanel instanceof AbstraktElpetozedePanel)
    {
      ((AbstraktElpetozedePanel) contentPanel).updatePanel();
    }

    contentPanel.setBackground(Color.WHITE);

    m_contentPanel.repaint();
    m_contentPanel.validate();
  }

  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
    * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
    */
    try
    {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
      {
        if ("Nimbus".equals(info.getName()))
        {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(ElpetozedeMain.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (InstantiationException ex)
    {
      Logger.getLogger(ElpetozedeMain.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IllegalAccessException ex)
    {
      Logger.getLogger(ElpetozedeMain.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (javax.swing.UnsupportedLookAndFeelException ex)
    {
      Logger.getLogger(ElpetozedeMain.class.getName()).log(Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      @Override
      public void run()
      {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null)
        {
          System.out.println("SplashScreen.getSplashScreen() returned null");

        }
        else
        {
          Graphics2D g = splash.createGraphics();
          if (g == null)
          {
            System.out.println("g is null");
          }
          else
          {

            try
            {
              Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {

            }
          }
        }
        new ElpetozedeMain().setVisible(true);
      }
    });
  }

  private class NavItem extends JPanel
  {
    private List<JLabel> m_navLabelList;
    private JPanel m_inhaltsPanel;
    private String m_titleText;

    public NavItem(String titleText, JPanel inhaltsPanel, boolean withNextStep)
    {
      m_navLabelList = new ArrayList<>();
      m_titleText = titleText;
      m_inhaltsPanel = inhaltsPanel;
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

      setBackground(new Color(243, 210, 85));
      setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));

      int itemHeight = 100;

      JLabel navItemNumberLabel = new JLabel();
      navItemNumberLabel.setText(m_navItems.size() + 1 + ".");
      navItemNumberLabel.setMinimumSize(new Dimension(150, itemHeight));
      add(Box.createRigidArea(new Dimension(20, itemHeight)));
      add(navItemNumberLabel);
      add(Box.createRigidArea(new Dimension(20, itemHeight)));
      m_navLabelList.add(navItemNumberLabel);

      JLabel navItemLabel = new JLabel();
      navItemLabel.setForeground(NAVITEM_FONTCOLOR_UNSELECTED);
      navItemLabel.setText(titleText);
      navItemLabel.setMinimumSize(new Dimension(500, itemHeight));
      add(navItemLabel);
      add(Box.createHorizontalGlue());
      m_navLabelList.add(navItemLabel);

      JLabel navItemNextLabel = new JLabel();

      if (withNextStep)
      {
        navItemNextLabel.setText(">");
      }
      navItemNextLabel.setMinimumSize(new Dimension(150, itemHeight));
      add(navItemNextLabel);
      add(Box.createRigidArea(new Dimension(20, itemHeight)));
      m_navLabelList.add(navItemNextLabel);

      m_contentPanel.add(inhaltsPanel, titleText);
      m_navPanel.add(this);

      addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          handleNavEvent(NavItem.this);
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
          setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

      });

      for (JLabel tempLabel : m_navLabelList)
      {
        tempLabel.setFont(new Font("Arial", Font.BOLD, 18));
        tempLabel.setForeground(NAVITEM_FONTCOLOR_UNSELECTED);
      }

      setMaximumSize(new Dimension(10000, itemHeight + 10));
    }

    public JPanel getContentPanel()
    {
      return m_inhaltsPanel;
    }

    public void setSelected(boolean isSelected)
    {
      Color labelCol = NAVITEM_FONTCOLOR_UNSELECTED;

      if (isSelected)
      {
        labelCol = NAVITEM_FONTCOLOR_SELECTED;
        m_inhaltsPanel.repaint();
        m_inhaltsPanel.validate();
      }

      for (JLabel tempLabel : m_navLabelList)
      {
        tempLabel.setForeground(labelCol);
      }

    }

  }

}
