package refactorings;

import models.CheckedElementModel;
import utils.Utils;

public class VariableRefactoring extends AbstractRefactoring
{
	public VariableRefactoring (CheckedElementModel checkedElementModel)
	{
		super(checkedElementModel);
	}
	
	@Override
	public void perform() throws Exception
	{
		setupRefactoring(checkedElementModel);
		variableName = checkedElementModel.getElementName();
		newMethodName = Utils.GENERIC_METHOD_NAME+"_Var_"+variableName+"_Line"+checkedElementModel.getOriginalConditionLine();
		if (checkedElementModel.getElseBlock() == null)
			refactorThen(checkedElementModel, variableName);
		else refactorElse(checkedElementModel, variableName);
	}
}
