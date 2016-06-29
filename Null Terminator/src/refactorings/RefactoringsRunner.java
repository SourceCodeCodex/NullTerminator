package refactorings;

import main.RefactoringException;
import models.CheckedElementModel;
import models.MetModel;
import models.VarModel;

public class RefactoringsRunner
{
	private AbstractRefactoring abstractRefactoring;
	
	public String run (CheckedElementModel model)
	{
		String message = "";
		try
		{
			establishRefactoringType(model);
			abstractRefactoring.makeOriginalCUnitsCopies(model);
			abstractRefactoring.perform();
			abstractRefactoring.discardCompilationUnits();
		}
		catch (RefactoringException ex)
		{
			message = ex.getContent();
			abstractRefactoring.restoreCompilationUnits();
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			abstractRefactoring.restoreCompilationUnits();
			ex.printStackTrace();
		}
		return message;
	}
	
	private void establishRefactoringType(CheckedElementModel model)
	{
		if (model instanceof VarModel)
			abstractRefactoring = new VariableRefactoring((VarModel) model);
		else 
			abstractRefactoring = new MethodRefactoring((MetModel) model);
	}
}
