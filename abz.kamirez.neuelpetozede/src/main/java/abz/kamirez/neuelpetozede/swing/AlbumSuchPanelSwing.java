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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.swing.Table;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.DvYear;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.IAlbumSearchService;
import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.elpetozede.domain.service.IProgressInfo;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.SearchServiceException;
import abz.kamirez.elpetozede.service.albumsearch.impl.AlbumSearchServiceDummyImpl;
import abz.kamirez.elpetozede.service.albumsearch.mbrainzjws.MBrainzWSSearchServiceImpl;
import abz.kamirez.elpetozede.service.albumsearch.ws2.MBrainzWS2ServiceImpl;

public class AlbumSuchPanelSwing extends AbstraktElpetozedePanel
{
  private IAlbumSearchService m_searchService;

  private List<Album> m_albumMatches;

  private JLabel m_interpretLabel;
  private JTextField m_interpretField;
  private JLabel m_albumLabel;
  private JTextField m_albumField;
  private JLabel m_releaseTypeLabel;
  private JComboBox<String> m_releaseTypes;
  private JButton m_searchBtn;
  private JComboBox<SearchServiceModell> m_services;
  private JLabel m_resultsLabel;
  private JComboBox<String> m_results;
  private JLabel m_albumInfoLabel;
  private JTable m_albumTable;

  private Table m_albumGrid;

  public AlbumSuchPanelSwing(AlbumProject albumProject, IAlbumSearchService searchService, ResourceBundle messages,
      ILogger logger)
  {
    super(albumProject, messages, logger);
    m_searchService = searchService;

    m_albumMatches = new ArrayList<Album>();
    initGui();
  }

  @Override
  protected void initGui()
  {
    m_interpretLabel = new JLabel(m_messages.getString("label.interpret"));
    m_interpretField = new JTextField();
    m_interpretField.setText(getAlbum().getInterpret());

    m_interpretField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        m_interpretField.selectAll();
      }
    });

    m_albumLabel = new JLabel(m_messages.getString("label.album"));
    m_albumField = new JTextField();
    m_albumField.setText(getAlbum().getTitle());

    m_albumField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        m_albumField.selectAll();
      }
    });

    m_releaseTypeLabel = new JLabel(m_messages.getString("label.releasetypes"));
    m_releaseTypes = new JComboBox<String>();
    m_releaseTypes.addItem(IAlbumSearchService.RELEASE_REGULAR);
    m_releaseTypes.addItem(IAlbumSearchService.RELEASE_LIVE);
    m_releaseTypes.addItem(IAlbumSearchService.RELEASE_COMPILATION);

    m_services = new JComboBox<AlbumSuchPanelSwing.SearchServiceModell>();

    m_services.addItem(new SearchServiceModell("MusicBrainz-WebService (neu)", new MBrainzWS2ServiceImpl()));
    m_services.addItem(new SearchServiceModell("MusicBrainz-WebService (alt)", new MBrainzWSSearchServiceImpl()));
    m_services
      .addItem(new SearchServiceModell("Album ohne Internetsuche neu erstellen", new FreieAlbumEingabeService()));
    m_services.addItem(new SearchServiceModell("Dummy-Service zum Testen", new AlbumSearchServiceDummyImpl(700)));

    m_services.setSelectedItem(m_searchService);

    m_services.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        SearchServiceModell selectedModell = (SearchServiceModell) m_services.getSelectedItem();
        m_searchService = selectedModell.m_service;
      }
    });

    m_searchBtn = new JButton(m_messages.getString("button.albumSuche"));

    m_searchBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        search();
      }
    });

    m_resultsLabel = new JLabel(m_messages.getString("label.results"));
    m_results = new JComboBox<String>();

    m_results.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        albumSelected();
      }
    });

    m_albumInfoLabel = new JLabel("Ausgesuchtes Album: " + getAlbum().toString());
    m_albumTable = new JTable(new AlbumErgebnisTableModel());
    setColumnWidths();

    m_albumGrid = new Table();
    //m_albumGrid.debug();
    // m_albumGrid.align(BaseTableLayout.LEFT);
    add(m_albumGrid);

    int feldbreite = 400;

    int labelAlign = BaseTableLayout.LEFT;
    int fieldAlign = BaseTableLayout.LEFT;
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_interpretLabel).align(labelAlign);
    m_albumGrid.addCell(m_interpretField).minWidth(feldbreite).align(fieldAlign);
    //m_albumGrid.addCell(audioFilePanel).expandY();
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_albumLabel).align(labelAlign);
    m_albumGrid.addCell(m_albumField).minWidth(feldbreite).align(fieldAlign);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_releaseTypeLabel).align(labelAlign);
    m_albumGrid.addCell(m_releaseTypes).minWidth(feldbreite).align(fieldAlign);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_searchBtn).fillX();
    m_albumGrid.addCell(m_services).minWidth(feldbreite).align(fieldAlign);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_resultsLabel).align(labelAlign);
    m_albumGrid.addCell(m_results).minWidth(feldbreite).align(fieldAlign);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_albumInfoLabel).align(BaseTableLayout.LEFT).minWidth(feldbreite * 2).colspan(2);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(new JScrollPane(m_albumTable)).minWidth(feldbreite * 2).colspan(2).minHeight(220);
    repaint();
    validate();
  }

  public Table getAlbumGrid()
  {
    return m_albumGrid;
  }

  private void updateProject()
  {
    final String albumTitle = m_albumField.getText().trim();
    final String interpret = m_interpretField.getText().trim();

    m_albumProject.getAlbum().setTitle(albumTitle);
    m_albumProject.getAlbum().setInterpret(interpret);

    notifyAlbumChange();
  }

  private void search()
  {
    updateProject();

    if (isValidQuery())
    {
      startSearch(getAlbum().getTitle(), getAlbum().getInterpret());
    }
    else
    {
      JOptionPane.showMessageDialog(this, "Nothing found",
        "Please fill " + m_messages.getString("label.interpret") + "/" + m_messages.getString("label.album"),
        JOptionPane.WARNING_MESSAGE);
    }

  }

  private boolean isValidQuery()
  {
    String albumTitle = m_albumProject.getAlbum().getTitle();
    String interpret = m_albumProject.getAlbum().getInterpret();

    boolean rueckgabe = albumTitle.length() > 1 && interpret.length() > 1;

    return rueckgabe;
  }

  private void startSearch(String albumTitle, String interpret)
  {
    SearchServiceException searchEx = null;

    try
    {
      JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
      ProgressDialog progMonDlg = new ProgressDialog(topFrame, "Searching for Album", m_logger, false);
      SearchProgress search = new SearchProgress(progMonDlg, interpret, albumTitle,
        (String) m_releaseTypes.getSelectedItem());
      search.execute();

    }
    catch (Exception ex)
    {
      Throwable cause = ex;
      if (ex.getCause() != null)
      {
        cause = ex.getCause();
      }

      if (ex.getCause() instanceof SearchServiceException)
      {
        searchEx = (SearchServiceException) ex.getCause();
      }

      cause.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error: " + cause.getMessage(), "Database or Connection Problem",
        JOptionPane.ERROR_MESSAGE);
    }

    m_results.removeAllItems();

    if (searchEx != null)
    {
      JOptionPane.showMessageDialog(this, "Error: " + searchEx.getMessage(), "Database or Connection Problem",
        JOptionPane.ERROR_MESSAGE);
    }

  }

  private void albumSelected()
  {
    int albumIdx = m_results.getSelectedIndex();

    if (albumIdx > -1)
    {
      Album selectedAlbum = m_albumMatches.get(albumIdx);
      m_albumProject.setAlbum(selectedAlbum);
      updateAlbumInfos();
      notifyAlbumChange();
    }
  }

  private void updateAlbumInfos()
  {
    Album album = getAlbum();
    m_albumInfoLabel.setText("Ausgesuchtes Album: " + album.toString());

    List<Track> trackList = album.getTracks();

    String infoStr = "";

    for (int i = 0; i < trackList.size(); i++)
    {
      Track tempTrack = trackList.get(i);
      infoStr = infoStr + (tempTrack.toString()) + "\n";
    }

    m_albumTable.setModel(new AlbumErgebnisTableModel());
    setColumnWidths();
  }

  private void setColumnWidths()
  {
    for (int i = 0; i < m_albumTable.getModel().getColumnCount(); i++)
    {
      TableColumn tempCol = m_albumTable.getColumnModel().getColumn(i);

      int relSize = 160;

      if (i == 2)
      {
        relSize = 20;
      }

      tempCol.setPreferredWidth(relSize);
    }
  }

  @Override
  protected void updatePanel()
  {
    Album album = getAlbum();

    m_interpretField.setText(album.getInterpret());
    m_albumField.setText(album.getTitle());

    updateAlbumInfos();
  }

  private class SearchProgress extends SwingWorker<Void, Void> implements IProgressListener
  {
    private ProgressDialog m_dlg;
    private String m_interpret;
    private String m_albumTitle;
    private String m_releaseType;

    private SearchProgress(ProgressDialog progDlg, final String interpret, final String albumTitle,
        final String releaseType)
    {
      m_dlg = progDlg;
      m_interpret = interpret;
      m_albumTitle = albumTitle;
      m_releaseType = releaseType;
    }

    @Override
    public Void doInBackground() throws SearchServiceException
    {
      try
      {
        m_dlg.setNote("Searchin for the Album");
        m_dlg.setProgress(0);
        m_dlg.open();
        m_searchService.addProgressListener(SearchProgress.this);
        m_albumMatches = m_searchService.findAlbums(m_interpret, m_albumTitle, m_releaseType);
      }
      catch (Exception sersEx)
      {
        m_searchService.removeProgressListener(SearchProgress.this);
        m_dlg.close();
        done();
        throw sersEx;
      }

      return null;
    }

    @Override
    protected void done()
    {
      try
      {
        get();
        m_searchService.removeProgressListener(SearchProgress.this);
        m_dlg.close();
      }
      catch (final InterruptedException ex)
      {
        throw new RuntimeException(ex);
      }
      catch (final ExecutionException ex)
      {
        throw new RuntimeException(ex.getCause());
      }
      finally
      {
        m_searchService.removeProgressListener(SearchProgress.this);
        m_dlg.close();
      }

      if (m_albumMatches.size() > 0)
      {
        for (int i = 0; i < m_albumMatches.size(); i++)
        {
          m_results.addItem(m_albumMatches.get(i).toString());
        }

        if (m_albumMatches.size() > 0)
        {
          m_results.setSelectedIndex(0);
          albumSelected();
        }
      }
      else
      {
        JOptionPane.showMessageDialog(AlbumSuchPanelSwing.this, "Nothing found", "Sorry, no Album-Infos found!",
          JOptionPane.INFORMATION_MESSAGE);
      }

      m_logger.log("");
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

  private class SearchServiceModell
  {
    private String m_name;
    private IAlbumSearchService m_service;

    public SearchServiceModell(String name, IAlbumSearchService service)
    {
      super();
      m_name = name;
      m_service = service;
    }

    @Override
    public String toString()
    {
      return m_name;
    }

  }

  private class AlbumErgebnisTableModel extends AbstractTableModel
  {

    @Override
    public int getRowCount()
    {
      return getAlbum().getTrackCount();
    }

    @Override
    public int getColumnCount()
    {
      return 3;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
      String rueckgabe = "";
      switch (columnIndex)
      {
      case 0:
        rueckgabe = m_messages.getString("table.column.artist");
        break;
      case 1:
        rueckgabe = m_messages.getString("table.column.title");
        break;
      case 2:
        rueckgabe = m_messages.getString("table.column.duration");
        break;

      }
      return rueckgabe;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      String rueckgabe = "";

      Track track = getAlbum().getTracks().get(rowIndex);

      switch (columnIndex)
      {
      case 0:
        rueckgabe = track.getArtist();
        break;
      case 1:
        rueckgabe = track.getName();
        break;
      case 2:
        rueckgabe = track.getTime().toString();
        break;
      }
      return rueckgabe;
    }

  }

  private class FreieAlbumEingabeService implements IAlbumSearchService
  {

    @Override
    public void addProgressListener(IProgressListener progListener)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void removeProgressListener(IProgressListener progListener)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public List<Album> findAlbums(String interpret, String title, String releaseType) throws SearchServiceException
    {
      List<Album> rueckgabe = new ArrayList<>();

      Album freiesAlbum = new Album(m_albumProject, title, interpret, DvYear.getNullValue());
      rueckgabe.add(freiesAlbum);
      freiesAlbum.addTrack(new Track(freiesAlbum, "Titel1", interpret, DvTime.valueOf(60)));

      return rueckgabe;
    }

  }
}
