package main;

import java.util.Comparator;
import models.CheckedElementModel;

public class NullCheckedElementsComparator implements Comparator
{

	@Override
	public int compare (Object arg0, Object arg1) 
	{
		CheckedElementModel e1 = (CheckedElementModel)arg0;
		CheckedElementModel e2 = (CheckedElementModel)arg1;
		if (e1.getElementName().compareTo(e2.getElementName()) < 0)
			return -1;
		else if (e1.getElementName().compareTo(e2.getElementName()) > 0)
			return 1;
		return 0;
	}

}
