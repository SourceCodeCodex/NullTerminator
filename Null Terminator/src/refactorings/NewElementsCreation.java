package refactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import main.Converter;
import utils.Utils;
import utils.Pair;

public class NewElementsCreation
{
	private IProgressMonitor monitor = new NullProgressMonitor();
	private RefactoringHelper refactoringHelper=new RefactoringHelper();
	private Converter converter=new Converter();

	public Pair<IType,Boolean> createNullClass(IType superclass) throws Exception
	{	
		ICompilationUnit nullClass;
		String superclassName=superclass.getElementName();
		String nullClassName=Utils.NULL+superclass.getElementName();
		IPackageFragment currentPackage=superclass.getPackageFragment();
	    ICompilationUnit[] cUnits=currentPackage.getCompilationUnits();
	    for (ICompilationUnit cUnit:cUnits)
	    	{
	    	IType[] types=cUnit.getAllTypes();
	    	for (IType type:types)
	    		if (type.getElementName().equals(nullClassName))
	    			return new Pair<IType,Boolean>(type,true);
	    	}
		String content="public class "+Utils.NULL+superclassName+" extends "+superclassName+"\n\t{\n\t}";
		if (!superclass.isMember())
			{
			nullClass=currentPackage.createCompilationUnit(Utils.NULL+superclassName+".java",content,true,null);
			if(!currentPackage.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
				nullClass.createPackageDeclaration(currentPackage.getElementName(), monitor);
			}
			overrideMethods(superclass, nullClass.getAllTypes()[0], superclassName);
			return new Pair<IType,Boolean>(nullClass.getAllTypes()[0], false);
			}
		else
			{
			IType enclosingType=(IType) superclass.getParent();
			String content2="class "+Utils.NULL+superclassName+" extends "+superclassName+"\n\t{\n\t}";
			IType nullType;
			nullType=enclosingType.createType(content2,null,true,monitor);
			overrideMethods(superclass, nullType, superclassName);
			return new Pair<IType,Boolean>(nullType,false);
			}
	}


	private void overrideMethods(IType superclass, IType nullClass, String superclassName)
			throws JavaModelException, Exception {
		String methodSig;
		String methodContent;
		ArrayList<IMethod> methods=refactoringHelper.getMethodsFromType(superclass);
		for (IMethod m:methods)
			{
				methodContent = "";
				System.out.println("Parsing method: " + m.getElementName());
				methodSig=converter.getMethodSignature(m);
				if (m.isConstructor())
				{
					methodContent="public "+Utils.NULL+superclassName+methodSig+"\n\t{\n\tsuper("+
								converter.getMethodParametersName(m)+");\n\t}";
					nullClass.createMethod(methodContent, null, true, monitor);
				}
				else
				{
					methodContent = methodContent + "@Override\n";
					int flags = m.getFlags();
					if (Flags.isPrivate(flags))
						break;
					else if (Flags.isPackageDefault(flags));
					else if (Flags.isProtected(flags))
						methodContent = methodContent + "protected ";
					else if (Flags.isPublic(flags))
						methodContent = methodContent + "public ";
					
					if (Flags.isStatic(flags))
					{
						methodContent = methodContent + "static ";
						methodContent = methodContent.replace("@Override","");
					}
					String returnType = new Converter().resolveTypeNameInContext(m.getReturnType(), m.getDeclaringType(), m);
					String qualifier = Signature.getSignatureQualifier(returnType);
					if (qualifier.equals(""))
					{
						//System.out.println("No qualifier, basic type");
						returnType = Signature.getSignatureSimpleName(returnType);
					}
					else returnType = qualifier + "." +Signature.getSignatureSimpleName(returnType);
					
					methodContent = methodContent + returnType + " " + m.getElementName()
							+ methodSig + "\n{\n";
					methodContent = methodContent + "\tthrow new NullPointerException();\n}\n";
					nullClass.createMethod(methodContent, null, false, monitor);
				}
			}
	}


	public void createNullMethod(IType location, IMethod referenceMethod) throws Exception
		{
			String returnType = new Converter().resolveTypeNameInContext(referenceMethod.getReturnType(), referenceMethod.getDeclaringType(), referenceMethod);
			String qualifier = Signature.getSignatureQualifier(returnType);
			if (qualifier.equals(""))
			{
				//System.out.println("No qualifier, basic type");
				returnType = Signature.getSignatureSimpleName(returnType);
			}
			else returnType = qualifier + "." +Signature.getSignatureSimpleName(returnType);
			List<Pair<String,String>> params=converter.getParameters(referenceMethod);
			String method = "public ";
			if (Flags.isStatic(referenceMethod.getFlags()))
				method = method + "static ";
			method = method + returnType + " "
					+referenceMethod.getElementName()+converter.getMethodSignature(referenceMethod)+"\n{\n";
			int i;
			for (i=0;i<params.size();i++)
				if (params.get(i).getKey().equals(returnType))
					method=method+"return "+params.get(i).getValue()+";\n";
			method=method+"}";
			location.createMethod(method, null, true, monitor);
		}

	public void createEmptyConstructor(IType location) throws JavaModelException
		{
			boolean flag=false;
			IMethod[] members=location.getMethods();
			for (IMethod m:members)
				if (m.isConstructor() && m.getParameters().length==0)
					{
					flag=true;
					break;
					}
			if (!flag)
				{
				String cons="public "+ location.getElementName()+"(){}";
				location.createMethod(cons, null, true, monitor);
				}
		}
}