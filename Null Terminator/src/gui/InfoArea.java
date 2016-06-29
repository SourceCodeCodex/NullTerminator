package gui;

import java.time.LocalTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import utils.Utils;

public class InfoArea 
{
	private Text text;
	
	public InfoArea (Composite parent)
	{
		NamedBorder border = new NamedBorder(parent, Utils.INFO_AREA);
		GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        border.setGridData(gridData);
		text = new Text(border.getGroup(), SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		text.setLayoutData(gridData);
		text.setBackground(new Color(Display.getCurrent(),255,255,255));	
		
	}
	
	public void setText (String content)
	{
		text.setText(content);
	}
	
	public void append (String content)
	{
		text.append(LocalTime.now() + ": " + content + "\n");
	}
	
	public void clear ()
	{
		text.setText("");
	}
}
