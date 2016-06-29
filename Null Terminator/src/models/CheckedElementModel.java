package models;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public abstract class CheckedElementModel
	{
	private ICompilationUnit iCompilationUnit; 		//the file where the null check is located
	private IMethodBinding nullCheckLocationMethod;	//the method where the condition is located
	private MethodDeclaration methodDeclaration; 	//the method where the condition is located
	private IType nullCheckLocationClass;
	private ITypeBinding checkedElementTypeBinding;		//checked variable type / checked method return type
	private IType checkedElementType;
	private InfixExpression.Operator operator;
	private Statement thenBlock;
	private Statement elseBlock;
	private int conditionLine;
	private int originalConditionLine;
	
	public CheckedElementModel(IMethodBinding nullCheckLocationMethod, ITypeBinding checkedElementTypeBinding,
			Operator operator, Statement thenBlock, Statement elseBlock, int conditionLine,
			MethodDeclaration methodDeclaration) 
	{
		this.nullCheckLocationMethod = nullCheckLocationMethod;
		this.checkedElementTypeBinding = checkedElementTypeBinding;
		this.operator = operator;
		this.thenBlock = thenBlock;
		this.elseBlock = elseBlock;
		this.conditionLine = conditionLine;
		originalConditionLine = conditionLine;
		this.methodDeclaration = methodDeclaration;
		iCompilationUnit = ((IMember)nullCheckLocationMethod.getJavaElement()).getCompilationUnit();
		checkedElementType = (IType) checkedElementTypeBinding.getJavaElement();
		nullCheckLocationClass = (IType) nullCheckLocationMethod.getDeclaringClass().getJavaElement();
	}
	
	
	public abstract boolean myEquals (IBinding element);
	public abstract String getElementName();

	public IMethodBinding getNullCheckLocationMethod() {
		return nullCheckLocationMethod;
	}

	public ITypeBinding getCheckedElementTypeBinding() {
		return checkedElementTypeBinding;
	}
	
	public IType getCheckedElementType() {
		return checkedElementType;
	}

	public InfixExpression.Operator getOperator() {
		return operator;
	}

	public Statement getThenBlock() {
		return thenBlock;
	}

	public Statement getElseBlock() {
		return elseBlock;
	}

	public int getConditionLine() {
		return conditionLine;
	}

	public ICompilationUnit getiCompilationUnit() {
		return iCompilationUnit;
	}

	public IType getNullCheckLocationClass() {
		return nullCheckLocationClass;
	}

	public void setThenBlock(Statement thenBlock) {
		this.thenBlock = thenBlock;
	}

	public void setElseBlock(Statement elseBlock) {
		this.elseBlock = elseBlock;
	}

	public void setConditionLine(int conditionLine) {
		this.conditionLine = conditionLine;
	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}
	
	public int getOriginalConditionLine() {
		return originalConditionLine;
	}
	}
