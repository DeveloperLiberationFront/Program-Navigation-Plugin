package edu.pdx.cs.multiview.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * use Jakarta Commons w/ Generics, if possible
 * @link http://sourceforge.net/projects/collections
 * 
 * @author pq, emerson
 */
public class CollectionUtils {

	/**
	 * @return all the elements of first without any of those in second
	 */
	public static <E> Collection<E> less(Collection<E> first, Collection<E> second) {
		Collection<E> accum = new ArrayList<E>(first);
		for (E e : second) {
			accum.remove(e);
		}
		return accum;
	}
	
	
	public static <E> Collection<E> select(Collection<E> c, IElementSelector<E> selector) {
		Collection<E> accum = new ArrayList<E>();
		for (E e : c) {
			if (selector.selects(e))
				accum.add(e);
		}
		return accum;
	}
	
	
	public static interface IElementSelector<E> {
		boolean selects(E e);
	}

	public static <A> List<A> reverse(List<A> list) {
		List<A> reversedList = new ArrayList<A>(list.size());
		for(int i = 0; i<list.size(); i++)
			reversedList.add(i, list.get(list.size()-i-1));
		return reversedList;
	}
	
	
}
