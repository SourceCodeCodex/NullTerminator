package gui;

import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import listeners.TableListener;
import utils.Utils;

public abstract class AbstractTableGui
	{
	private TableListener listener;
	protected TableViewer viewer;
	
	protected abstract void createColumns();
	
	public AbstractTableGui (Composite parent,TableListener listener,String title)
		{
		NamedBorder border = new NamedBorder(parent, title);
		viewer = new TableViewer(border.getGroup(),SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = viewer.getTable();
		GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		Font font = new Font(Display.getCurrent(), Utils.FONT, 10, SWT.NONE);
		table.setFont(font);
		listener.setTable(table);
		table.addMouseListener(listener);
		this.listener = listener;
		}
	
	public void clearAll()
		{
		viewer.setInput(null);
		viewer.getTable().clearAll();
		viewer.getTable().setEnabled(false);
		}

	public void setContentProvider(IContentProvider arrayContentProvider)
		{
		viewer.setContentProvider(arrayContentProvider);
		}
	
	public void setInput(List<?> list)
		{
		viewer.setInput(list);
		listener.setData(list);
		}
	
	public void setEnabled ()
		{
		viewer.getTable().setEnabled(true);
		}
	}
