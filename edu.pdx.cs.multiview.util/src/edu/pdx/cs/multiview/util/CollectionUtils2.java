package edu.pdx.cs.multiview.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collection utilities that I could not find in Jarkarta Commons
 * 
 * @author emerson
 *
 */
public class CollectionUtils2 {


	
	public static <A> List<A> concat(List<A> xs, List<A> ys){
		List<A> zs = new ArrayList<A>(xs.size()+ys.size());
		for(A x : xs)
			zs.add(x);
		for(A y : ys)
			zs.add(y);
		return zs;
	}


	public static <E> E anyElement(Collection<E> es) {
		
		for(E e : es)
			return e;
		
		return null;
	}
	
	public static <E> List<E> flatten(List<E> ... ess) {
		int size = 0;
		for(List<E> es : ess)
			size += es.size();
		
		List<E> results = new ArrayList<E>(size);
		
		for(List<E> es : ess)
			results.addAll(es);
		
		return results;
	}


	public static <E> List<E> flatten(E e, List<E> ... ess) {
		List<E> flattened = flatten(ess);
		flattened.add(e);
		return flattened;
	}
	
	public static <E> List<E> flatten(Collection<List<E>> items) {		
		List<E> answer = new ArrayList<E>();
		for(List<E> es : items)
			answer.addAll(es);
		return answer;
	}
}
