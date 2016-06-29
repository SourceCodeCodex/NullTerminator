package gui;

import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import handlers.ShowActiveProjectHandler;
import listeners.CheckedElementTableListener;
import listeners.NullAssignmentsTableListener;
import main.Pile;
import main.ViewContentProvider;
import utils.Utils;

@SuppressWarnings("restriction")
public class NullObjectRefactoringView extends ViewPart 
{
	public static final String ID = "main.NullObjectRefactoringView";
	private static NullObjectRefactoringView instance;
	private ViewContentProvider contentProvider=new ViewContentProvider();
	private AbstractTableGui checkedElementTableViewer;
	private AbstractTableGui nullVariablesTableViewer;
	private InfoArea infoArea;
	
	private Composite parent;
	private IJavaProject javaProject;
	private String currentLoadedJavaProject = Utils.UNDEFINED_PROJECT;
	private ISelectionListener listener = new ISelectionListener() 
		{
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) 
			{
			if (sourcepart != NullObjectRefactoringView.this) 
				{
				final IProject project=getProject(sourcepart, selection);
			    if (project!=null)	//change view only when the user clicks on a project
			    	javaProject=JavaCore.create(project);
				}
			}
		};

	public static NullObjectRefactoringView getInstance()
		{
		return instance;
		}
		
	//returns the project that is clicked
	private IProject getProject(IWorkbenchPart sourcepart, ISelection selection) 
		{
		IProject project=null;
		if(selection instanceof IStructuredSelection)
			{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IResource) 
				{
				project=  ((IResource)element).getProject();
				} 
			else 
				if (element instanceof PackageFragmentRootContainer) 
					{
					IJavaProject jProject = ((PackageFragmentRootContainer)element).getJavaProject();
					project =  jProject.getProject();
					} 
				else if (element instanceof IJavaElement) 
						{
						IJavaProject jProject= ((IJavaElement)element).getJavaProject();
						project = jProject.getProject();
						}
					else
						return null;
			}
		return project;
		}
	
	public void createPartControl(Composite parent) 
		{		
		this.parent=parent;
		instance=this;
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridLayout.numColumns = 4;
		parent.setLayoutData(gridData);
		parent.setLayout(gridLayout);
		parent.setBackground(new Color(Display.getCurrent(),200,245,245));
		
		
		checkedElementTableViewer = new CheckedElementTableGui(parent,new CheckedElementTableListener(parent), Utils.NULL_CHECKED_ELEMENTS_TABLE);
		nullVariablesTableViewer = new NullAssignmentsTableGui(parent,new NullAssignmentsTableListener(), Utils.NULL_ASSIGNMENTS_FOUND);
		infoArea = new InfoArea(parent);
		
		
		
		checkedElementTableViewer.setContentProvider(new ArrayContentProvider());
		nullVariablesTableViewer.setContentProvider(new ArrayContentProvider());
		
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
		}
	
	public void loadCurrentSelectedProject()
	{
		loadProject(javaProject);
	}
	
	public void loadProject(IJavaProject javaProject)
		{
		if (javaProject == null)
		{
			infoArea.append("No selected project");
			return;
		}
		if (javaProject.getElementName().equals(currentLoadedJavaProject))
		{
			infoArea.append("Project already loaded");
			return;
		}
		currentLoadedJavaProject=javaProject.getElementName();
		contentProvider.getPile().clear();
	    contentProvider.setJavaProject(javaProject);
	    try
			{
			checkedElementTableViewer.setEnabled();
			contentProvider.showView();
			checkedElementTableViewer.setInput(contentProvider.getPile().getNullCheckedElements());
			nullVariablesTableViewer.clearAll();
			nullVariablesTableViewer.setEnabled();
			ShowActiveProjectHandler.getInstance().setProject(currentLoadedJavaProject);
			infoArea.append("There are " + contentProvider.getPile().getNullCheckedElements().size() + 
					" null checked elements in " + currentLoadedJavaProject + " project");
			} 
	    catch (JavaModelException e)
			{
			e.printStackTrace();
			}
		}
	
	public void loadCurrentActiveProject() 
	{
		IEditorPart editorPart = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
		if(editorPart != null)
		{
		    IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput() ;
		    IFile file = input.getFile();
		    IProject activeProject = file.getProject();
		    loadProject(JavaCore.create(activeProject));
		}
		else
		{
			infoArea.append("No project opened in editor");
		}
	}
	
	public void unloadItems()
		{
		if (currentLoadedJavaProject.equals(Utils.UNDEFINED_PROJECT))
		{
			infoArea.append("No project loaded");
			return;
		}
		contentProvider.getPile().clear();
		checkedElementTableViewer.clearAll();
		nullVariablesTableViewer.clearAll();
		currentLoadedJavaProject = Utils.UNDEFINED_PROJECT;
		ShowActiveProjectHandler.getInstance().setProject(Utils.NONE);
		infoArea.append("Project unloaded");
		}
	
	public void showStatistics()
	{
		if (currentLoadedJavaProject.equals(Utils.UNDEFINED_PROJECT))
		{
			infoArea.append("No project loaded");
			return;
		}
		infoArea.append("Statistics to be added");
	}
	
	public String getCurrentLoadedJavaProject()
		{
		return currentLoadedJavaProject;
		}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
		{
		
		}

	public AbstractTableGui getMetTableViewer()
		{
		return checkedElementTableViewer;
		}
	
	public Composite getParent()
		{
		return parent;
		}

	public IJavaProject getJavaProject()
		{
		return javaProject;
		}
	
	public AbstractTableGui getNullVariablesTableViewer() {
		return nullVariablesTableViewer;
	}

	public ViewContentProvider getContentProvider() {
		return contentProvider;
	}

	public InfoArea getInfoArea() {
		return infoArea;
	}
} 