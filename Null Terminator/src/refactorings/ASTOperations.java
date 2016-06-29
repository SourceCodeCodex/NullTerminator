package refactorings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import main.Converter;
import models.CheckedElementModel;
import utils.Utils;

public class ASTOperations
{
	private IProgressMonitor monitor = new NullProgressMonitor();
	private RefactoringHelper refactoringHelper=new RefactoringHelper();

	public VariableDeclarationStatement insertVariableDeclaration (ICompilationUnit cUnit, ASTNode nextNode,
			String variableName, String type, String initializer) throws JavaModelException
	{
		final AST ast=nextNode.getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast);
		final VariableDeclarationFragment retTypeVarDeclFragment=ast.newVariableDeclarationFragment();
		if (initializer == null)
			retTypeVarDeclFragment.setInitializer(ast.newNullLiteral());
		else
			{
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue(initializer);
			retTypeVarDeclFragment.setInitializer(stringLiteral);
			}
		retTypeVarDeclFragment.setName(ast.newSimpleName(variableName));
		VariableDeclarationStatement declarationStatement=ast.newVariableDeclarationStatement(retTypeVarDeclFragment);
		declarationStatement.setType(ast.newSimpleType(ast.newSimpleName(type)));
		if (nextNode.getParent() instanceof Block)
		{
			System.out.println("insertVariableDeclaration Is block");
			ListRewrite lrw = rewriter.getListRewrite(nextNode.getParent(),Block.STATEMENTS_PROPERTY);
			lrw.insertBefore(declarationStatement, nextNode, null);
			rewriteCUnit(cUnit, rewriter);
			try {
				cUnit.commitWorkingCopy(true, null);
				cUnit.save(monitor, true);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("insertVariableDeclaration Is not block: " + nextNode.getParent().getNodeType());
			Block block = ast.newBlock();
			block.statements().add(declarationStatement);
			block.statements().add(rewriter.createCopyTarget(nextNode));
			rewriter.replace(nextNode, block, null);
			rewriteCUnit(cUnit, rewriter);	
			try {
				cUnit.commitWorkingCopy(true, null);
				cUnit.save(monitor, true);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return declarationStatement;
	}
	
	public void removeVariableDeclaration (final ICompilationUnit cUnit, final VariableDeclarationStatement declaration) throws JavaModelException
	{
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			public boolean visit(VariableDeclarationStatement node)
				{
				if (node.toString().equals(declaration.toString()))
				{
					System.out.println("FOUnd my declaration");
					AST ast=node.getParent().getAST();
					final ASTRewrite rewriter=ASTRewrite.create(ast);
					ListRewrite lrw = rewriter.getListRewrite(node.getParent(),Block.STATEMENTS_PROPERTY);
					lrw.remove(node, null);
					rewriteCUnit(cUnit, rewriter);
					try {
						cUnit.commitWorkingCopy(true, null);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
				}});
	}

	public void deleteEmptyStatement (final ICompilationUnit cUnit)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			public boolean visit (EmptyStatement statement)
			{
				final AST ast = statement.getAST();
				final ASTRewrite rewriter=ASTRewrite.create(ast);
				rewriter.remove(statement, null);
				rewriteCUnit(cUnit, rewriter);	
				return true;
			}
		});
	}
	
	public void insertEmptyStatement (final ICompilationUnit cUnit, Statement statement) throws JavaModelException
	{
		if (statement == null)
			return;
		System.out.println("Statement type: " + statement.getNodeType());

		if (statement.getNodeType() == ASTNode.BLOCK)
		{
			Block block = (Block) statement;
			System.out.println("Block: " + block.toString());
			Statement expressionStatement = (Statement) block.statements().get(0);
			final AST ast = block.getAST();
			final ASTRewrite rewriter=ASTRewrite.create(ast);
			ListRewrite lrw = rewriter.getListRewrite(block,Block.STATEMENTS_PROPERTY);
			EmptyStatement emptyStatement = ast.newEmptyStatement();
			lrw.insertAfter(emptyStatement, expressionStatement, null);
			rewriteCUnit(cUnit, rewriter);
			try {
				cUnit.commitWorkingCopy(true, null);
				cUnit.save(monitor, true);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Expression statement: " + statement.toString());
			//ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			final AST ast = statement.getAST();
			final ASTRewrite rewriter=ASTRewrite.create(ast);
			Block block = ast.newBlock();
			EmptyStatement emptyStatement = ast.newEmptyStatement();
			block.statements().add((Statement)rewriter.createCopyTarget(statement));
			block.statements().add(emptyStatement);
			rewriter.replace(statement, block, null);
			rewriteCUnit(cUnit, rewriter);	
			cUnit.commitWorkingCopy(true, null);
		}
	}
	public void removeCondition(final ICompilationUnit cUnit, final Statement replacement, final int conditionLine)
	{	
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			public boolean visit(IfStatement node)
				{
				int foundConditionLine=unit.getLineNumber(node.getStartPosition());
				if (conditionLine == foundConditionLine)
					{
					System.out.println("Remove condition");
					ASTRewrite rewriter=ASTRewrite.create(node.getParent().getAST());
					if (replacement instanceof Block)
						rewriter.replace(node, (ASTNode)((Block) replacement).statements().get(0), null);
					else rewriter.replace(node, replacement, null);
					rewriteCUnit(cUnit, rewriter);
					try {
						cUnit.commitWorkingCopy(true, null);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
				return true;
				}});
	}

	public void removeMethod (final ICompilationUnit cUnit,final String methodName)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true); // we need bindings later on
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration node)
				{
				if (node.getName().toString().equals(methodName))
					{					
					ASTRewrite rewriter=ASTRewrite.create(node.getParent().getAST());
					rewriter.remove(node,null);
					rewriteCUnit(cUnit, rewriter);
					}
				return true;
				}});
	}

public void createMethodInvocation(final ICompilationUnit cUnit,final IType returnType, final int conditionLine)
	{
		System.out.println("Create method invocation!!");
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true); // we need bindings later on
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
		public boolean visit (IfStatement node)
			{
			int foundConditionLine=unit.getLineNumber(node.getStartPosition());
			if (conditionLine == foundConditionLine)
				{
				String returnTypeName=returnType.getElementName();
				final String retTypeVarName=refactoringHelper.toCamelCase(returnTypeName);
				final AST ast=node.getAST();
				final ASTRewrite rewriter=ASTRewrite.create(ast);
				InfixExpression expression = (InfixExpression) node.getExpression();
				MethodInvocation existingInvocation;
				if (expression.getLeftOperand() instanceof NullLiteral)
					existingInvocation = (MethodInvocation) expression.getRightOperand();
				else
					existingInvocation = (MethodInvocation) expression.getLeftOperand();

				final VariableDeclarationFragment retTypeVarDeclFragment=ast.newVariableDeclarationFragment();
				MethodInvocation initInvocation = (MethodInvocation) rewriter.createCopyTarget(existingInvocation);
				retTypeVarDeclFragment.setInitializer(initInvocation);
				retTypeVarDeclFragment.setName(ast.newSimpleName(retTypeVarName));
				VariableDeclarationStatement newDeclarationStatement=ast.newVariableDeclarationStatement(retTypeVarDeclFragment);
				newDeclarationStatement.setType(ast.newSimpleType(ast.newSimpleName(returnTypeName)));	
				
				if (node.getParent() instanceof Block)
				{
					System.out.println("Is block");
					ListRewrite lrw = rewriter.getListRewrite(node.getParent(),Block.STATEMENTS_PROPERTY);
					lrw.insertBefore(newDeclarationStatement, node, null);
					rewriteCUnit(cUnit, rewriter);
					try {
						cUnit.commitWorkingCopy(true, null);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					System.out.println("Is not block: " + node.getParent().getNodeType());
					Block block = ast.newBlock();
					block.statements().add(newDeclarationStatement);
					block.statements().add(rewriter.createCopyTarget(node));
					rewriter.replace(node, block, null);
					rewriteCUnit(cUnit, rewriter);	
					try {
						cUnit.commitWorkingCopy(true, null);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}
			return true;
			}
		});
	}

	public void replaceVariableInvokingMethod(final ICompilationUnit cUnit, final Statement statement, final String variableName)
	{
		statement.accept(new ASTVisitor() {
			public boolean visit (MethodInvocation invocation)
			{
				System.out.println("invoc:" + invocation.getExpression().toString());
				
				final AST ast=statement.getParent().getAST();
				final ASTRewrite rewriter=ASTRewrite.create(ast);
				//ListRewrite lrw = rewriter.getListRewrite(statement.getParent().getParent(),Block.STATEMENTS_PROPERTY);
				//MethodInvocation newInvocation = ast.newMethodInvocation();
				//newInvocation.setName(ast.newSimpleName(invocation.getName().toString()));
				//newInvocation.setExpression(ast.newSimpleName(variableName));
				rewriter.replace(invocation.getExpression(), ast.newSimpleName(variableName), null);
				rewriteCUnit(cUnit, rewriter);
				try {
					cUnit.commitWorkingCopy(true, null);
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});
	}

public void rewriteCUnit(final ICompilationUnit cUnit,ASTRewrite rewriter)
	{
	try
		{
		rewriter.rewriteAST();
		TextEdit textEdit;
		Document document=new Document(cUnit.getSource());
		textEdit=rewriter.rewriteAST(document,cUnit.getJavaProject().getOptions(true));
		textEdit.apply(document);
		String newSource=document.get();
		cUnit.getBuffer().setContents(newSource);
		File file=cUnit.getResource().getLocation().toFile();
		PrintWriter printWriter=new PrintWriter(new BufferedWriter(new FileWriter(file)));
		printWriter.write(newSource);
		printWriter.close();
		cUnit.getResource().refreshLocal(0,null);
		cUnit.close();
		}
	catch (Exception ex)
		{
		ex.printStackTrace();
		}
	}

	public void refindCondition (final CheckedElementModel model)
	{
		final ICompilationUnit cUnit=model.getiCompilationUnit();
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			private boolean found = false;
			public boolean visit(VariableDeclarationStatement node)
				{
				VariableDeclarationStatement dummyDeclaration = (VariableDeclarationStatement)node;
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) dummyDeclaration.fragments().get(0);
				if (fragment != null && fragment.getInitializer() != null)
					if (fragment.getInitializer().getNodeType() == ASTNode.STRING_LITERAL)
						if (((StringLiteral)fragment.getInitializer()).getLiteralValue().equals(Utils.DUMMY_VARIABLE_INITIALIZER))
							{
							//System.out.println("Found dummy declaration: " + dummyDeclaration.toString());
							found = true;
							}
				return true;
				}
			public boolean visit (IfStatement node)
			{
				if (found)
				{
					//System.out.println("Found condition at line " + unit.getLineNumber(node.getStartPosition()));
					model.setConditionLine(unit.getLineNumber(node.getStartPosition()));
					model.setThenBlock(node.getThenStatement());
					model.setElseBlock(node.getElseStatement());	
					found = false;
				}
				return true;
			}
			});
	}
	
	public void removeStaticModifier (final IMethod method)
	{
		final ICompilationUnit cUnit = method.getCompilationUnit();
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true); // we need bindings later on
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration node)
				{
				if (node.getName().toString().equals(method.getElementName()))
					{
					ASTRewrite rewriter=ASTRewrite.create(node.getAST());
					rewriter.remove((ASTNode) node.modifiers().get(1), null);
					rewriteCUnit(cUnit, rewriter);
					try {
						cUnit.commitWorkingCopy(true, null);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
				return true;
				}});
	}
	
	public void addThisAsParameterIfNeccessary(final IMethod method,final IType nullCheckClass,Statement statement) throws JavaModelException
	{
		ILocalVariable[] params=method.getParameters();
		if (params.length == 0)
			return;
		int i=0;
		for (i=0;i<params.length;i++)
			{
			String typeSig = params[i].getTypeSignature();
			System.out.println("addThisAsParameterIfNeccessary Typesig: " + typeSig);
			String resolved = new Converter().resolveTypeNameInContext(typeSig,method.getDeclaringType(),method);
			System.out.println("Resolved: " + resolved);
			String qualifier = Signature.getSignatureQualifier(resolved);
			if (qualifier.equals(""))
			{
				System.out.println("No qualifier, basic type");
				resolved = Signature.getSignatureSimpleName(resolved);
			}
			else resolved = qualifier + "." +Signature.getSignatureSimpleName(resolved);
			System.out.println("addThisAsParameterIfNeccessary Resolved Typesig: " + resolved);
			if(resolved.equals(nullCheckClass.getFullyQualifiedName()))
				break;
			}
		final int position = i;
		if (position >= params.length)
			return;
		System.out.println("Will add 'this' at position " + position);
		statement.accept(new ASTVisitor(){
			public boolean visit(MethodInvocation node)
				{
				System.out.println("################### " + node.toString());
				AST ast = node.getParent().getAST();
				ASTRewrite rewriter=ASTRewrite.create(ast);
				Expression targetParam = (Expression) node.arguments().get(position);
				System.out.println("Target param: " + targetParam.toString());
				//rewriter.replace(targetParam, ast.newThisExpression(), null);
				node.arguments().remove(position);
				node.arguments().add(position,ast.newThisExpression());
				rewriteCUnit(nullCheckClass.getCompilationUnit(), rewriter);
				try {
					nullCheckClass.getCompilationUnit().commitWorkingCopy(true, null);
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
				});
	}
}
