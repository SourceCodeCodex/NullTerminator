package models;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class VariableNullAssignmentModel extends NullAssignmentModel
{
	private IVariableBinding variable;
	
	public VariableNullAssignmentModel (IVariableBinding variable, int line)
	{
		super(line);
		this.variable = variable;
		setcUnit(getICompilationUnit(variable));
	}
	
	private ICompilationUnit getICompilationUnit (IVariableBinding variableBinding)
	{
		if (variableBinding.getJavaElement() instanceof IMember)
			return ((IMember)variableBinding.getJavaElement()).getCompilationUnit();
		else return ((ILocalVariable)variableBinding.getJavaElement()).getDeclaringMember().getCompilationUnit();
	}

	public String getElementName() 
	{
		return variable.getName();
	}

}
