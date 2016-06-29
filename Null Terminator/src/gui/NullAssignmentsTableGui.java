package gui;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import listeners.TableListener;
import main.TableColumnProvider;
import models.NullAssignmentModel;
import utils.Utils;

public class NullAssignmentsTableGui extends AbstractTableGui {

	public NullAssignmentsTableGui(Composite parent, TableListener listener, String title) {
		super(parent, listener, title);
		createColumns();
	}

	@Override
	protected void createColumns() 
	{
		TableViewerColumn variableColumn=new TableViewerColumn(viewer,SWT.NONE);
		TableViewerColumn cUnitColumn=new TableViewerColumn(viewer,SWT.NONE);
		TableViewerColumn lineColumn=new TableViewerColumn(viewer,SWT.NONE);
		variableColumn.getColumn().setText(Utils.HEADER_ELEMENT);
		variableColumn.getColumn().setWidth(100);
		variableColumn.setLabelProvider(new TableColumnProvider(){
		 @Override
		  public String getText(Object element) {
		   return ((NullAssignmentModel)(element)).getElementName();
		  }
		});	
		
		cUnitColumn.getColumn().setText(Utils.HEADER_LOCATION);
		cUnitColumn.getColumn().setWidth(100);
		cUnitColumn.setLabelProvider(new TableColumnProvider(){
		 @Override
		  public String getText(Object element) {
		   return ((NullAssignmentModel)(element)).getFileName();
		  }
		});	
		
		lineColumn.getColumn().setText(Utils.HEADER_LINE);
		lineColumn.getColumn().setWidth(50);
		lineColumn.setLabelProvider(new TableColumnProvider(){
		 @Override
		  public String getText(Object element) {
		   return ((NullAssignmentModel)(element)).getLineNumber()+"";
		  }
		});	

	}

}
