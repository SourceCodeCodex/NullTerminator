package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

import utils.Utils;

public class NamedBorder 
{
	private Group group;
	
	public NamedBorder (Composite parent, String title)
	{
		GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
		group = new Group(parent,SWT.None);
		group.setText(title);
		group.setLayout(new GridLayout());
		group.setLayoutData(gridData);
		//group.setForeground(new Color(Display.getCurrent(),13,0,85));
		Font font = new Font(Display.getCurrent(), Utils.FONT, 12, SWT.BOLD);
		group.setFont(font);
	}

	public Group getGroup() {
		return group;
	}
	
	public void setGridData (GridData gridData)
	{
		group.setLayoutData(gridData);
	}
}
