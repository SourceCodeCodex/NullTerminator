package gui;

import org.eclipse.jdt.core.ICompilationUnit;

public class FoundMethodDeclarationWindow extends GenericWindow
	{
	private static final long serialVersionUID = -4484179881727791397L;
	private String methodName;
	private int lineNumber=-1;
	private ICompilationUnit cUnit;
	
	public FoundMethodDeclarationWindow (String methodName)
		{
		super("Method declaration");
		this.methodName=methodName;
		}
	
	public void setLineNumber(int lineNumber)
		{
		this.lineNumber=lineNumber;
		}
	
	public void setCUnit(ICompilationUnit cUnit)
		{
		this.cUnit=cUnit;
		}
	
	@Override
	public void showWindow()
		{
		String content;
		if (lineNumber!=-1)
			content="The method "+methodName+" is declared at line "+lineNumber+" in the file "+cUnit.getElementName();
		else
			content="The declaration of method "+methodName+" can't be accessed.";
		setContent(content);
		repaint();
		setVisible(true);
		}
	}
