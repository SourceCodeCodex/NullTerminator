package refactorings;

import models.CheckedElementModel;
import utils.Utils;

public class MethodRefactoring extends AbstractRefactoring
{
	public MethodRefactoring(CheckedElementModel checkedElementModel) 
	{
		super(checkedElementModel);
	}
	
	@Override
	public void perform() throws Exception
	{
		setupRefactoring(checkedElementModel);
		//insert a variable declaration that takes the value returned by the null checked method so the scenario will be the same as in variable case
		//error in case the newly create variable has the same name as another visible variable in that place
		variableName = refactoringHelper.toCamelCase(checkedElementModel.getCheckedElementType().getElementName());
		astOperations.createMethodInvocation(nullCheckCUnit,checkedElementModel.getCheckedElementType(),checkedElementModel.getConditionLine());
		astOperations.refindCondition(checkedElementModel);
		newMethodName=Utils.GENERIC_METHOD_NAME+"_Met_"+checkedElementModel.getElementName()+"_Line"+checkedElementModel.getOriginalConditionLine();
		if (checkedElementModel.getElseBlock() == null)
			refactorThen(checkedElementModel, variableName);
		else refactorElse(checkedElementModel, variableName);
	}
}
