package listeners;

import java.util.List;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Table;

public abstract class TableListener implements MouseListener
	{
	protected Table table;
	protected List<?> data;
	
	public void setTable(Table table)
		{
		this.table=table;
		}
	
	public void setData(List<?> data)
	{
		this.data = data;
	}
	}
