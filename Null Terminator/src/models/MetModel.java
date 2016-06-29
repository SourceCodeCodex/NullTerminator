package models;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

public class MetModel extends CheckedElementModel
	{
	private IMethodBinding checkedMethodBinding;
	private MethodInvocation invocation;
	
	public MetModel(IMethodBinding checkedMethodBinding, IMethodBinding nullCheckLocationMethod,
			ITypeBinding checkedElementType, Operator operator, Statement thenBlock, Statement elseBlock,
			int conditionLine, MethodInvocation invocation, MethodDeclaration methodDeclaration) 
	{
		super(nullCheckLocationMethod, checkedElementType, operator, thenBlock, elseBlock, conditionLine, methodDeclaration);
		this.checkedMethodBinding = checkedMethodBinding;
		this.invocation = invocation;
	}

	
	@Override
	public boolean myEquals (IBinding element)
	{
		if (checkedMethodBinding.isEqualTo(element))
			return true;
		return false;	
	}
	
	@Override
	public String getElementName() 
	{
		return checkedMethodBinding.getName();
	}
	
	public IMethodBinding getCheckedMethodBinding()
	{
		return checkedMethodBinding;
	}

	public MethodInvocation getInvocation() {
		return invocation;
	}

	}
