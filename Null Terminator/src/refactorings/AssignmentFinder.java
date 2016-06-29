package refactorings;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

import models.ImpossibleVariableNullAssignmentModel;
import models.MethodNullAssignmentModel;
import models.NullAssignmentModel;
import models.VariableNullAssignmentModel;
import utils.Utils;


public class AssignmentFinder 
{
	private List<NullAssignmentModel> nullAssignments = new ArrayList<NullAssignmentModel>();
	private List<VariableNullAssignmentModel> collectionAssignments = new ArrayList<VariableNullAssignmentModel>();
	private List<IVariableBinding> processedVariables = new ArrayList<IVariableBinding>();
	private Stack<IVariableBinding> interestingVariables = new Stack<IVariableBinding>();
		
	public boolean myContains (IVariableBinding variable, Stack<IVariableBinding> stack)
	{
		List <IVariableBinding> list = new ArrayList<IVariableBinding> (stack);
		return myContains(variable, list);
	}
	
	public boolean myContains (IVariableBinding variable, List<IVariableBinding> list)
	{
		for (IVariableBinding v:list)
			if (v.isEqualTo(variable))
				return true;
		return false;
	}
	
	public Expression solveCast (Expression expression)
	{
		if (expression instanceof CastExpression)
			return ((CastExpression) expression).getExpression();
		return expression;
	}
	
	//var = null
	public void inspectAssignment (IVariableBinding variable, Expression expression)
	{
		//expression = solveCast(expression);
		if (expression instanceof NullLiteral)
			{
			System.out.println("Added null assignment: " + variable.getName() + "=null");
			nullAssignments.add(new VariableNullAssignmentModel(variable,
					((CompilationUnit)expression.getRoot()).getLineNumber(expression.getStartPosition())));
			}
		else inspectAssignment(expression);
	}
	
	//methodInvocation (expression)
	public void inspectAssignment (MethodInvocation methodInvocation, Expression expression)
	{
		//expression = solveCast(expression);
		if (expression instanceof NullLiteral)
			{
			System.out.println("Added null sent as argument: " + methodInvocation.getName() + "=null");
			nullAssignments.add(new MethodNullAssignmentModel(methodInvocation.resolveMethodBinding(),
					((CompilationUnit)methodInvocation.getRoot()).getLineNumber(expression.getStartPosition()),
					((ICompilationUnit)((CompilationUnit)expression.getRoot()).getJavaElement())));
			}
		else inspectAssignment(expression);
	}
	
	//new Constructor (expression)
	public void inspectAssignment (ClassInstanceCreation instanceCreation, Expression expression)
	{
		System.out.println("Calling inspect assignment");
		//expression = solveCast(expression);
		if (expression!=null)
			System.out.println("Expr: " + expression.toString());
		if (expression instanceof NullLiteral)
			{
			System.out.println("Added null sent as argument: " + instanceCreation.getType() + "=null");
			nullAssignments.add(new MethodNullAssignmentModel(instanceCreation.getType().resolveBinding(),
					((CompilationUnit)instanceCreation.getRoot()).getLineNumber(expression.getStartPosition()),
					((ICompilationUnit)((CompilationUnit)expression.getRoot()).getJavaElement())));
			}
		else inspectAssignment(expression);
	}
	/*
	 * var = otherVar
	 * var = method()
	 * var = array[i]
	 * var = this.field
	 * var = super.field
	 */
	public void inspectAssignment (Expression expression)
	{
		expression = solveCast(expression);
		if (expression instanceof MethodInvocation)	//works also for chain invocation
			{
			System.out.println("Method invocation" + expression.toString());
			MethodInvocation invocation = (MethodInvocation) expression;
			//invocation.resolveMethodBinding().getMethodDeclaration();
			findMethodDeclaration(invocation);
			}
		else 
		{
			IVariableBinding variableBinding = null;
			if (expression != null)
				System.out.println("Expression type: " + expression.getNodeType());
			if (expression instanceof SimpleName)
				variableBinding = (IVariableBinding) ((SimpleName)expression).resolveBinding();	
			else if (expression instanceof QualifiedName)
				variableBinding = (IVariableBinding) ((QualifiedName)expression).getName().resolveBinding();
			else if (expression instanceof ArrayAccess)
				inspectAssignment(((ArrayAccess) expression).getArray());
			else if (expression instanceof FieldAccess)
				variableBinding = (IVariableBinding) ((FieldAccess)expression).getName().resolveBinding();							
			else if (expression instanceof SuperFieldAccess)
				variableBinding = (IVariableBinding) ((SuperFieldAccess)expression).getName().resolveBinding();
			
			if (variableBinding != null)
				if (!myContains(variableBinding, interestingVariables))
					{
					System.out.println("Added new variable: " + variableBinding.getName());
					interestingVariables.push(variableBinding);
					}
		}
	}
		
	public void findMethodDeclaration(MethodInvocation invocation)
	{
		int i;
		final IMethodBinding methodBinding = invocation.resolveMethodBinding();
		System.out.println("Method binding: " + methodBinding.getName());
		if (!methodBinding.getDeclaringClass().isFromSource())
		{
			System.out.println("Source not accesible");
			inspectImpossibleMethod (invocation);
			return;
		}
		SearchPattern pattern = SearchPattern.createPattern((IMethod)methodBinding.getJavaElement(), IJavaSearchConstants.DECLARATIONS);
		SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
		MethodsCollector collector = new MethodsCollector();
		IJavaSearchScope scope = null;
		try 
		{
			scope = SearchEngine.createHierarchyScope(((IMethod)methodBinding.getJavaElement()).getDeclaringType());
			new SearchEngine().search(pattern, participants, scope, collector, new NullProgressMonitor());
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}

		List<IMethod> result = collector.getMethods();
		IMethod[] resultsArray = result.toArray(new IMethod[result.size()]);
		IBinding[] foundBindings = null;
		
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setProject(result.get(0).getJavaProject());
		parser.setResolveBindings(true);
		foundBindings = (IBinding[]) parser.createBindings(resultsArray,new NullProgressMonitor());
		System.out.println("RESULT SIZE = " + result.size());
		for (i=0;i<result.size();i++)
		{	
			System.out.println("METHOD DECLARATION FOUND: " + result.get(i).getElementName()
					+ " in file " + result.get(i).getCompilationUnit().getElementName());
			if (foundBindings[i] instanceof IMethodBinding)
				{
				final IMethodBinding currentBinding = (IMethodBinding) foundBindings[i];
				ICompilationUnit cUnit=((IMethod)foundBindings[i].getJavaElement()).getCompilationUnit();
				ASTParser parser1=ASTParser.newParser(AST.JLS4);
				parser1.setKind(ASTParser.K_COMPILATION_UNIT);
				parser1.setSource(cUnit);
				parser1.setResolveBindings(true);
				final CompilationUnit unit = (CompilationUnit) parser1.createAST(null);
				unit.accept(new ASTVisitor(){
					public boolean visit (MethodDeclaration declaration)
					{
						final IMethodBinding localBinding = declaration.resolveBinding();
						if (localBinding == null)
							return false;
						if (localBinding.isEqualTo(currentBinding))
							{
							declaration.accept(new ASTVisitor() {
							public boolean visit (ReturnStatement returnStatement)
							{
								System.out.println("Inspecting return statement: " + returnStatement.toString());
								if (returnStatement.getExpression() instanceof NullLiteral)
									{
									nullAssignments.add(new MethodNullAssignmentModel(localBinding,
											((CompilationUnit)returnStatement.getRoot()).getLineNumber(returnStatement.getStartPosition()),
											((ICompilationUnit)((CompilationUnit)returnStatement.getRoot()).getJavaElement())));
									System.out.println("Added null return statement");
									}
								else if (returnStatement.getExpression() instanceof MethodInvocation)
								{
									IMethodBinding invocationBinding = ((MethodInvocation)returnStatement.getExpression()).resolveMethodBinding();
									System.out.println("invocationBinding: " + invocationBinding.getName() + " " + invocationBinding.toString());
									System.out.println("currentBinding: " + currentBinding.getName() + " " + currentBinding.toString());
									System.out.println("local binding: " + localBinding.getName() + " " + localBinding.toString());
									
									if (localBinding.isEqualTo(invocationBinding)) //||
										//	currentBinding.isEqualTo(localBinding))
									{
										System.out.println("Recursion!!!");
									}
									else inspectAssignment(returnStatement.getExpression());
								}
								else inspectAssignment(returnStatement.getExpression());
								return true;						
							}
						});
							}
						return true;
					}
				});
			}
		}
	}
	
	private void inspectImpossibleMethod(MethodInvocation invocation) 
	{
		Expression expression = invocation.getExpression();
		System.out.println("Expression: " + expression);
		IVariableBinding variableBinding = null;
		if (expression instanceof SimpleName)
			variableBinding = (IVariableBinding)((SimpleName) invocation.getExpression()).resolveBinding();
		else if (expression instanceof QualifiedName)
			variableBinding = (IVariableBinding)((QualifiedName) invocation.getExpression()).resolveBinding();
		else if (expression instanceof FieldAccess)
			variableBinding = (IVariableBinding) ((FieldAccess)expression).getName().resolveBinding();	
		else if (expression instanceof SuperFieldAccess)
			variableBinding = (IVariableBinding) ((SuperFieldAccess)expression).getName().resolveBinding();
		else if (expression instanceof MethodInvocation)
		{
			System.out.println("Another invocation");
			//inspectImpossibleMethod((MethodInvocation) expression);
			findMethodDeclaration((MethodInvocation) expression);
			return;
		}
		else System.out.println("Other type: " + expression.getNodeType());
		nullAssignments.add(new ImpossibleVariableNullAssignmentModel(variableBinding,
				((CompilationUnit)invocation.getRoot()).getLineNumber(expression.getStartPosition())));
		Utils.foundImpossibleMethod = true;
	}
	
	public void processAssignment (Expression leftSide, Expression rightSide, IVariableBinding variable)
	{
		if (leftSide instanceof SimpleName)
		{
			System.out.println ("left side is simpleName");
			IVariableBinding leftVariableBinding = (IVariableBinding) ((SimpleName)leftSide).resolveBinding();
			if (leftVariableBinding.isEqualTo(variable))
				inspectAssignment(leftVariableBinding, rightSide);
		}
		else if (leftSide instanceof QualifiedName)
		{
			System.out.println ("left side is qualifiedName");
			IVariableBinding leftVariableBinding = (IVariableBinding) ((QualifiedName)leftSide).getName().resolveBinding();
			if (leftVariableBinding.isEqualTo(variable))
				inspectAssignment(leftVariableBinding, rightSide);
		}
		else if (leftSide instanceof ArrayAccess)
		{
			Expression arrayVariable = ((ArrayAccess)leftSide).getArray();
			processAssignment(arrayVariable,rightSide,variable);
		}
		else if (leftSide instanceof FieldAccess)
		{
			System.out.println ("left side is fieldaccess");
			IVariableBinding leftVariableBinding = (IVariableBinding) ((FieldAccess)leftSide).getName().resolveBinding();				
			if (leftVariableBinding.isEqualTo(variable))
				inspectAssignment(leftVariableBinding, rightSide);
		}
		else if (leftSide instanceof SuperFieldAccess)
		{
			System.out.println ("left side is superfieldaccess");
			IVariableBinding leftVariableBinding = (IVariableBinding) ((SuperFieldAccess)leftSide).getName().resolveBinding();				
			if (leftVariableBinding.isEqualTo(variable))
				inspectAssignment(leftVariableBinding, rightSide);
		}		
		else System.out.println ("left side is " + leftSide.getNodeType());
	}
	
	public void findAssignment(final IMethodBinding methodBinding, final IVariableBinding variable)
	{
		if (methodBinding == null)
		{
			System.out.println("Method binding is null");
			return;
		}
		//System.out.println("Method binding: " + methodBinding.getName());
		IMethod method = (IMethod) methodBinding.getJavaElement();
		if (method == null)
			{
			System.out.println("Default constructor");
			return;	//covers the case when we try to access the default constructor
			}
		ICompilationUnit iCompilationUnit=method.getCompilationUnit();
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(iCompilationUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
		public boolean visit(MethodDeclaration node)
			{
			IMethodBinding localBinding = node.resolveBinding();
			if (localBinding == null)
				{
				System.out.println("Local binding is null");
				return false;
				}
			if (localBinding.isEqualTo(methodBinding))
				node.accept(new ASTVisitor(){
				
				public boolean visit(Assignment assignment)	//var = something;
					{
					processAssignment(assignment.getLeftHandSide(),assignment.getRightHandSide(),variable);
					return true;
					}
				
				public boolean visit(VariableDeclarationFragment declaration)  //Type var = something;
					{
					IVariableBinding variableBinding = (IVariableBinding)(declaration.getName()).resolveBinding();
					if (variableBinding == null)
						return false;
					if (variableBinding.isEqualTo(variable))
						inspectAssignment(variableBinding, declaration.getInitializer());
					return true;
					}
				});
			return true;
			}
		});
	}
	
	/*
	 * used to find uninitialized or null initialized fields
	 */
	public void findFieldDeclaration (ITypeBinding typeBinding, final IVariableBinding variable)
	{
		System.out.println("Looking for field declaration for field: " + variable.getName() + " in type: " + typeBinding.getName());
		ICompilationUnit iCompilationUnit=((IField)variable.getJavaElement()).getCompilationUnit();
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(iCompilationUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			public boolean visit (VariableDeclarationFragment declaration)
			{
				IVariableBinding variableBinding = (IVariableBinding)(declaration.getName()).resolveBinding();
				if (variableBinding.isEqualTo(variable))
					//no need to do this for local variables since eclipse cries at compile time about this
					if (declaration.getInitializer() == null)
					{
						System.out.println("Adding uninitialized field" + variable.getName());
						//nullAssignments.put(variable, ((CompilationUnit)declaration.getRoot()).getLineNumber(declaration.getStartPosition()));
						nullAssignments.add(new VariableNullAssignmentModel(variable, ((CompilationUnit)declaration.getRoot()).getLineNumber(declaration.getStartPosition())));
					}
					else {
						System.out.println("Initialized with something");
						inspectAssignment(variableBinding, declaration.getInitializer());
					}
				return true;	
			}
		});
	}
	
	public void inspectField (IVariableBinding variable) throws JavaModelException
	{
		System.out.println("Is field");
		ITypeBinding declaringClass = variable.getDeclaringClass();
		IMethodBinding[] methods = declaringClass.getDeclaredMethods();
		IField field = (IField) variable.getJavaElement();
	
		findFieldDeclaration(declaringClass, variable);
		if (Flags.isPrivate(field.getFlags()))
		{
			System.out.println("Private field");
			//check all methods in the field class that access the current field
			for (IMethodBinding method:methods)
			{
				System.out.println("Parsing method: " + method.getName());
				findAssignment(method, variable);
			}
		}
		else if (Flags.isProtected(field.getFlags()) || Flags.isPackageDefault(field.getFlags()))
		{
			System.out.println("Protected or package field");
			IPackageFragment packageFragment = (IPackageFragment) declaringClass.getPackage().getJavaElement();
			System.out.println("Package: " + packageFragment.getElementName());
			ICompilationUnit[] compilationUnits = packageFragment.getCompilationUnits();
			for (ICompilationUnit compilationUnit:compilationUnits)	//for every .java file
				{
					System.out.println("Unit: " + compilationUnit.getElementName());
					IType[] types = compilationUnit.getAllTypes();
					for (IType type:types)
					{
						System.out.println("Type: " + type.getElementName());
						ASTParser parser=ASTParser.newParser(AST.JLS4);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setProject(packageFragment.getJavaProject());
						parser.setResolveBindings(true);
						IBinding[] methodsBinding = parser.createBindings(type.getMethods(), new NullProgressMonitor());
						System.out.println("methods bindings found: "+ methodsBinding.length);
						for (IBinding currentMethod:methodsBinding)
							{
							System.out.println("Searching assignment in method " + currentMethod.getName());
							findAssignment((IMethodBinding) currentMethod, variable);
							}
					}
				}
			if (Flags.isProtected(field.getFlags()))
			{
				System.out.println("Look for subclasses");
				//should search for subclasses
			}
		}
		else
		{
			System.out.println("Public field");
			IJavaProject javaProject = field.getJavaProject();
			int i;
			IPackageFragmentRoot[] packages=javaProject.getPackageFragmentRoots();
			for (IPackageFragmentRoot iPackageFragmentRoot:packages)
				if (!(iPackageFragmentRoot.isExternal()))
					{
					IJavaElement[] javaElements=iPackageFragmentRoot.getChildren();
					for (i=0;i<javaElements.length;i++)
						{
						IPackageFragment packageFragment=(IPackageFragment)javaElements[i];				
						ICompilationUnit[] compilationUnits = packageFragment.getCompilationUnits();
						for (ICompilationUnit compilationUnit:compilationUnits)	//for every .java file
							{
								IType[] types = compilationUnit.getAllTypes();
								for (IType type:types)
								{
									ASTParser parser=ASTParser.newParser(AST.JLS4);
									parser.setKind(ASTParser.K_COMPILATION_UNIT);
									parser.setProject(javaProject);
									parser.setResolveBindings(true);
									IBinding[] methodsBinding = parser.createBindings(type.getMethods(), new NullProgressMonitor());
									for (IBinding currentMethod:methodsBinding)
										findAssignment((IMethodBinding) currentMethod, variable);
								}
							}
						}
					}
		}
	}
	
	public void inspectParameter (IVariableBinding variable, final int position) throws JavaModelException
	{
		int i;
		ICompilationUnit[] compilationUnits;
		System.out.println("Is parameter");
		final IMethodBinding declaringMethod = variable.getDeclaringMethod();
		System.out.println("declaring method: " + declaringMethod.getName());
		//should search in entire project for calls of this method
		IJavaProject javaProject = declaringMethod.getJavaElement().getJavaProject();
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
						ASTParser parser=ASTParser.newParser(AST.JLS4);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setSource(compilationUnit);
						parser.setResolveBindings(true);
						CompilationUnit cUnit = (CompilationUnit)parser.createAST(null);
						cUnit.accept(new ASTVisitor(){
							public boolean visit (MethodInvocation methodInvocation)
							{
								if (methodInvocation.resolveMethodBinding() != null)
								{
									if (methodInvocation.resolveMethodBinding().isEqualTo(declaringMethod))
									{
										System.out.println("Found invocation: " + methodInvocation.toString());
										List<Expression> list = methodInvocation.arguments();
										inspectAssignment(methodInvocation, list.get(position));
									}
								}
								return true;
							}
							
							public boolean visit (ClassInstanceCreation instanceCreation)
							{
								if (!declaringMethod.isConstructor())
									return false;
								ITypeBinding constructorBinding = instanceCreation.getType().resolveBinding();
								if (constructorBinding!=null)
									if (constructorBinding.getName().equals(declaringMethod.getDeclaringClass().getName()))
										{
										System.out.println("Class creation " + instanceCreation.toString() + "   " + 
												instanceCreation.getExpression() + " "+ instanceCreation.getType().toString());
	
										List<Expression> list = instanceCreation.arguments();
										//must check the size in case a class has more than 1 constructor
										if (position >= list.size())
											return false;
										inspectAssignment(instanceCreation, list.get(position));
										}
								return true;
							}
						});
						}
					}
				}
	}
	
	public void showResults()
	{
		int i;
		StringBuilder content = new StringBuilder();
		if (collectionAssignments.isEmpty())
			return;
		content.append("Collections: \n");
		//should be also added in the null assignments table
		for (i=0;i<collectionAssignments.size();i++)
		{
			content.append(collectionAssignments.get(i).getElementName() + " in file "
					+ collectionAssignments.get(i).getFileName() + " at line "
					+ collectionAssignments.get(i).getLineNumber());
		}
	}
	
	public List<NullAssignmentModel> mainFinder (MethodInvocation invocation) throws JavaModelException
	{
		findMethodDeclaration(invocation);
		return mainFinder();
	}
	
	public List<NullAssignmentModel> mainFinder (IVariableBinding variable) throws JavaModelException
	{
		nullAssignments.clear();
		interestingVariables.clear();
		interestingVariables.push(variable);
		return mainFinder();
	}
	
	public List<NullAssignmentModel> mainFinder () throws JavaModelException
	{
		IVariableBinding currentVariable;
		while (!interestingVariables.isEmpty())
		{		
			currentVariable = interestingVariables.pop();
			System.out.println("Inspecting " + currentVariable.getName());
			if (myContains(currentVariable, processedVariables))
				System.out.println("Already processed!");
			else
			{
				findAssignment(currentVariable.getDeclaringMethod(), currentVariable);
				processedVariables.add(currentVariable);
				if (currentVariable.isField())
					inspectField(currentVariable);
				else if (currentVariable.isParameter())
					{
					System.out.println("Param name: " + currentVariable.getName());
					System.out.println("method declaration: "+currentVariable.getDeclaringMethod().getName());
					ITypeBinding[] paramTypes = currentVariable.getDeclaringMethod().getParameterTypes();
					String[] paramNames = ((IMethod)currentVariable.getDeclaringMethod().getJavaElement()).getParameterNames();
					int i;
					for (i=0;i<paramTypes.length;i++)
					{
						System.out.println("argument " + paramTypes[i].getName() + " " + paramNames[i]);
						//try to get the actual name of the parameters
						if (paramTypes[i].isEqualTo(currentVariable.getType()) && 
							paramNames[i].equals(currentVariable.getName()))
						{
							System.out.println("Found argument position = "+i);
							inspectParameter(currentVariable,i);
							break;
						}
					}
				}
			}
		}
		showResults();
		return nullAssignments;
	}
	
	public List<NullAssignmentModel> getNullAssignmentModel() {
		return nullAssignments;
	}
}