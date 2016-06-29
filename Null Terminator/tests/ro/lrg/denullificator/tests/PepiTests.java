package ro.lrg.denullificator.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.Test;

import main.ViewContentProvider;
import models.CheckedElementModel;
import models.NullAssignmentModel;
import models.VarModel;
import refactorings.AssignmentFinder;
import ro.lrg.denullificator.tests.utils.TestUtil;

public class PepiTests {

	private List<CheckedElementModel> actualChecks;
	private List<NullAssignmentModel> actualAssignments;
	
	private void loadProject(String name) throws JavaModelException {
		TestUtil.importProject(name, name + ".zip");
		ViewContentProvider contentProvider = new ViewContentProvider();
		contentProvider.setJavaProject(TestUtil.getProject(name));
		contentProvider.showView();
		actualChecks = contentProvider.getPile().getNullCheckedElements();
	}

	private void loadAssignments(VarModel model) throws JavaModelException {
		AssignmentFinder af = new AssignmentFinder();
		af.mainFinder(model.getCheckedVariableBinding());
		actualAssignments = af.getNullAssignmentModel();	
	}
	
	private void compareChecks( List<String> expectedElementsNames, List<Integer> expectedElementsLines) {
		Assert.assertEquals("Incorect check number.",actualChecks.size(), expectedElementsNames.size());
		for (int i = 0; i < actualChecks.size(); i++)
		{
			Assert.assertEquals(expectedElementsNames.get(i),actualChecks.get(i).getElementName());
			Assert.assertEquals((int)expectedElementsLines.get(i),actualChecks.get(i).getConditionLine());
		}
	}

	private void compareAssignmnets( List<String> expectedElementsNames, List<Integer> expectedElementsLines) {
		next:for (int i = 0; i < actualAssignments.size(); i++)
		{
			for(int j = 0; j < expectedElementsNames.size(); j++) {
				if(expectedElementsNames.get(j).equals(actualAssignments.get(i).getElementName()) &&
						(int)expectedElementsLines.get(j) == actualAssignments.get(i).getLineNumber()) {
					expectedElementsNames.remove(j);
					expectedElementsLines.remove(j);
					continue next;
				}
			}
			Assert.fail("Unexpected assignemnt (" + actualAssignments.get(i).getElementName() + "," + actualAssignments.get(i).getLineNumber() + ")");
		}
		if(expectedElementsNames.size() != 0) {
			String msg = "";
			for(int j = 0; j < expectedElementsNames.size(); j++) {
				msg += "Undetected assignemnt (" + expectedElementsNames.get(j) + "," + expectedElementsLines.get(j) + ")\n";
			}
			Assert.fail(msg);			
		}
	}

	@Test
	public void test1() throws JavaModelException {
		loadProject("pepi1");
		
		//Verify the null checks
		List<String> expectedElementsNames = new ArrayList<String>();
		List<Integer> expectedElementsLines = new ArrayList<Integer>();
		expectedElementsNames.add("parent");
		expectedElementsLines.add(18);
		compareChecks(expectedElementsNames, expectedElementsLines);

		loadAssignments(((VarModel) actualChecks.get(0)));

		//Verify null assignments
		expectedElementsNames.clear();
		expectedElementsLines.clear();
		expectedElementsNames.add("parent");
		expectedElementsLines.add(15);
		expectedElementsNames.add("parent");
		expectedElementsLines.add(8);
		expectedElementsNames.add("parent");
		expectedElementsLines.add(34);
		compareAssignmnets(expectedElementsNames, expectedElementsLines);

		TestUtil.deleteProject("pepi1");
	}

	@Test
	public void test2() throws JavaModelException {
		loadProject("pepi2");
		
		//Verify the null checks
		List<String> expectedElementsNames = new ArrayList<String>();
		List<Integer> expectedElementsLines = new ArrayList<Integer>();
		expectedElementsNames.add("parent");
		expectedElementsLines.add(18);
		compareChecks(expectedElementsNames, expectedElementsLines);
		
		loadAssignments(((VarModel) actualChecks.get(0)));
		
		//Verify null assignments
		expectedElementsNames.clear();
		expectedElementsLines.clear();
		expectedElementsNames.add("parent");
		expectedElementsLines.add(8);
		expectedElementsNames.add("parent");
		expectedElementsLines.add(15);
		expectedElementsNames.add("parent");
		expectedElementsLines.add(30);
		compareAssignmnets(expectedElementsNames, expectedElementsLines);

		TestUtil.deleteProject("pepi2");
	}

}
