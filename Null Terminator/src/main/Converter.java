package main;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import utils.Pair;

@SuppressWarnings("restriction")
public class Converter
{		
	public String getCustomType (String type)
		{
		String newType = "";
		String dimension = "";
		if (type.equals("V"))
			newType = "void";
		else if (type.equals("I"))
			newType = "int";
		else if (type.equals("F"))
			newType = "double";
		else if (type.equals("D"))
			newType = "double";
		else if (type.equals("J"))
			newType = "long";
		else if (type.equals("S"))
			newType = "short";
		else if (type.equals("Z"))
			newType = "boolean";
		else if (type.equals("C"))
			newType = "char";
		else if (type.equals("B"))
			newType = "byte";
		else newType = type;
		
		if (type.length() > 2)
			if (type.charAt(0) == '[')
				if (type.charAt(1) == '[')
					{
					dimension = "[][]";
					newType = newType.substring(2, newType.length());
					}
				else 
					{
					dimension = "[]";
					newType = newType.substring(1, newType.length());
					}
		newType = newType.replaceFirst("Q","");
		newType = newType.replaceAll(";","");
		if (newType.contains("<") && newType.contains(">"))
			{
				String sub = newType.substring(newType.indexOf("<"), newType.indexOf(">"));
				String newSub = sub.replaceFirst("Q","");
				newType = newType.replaceFirst(sub, newSub);
			}
		newType = newType + dimension;		
		return newType;
		}
	
	public String getMethodSignature(IMethod method) throws Exception
	{
	String signature = "(";
	if (method.getNumberOfParameters() == 0)
		signature = signature + ")";
	else
		{
		ILocalVariable[] params = method.getParameters();
		int i = 0;
		for (i=0;i<params.length;i++)
			{
			String typeSig = params[i].getTypeSignature();
			//System.out.println("Typesig: " + typeSig);
			String resolved = resolveTypeNameInContext(typeSig,method.getDeclaringType(),method);
			//System.out.println("Resolved: " + resolved);
			String qualifier = Signature.getSignatureQualifier(resolved);
			if (qualifier.equals(""))
			{
				//System.out.println("No qualifier, basic type");
				resolved = Signature.getSignatureSimpleName(resolved);
			}
			else resolved = qualifier + "." +Signature.getSignatureSimpleName(resolved);
			//System.out.println("Resolved Typesig: " + resolved);
			signature = signature + resolved + " " + params[i].getElementName() + ", ";
			}
		signature = signature.substring(0,signature.length()-2) + ")";
		}
	return signature;
	}
	
	public String getMethodParametersName(IMethod method) throws Exception
		{
		String params = "";
		ILocalVariable[] types = method.getParameters();
		int i = 0;
		for (i=0;i<types.length;i++)
			params = params + types[i].getElementName() + ", ";
		if (params.length() != 0)
			params = params.substring(0,params.length()-2);
		return params;
		}
	
	public List<Pair<String,String>> getParameters(IMethod method) throws Exception
		{
		List<Pair<String,String>> parameters = new ArrayList<Pair<String,String>>();
		String[] parametersNames = method.getParameterNames();
		String[] parametersTypes = method.getParameterTypes();
		int i;
		for (i=0;i<parametersNames.length;i++)
			{
			System.out.println("Type1: " + parametersTypes[i] + " Name1: " + parametersNames[i]);
			parameters.add(new Pair<String,String>(getCustomType(parametersTypes[i]),parametersNames[i]));
			}
		return parameters;
		}
	
	public String resolveTypeNameInContext(String tsig, IType contextType, IMethod theMethod) throws JavaModelException {
		int arrayCount = Signature.getArrayCount(tsig);
		if(tsig.charAt(arrayCount) == Signature.C_TYPE_VARIABLE) {
			String newSig = "" + Signature.C_TYPE_VARIABLE + Signature.C_SEMICOLON;
			for(int i = 0; i < arrayCount; i++) {
				newSig = Signature.C_ARRAY + newSig;
			}
			return newSig;
		}
		if(tsig.charAt(arrayCount) == Signature.C_UNRESOLVED) {
			int lastNamePos;
			String newSig;
			int bracket = tsig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
			if (bracket > 0) {
				lastNamePos = bracket;
				newSig = ";";
			} else {
				int semi= tsig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
				lastNamePos = semi;
				newSig = tsig.substring(lastNamePos);
			}
			String resolvedType = 	JavaModelUtil.getResolvedTypeName(tsig, contextType);
			if(resolvedType == null) {
				//Generic (?)
				String potentialTemplateName = tsig.substring(arrayCount + 1,tsig.length()-1);
				ITypeParameter tp = contextType.getTypeParameter(potentialTemplateName);
				if(tp.exists()) {
					newSig = "" + Signature.C_TYPE_VARIABLE + Signature.C_SEMICOLON;
					for(int i = 0; i < arrayCount; i++) {
						newSig = Signature.C_ARRAY + newSig;
					}
					return newSig;
				}
				tp = theMethod.getTypeParameter(potentialTemplateName);
				if(tp.exists()) {
					newSig = "" + Signature.C_TYPE_VARIABLE + Signature.C_SEMICOLON;
					for(int i = 0; i < arrayCount; i++) {
						newSig = Signature.C_ARRAY + newSig;
					}
					return newSig;
				}
				throw new RuntimeException("Cannot resolve type " + tsig + " in context " + contextType.getFullyQualifiedName());
			}
			newSig = resolvedType + newSig;
			newSig = Signature.C_RESOLVED + newSig;
			for(int i = 0; i < arrayCount; i++) {
				newSig = Signature.C_ARRAY + newSig;
			}
			return newSig;
		} else {
			return tsig;
		}
	}
}
