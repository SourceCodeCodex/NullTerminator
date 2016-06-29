package handlers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import utils.Utils;

public class ShowActiveProjectHandler extends WorkbenchWindowControlContribution
{
	private static ShowActiveProjectHandler instance;
	private Label label;
		
	public static ShowActiveProjectHandler getInstance()
	{
		return instance;
	}
	
	@Override
	protected Control createControl(Composite parent) 
	{
		instance = this;
		label = new Label(parent, SWT.NONE);
		label.setText("Current project: " + Utils.NONE + "                                          ");
		label.setFont(new Font(Display.getCurrent(), Utils.FONT, 11, SWT.NONE));
		label.setAlignment(SWT.LEFT);
		return null;
	}
	
	public void setProject(String projectName)
	{
		label.setText("Current project:" + projectName);
		label.pack();
	}

}
