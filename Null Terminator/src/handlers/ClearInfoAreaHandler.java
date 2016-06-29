package handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gui.NullTerminatorView;

public class ClearInfoAreaHandler extends AbstractHandler
{
	  @Override
	  public Object execute(ExecutionEvent event) throws ExecutionException 
	  {
		  NullTerminatorView.getInstance().getInfoArea().clear();
		  return null;
	  }
}
