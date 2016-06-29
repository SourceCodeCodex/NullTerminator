package refactorings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import main.Converter;
import utils.Pair;

public class RefactoringHelper
	{
	
	public String proposeParamName(String type, String name, IMethod method) throws JavaModelException
	{
	String proposedName = name;
	int i=0;
	while (exists(type,proposedName,method))
		{
		proposedName = name+i;
		i++;
		}
	return proposedName;
	}
	
	public boolean exists(String type, String name, IMethod method) throws JavaModelException
		{
		Converter converter = new Converter ();
		String []paramsNames = method.getParameterNames();
		String []paramsTypes = method.getParameterTypes();
		System.out.println("Target type: " + type + "    Target name: " + name);
		int i;
		if (paramsNames.length == 0 || paramsTypes.length == 0 || paramsNames.length != paramsTypes.length)
			return false;
		for (i=0;i<paramsTypes.length;i++)
			{
			System.out.println("Type: " + converter.getCustomType(paramsTypes[i]) + "   Name: " + paramsNames[i]);
			if (paramsNames[i].equals(name) && converter.getCustomType(paramsTypes[i]).equals(type))
				return true;
			}
		return false;
		}
	
	public String elementToHandle(String project, IJavaElement element)
		{
		String handler=element.getHandleIdentifier();
		if (project!=null && !(element instanceof IJavaProject))
			{
			IJavaProject javaProject=element.getJavaProject();
			if (javaProject!=null && project.equals(javaProject.getElementName()))
				{
				String id=javaProject.getHandleIdentifier();
				handler=handler.substring(id.length());
				}
			}
		return handler;
		}
	
	public IMethod getIMethodFromType (IType type, String methodName) throws JavaModelException
		{
		IMethod[] methods=type.getMethods();
		System.out.println("Looking for method " + methodName + " in type " + type.getElementName());
		int j;
		int length=methods.length;
		for (j=length-1;j>=0;j--)	//must be parsed in reverse order
			{
			//System.out.println("Listing method: " + methods[j].getElementName());
			if (methods[j].getElementName().equals(methodName))
				{
				System.out.println("Found it!");
				return methods[j];
				}
			}
		return null;
		}
	
	public IMethod getIMethodFromICompilationUnit (ICompilationUnit cUnit, String methodName) throws JavaModelException
	{
    IType[] types=cUnit.getAllTypes();
	int i;
	for (i=0;i<types.length;i++)
		{
		IMethod[] methods=types[i].getMethods();
		int j;
		int length=methods.length;
		for (j=length-1;j>=0;j--)	//must be parsed in reverse order
			{
			if (methods[j].getElementName().equals(methodName))
				{
				return methods[j];
				}
			}
		}
	return null;
	}
	
	public ArrayList<IMethod> getMethodsFromType(IType type) throws JavaModelException
	{
	ArrayList<IMethod> methods=new ArrayList<IMethod>();
	int j;
	IMethod[] meth=type.getMethods();
	int length=meth.length;
	for (j=0;j<length;j++)
			methods.add(meth[j]);
	return methods;
	}
	
	public boolean sameClass (IType type1,IType type2)
		{
		if (type1.getElementName().equals(type2.getElementName()))
			return true;
		return false;
		}
		
	public void openFileInEditor(ICompilationUnit cUnit,int lineNumber)
		{
		 try
			 {
			 String path=null;
			 File fileToOpen=null;
			 IFile ifile=null;
			 Map<String,Integer> map=new HashMap<String,Integer>();
			 IWorkbenchPage page;
			 IMarker marker;
			 IResource resource = cUnit.getUnderlyingResource();
			 if (resource.getType()==IResource.FILE) 
				 {
				 ifile = (IFile) resource;
				 path = ifile.getRawLocation().toString();
				 }
			 if (path!=null)
				 fileToOpen=new File(path);
			 if (fileToOpen.exists() && fileToOpen.isFile()) 
				{
			     page=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();		     
			     map.put(IMarker.LINE_NUMBER,new Integer(lineNumber));
			     marker=ifile.createMarker(IMarker.LINE_NUMBER);
			     marker.setAttributes(map);
			     IDE.openEditor(page,marker);
			     marker.delete();
				} 
			 else 
				throw new FileNotFoundException();
			   }
		 catch(Exception ex)
			   {
			   }		
		}
	
	public List<Pair<String,String>> removeDuplicatedParameters (List<Pair<String,String>> parameters)
		{
		List<Pair<String,String>> newParameters = new ArrayList<Pair<String,String>>();
		int i;
		for (i=0;i<parameters.size();i++)
			{
			if (!newParameters.contains(parameters.get(i)))
				{
				newParameters.add(parameters.get(i));
				System.out.println("Added parameter" + parameters.get(i).getValue());
				}
			}
		return newParameters;
		}
	
	public String toCamelCase (String s)
	{
		return s.substring(0,1).toLowerCase()+s.substring(1);
	}
	
	}
