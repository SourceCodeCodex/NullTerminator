package refactorings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.MoveMethodDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

import gui.GenericWindow;
import main.RefactoringException;
import utils.Pair;
import utils.Utils;

public class EclipseBasedRefactorings 
{
	private IProgressMonitor monitor = new NullProgressMonitor();
	private RefactoringHelper refactoringHelper = new RefactoringHelper();
	private static final String ATTRIBUTE_DEPRECATE= "deprecate"; //$NON-NLS-1$
	private static final String ATTRIBUTE_INLINE= "inline"; //$NON-NLS-1$
	private static final String ATTRIBUTE_REMOVE= "remove"; //$NON-NLS-1$
	private static final String ATTRIBUTE_TARGET_INDEX= "targetIndex"; //$NON-NLS-1$
	private static final String ATTRIBUTE_TARGET_NAME= "targetName"; //$NON-NLS-1$
	private static final String ATTRIBUTE_USE_GETTER= "getter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_USE_SETTER= "setter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_INPUT= "input"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME= "name"; //$NON-NLS-1$
	//private static final String ATTRIBUTE_ABSTRACT= "abstract"; //$NON-NLS-1$
	//private static final String ATTRIBUTE_ELEMENT= "element"; //$NON-NLS-1$ 
	
	public IMethod moveMethod(IMethod method, IType destination, String targetParameter) throws Exception
	{
		System.out.println("In move method");
		RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(IJavaRefactorings.MOVE_METHOD);
		int flags= JavaRefactoringDescriptor.JAR_REFACTORING | JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE; 
		String project= null; 
		String name=method.getElementName();
		project=method.getJavaProject().getElementName();
		IMethod newMethod=null;
		final Map<String, String> arguments= new HashMap<String, String>();
		arguments.put(ATTRIBUTE_INPUT, refactoringHelper.elementToHandle(project, method));
		arguments.put(ATTRIBUTE_NAME, method.getElementName());
		//the targetName should be the name of the parameter added in case 'this' should be sent
		String targetName = new RefactoringHelper().toCamelCase(method.getDeclaringType().getElementName());
		arguments.put(ATTRIBUTE_TARGET_NAME,targetName);
		arguments.put(ATTRIBUTE_DEPRECATE, Boolean.valueOf(false).toString());
		arguments.put(ATTRIBUTE_REMOVE, Boolean.valueOf(true).toString());
		arguments.put(ATTRIBUTE_INLINE, Boolean.valueOf(true).toString());
		arguments.put(ATTRIBUTE_USE_GETTER, Boolean.valueOf(true).toString());
		arguments.put(ATTRIBUTE_USE_SETTER, Boolean.valueOf(true).toString());
		arguments.put(ATTRIBUTE_TARGET_INDEX, new Integer(0).toString()); //first argument is always destination
		MoveMethodDescriptor descriptor=(MoveMethodDescriptor) contribution.createDescriptor(IJavaRefactorings.MOVE_METHOD, project,
				"Move method "+method.getElementName(), null, arguments, flags); 
		RefactoringStatus status = new RefactoringStatus();
		Refactoring refactoring = descriptor.createRefactoring(status);
		CreateChangeOperation create = new CreateChangeOperation(new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
				RefactoringStatus.FATAL);
		PerformChangeOperation perform = new PerformChangeOperation(create); 
		status.merge(refactoring.checkInitialConditions(new NullProgressMonitor()));
		if (status.getSeverity()==RefactoringStatus.FATAL)
			throw new RefactoringException(Utils.REFACTORING_STATUS_INIT_ERROR+"The method "+method.getElementName()+" can't be moved "+
			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
		status.merge(refactoring.checkFinalConditions(new NullProgressMonitor()));
		if (status.getSeverity()==RefactoringStatus.FATAL)
			throw new RefactoringException(Utils.REFACTORING_STATUS_FINAL_ERROR+"The method "+method.getElementName()+" can't be moved "+
			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
		perform.run(monitor);
		newMethod= refactoringHelper.getIMethodFromType(destination,name);
		if (newMethod!=null)
			{
			System.out.println("Method found in class " + newMethod.getDeclaringType().getElementName());
			return newMethod;
			}
		else
		{
			System.out.println("Method not found in destination");
			return method;
		}
	}

//must extract only the target statement, not all which are identical in the same class
	@SuppressWarnings("restriction")
	public IMethod extractMethod(IType location, Statement statement,String methodName, ICompilationUnit cUnit) throws Exception
	{
		//System.out.println("Will extract the new method " + methodName + " in type " + location.getElementName());
		//System.out.println("Block to be extracted: " + statement.toString());
		int start,length;
		if (statement.toString().charAt(0)=='{')
			{
			start=statement.getStartPosition()+1;
			length=statement.getLength()-2;
			}
		else
			{
			start=statement.getStartPosition();
			length=statement.getLength();	   
			}
		//positions for else taken after removing then->problem
	    RefactoringStatus status;
	    ExtractMethodRefactoring extractMetRef = new ExtractMethodRefactoring(cUnit,start,length);
	    extractMetRef.setMethodName(methodName);
	    extractMetRef.setVisibility(Flags.AccPublic);
	    extractMetRef.setReplaceDuplicates(true);
	    extractMetRef.setValidationContext(null);
	    status=extractMetRef.checkAllConditions(monitor);
	    if (status.getSeverity()==RefactoringStatus.FATAL)
	    	throw new RefactoringException("The method "+extractMetRef.getMethodName()+" can't be extracted "+
	    			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
	    Change change = extractMetRef.createChange(monitor);
	    change.perform(monitor);
	    IMethod extractedMethod = refactoringHelper.getIMethodFromType(location,methodName);
	    if (extractedMethod != null && Flags.isStatic(extractedMethod.getFlags()))
	    	new ASTOperations().removeStaticModifier(extractedMethod);
	    return refactoringHelper.getIMethodFromType(location,methodName);
	}

	//adds a parameter with the type of the class where the method will be moved. The param will be first argument
	@SuppressWarnings("restriction")
	public IMethod addParameter(IMethod method,String type,String name)	 throws Exception
	{
		System.out.println("Will add the following parameter: " + type + " " + name);
		IType cUnit=(IType) method.getParent();
		String name1=method.getElementName();
		RefactoringStatus status;
		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		ParameterInfo info=new ParameterInfo(type, name, ParameterInfo.INDEX_FOR_ADDED);
		info.setDefaultValue(name);
		processor.getParameterInfos().add(0,info);
		CheckConditionsContext context= new CheckConditionsContext();
		context.add(new ValidateEditChecker(null));
		context.add(new ResourceChangeChecker());
		status=processor.checkInitialConditions(monitor);
	    if (status.getSeverity()==RefactoringStatus.FATAL)
	    	throw new RefactoringException("The parameter '"+type+":"+name+"' can't be added "+
	    			"to the method "+method.getElementName()+"because of the following reason: "+
	    			status.getMessageMatchingSeverity(status.getSeverity()));
	    status=processor.checkFinalConditions(monitor,context);
	    if (status.getSeverity()==RefactoringStatus.FATAL)
	    	throw new RefactoringException("The parameter '"+type+":"+name+"' can't be added to the method "
	    			+ method.getElementName()+"because of the following reason: "+
	    			status.getMessageMatchingSeverity(status.getSeverity()));
	    processor.createChange(monitor).perform(monitor);
		return refactoringHelper.getIMethodFromType(cUnit,name1);
		}
	
	
	@SuppressWarnings("restriction")
	public IMethod renameMethod (IMethod method,String newName) throws Exception
		{
		RefactoringStatus status;
		IType type=(IType) method.getParent();
		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		CheckConditionsContext context= new CheckConditionsContext();
		processor.setNewMethodName(newName);
		context.add(new ValidateEditChecker(null));
		context.add(new ResourceChangeChecker());
		status=processor.checkInitialConditions(monitor);
		if (status.getSeverity()==RefactoringStatus.FATAL)
		    	throw new RefactoringException("The method"+method.getElementName()+" can't be renamed "+
		    			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
		status=processor.checkFinalConditions(monitor,context);
		if (status.getSeverity()==RefactoringStatus.FATAL)
		    	throw new RefactoringException("The method"+method.getElementName()+" can't be renamed "+
		    			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
		processor.createChange(monitor).perform(monitor);
		return refactoringHelper.getIMethodFromType(type,newName);
		}
		
	@SuppressWarnings("restriction")
	public IMethod changeParametersOrder(IMethod method, String type, String name)	 throws Exception
		{
		System.out.println("Starting change parameters order!!!");
		System.out.println("Target param: " + type + " " + name);
		IType cUnit=(IType) method.getParent();
		RefactoringStatus status;
		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		List<ParameterInfo> list=processor.getParameterInfos();
		ParameterInfo temp = null;
		int i;
		if (list.size()>1)	//pointless if is only a parameter
			{
			if (list.get(0).getOldTypeName().equals(type) && list.get(0).getOldName().equals(name))
				//if the desired parameter is already first		
				return method;
			for (i=0;i<list.size();i++)
			{
				System.out.println("Current param: " + list.get(i).getOldTypeName() + " " + list.get(i).getOldName());
				if (list.get(i).getOldTypeName().equals(type) && list.get(i).getOldName().equals(name))
					{
					System.out.println("Found it");
					temp=list.get(i);
					list.remove(i);
					break;
					}
			}
			processor.getParameterInfos().add(0, temp);
			CheckConditionsContext context= new CheckConditionsContext();
			context.add(new ValidateEditChecker(null));
			context.add(new ResourceChangeChecker());
			status=processor.checkInitialConditions(monitor);
		    if (status.getSeverity()==RefactoringStatus.FATAL)
		    	throw new RefactoringException("Can't change the order of parameters for method "+
		    			method.getElementName()+"because of the following reason: "+
		    			status.getMessageMatchingSeverity(status.getSeverity()));
		    status=processor.checkFinalConditions(monitor,context);
		    if (status.getSeverity()==RefactoringStatus.FATAL)
		    	throw new RefactoringException("Can't change the order of parameters for method "+
		    			method.getElementName()+"because of the following reason: "+
		    			status.getMessageMatchingSeverity(status.getSeverity()));
		    processor.createChange(monitor).perform(monitor);
		    System.out.println("Ending change parameters order!!!");
			return refactoringHelper.getIMethodFromType(cUnit,method.getElementName());
			}
		return method;
	}

	@SuppressWarnings("restriction")
	public IMethod changeSignature (IMethod method, List<Pair<String,String>> params) throws Exception
	{
		RefactoringStatus status;
		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		IType methodType=(IType) method.getParent();
		processor.getParameterInfos().clear();
		ParameterInfo info;
		int i;
		for (i=0;i<params.size();i++)
	    	{
	        String type = params.get(i).getKey();
	        String name = params.get(i).getValue();
	        System.out.println("Type: " + params.get(i).getKey() + " Name: " + params.get(i).getValue());
	        info=new ParameterInfo(type, name, ParameterInfo.INDEX_FOR_ADDED);
			info.setDefaultValue(name);
			processor.getParameterInfos().add(info);
	    	}
		CheckConditionsContext context= new CheckConditionsContext();
		context.add(new ValidateEditChecker(null));
		context.add(new ResourceChangeChecker());
		status=processor.checkInitialConditions(monitor);
		 if (status.getSeverity()==RefactoringStatus.FATAL)
		    	throw new RefactoringException("The signature of method"+method.getElementName()+" can't be changed "+
		    			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
		status=processor.checkFinalConditions(monitor,context);
		 if (status.getSeverity()==RefactoringStatus.FATAL)
		    	throw new RefactoringException("The signature of method"+method.getElementName()+" can't be changed "+
		    			"because of the following reason: "+status.getMessageMatchingSeverity(status.getSeverity()));
		processor.createChange(monitor).perform(monitor);
		return refactoringHelper.getIMethodFromType(methodType,method.getElementName());
	}
}
