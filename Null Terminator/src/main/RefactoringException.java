package main;

public class RefactoringException extends Exception
	{
	private static final long serialVersionUID = 6859896551328595154L;
	private String content = "There has been a problem during refactoring: ";
	
	public RefactoringException(String content)
		{
		this.content = this.content + content;
		}
	
	public String getContent()
	{
		return content;
	}
	}
