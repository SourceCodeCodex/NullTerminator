package handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gui.NullTerminatorView;

public class LoadSelectedProjectHandler extends AbstractHandler 
{
	  @Override
	  public Object execute(ExecutionEvent event) throws ExecutionException 
	  {
		NullTerminatorView.getInstance().loadCurrentSelectedProject();
		return null;
	  }
}
