package listeners;

import gui.NullTerminatorView;

import java.util.List;

import models.CheckedElementModel;
import models.MetModel;
import models.NullAssignmentModel;
import models.VarModel;
import refactorings.AssignmentFinder;
import refactorings.RefactoringHelper;
import refactorings.RefactoringsRunner;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import utils.Utils;

public class CheckedElementTableListener extends TableListener
	{
	private Composite parent;
	
	public CheckedElementTableListener (Composite parent) 
	{
		this.parent = parent;
	}
	
	@Override
	public void mouseDown(MouseEvent e)
		{
		if (e.button != 3)
			return;
		Menu popupMenu=new Menu(parent);
		//MenuItem viewEditorItem=new MenuItem(popupMenu, SWT.NONE);
		//viewEditorItem.setText(Utils.VIEW_IN_EDITOR);
	    MenuItem refactorItem=new MenuItem(popupMenu, SWT.NONE);
	    refactorItem.setText(Utils.REFACTOR);
	    MenuItem assignmentItem = new MenuItem(popupMenu, SWT.NONE);
	    assignmentItem.setText(Utils.FIND_ASSIGNMENT);
	    
	    popupMenu.setVisible(true);
	    
	    refactorItem.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event)
				{
				NullTerminatorView.getInstance().getInfoArea().append(Utils.REFACTORING_START);
				String message = new RefactoringsRunner().run((CheckedElementModel)data.get(table.getSelectionIndex()));
				NullTerminatorView.getInstance().getInfoArea().append(Utils.REFACTORING_STOP);
				if (!message.equals(""))
					NullTerminatorView.getInstance().getInfoArea().append(message);
				}
	    });
	    
	    assignmentItem.addListener(SWT.Selection, new Listener(){
	  		@Override
	  		public void handleEvent(Event event)
	  			{
	  				CheckedElementModel model = (CheckedElementModel) data.get(table.getSelectionIndex());
						try {
							List<NullAssignmentModel> nullAssignments;
							NullTerminatorView.getInstance().getInfoArea().append(Utils.NULL_ASSIGNMENT_START);
							if (model instanceof VarModel)
								nullAssignments = new AssignmentFinder().mainFinder(((VarModel)model).getCheckedVariableBinding());
							else nullAssignments = new AssignmentFinder().mainFinder(((MetModel)model).getInvocation());
							
							NullTerminatorView.getInstance().getNullVariablesTableViewer().setInput(nullAssignments);
							NullTerminatorView.getInstance().getNullVariablesTableViewer().setEnabled();
							NullTerminatorView.getInstance().getInfoArea().append(Utils.NULL_ASSIGNMENT_STOP);
							if (Utils.foundImpossibleMethod)
								NullTerminatorView.getInstance().getInfoArea().append("The elements marked with (*) couldn't be inspected further"
										+ " because the method's body is not accessible. This may be because is an access to a collection. In this"
										+ " case, please check where 'null' is added to the collection andd replace it with an instance of the"
										+ " Null" + model.getCheckedElementType().getElementName() + ". If the method is in an external .jar "
										+ " please proceed to undo the refactoring.");
							Utils.foundImpossibleMethod = false;
						} 
						catch (JavaModelException e) 
						{
							e.printStackTrace();
						}
	  			}
	      });
		}
	

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (e.button != 1)
			return;
		if (table.getSelectionIndex() == -1)
			return;
		CheckedElementModel model = (CheckedElementModel) data.get(table.getSelectionIndex());
		new RefactoringHelper().openFileInEditor(model.getiCompilationUnit(),model.getConditionLine());    		

	}
	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	}
