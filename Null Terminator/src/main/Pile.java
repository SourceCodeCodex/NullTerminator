package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.IBinding;

import models.CheckedElementModel;


public class Pile
	{
	private List<CheckedElementModel> nullCheckedElements=new ArrayList<CheckedElementModel>();
	
	public void addElement(CheckedElementModel newModel)
		{
		if (nullCheckedElements.contains(newModel))
			System.out.println("Already added");
		else
			nullCheckedElements.add(newModel);
		}
	
	public void clear()
		{
		nullCheckedElements.clear();
		}

	public List<CheckedElementModel> getNullCheckedElements()
	{
		return nullCheckedElements;
	}

	public void removeAllOccurences(IBinding element) 
	{
		int i=0;
		for (i=0;i<nullCheckedElements.size();i++)
			if (nullCheckedElements.get(i).myEquals(element))
				nullCheckedElements.remove(i);
	}
	
	public void sort (Comparator<CheckedElementModel> comparator)
	{
		nullCheckedElements.sort(comparator);
	}
	}
