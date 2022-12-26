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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.swing.Table;

import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.AudioFile;
import abz.kamirez.elpetozede.domain.material.DvTime;
import abz.kamirez.elpetozede.domain.material.IAudioFile;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.ILogger;

public class AlbumEditorPanelSwing extends AbstraktElpetozedePanel
{
  private JLabel m_albumInfoLabel;
  private JTable m_albumTable;

  private JButton m_assignTracksToFileBtn;
  private JButton m_addFileBtn;
  private JList<IAudioFile> m_audioFileList;

  private JTabbedPane m_waveTabPanel;

  private Table m_albumGrid;

  public AlbumEditorPanelSwing(AlbumProject albumProject, ResourceBundle messages, ILogger logger)
  {
    super(albumProject, messages, logger);
    initGui();
  }

  @Override
  protected void initGui()
  {
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    JPanel editorPanel = new JPanel();
    editorPanel.setBackground(Color.WHITE);

    m_albumInfoLabel = new JLabel("Ausgesuchtes Album: " + getAlbum().toString());
    m_albumTable = new JTable(new AlbumTableModel());
    setColumnWidths();
    m_albumTable.addMouseListener(new TableMouseListener(m_albumTable));
    m_albumTable.setComponentPopupMenu(createRowPopupMenu());

    m_assignTracksToFileBtn = new JButton(m_messages.getString("button.assign_tracks_to_file"));
    m_assignTracksToFileBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        assignTracksToFile();
      }
    });

    JPanel audioFilePanel = new JPanel();

    JPanel audioBtnPanel = new JPanel();
    audioBtnPanel.setLayout(new GridLayout(0, 1));

    m_addFileBtn = new JButton(m_messages.getString("button.add_audio_file"));

    m_addFileBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        addAudioFile();
      }
    });

    Table audioFileGrid = new Table();
    audioFilePanel.add(audioFileGrid);
    // audioFileGrid.debug();

    int fileListWidth = 200;
    m_audioFileList = new JList<IAudioFile>();
    m_audioFileList.setVisibleRowCount(4);
    m_audioFileList.setFixedCellWidth(fileListWidth);

    audioBtnPanel.add(m_assignTracksToFileBtn);

    audioFileGrid.addCell(m_addFileBtn).minWidth(fileListWidth);
    audioFileGrid.addCell(new JLabel(""));
    audioFileGrid.row();
    audioFileGrid.addCell(m_audioFileList).expandY().minWidth(fileListWidth);
    audioFileGrid.addCell(audioBtnPanel);
    audioFileGrid.row();

    m_albumGrid = new Table();
    //m_albumGrid.debug();
    // m_albumGrid.align(BaseTableLayout.LEFT);
    editorPanel.add(m_albumGrid);

    int feldbreite = 400;

    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(m_albumInfoLabel).align(BaseTableLayout.LEFT).minWidth(feldbreite * 2).colspan(2);
    m_albumGrid.row().pad(2, 10, 2, 10);
    m_albumGrid.addCell(audioFilePanel).fill();
    m_albumGrid.addCell(new JScrollPane(m_albumTable)).minWidth(feldbreite * 2).minHeight(220);

    add(editorPanel);

    m_waveTabPanel = new JTabbedPane();

    add(m_waveTabPanel);

    repaint();
    validate();
  }

  private JPopupMenu createRowPopupMenu()
  {
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem menuItemAdd = new JMenuItem(m_messages.getString("editor.table.popupmenu.add_track"));
    JMenuItem menuItemRemove = new JMenuItem(m_messages.getString("editor.table.popupmenu.delete_track"));
    JMenuItem menuItemMoveUp = new JMenuItem(m_messages.getString("editor.table.popupmenu.moveup_track"));
    JMenuItem menuItemMoveDown = new JMenuItem(m_messages.getString("editor.table.popupmenu.movedown_track"));

    menuItemAdd.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        int selectedRow = m_albumTable.getSelectedRow();
        insertTrack(selectedRow);
      }
    });

    menuItemRemove.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        int selectedRow = m_albumTable.getSelectedRow();
        deleteTrack(selectedRow);
      }
    });

    menuItemMoveUp.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        int selectedRow = m_albumTable.getSelectedRow();
        moveTrack(selectedRow, true);
      }
    });

    menuItemMoveDown.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        int selectedRow = m_albumTable.getSelectedRow();
        moveTrack(selectedRow, false);
      }
    });

    popupMenu.add(menuItemAdd);
    popupMenu.add(menuItemRemove);
    popupMenu.add(menuItemMoveUp);
    popupMenu.add(menuItemMoveDown);

    return popupMenu;
  }

  public Table getAlbumGrid()
  {
    return m_albumGrid;
  }

  private void updateProject()
  {
    m_audioFileList.setListData(new Vector<>(m_albumProject.getAudioFiles()));

    notifyAlbumChange();
  }

  private void insertTrack(int selectedRow)
  {
    Track newTrack = new Track(getAlbum(), "neu", getAlbum().getInterpret(), DvTime.valueOf(1));
    m_albumProject.getAlbum().getTracks().add(selectedRow, newTrack);
    tracksChanged();
  }

  private void deleteTrack(int selectedRow)
  {
    int answer = JOptionPane.showConfirmDialog(this,
      m_messages.getString("editor.table.popupmenu.delete_track.question_title"),
      m_messages.getString("editor.table.popupmenu.delete_track.question"), JOptionPane.OK_CANCEL_OPTION);

    if (answer == JOptionPane.OK_OPTION)
    {
      m_albumProject.getAlbum().getTracks().remove(selectedRow);
      tracksChanged();
    }
  }

  private void moveTrack(int selectedRow, boolean up)
  {
    boolean moveable = false;

    int tracks = m_albumProject.getAlbum().getTrackCount();

    moveable = (up && selectedRow > 0) || ((up == false) && selectedRow < tracks - 1);

    if (moveable)
    {
      Track selTrack = m_albumProject.getAlbum().getTracks().get(selectedRow);
      m_albumProject.moveTrack(selTrack, up);
      tracksChanged();
    }

  }

  private void tracksChanged()
  {
    notifyAlbumChange();
    updatePanel();
  }

  private void setColumnWidths()
  {
    for (int i = 0; i < m_albumTable.getModel().getColumnCount(); i++)
    {
      TableColumn tempCol = m_albumTable.getColumnModel().getColumn(i);

      int relSize = 100;

      if (i == 2 || i == 3)
      {
        relSize = 20;
      }

      tempCol.setPreferredWidth(relSize);
    }
  }

  private void assignTracksToFile()
  {
    int[] trackIndices = m_albumTable.getSelectedRows();
    IAudioFile selFile = m_audioFileList.getSelectedValue();

    boolean assignmentPossible = true;

    if (selFile == null)
    {
      assignmentPossible = false;
      JOptionPane.showMessageDialog(this, m_messages.getString("msg.no_audiofile_selected"),
        m_messages.getString("msg.assignment_error"), JOptionPane.ERROR_MESSAGE);
    }
    else if (trackIndices.length == 0)
    {
      JOptionPane.showMessageDialog(this, m_messages.getString("msg.no_tracks_selected"),
        m_messages.getString("msg.assignment_error"), JOptionPane.ERROR_MESSAGE);
      assignmentPossible = false;
    }

    if (assignmentPossible)
    {

      List<Track> trackList = getAlbum().getTracks();

      List<Track> selTrackList = new ArrayList<>();

      for (int i = 0; i < trackIndices.length; i++)
      {
        int trackIndex = trackIndices[i];
        selTrackList.add(trackList.get(trackIndex));
      }

      selFile.setTrackList(selTrackList);

      updateProject();
      updateAlbumInfos();
    }
  }

  private void addAudioFile()
  {
    JFileChooser chooser = new JFileChooser(m_albumProject.getFilePath());
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Wave-Dateien (16Bit PCM)", "wav");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile();

      IExceptionHandler exhandler = new MessageBoxExceptionHandler(this,
        m_messages.getString("button.add_audio_file.openerror_msg"));

      try
      {
        String fullName = file.getCanonicalPath();
        File tempWaveFile = new File(fullName);
        m_logger.log("adding File " + tempWaveFile.getAbsolutePath());

        Cursor cursorDefault = getCursor();

        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        String loadingMessage = m_messages.getString("albumeditor.loadprogress.message") + " " + fullName + "...";

        ProgressDialog progDlg = new ProgressDialog(topFrame, m_messages.getString("albumeditor.loadprogress.title"),
          m_logger, true);

        LoadProgressSwing<AudioFile> loadProgress = new LoadProgressSwing<AudioFile>(progDlg, loadingMessage, m_logger,
          new IFileLoader<AudioFile>()
          {

            @Override
            public AudioFile load() throws IOException, UnsupportedAudioFileException
            {
              AudioFile audioFile = new AudioFile(m_albumProject);
              audioFile.setFile(tempWaveFile);
              setCursor(new Cursor(Cursor.WAIT_CURSOR));
              m_albumProject.addAudioFile(audioFile);
              audioFile.setFile(tempWaveFile);
              updateProject();
              setCursor(cursorDefault);
              synchWavePanels();
              return audioFile;

            }
          }, exhandler);

        loadProgress.execute();
      }
      catch (Exception ex)
      {
        exhandler.handleException(ex);
      }
    }

    synchWavePanels();

  }

  private void updateAlbumInfos()
  {
    m_albumInfoLabel.setText("Ausgesuchtes Album: " + getAlbum().toString());

    List<Track> trackList = getAlbum().getTracks();

    String infoStr = "";

    for (int i = 0; i < trackList.size(); i++)
    {
      Track tempTrack = trackList.get(i);
      infoStr = infoStr + (tempTrack.toString()) + "\n";
    }

    m_albumTable.setModel(new AlbumTableModel());
    setColumnWidths();
  }

  @Override
  protected void updatePanel()
  {
    m_audioFileList.setListData(new Vector<>(m_albumProject.getAudioFiles()));

    updateAlbumInfos();
    synchWavePanels();
  }

  private void synchWavePanels()
  {
    int tabs = m_waveTabPanel.getTabCount();

    List<IAudioFile> files = m_albumProject.getAudioFiles();

    List<WavePanel> toDeletePanels = new ArrayList<WavePanel>();

    for (int i = 0; i < tabs; i++)
    {
      WavePanel tempWavePanel = (WavePanel) m_waveTabPanel.getTabComponentAt(i);

      if (files.size() < i)
      {
        IAudioFile tempFile = files.get(i);

        if (tempWavePanel.getAudioFile() != tempFile)
        {
          tempWavePanel.setAudioFile(tempFile);
        }
      }
      else
      {
        toDeletePanels.add(tempWavePanel);
      }
    }

    if (files.size() > tabs)
    {
      for (int i = tabs; i < files.size(); i++)
      {
        IAudioFile tempFile = files.get(i);

        WavePanel newWavePanel = new WavePanel(m_albumProject, m_messages, m_logger);
        m_waveTabPanel.addTab(tempFile.getName(), newWavePanel);
        newWavePanel.setAudioFile(tempFile);
      }
    }

  }

  private class AlbumTableModel extends AbstractTableModel
  {

    private static final long serialVersionUID = 1L;

    @Override
    public int getRowCount()
    {
      return getAlbum().getTrackCount();
    }

    @Override
    public int getColumnCount()
    {
      return 5;
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
      case 3:
        rueckgabe = m_messages.getString("table.column.total");
        break;
      case 4:
        rueckgabe = m_messages.getString("table.column.audiofile");
        break;
      }
      return rueckgabe;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return columnIndex < 3;
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
      case 3:
        rueckgabe = getTotalTime(rowIndex).toString();
        break;
      case 4:
        rueckgabe = m_messages.getString("table.audiofile_unassigned");

        IAudioFile audioFile = m_albumProject.getAudioFileWithTrack(track);
        if (audioFile != null)
        {
          rueckgabe = audioFile.getName();
        }

        break;
      }
      return rueckgabe;
    }

    private DvTime getTotalTime(int trackIndex)
    {
      DvTime rueckgabe = DvTime.getNullValue();
      IAudioFile audioFile = null;

      for (int i = 0; i <= trackIndex; i++)
      {
        Track tempTrack = getAlbum().getTracks().get(i);
        IAudioFile aktAudioFile = m_albumProject.getAudioFileWithTrack(tempTrack);

        if (audioFile != aktAudioFile)
        {
          audioFile = aktAudioFile;
          rueckgabe = DvTime.getNullValue();
        }

        rueckgabe = rueckgabe.add(tempTrack.getTime());
      }

      return rueckgabe;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
      Track track = getAlbum().getTracks().get(rowIndex);

      if (aValue != null)
      {

        switch (columnIndex)
        {
        case 0:
          track.setArtist(aValue.toString());
          break;
        case 1:
          track.setName(aValue.toString());
          break;
        case 2:
          String timeStr = aValue.toString();

          if (DvTime.isValidTimeString(timeStr))
          {
            track.setTime(DvTime.valueOf(timeStr));
          }
          break;
        }

        tracksChanged();
      }
    }

    @Override
    public void addTableModelListener(TableModelListener l)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void removeTableModelListener(TableModelListener l)
    {
      // TODO Auto-generated method stub

    }

  }

  /**
   * A mouse listener for a JTable component.
   * @author www.codejava.neet
   *
   */
  public class TableMouseListener extends MouseAdapter
  {

    private JTable m_table;

    public TableMouseListener(JTable table)
    {
      m_table = table;
    }

    @Override
    public void mousePressed(MouseEvent event)
    {
      // selects the row at which point the mouse is clicked
      if (event.isPopupTrigger())
      {
        Point point = event.getPoint();
        int currentRow = m_table.rowAtPoint(point);
        m_table.setRowSelectionInterval(currentRow, currentRow);
      }
      else
      {
        super.mousePressed(event);
      }
    }
  }

}
