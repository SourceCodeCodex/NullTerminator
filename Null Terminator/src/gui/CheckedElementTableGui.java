package gui;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import listeners.TableListener;
import main.TableColumnProvider;
import models.CheckedElementModel;
import utils.Utils;

public class CheckedElementTableGui extends AbstractTableGui
	{
	public CheckedElementTableGui(Composite parent, TableListener listener, String title)
		{
		super(parent,listener, title);
		createColumns();
		}

	@Override
	protected void createColumns()
		{
		TableViewerColumn nameColumn = new TableViewerColumn(viewer,SWT.NONE);
		TableViewerColumn typeColumn = new TableViewerColumn(viewer,SWT.NONE);
		TableViewerColumn declaringClassColumn = new TableViewerColumn(viewer,SWT.NONE);	
		nameColumn.getColumn().setText(Utils.HEADER_ELEMENT);
		nameColumn.getColumn().setWidth(100);
		nameColumn.setLabelProvider(new TableColumnProvider(){
		 @Override
		  public String getText(Object element) {
		   return ((CheckedElementModel)(element)).getElementName();
		  }
		});		
		
		typeColumn.getColumn().setText(Utils.HEADER_TYPE);
		typeColumn.getColumn().setWidth(100);
		typeColumn.setLabelProvider(new TableColumnProvider(){
		 @Override
		  public String getText(Object element) {
		   return ((CheckedElementModel)(element)).getCheckedElementTypeBinding().getName();
		  }
		});
		
		declaringClassColumn.getColumn().setText(Utils.HEADER_LOCATION);
		declaringClassColumn.getColumn().setWidth(100);
		declaringClassColumn.setLabelProvider(new TableColumnProvider(){
		 @Override
		  public String getText(Object element) {
		   return ((CheckedElementModel)(element)).getiCompilationUnit().getElementName();
		  }
		});	
		
		}
	}
