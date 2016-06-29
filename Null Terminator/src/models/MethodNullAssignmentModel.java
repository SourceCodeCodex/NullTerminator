package models;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

public class MethodNullAssignmentModel extends NullAssignmentModel
{
	private IBinding method;
	
	public MethodNullAssignmentModel (IBinding method, int line, ICompilationUnit cUnit)
	{
		super(line,cUnit);
		this.method = method;
	}

	public String getElementName() 
	{
		return method.getName();
	}
}
