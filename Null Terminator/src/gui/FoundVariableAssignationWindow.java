package gui;

import java.util.ArrayList;
import java.util.List;

public class FoundVariableAssignationWindow extends GenericWindow
	{
	private static final long serialVersionUID = 3070155945479242147L;
	private List<Integer> lines=new ArrayList<Integer>();
	private String variableName;
	
	public FoundVariableAssignationWindow (String variableName)
		{
		super("Variable assignations");
		this.variableName=variableName;
		}
	
	public void addLine(int line)
		{
		lines.add(line);
		}
	
	@Override
	public void showWindow()
		{
		StringBuffer buf=new StringBuffer();
		if (lines.size()==0)
			buf.append("Could not find assignments for variable "+variableName);
		else
			{
			buf.append("The variable "+variableName+" is assigned at the following lines: \n");
			for (int i:lines)
				buf.append(i+", ");
			buf.replace(buf.length()-2,buf.length(),"");
			}
		buf.append(".");
		setContent(buf.toString());
		repaint();
		setVisible(true);
		}
	}
