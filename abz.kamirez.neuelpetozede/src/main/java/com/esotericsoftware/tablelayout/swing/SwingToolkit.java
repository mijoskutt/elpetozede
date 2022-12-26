
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JScrollPane;

import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;

class SwingToolkit extends Toolkit<Component, Table, TableLayout>
{
  static Timer timer;
  static ArrayList<TableLayout> debugLayouts = new ArrayList(0);

  @Override
  public Cell obtainCell(TableLayout layout)
  {
    Cell cell = new Cell();
    cell.setLayout(layout);
    return cell;
  }

  @Override
  public void freeCell(Cell cell)
  {
  }

  @Override
  public void addChild(Component parent, Component child)
  {
    if (parent instanceof JScrollPane)
      ((JScrollPane) parent).setViewportView(child);
    else
      ((Container) parent).add(child);
  }

  @Override
  public void removeChild(Component parent, Component child)
  {
    ((Container) parent).remove(child);
  }

  @Override
  public float getMinWidth(Component widget)
  {
    return widget.getMinimumSize().width;
  }

  @Override
  public float getMinHeight(Component widget)
  {
    return widget.getMinimumSize().height;
  }

  @Override
  public float getPrefWidth(Component widget)
  {
    return widget.getPreferredSize().width;
  }

  @Override
  public float getPrefHeight(Component widget)
  {
    return widget.getPreferredSize().height;
  }

  @Override
  public float getMaxWidth(Component widget)
  {
    return widget.getMaximumSize().width;
  }

  @Override
  public float getMaxHeight(Component widget)
  {
    return widget.getMaximumSize().height;
  }

  @Override
  public float getWidth(Component widget)
  {
    return widget.getWidth();
  }

  @Override
  public float getHeight(Component widget)
  {
    return widget.getHeight();
  }

  @Override
  public void clearDebugRectangles(TableLayout layout)
  {
    if (layout.debugRects != null)
      debugLayouts.remove(this);
    layout.debugRects = null;
  }

  @Override
  public void addDebugRectangle(TableLayout layout, Debug type, float x, float y, float w, float h)
  {
    if (layout.debugRects == null)
    {
      layout.debugRects = new ArrayList();
      debugLayouts.add(layout);
    }
    layout.debugRects.add(new DebugRect(type, x, y, w, h));
  }

  static void startDebugTimer()
  {
    if (timer != null)
      return;
    timer = new Timer("TableLayout Debug", true);
    timer.schedule(newDebugTask(), 100);
  }

  static TimerTask newDebugTask()
  {
    return new TimerTask()
    {
      @Override
      public void run()
      {
        if (!EventQueue.isDispatchThread())
        {
          EventQueue.invokeLater(this);
          return;
        }
        for (TableLayout layout : debugLayouts)
          layout.drawDebug();
        timer.schedule(newDebugTask(), 250);
      }
    };
  }

  static class DebugRect
  {
    final Debug type;
    final int x, y, width, height;

    public DebugRect(Debug type, float x, float y, float width, float height)
    {
      this.x = (int) x;
      this.y = (int) y;
      this.width = (int) (width - 1);
      this.height = (int) (height - 1);
      this.type = type;
    }
  }
}
