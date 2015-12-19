package edu.pdx.cs.multiview.jface.annotation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;

public class AnnTransaction {

	private Map<ISelfDrawingAnnotation,Position> toAdd = 
		new HashMap<ISelfDrawingAnnotation, Position>();
	private List<ISelfDrawingAnnotation> toRemove = 
		new LinkedList<ISelfDrawingAnnotation>();
	
	public void add(ISelfDrawingAnnotation a,Position p){
		toAdd.put(a, p);		
	}
	
	public void remove(ISelfDrawingAnnotation a){
		toRemove.add(a);
	}
	
	public void replaceAnnotations(AnnotationPainter painter){
		ISelfDrawingAnnotation[] toRemoveArray = 
			new ISelfDrawingAnnotation [toRemove.size()];
		
		for(int i = 0; i < toRemoveArray.length; i++)
			toRemoveArray[i] = toRemove.get(i);
		
		painter.replaceAnnotations(toRemoveArray, toAdd);
	}
}
