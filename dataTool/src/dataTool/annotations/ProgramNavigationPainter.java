package dataTool.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder.OccurrenceLocation;
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

import edu.pdx.cs.multiview.jface.annotation.AnnTransaction;
import edu.pdx.cs.multiview.jface.annotation.AnnotationPainter;
import edu.pdx.cs.multiview.jface.annotation.ISelfDrawingAnnotation;

import dataTool.AnnotationManager;
import dataTool.UpFinder;

/**
 * Class to handle the annotation painting for the Program Navigation Plugin. Basically
 * the only difference between this and AnnotationPainter is the paintControl function
 * that performs the search and highlights multiple positions.
 * 
 * @author Chris
 */
public class ProgramNavigationPainter extends AnnotationPainter {
	
	/*
	 * remove this field and detangle
	 */
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
	private SourceViewer viewer;
	private Map<ISelfDrawingAnnotation,Position> anns = new HashMap<ISelfDrawingAnnotation,Position>();
	
	public ProgramNavigationPainter(SourceViewer v) {
		super(v);
		viewer = v;
		viewer.getTextWidget().addPaintListener(this);	}
	
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

	/**
	 * Controls what is painted in the display.
	 */
	public void paintControl(PaintEvent e) {
		viewer.showAnnotations(false);
		IRegion r;
		ISelfDrawingAnnotation ann;
		Position p;
		OccurrenceLocation[] locations = null;
		//DeclarationFinder finder = new DeclarationFinder();
		UpFinder finder = UpFinder.getInstance();
		for(Map.Entry<ISelfDrawingAnnotation,Position> entry : anns.entrySet()){
			ann = entry.getKey();
			p = entry.getValue();
			r = viewer.modelRange2WidgetRange(new Region(p.offset,p.length));
			//draw the annotation only if it's visible
			if(r!=null) {
				ann.draw(e.gc, viewer.getTextWidget(),
								r.getOffset(),
								r.getLength());
				//TODO Original implementation that doesn't work - will revisit this with down finder
				//finder.initialize(finder.getCompUnit(source), r.getOffset(), r.getLength());
				//locations = finder.getUpOccurrences();
				//System.out.println(locations);
				String source = viewer.getTextWidget().getText();
				String word = viewer.getTextWidget().getText(r.getOffset(), r.getOffset() + r.getLength()-1);
				if(finder.containsVar(word)) {
					for (ASTNode up: finder.getUpOccurences(word)) {
						int length;
						if(up.getParent().getNodeType() == 60) {
							length = word.length();
						}
						else {
							length = up.getLength();
						}
						ann.draw(e.gc,viewer.getTextWidget(),up.getStartPosition(),length);
					}
				}
			}
		}
		viewer.showAnnotationsOverview(true);
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
