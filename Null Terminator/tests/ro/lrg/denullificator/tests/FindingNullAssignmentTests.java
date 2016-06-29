package ro.lrg.denullificator.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.PartInitException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import main.ViewContentProvider;
import models.CheckedElementModel;
import ro.lrg.denullificator.tests.utils.TestUtil;

public class FindingNullAssignmentTests {
	private static List<String> expectedElementsNames = new ArrayList<String>();
	private static List<Integer> expectedElementsLines = new ArrayList<Integer>();
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		expectedElementsNames.clear();
		expectedElementsLines.clear();
		
	}
		
	@Test
	public void testOne() throws JavaModelException {
		TestUtil.importProject("Test", "Test.zip");
		ViewContentProvider contentProvider = new ViewContentProvider();
		contentProvider.setJavaProject(TestUtil.getProject("Test"));
		contentProvider.showView();
		List<CheckedElementModel> actualElements = contentProvider.getPile().getNullCheckedElements();
		Assert.assertTrue("Expected:" + "" + " Actual:" + "", true);
		expectedElementsNames.add("t5");
		expectedElementsLines.add(68);
		expectedElementsNames.add("t1");
		expectedElementsLines.add(71);
		expectedElementsNames.add("t8");
		expectedElementsLines.add(75);
		expectedElementsNames.add("t10");
		expectedElementsLines.add(78);
		
		Assert.assertEquals(actualElements.size(), expectedElementsNames.size());
		int i;
		for (i=0;i<actualElements.size();i++)
		{
			Assert.assertEquals(expectedElementsNames.get(i),actualElements.get(i).getElementName());
			Assert.assertEquals((int)expectedElementsLines.get(i),actualElements.get(i).getConditionLine());
		}
		TestUtil.deleteProject("Test");
	}
	
	@Test
	public void testFour() throws JavaModelException
	{
		TestUtil.importProject("Test4", "Test4.zip");
		ViewContentProvider contentProvider = new ViewContentProvider();
		contentProvider.setJavaProject(TestUtil.getProject("Test4"));
		contentProvider.showView();
		List<CheckedElementModel> actualElements = contentProvider.getPile().getNullCheckedElements();
		Assert.assertTrue("Expected:" + "" + " Actual:" + "", true);
		expectedElementsNames.add("target");
		expectedElementsLines.add(18);
		expectedElementsNames.add("target");
		expectedElementsLines.add(9);
		
		Assert.assertEquals(actualElements.size(), expectedElementsNames.size());
		int i;
		for (i=0;i<actualElements.size();i++)
		{
			Assert.assertEquals(expectedElementsNames.get(i),actualElements.get(i).getElementName());
			Assert.assertEquals((int)expectedElementsLines.get(i),actualElements.get(i).getConditionLine());
		}
		TestUtil.deleteProject("Test4");	
	}
	
}
