package models;

import org.eclipse.jdt.core.ICompilationUnit;

public abstract class NullAssignmentModel 
{
	protected ICompilationUnit cUnit;
	private int line;
	
	public NullAssignmentModel(int line) 
	{
		this.line = line;
	}
	
	public NullAssignmentModel(int line, ICompilationUnit cUnit) 
	{
		this.line = line;
		this.cUnit = cUnit;
	}
	
	public ICompilationUnit getCompilationUnit()
	{
		return cUnit;
	}
	
	public void setcUnit(ICompilationUnit cUnit) 
	{
		this.cUnit = cUnit;
	}

	public int getLineNumber()
	{
		return line;
	}

	public abstract String getElementName();

	public String getFileName() 
	{
		return cUnit.getElementName();
	}
}
