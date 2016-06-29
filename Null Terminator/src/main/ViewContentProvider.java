package main;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import models.MetModel;
import models.VarModel;


public class ViewContentProvider implements IStructuredContentProvider
	{	
	private IJavaProject javaProject;
	private ICompilationUnit[] compilationUnits;
	private Pile pile=new Pile();
	private IMethodBinding nullCheckLocationMethod;	//the method where the if (...!=null) is located
	private MethodDeclaration methodDeclaration;
	private CompilationUnit compilationUnit;
	private int conditionLine;
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) 
		{
		v.refresh();		
		}
	
	public void dispose() 
		{}
	
	public void showView() throws JavaModelException
		{
		int i;
		IPackageFragmentRoot[] packages=javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot iPackageFragmentRoot:packages)
			if (!(iPackageFragmentRoot.isExternal()))
				{
				IJavaElement[] javaElements=iPackageFragmentRoot.getChildren();
				for (i=0;i<javaElements.length;i++)
					{
					IPackageFragment packageFragment=(IPackageFragment)javaElements[i];				
					compilationUnits=packageFragment.getCompilationUnits();
					for (ICompilationUnit compilationUnit:compilationUnits)	//for every .java file
						{
						startParsing(compilationUnit);
						}
					}
				}
		System.out.println("Sorting");
		pile.sort(new NullCheckedElementsComparator());
		}
	
	
	class MyASTVisitor extends ASTVisitor
		{		
		public boolean visit(MethodDeclaration method)
			{
			if (method.resolveBinding()!=null)
				{
				methodDeclaration = method;
				nullCheckLocationMethod= method.resolveBinding();
				}
			return true;
			}
	
		public boolean visit (final IfStatement node)
			{
			node.getExpression().accept(new ASTVisitor(){
			public boolean visit(InfixExpression ex)
				{
				Expression left,right;
				//System.out.println("InfixExpression: " + ex.toString());
				InfixExpression.Operator operator;
				conditionLine=compilationUnit.getLineNumber(node.getStartPosition());
				left=ex.getLeftOperand();
				right=ex.getRightOperand();
				operator=ex.getOperator();
				try
					{
					if (left instanceof NullLiteral)
						{
						if (right instanceof MethodInvocation)
							processMethod((MethodInvocation)right, node, operator);
						else if (right instanceof SimpleName)
							processVariable((SimpleName)right,node, operator);
						else if (right instanceof QualifiedName)
							processVariable(((QualifiedName)right).getName(),node, operator);
						else if (right instanceof FieldAccess)
							processVariable(((FieldAccess)right).getName(), node, operator);
						else if (right instanceof SuperFieldAccess)
							processVariable(((SuperFieldAccess)right).getName(), node, operator);
						}
					else if (right instanceof NullLiteral)
						{
						if (left instanceof MethodInvocation)
							processMethod((MethodInvocation)left, node, operator);
						else if (left instanceof SimpleName)
							processVariable((SimpleName)left,node, operator);
						else if (left instanceof QualifiedName)
							{
							processVariable(((QualifiedName)left).getName(),node, operator);
							}
						else if (left instanceof FieldAccess)
							processVariable(((FieldAccess)left).getName(), node, operator);
						else if (left instanceof SuperFieldAccess)
							processVariable(((SuperFieldAccess)left).getName(), node, operator);
						}
					}
				catch (Exception e)
					{
					}
				return true;
				}
			});
			return true;
			}
		}
	
	private void processVariable(SimpleName var, IfStatement node, InfixExpression.Operator op) throws JavaModelException
	{
	IVariableBinding variableBinding = (IVariableBinding) ((SimpleName)var).resolveBinding();
	if (variableBinding!=null)
		{
		ITypeBinding variableType=variableBinding.getType();
		if (variableType != null)
			{
			if (canBeRefactored((IType) variableType.getJavaElement()) && 
					canBeRefactored((IType) nullCheckLocationMethod.getDeclaringClass().getJavaElement()) &&
					canBeRefactored(variableType) &&
					canBeRefactored(node) &&
					shouldBeRefactored(node) && shouldBeRefactored(variableBinding, node))
				{
					pile.addElement(new VarModel(variableBinding,
							nullCheckLocationMethod, variableType, op, node.getThenStatement(),
							node.getElseStatement(), conditionLine, methodDeclaration));
				}
			else
				{
				pile.removeAllOccurences(variableBinding);
				}
			}
		}
	}
	
	private void processMethod(MethodInvocation invocation, IfStatement node, InfixExpression.Operator op) throws JavaModelException
		{
		IMethodBinding checkedMethodBinding=invocation.resolveMethodBinding();
		if (checkedMethodBinding!=null)
			{
			IType retType=null;
			if (checkedMethodBinding.getReturnType().getJavaElement() instanceof IType)
				retType=(IType) checkedMethodBinding.getReturnType().getJavaElement();
			ITypeBinding declaringType=null;
			if (checkedMethodBinding.getDeclaringClass().getJavaElement() instanceof IType)
				declaringType = checkedMethodBinding.getDeclaringClass();
			if (retType!=null && declaringType!=null)
				{
				if (canBeRefactored(retType) && 
						canBeRefactored((IType) nullCheckLocationMethod.getDeclaringClass().getJavaElement()) &&
						canBeRefactored(checkedMethodBinding.getReturnType()) &&
						canBeRefactored(node) &&
						shouldBeRefactored(node))
					{
						pile.addElement(new MetModel(checkedMethodBinding,
								nullCheckLocationMethod, checkedMethodBinding.getReturnType(), op, 
								node.getThenStatement(), node.getElseStatement(),conditionLine,invocation,methodDeclaration));
					}
				else
					{
					pile.removeAllOccurences(checkedMethodBinding);
					}
				}
			}
		else System.out.println("checkedMethodBinding is null");
		}
	
	private boolean canBeRefactored(IfStatement node) 
	{
		if (node.getExpression().toString().contains("&&") || node.getExpression().toString().contains("||"))
			return false;
		return true;
	}
	
	private boolean canBeRefactored (ITypeBinding typeBinding)
	{
		if (typeBinding.isArray())
			return false;
		return true;
	}

	private boolean canBeRefactored (IType iType) throws JavaModelException
	{
		if (iType.isBinary() || iType.isInterface() || iType.isEnum() || iType.isAnonymous())
			return false;
		return true;
	}
	
	private boolean shouldBeRefactored (IfStatement node)
	{
		if (node.getThenStatement().getNodeType() == ASTNode.BLOCK)
		{
			Block block = (Block) node.getThenStatement();
			if (block.statements().size() == 1)
			{
				if (block.statements().get(0).toString().contains("return null;") ||
					node.getThenStatement().toString().contains("return;") ||
					node.getThenStatement().toString().contains("continue;"))
					return false;
			}
		}
		else 
			if (node.getThenStatement().getNodeType() == ASTNode.RETURN_STATEMENT)
				if (node.getThenStatement().toString().contains("return null;") ||
					node.getThenStatement().toString().contains("return;") ||
					node.getThenStatement().toString().contains("continue;"))
					return false;
		return true;	
	}
	
	private boolean shouldBeRefactored (final IVariableBinding variable, IfStatement node)
	{
		final List<IBinding> bindings = new ArrayList<IBinding>();
		node.getThenStatement().accept(new ASTVisitor() {
			public boolean visit(SimpleName name)
			{
				IBinding localBinding = name.resolveBinding();
				if (localBinding != null && localBinding.isEqualTo(variable))
					{
					bindings.add(localBinding);
					}
				return true;
			}
		});
		if (bindings.size() == 0)
			return false;
		return true;
	}
	
	public boolean myContains (IBinding variable, List<IBinding> list)
	{
		for (IBinding v:list)
			if (v.isEqualTo(variable))
				return true;
		return false;
	}
	
	public void startParsing(ICompilationUnit iCUnit)
		{
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(iCUnit);
		parser.setResolveBindings(true);
		compilationUnit=(CompilationUnit)parser.createAST(null);
		compilationUnit.accept(new MyASTVisitor());
		}
	
	
	public void setJavaProject(IJavaProject javaProject)
		{
		this.javaProject = javaProject;
		}
	
	@Override
	public Object[] getElements(Object inputElement)
		{
		return new String[] {getPile().toString()};
		}
	
	public Pile getPile()
		{
		return pile;
		}
	}
