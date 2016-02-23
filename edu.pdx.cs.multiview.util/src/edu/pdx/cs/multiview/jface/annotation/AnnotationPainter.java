package edu.pdx.cs.multiview.jface.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

/**
 * I handle the job of {@link org.eclipse.jface.text.source.AnnotationPainter} and
 * {@link AnnotationModel}.  I am used like so:
 * 
 * <pre>
 * ISelfDrawingAnnotation ann = ... // your annotation
 * Position p = ... // the position that annotation goes at
 * 
 * AbstractDecoratedSourceEditor editor = ... //the editor you want to annotate
 * 
 * AnnotationPainter painter = new AnnotationPainter(editor);
 * painter.addAnnotation(ann,p); 
 * </pre>
 * 
 * 
 * @author emerson
 */
public class AnnotationPainter implements IPainter, PaintListener, ISelectionProvider {
	
	/*
	 * remove this field and detangle
	 */
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
	private SourceViewer viewer;
	private Map<ISelfDrawingAnnotation,Position> anns = new HashMap<ISelfDrawingAnnotation,Position>();
	
	/**
	 * @param v		the {@link SourceViewer} that you want to 
	 */
	public AnnotationPainter(SourceViewer v){
		viewer = v;
		viewer.getTextWidget().addPaintListener(this);
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		listeners.add(listener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		listeners.remove(listener);
	}
	
	private void fireSelectionEvent(){
		SelectionChangedEvent e = new SelectionChangedEvent(this,getSelection());
		for(ISelectionChangedListener l : listeners)
			l.selectionChanged(e);
	}
	
	/**
	 * @param annotation
	 * 
	 * @return	the position of the given parameter, or null if none
	 */
	public Position getPosition(ISelfDrawingAnnotation annotation) {
		return anns.get(annotation);
	}

	/**
	 * @see IAnnotationModelExtension#replaceAnnotations(org.eclipse.jface.text.source.Annotation[], Map)
	 * 
	 * @deprecated use replaceAnnotations(AnnTransaction) instead
	 */
	public void replaceAnnotations(ISelfDrawingAnnotation[] remove, Map<ISelfDrawingAnnotation, Position> add) {
		
		List<Position> positions = new ArrayList<Position>();
		
		if(remove!=null)
			for(ISelfDrawingAnnotation r : remove){
				Position p = anns.remove(r);
				if(p!=null)
					positions.add(p);
			}
		
		if(add!=null){
			anns.putAll(add);
			positions.addAll(add.values());
		}
			
		fireAnnotationChangedEvent(positions);
	}
	
	public void replaceAnnotations(AnnTransaction trans){
		trans.replaceAnnotations(this);
	}

	/**
	 * @see AnnotationModel#addAnnotation(org.eclipse.jface.text.source.Annotation, Position)
	 */
	public void addAnnotation(ISelfDrawingAnnotation a, Position p){
		
		anns.put(a,p);
		System.out.println(a);
		System.out.println(p);
		List<Position> positions = new ArrayList<Position>(1);
		positions.add(p);
		fireAnnotationChangedEvent(positions);
	}
	

	/**
	 * @see AnnotationModel#removeAnnotation(org.eclipse.jface.text.source.Annotation)
	 */
	public void removeAnnotation(ISelfDrawingAnnotation ann) {
		
		Position position = anns.remove(ann);
		
		if(position!=null){
			List<Position> positions = new ArrayList<Position>(1);
			positions.add(position);
			fireAnnotationChangedEvent(positions);
		}
	}
	
	public void removeAllAnnotations(){
		Collection<Position> positions = new ArrayList<Position>(anns.values());
		anns.clear();
		fireAnnotationChangedEvent(positions);
	}

	private void fireAnnotationChangedEvent(Collection<Position> positions) {
		
		if(positions.isEmpty())
			return;
		
		int tempEnd, start = Integer.MAX_VALUE, end = Integer.MIN_VALUE;
		
		for(Position p : positions){
			if(start>p.getOffset())
				start = p.offset;
			tempEnd = p.getOffset()+p.getLength();
			if(end<tempEnd)
				end = tempEnd;
		}
		
		start = widgetIndex(start);
		end = widgetIndex(end);
		viewer.getTextWidget().redrawRange(start, end-start, false);
	}
	
	public void deactivate(boolean redraw) {}
	public void setPositionManager(IPaintPositionManager manager) {}

	public void paint(int reason) {
		
		switch(reason){
			case IPainter.KEY_STROKE:
			case IPainter.MOUSE_BUTTON:
			case IPainter.SELECTION:
			case IPainter.TEXT_CHANGE:
				fireSelectionEvent();
		}
	}
	
	public void dispose() {
	
		refresh();
		
		anns.clear();
		paint(IPainter.INTERNAL);
		
		viewer.getTextWidget().removePaintListener(this);
		viewer.removePainter(this);
	}

	public void paintControl(PaintEvent e) {
		IRegion r;
		ISelfDrawingAnnotation ann;
		Position p;
		for(Map.Entry<ISelfDrawingAnnotation,Position> entry : anns.entrySet()){
			ann = entry.getKey();
			p = entry.getValue();
			//System.out.println(p);
			r = viewer.modelRange2WidgetRange(new Region(p.offset,p.length));
			
			//draw the annotation only if it's visible
			if(r!=null)
				ann.draw(e.gc, viewer.getTextWidget(),
								r.getOffset(),
								r.getLength());
		}
	}
	
	private int widgetIndex(int offset){
		int index = viewer.modelOffset2WidgetOffset(offset);
		if(index < 0)
			index = viewer.getBottomIndexEndOffset();
		return index;
	}

	public ITextSelection getSelection() {
		return (ITextSelection)viewer.getSelection();
	}

	public void setSelection(ISelection selection) {
		viewer.setSelection(selection);
	}

	/**
	 * @return	all of my annotations
	 */
	public Iterator<ISelfDrawingAnnotation> getAnnotationIterator() {
		return anns.keySet().iterator();
	}

	private void refresh() {
		fireAnnotationChangedEvent(anns.values());
	}
	
	public void refresh(Collection<ISelfDrawingAnnotation> someAnnotations) {
		List<Position> positions = new ArrayList<Position>();
		for(ISelfDrawingAnnotation ann : someAnnotations){
			positions.add(anns.get(ann));
		}
		fireAnnotationChangedEvent(positions);
	}
}