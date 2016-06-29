package refactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import main.Converter;
import main.RefactoringException;
import models.CheckedElementModel;
import utils.Pair;
import utils.Utils;

public abstract class AbstractRefactoring
{
	protected RefactoringHelper refactoringHelper=new RefactoringHelper();
	protected ASTOperations astOperations = new ASTOperations();
	protected EclipseBasedRefactorings eclipseRefactorings = new EclipseBasedRefactorings();
	protected NewElementsCreation newElementsCreation = new NewElementsCreation();
	
	protected ICompilationUnit originalNullCheckLocationCUnit;
	protected ICompilationUnit originalNullCheckElementTypeCUnit;
	protected ICompilationUnit originalNullClassCompilationCUnit;
	protected Pair<IType,Boolean> nullClassCompilationUnit;
	protected VariableDeclarationStatement dummyDeclaration;
	protected IType notNullClass;
	protected IType nullClass;
	protected String notNullClassName;
	protected String nullClassName;
	protected String notNullClassParam;
	protected String nullClassParam;
	protected String variableName;
	protected String newMethodName;
	protected String targetParameterName;
	protected IType nullCheckClass;
	protected ICompilationUnit nullCheckCUnit;
	protected IMethod extractedThenMethod,extractedElseMethod,movedThenMethod,movedElseMethod;
	protected CheckedElementModel checkedElementModel;
	
	public AbstractRefactoring (CheckedElementModel checkedElementModel)
	{
		this.checkedElementModel = checkedElementModel;
		nullCheckClass = checkedElementModel.getNullCheckLocationClass();
		nullCheckCUnit = checkedElementModel.getiCompilationUnit();
	}
	
	public abstract void perform() throws Exception;
	
	//make copies of the compilation units in case the refactoring doesn't work
	public void makeOriginalCUnitsCopies (CheckedElementModel model)
	{
		try 
		{
			originalNullCheckLocationCUnit = model.getiCompilationUnit().getWorkingCopy(null);
			originalNullCheckElementTypeCUnit = model.getCheckedElementType().getCompilationUnit().getWorkingCopy(null);
		} 
		catch (JavaModelException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void makeNullClassOriginalCopy (IType nullClass)
	{
		try
		{
			if (nullClass.getCompilationUnit().getElementName().equals(originalNullCheckLocationCUnit.getElementName()))
				{
				System.out.println("Null class is in the same cUnit as the null check");
				}
			else
				{
				System.out.println("Null class is in different cUnit");
				originalNullClassCompilationCUnit = nullClass.getCompilationUnit().getWorkingCopy(null);
				}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * in case something goes wrong with the refactoring process, we must restore the compilation units to the
	 * content before starting the refactoring
	 */
	public void restoreCompilationUnits ()
	{
		try
		{
			originalNullCheckElementTypeCUnit.commitWorkingCopy(true, null);
			originalNullCheckLocationCUnit.commitWorkingCopy(true, null);
			if (nullClassCompilationUnit == null)
				return;
			if (nullClassCompilationUnit.getValue())
				{
				System.out.println("EXISTS!");
				if (originalNullClassCompilationCUnit != null)
					{
					System.out.println("Restore null class");
					originalNullClassCompilationCUnit.commitWorkingCopy(true, null);
					}
				//else done when restoring originalNullCheckLocationCUnit
				}
			else
				{
				System.out.println("NOT EXISTS!");
				if (originalNullClassCompilationCUnit != null)
				{
					System.out.println("Null cUnit must be deleted");
					originalNullClassCompilationCUnit.delete(true, null);
				}
				else
				{
					//since this is done when restoring originalNullCheckLocationCUnit, do nothing here
					System.out.println("Null class must be deleted, not the entire cUnit");
				}
				

				}
			discardCompilationUnits();
		}
		catch (JavaModelException e) 
		{
			e.printStackTrace();
		}	
	}
	
	//remove the working copies as they are not needed anymore
	public void discardCompilationUnits()
	{
		try
		{
		originalNullCheckElementTypeCUnit.discardWorkingCopy();
		originalNullCheckLocationCUnit.discardWorkingCopy();
		if (originalNullClassCompilationCUnit != null)
			originalNullClassCompilationCUnit.discardWorkingCopy();
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setupRefactoring(CheckedElementModel checkedElementModel) throws Exception
	{
		dummyDeclaration = astOperations.insertVariableDeclaration(nullCheckCUnit,
				checkedElementModel.getThenBlock().getParent(), Utils.DUMMY_VARIABLE_NAME, 
				"String", Utils.DUMMY_VARIABLE_INITIALIZER);
		astOperations.refindCondition(checkedElementModel);
		astOperations.insertEmptyStatement(nullCheckCUnit,checkedElementModel.getThenBlock());
		astOperations.refindCondition(checkedElementModel);
		astOperations.insertEmptyStatement(nullCheckCUnit,checkedElementModel.getElseBlock());
		astOperations.refindCondition(checkedElementModel);
		notNullClass = checkedElementModel.getCheckedElementType();
		notNullClassName = notNullClass.getElementName();
		notNullClassParam = refactoringHelper.toCamelCase(notNullClassName);
		//newElementsCreation.createEmptyConstructor(notNullClass);
		astOperations.refindCondition(checkedElementModel);
		nullClassCompilationUnit = newElementsCreation.createNullClass (notNullClass);
		nullClass = nullClassCompilationUnit.getKey();
		nullClassName = nullClass.getElementName();
		nullClassParam = refactoringHelper.toCamelCase(nullClassName);
		makeNullClassOriginalCopy(nullClass);
		astOperations.refindCondition(checkedElementModel);
	}
	
	public void refactorThen(CheckedElementModel checkedElementModel, String variableName) throws Exception
	{
		extractedThenMethod = eclipseRefactorings.extractMethod (nullCheckClass,checkedElementModel.getThenBlock(),
				newMethodName,nullCheckCUnit);
		if (checkedElementModel.getOperator().equals(InfixExpression.Operator.NOT_EQUALS))
			refactorThenNotEquals(variableName);
		else
			refactorThenEquals(checkedElementModel, variableName);
		clean(checkedElementModel);
	}
	
	public void refactorElse(CheckedElementModel checkedElementModel, String variableName) throws Exception
	{
		VariableDeclarationStatement declaration = astOperations.insertVariableDeclaration(nullCheckCUnit,
				checkedElementModel.getThenBlock().getParent(),nullClassParam,nullClassName,null);
		astOperations.refindCondition(checkedElementModel);
		if (checkedElementModel.getOperator().equals(InfixExpression.Operator.NOT_EQUALS))
			refactorThenElseNotEquals(checkedElementModel,variableName);
		else
			refactorThenElseEquals(checkedElementModel,variableName);
		uniformizeSignatures();
		astOperations.refindCondition(checkedElementModel);
		astOperations.removeVariableDeclaration(nullCheckCUnit,declaration);
		clean(checkedElementModel);
	}
	
	public void refactorThenNotEquals(String variableName) throws Exception
	{
		if (!refactoringHelper.exists(notNullClassName,variableName,extractedThenMethod))
			extractedThenMethod = eclipseRefactorings.addParameter(extractedThenMethod,notNullClassName,variableName);
		else 
			extractedThenMethod = eclipseRefactorings.changeParametersOrder(extractedThenMethod,notNullClassName,variableName);
		movedThenMethod = eclipseRefactorings.moveMethod(extractedThenMethod,notNullClass,variableName);
		newElementsCreation.createNullMethod(nullClass,movedThenMethod);
	}
	
	public void refactorThenEquals(CheckedElementModel checkedElementModel, String variableName) throws Exception
	{
		VariableDeclarationStatement declaration = astOperations.insertVariableDeclaration(nullCheckCUnit,
				checkedElementModel.getThenBlock().getParent(),nullClassParam,nullClassName, null);
		extractedThenMethod = eclipseRefactorings.addParameter(extractedThenMethod,nullClassName,nullClassParam);
		movedThenMethod = eclipseRefactorings.moveMethod(extractedThenMethod,nullClass,nullClassParam);
		newElementsCreation.createNullMethod(notNullClass,movedThenMethod);
		astOperations.refindCondition(checkedElementModel);
		astOperations.replaceVariableInvokingMethod(nullCheckCUnit,checkedElementModel.getThenBlock(),variableName);
		astOperations.removeVariableDeclaration(nullCheckCUnit,declaration);
	}
	
	public void refactorThenElseNotEquals(CheckedElementModel checkedElementModel, String variableName) throws Exception
	{
		System.out.println("refactorThenElseNotEquals with varname: " + variableName);
		extractedElseMethod = eclipseRefactorings.extractMethod(nullCheckClass,
				checkedElementModel.getElseBlock(),newMethodName,nullCheckCUnit);
		extractedElseMethod = eclipseRefactorings.addParameter(extractedElseMethod,nullClassName,nullClassParam);
		astOperations.refindCondition(checkedElementModel);
		movedElseMethod = eclipseRefactorings.moveMethod(extractedElseMethod,nullClass,nullClassParam);
		astOperations.refindCondition(checkedElementModel);
		extractedThenMethod = eclipseRefactorings.extractMethod(nullCheckClass,
				checkedElementModel.getThenBlock(),newMethodName,nullCheckCUnit);
		if (!refactoringHelper.exists(notNullClassName,variableName,extractedThenMethod))
			extractedThenMethod = eclipseRefactorings.addParameter(extractedThenMethod,notNullClassName,variableName);
		else
			extractedThenMethod = eclipseRefactorings.changeParametersOrder(extractedThenMethod,notNullClassName,variableName);
		astOperations.refindCondition(checkedElementModel);
		movedThenMethod = eclipseRefactorings.moveMethod(extractedThenMethod,notNullClass,variableName);
		astOperations.refindCondition(checkedElementModel);
	}
	
	public void refactorThenElseEquals(CheckedElementModel checkedElementModel, String variableName) throws Exception
	{
		extractedElseMethod = eclipseRefactorings.extractMethod(nullCheckClass,
				checkedElementModel.getElseBlock(),newMethodName,nullCheckCUnit);
		if (!refactoringHelper.exists(notNullClassName,variableName,extractedElseMethod))
			extractedElseMethod = eclipseRefactorings.addParameter(extractedElseMethod,notNullClassName,variableName);
		else
			extractedElseMethod = eclipseRefactorings.changeParametersOrder(extractedElseMethod,notNullClassName,variableName);
		movedElseMethod = eclipseRefactorings.moveMethod(extractedElseMethod,notNullClass,variableName);
		astOperations.refindCondition(checkedElementModel);
		extractedThenMethod = eclipseRefactorings.extractMethod(nullCheckClass,
				checkedElementModel.getThenBlock(),newMethodName,nullCheckCUnit);
		extractedThenMethod = eclipseRefactorings.addParameter(extractedThenMethod,nullClassName,nullClassParam);
		movedThenMethod = eclipseRefactorings.moveMethod(extractedThenMethod,nullClass,nullClassParam);
		astOperations.refindCondition(checkedElementModel);
		astOperations.replaceVariableInvokingMethod(nullCheckCUnit,checkedElementModel.getThenBlock(),variableName);	
	}
	
	public void uniformizeSignatures () throws Exception
	{
		astOperations.refindCondition(checkedElementModel);
		List<Pair<String,String>> parameters = new ArrayList<Pair<String,String>>();
		if (!movedElseMethod.getReturnType().equals(movedThenMethod.getReturnType()))
		{
			throw new RefactoringException("Different return types. Can't refactor");
		}
		if (!movedElseMethod.getSignature().equals(movedThenMethod.getSignature()))
			{
			parameters.addAll(new Converter().getParameters(movedThenMethod));
			parameters.addAll(new Converter().getParameters(movedElseMethod));
			System.out.println("Before remove duplicates: " + parameters.size());
			if (!parameters.isEmpty())
				{
				parameters = refactoringHelper.removeDuplicatedParameters(parameters);
				System.out.println("After remove duplicates: " + parameters.size());
				//printParameters("movedElseMethod before change sig",movedElseMethod);
				//printParameters("movedThenMethod before change sig",movedThenMethod);
				movedElseMethod = eclipseRefactorings.changeSignature(movedElseMethod,parameters);			
				movedThenMethod = eclipseRefactorings.changeSignature(movedThenMethod,parameters);
				//printParameters("movedElseMethod after change sig",movedElseMethod);
				//printParameters("movedThenMethod after change sig",movedThenMethod);
				}
			}
	}
	
	public void clean (CheckedElementModel checkedElementModel) throws JavaModelException
	{
		astOperations.refindCondition(checkedElementModel);
		//TBD if extracted method has a parameter of the type of nullchecklocationclass, change the invocation by adding 'this'
		astOperations.addThisAsParameterIfNeccessary(movedThenMethod,nullCheckClass,checkedElementModel.getThenBlock());
		astOperations.removeCondition(checkedElementModel.getiCompilationUnit(),checkedElementModel.getThenBlock(),
				checkedElementModel.getConditionLine());
		astOperations.deleteEmptyStatement(nullClass.getCompilationUnit());
		astOperations.deleteEmptyStatement(notNullClass.getCompilationUnit());
		astOperations.removeVariableDeclaration(nullCheckCUnit,dummyDeclaration);
	}
	
	public void printParameters(String message,IMethod method) throws JavaModelException
	{
		System.out.println();
		System.out.print("PRINTING PARAMETERS  " + message + ": ");
		ILocalVariable[] params=method.getParameters();
		int i=0;
		for (i=0;i<params.length;i++)
			{
			System.out.println(params[i].getTypeSignature()+" "+params[i].getElementName());
			}
	}
}
