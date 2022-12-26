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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.swing.Table;

import abz.kamirez.elpetozede.domain.material.Album;
import abz.kamirez.elpetozede.domain.material.AlbumProject;
import abz.kamirez.elpetozede.domain.material.IAudioFile;
import abz.kamirez.elpetozede.domain.material.LAOEWaveWriter;
import abz.kamirez.elpetozede.domain.material.Track;
import abz.kamirez.elpetozede.domain.service.ILogger;
import abz.kamirez.elpetozede.domain.service.IProgressInfo;
import abz.kamirez.elpetozede.domain.service.IProgressListener;
import abz.kamirez.elpetozede.domain.service.IWaveWriter;
import abz.kamirez.elpetozede.domain.service.WriterUtil;
import abz.kamirez.elpetozede.service.mp3.MP3Writer;

public class ExportPanelSwing extends AbstraktElpetozedePanel
{
  private JLabel m_formatLabel;
  private JCheckBox m_mitWaveErzeugung;
  private JCheckBox m_mitMP3Erzeugung;
  private JTable m_exportTable;
  private ExportTableModel m_exportModel;
  private JButton m_exportBtn;

  private Properties m_elpetozedeProps;

  private final static String FORMAT_WAVE = "Wave";
  private final static String FORMAT_MP3 = "Mp3";

  private String m_mp3Encoder;

  public ExportPanelSwing(AlbumProject albumProject, ResourceBundle messages, ILogger logger,
      Properties elpetozedeProps)
  {
    super(albumProject, messages, logger);
    m_elpetozedeProps = elpetozedeProps;

    m_mp3Encoder = m_elpetozedeProps.getProperty("mp3encoder", "");

    initGui();
  }

  @Override
  protected void initGui()
  {

    setLayout(new BorderLayout());

    m_formatLabel = new JLabel(m_messages.getString("export.format"));
    m_mitWaveErzeugung = new JCheckBox(m_messages.getString("export.format.wave"));
    m_mitWaveErzeugung.setSelected(true);

    m_mitMP3Erzeugung = new JCheckBox(m_messages.getString("export.format.mp3"));

    m_mitMP3Erzeugung.setEnabled(m_mp3Encoder.isEmpty() == false);

    m_exportModel = new ExportTableModel();
    m_exportTable = new JTable(m_exportModel);
    setColumnWidths();
    m_exportBtn = new JButton(m_messages.getString("export.button.export"));

    m_exportBtn.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        exportFiles();
      }

    });

    Table exportGrid = new Table();
    //exportGrid.debug();

    int feldbreite = 400;

    int labelAlign = BaseTableLayout.LEFT;
    int fieldAlign = BaseTableLayout.LEFT;
    exportGrid.row().pad(2, 10, 2, 10);
    exportGrid.addCell(m_formatLabel).align(labelAlign);
    exportGrid.row().pad(2, 10, 2, 10);
    exportGrid.addCell(m_mitWaveErzeugung).align(labelAlign);
    exportGrid.row().pad(2, 10, 2, 10);
    exportGrid.addCell(m_mitMP3Erzeugung).align(labelAlign);
    exportGrid.row().pad(2, 10, 2, 10);
    exportGrid.addCell(new JScrollPane(m_exportTable)).colspan(2).minWidth(feldbreite * 2).minHeight(220);
    exportGrid.row().pad(2, 10, 2, 10);
    exportGrid.addCell(m_exportBtn).align(labelAlign);

    add(exportGrid, BorderLayout.CENTER);
  }

  private void setColumnWidths()
  {
    for (int i = 0; i < m_exportTable.getModel().getColumnCount(); i++)
    {
      TableColumn tempCol = m_exportTable.getColumnModel().getColumn(i);

      int relSize = 100;

      if (i == 0)
      {
        relSize = 4;
      }

      tempCol.setPreferredWidth(relSize);
    }
  }

  @Override
  protected void updatePanel()
  {
    m_exportTable.setModel(new ExportTableModel());
    setColumnWidths();
  }

  private void exportFiles()
  {
    JFileChooser chooser = new JFileChooser(m_albumProject.getFilePath());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
    {
      String exportPath = chooser.getSelectedFile().getAbsolutePath();

      IExceptionHandler exhandler = new MessageBoxExceptionHandler(this,
        m_messages.getString("export.button.export.exporterror_msg"));

      try
      {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ProgressDialog progMonDlg = new ProgressDialog(topFrame, m_messages.getString("export.button.exportprogress"),
          m_logger, false);

        String formatStr = FORMAT_WAVE;

        if (m_mitMP3Erzeugung.isSelected())
        {
          formatStr = FORMAT_MP3;
        }

        WriteProgress writer = new WriteProgress(progMonDlg, formatStr, m_albumProject.getAudioFiles(), exportPath,
          exhandler);
        writer.execute();

      }
      catch (Exception ex)
      {
        Throwable cause = ex;
        if (ex.getCause() != null)
        {
          cause = ex.getCause();
        }

        exhandler.handleException(ex);
      }

    }
    else
    {
      System.out.println("No Selection ");
    }

  }

  private String getFilePrefix(IAudioFile audioFile)
  {
    String rueckgabe = "";

    int fileNr = m_albumProject.getAudioFiles().indexOf(audioFile) + 1;

    if (fileNr > 0)
    {
      rueckgabe = fileNr + "_";
    }

    return rueckgabe;
  }

  private class WriteProgress extends SwingWorker<Void, Void> implements IProgressListener
  {
    private ProgressDialog m_dlg;
    private IWaveWriter m_waveWriter;
    private List<IAudioFile> m_fileList;
    private String m_exportPath;
    private String m_format;
    private IExceptionHandler m_exHandler;

    private WriteProgress(ProgressDialog progDlg, String format, final List<IAudioFile> sourceFiles,
        final String exportPath, IExceptionHandler exHandler)
    {
      m_dlg = progDlg;
      m_format = format;
      m_fileList = sourceFiles;
      m_exportPath = exportPath;
      m_exHandler = exHandler;
    }

    @Override
    public Void doInBackground()
    {

      if (m_format.equals(FORMAT_MP3))
      {
        m_waveWriter = new MP3Writer(m_mp3Encoder);
        ((MP3Writer) m_waveWriter).setDeleteWavFile(m_mitWaveErzeugung.isSelected() == false);
      }
      else
      {
        m_waveWriter = new LAOEWaveWriter();
      }

      try
      {

        for (IAudioFile audioFile : m_fileList)
        {

          String noteStr = m_messages.getString("export.button.exportprogress") + " " + audioFile.getName();
          m_dlg.setNote(noteStr);
          m_dlg.setTitle(noteStr);
          m_dlg.setProgress(0);

          if (m_dlg.isShowing() == false)
          {
            m_dlg.open();
          }

          m_waveWriter.addProgressListener(WriteProgress.this);
          m_waveWriter.writeTrackFiles(getFilePrefix(audioFile), audioFile, m_exportPath);

          m_waveWriter.removeProgressListener(WriteProgress.this);
        }

      }
      catch (Exception ex)
      {
        m_waveWriter.removeProgressListener(WriteProgress.this);
        m_dlg.close();
      }

      return null;
    }

    @Override
    public void done()
    {
      m_waveWriter.removeProgressListener(WriteProgress.this);
      m_dlg.close();
      try
      {
        get();
      }
      catch (Exception ex)
      {
        m_exHandler.handleException(ex);
      }
    }

    @Override
    public void progressReported(IProgressInfo progInfo)
    {
      final int newWork = progInfo.getPercentage();
      final String msg = progInfo.getMessage();

      m_dlg.setProgress(newWork);
      m_dlg.setNote(msg);
    }
  }

  private class ExportTableModel extends AbstractTableModel
  {

    private List<Boolean> m_isInExport;

    public ExportTableModel()
    {
      Album album = getAlbum();
      m_isInExport = new ArrayList<Boolean>();

      for (int i = 0; i < album.getTrackCount(); i++)
      {
        m_isInExport.add(Boolean.TRUE);
      }
    }

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
    public Class<?> getColumnClass(int columnIndex)
    {
      Class<?> rueckgabe = super.getColumnClass(columnIndex);

      if (columnIndex == 0)
      {
        rueckgabe = Boolean.class;
      }

      return rueckgabe;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
      String rueckgabe = "";

      switch (columnIndex)
      {
      case 0:
        rueckgabe = m_messages.getString("export.table.column.export");
        break;
      case 1:
        rueckgabe = m_messages.getString("export.table.column.title");
        break;
      case 2:
        rueckgabe = m_messages.getString("export.table.column.filename");
        break;
      }
      return rueckgabe;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return columnIndex == 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      Object rueckgabe = "";

      Track track = getAlbum().getTracks().get(rowIndex);

      switch (columnIndex)
      {
      case 0:
        rueckgabe = m_isInExport.get(rowIndex);
        break;
      case 1:
        rueckgabe = track.getName();
        break;
      case 2:
        String filename = track.getFileName();

        if (filename.isEmpty())
        {
          IAudioFile audioFile = m_albumProject.getAudioFileWithTrack(track);

          String prefix = "";

          if (audioFile != null)
          {
            prefix = getFilePrefix(audioFile);
          }

          filename = prefix + WriterUtil.createNormalizedFileName(track);
        }

        rueckgabe = filename;

        break;
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
          m_isInExport.set(rowIndex, (Boolean) aValue);
          break;
        case 2:
          track.setFileName(aValue.toString());
          break;
        }
      }
    }
  }

}
