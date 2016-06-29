package refactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class MethodsCollector extends SearchRequestor
{

	private List<IMethod> methods = new ArrayList<IMethod>();

	public List<IMethod> getMethods() 
	{
		return methods;
	}

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException 
	{
		Object element = match.getElement();
		if (element instanceof IMethod) 
			methods.add((IMethod)element);		
	}

}
