package listeners;

import org.eclipse.swt.events.MouseEvent;

import models.NullAssignmentModel;
import refactorings.RefactoringHelper;

public class NullAssignmentsTableListener extends TableListener
{

	@Override
	public void mouseDown(MouseEvent e)
	{
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (e.button != 1)
			return;
		if (table.getSelectionIndex() == -1)
			return;
		NullAssignmentModel model = (NullAssignmentModel) data.get(table.getSelectionIndex());
		table.setSelection(-1);
		new RefactoringHelper().openFileInEditor(model.getCompilationUnit(), model.getLineNumber());
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
