package models;

import org.eclipse.jdt.core.dom.IVariableBinding;

public class ImpossibleVariableNullAssignmentModel extends VariableNullAssignmentModel 
{

	public ImpossibleVariableNullAssignmentModel(IVariableBinding variable, int line) {
		super(variable, line);
		// TODO Auto-generated constructor stub
	}
	
	public String getElementName()
	{
		return super.getElementName() + "(*)";
	}

}
