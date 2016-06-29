package models;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class VarModel extends CheckedElementModel
	{
	private IVariableBinding checkedVariableBinding;
	
	public VarModel(IVariableBinding checkedVariableBinding, IMethodBinding nullCheckLocationMethod, 
			ITypeBinding checkedElementType, Operator operator, Statement thenBlock, Statement elseBlock,
			int conditionLine, MethodDeclaration methodDeclaration) 
	{
		super(nullCheckLocationMethod, checkedElementType, operator, thenBlock, elseBlock, conditionLine, methodDeclaration);
		this.checkedVariableBinding = checkedVariableBinding;
	}
	
	@Override
	public boolean myEquals (IBinding model)
	{
		if (checkedVariableBinding.isEqualTo(model))
			return true;
		return false;	
	}
	
	@Override
	public String getElementName() 
	{
		return checkedVariableBinding.getName();
	}
	
	public IVariableBinding getCheckedVariableBinding()
	{
		return checkedVariableBinding;
	}
	
	}
