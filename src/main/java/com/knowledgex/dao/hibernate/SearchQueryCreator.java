package com.knowledgex.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Pair;
import com.knowledgex.domain.SearchParams;
import com.knowledgex.domain.SearchParams.Keyword;

public final class SearchQueryCreator {
	
	public static final String WORD_BOUNDARY = "[^a-zA-Z0-9_]";
    
    public static String getPatternFromKeyword(SearchParams.Keyword keyword) {
        String word = keyword.getWord();
        
        if (! keyword.isCaseSensitive() && ! keyword.isRegex()) {
            word = word.toLowerCase();
        }

        if (keyword.isTrivial()) {
            final Pair<String, Character> tmp =
                SearchParams.Keyword.escapeSqlWildcardCharacters(word);
            word = "%" + tmp.getFirst() + "%";
        }

        return word;
    }
    
    
    private static Junction populateQueryWithKeywords(List<Keyword> words, int target, boolean any) {
    	final String[] targetColumns = {
    			null, "tagName", "title", "content", "content"
    	};
    	
    	final String column = targetColumns[target];
    	if (column == null) {
    		throw new IllegalArgumentException();
    	}
    	
    	final Junction junction = any ?
    			Restrictions.disjunction() : Restrictions.conjunction();
    			
		for (SearchParams.Keyword w : words) {
			final String pattern = getPatternFromKeyword(w);
			
			if (w.isTrivial()) {
                junction.add(w.isCaseSensitive() ?
                        Restrictions.like(column, pattern) : Restrictions.ilike(column, pattern));
            }
			else {
			    if (w.isWholeWord()) {
			        final Disjunction disj = Restrictions.disjunction();
	                String p = null;
	                
	                p = "%" + WORD_BOUNDARY + pattern + WORD_BOUNDARY + "%";
	                disj.add(w.isCaseSensitive() ? 
	                        Restrictions.like(column, p) : Restrictions.ilike(column, p));
	                p = pattern + WORD_BOUNDARY + "%";
	                disj.add(w.isCaseSensitive() ? 
	                        Restrictions.like(column, p) : Restrictions.ilike(column, p));
	                p = "%" + WORD_BOUNDARY + pattern;
	                disj.add(w.isCaseSensitive() ? 
	                        Restrictions.like(column, p) : Restrictions.ilike(column, p));
	                p =  pattern;
	                disj.add(w.isCaseSensitive() ? 
	                        Restrictions.like(column, p) : Restrictions.ilike(column, p));
	                junction.add(disj);
			    }
			}
		}
		
		return junction;
    }
    
    public static Criteria newQuery(SearchParams params, Session session) {
    	final Criteria output = session.createCriteria(Fragment.class);
    	Criteria tagCrit = null;
    	
    	if (params.hasTarget(SearchParams.TARGET_TAG)) {
    		tagCrit = output.createCriteria("tags");
    	}
    	
    	Junction rootJunction = Restrictions.conjunction();
    	
    	for (SearchParams.Keywords keywords : params.getKeywords()) {
    		final int target = keywords.getTarget();
    		final List<Keyword> words = keywords.getWords();
    		final boolean any = keywords.isAny();
    		
			if (target == SearchParams.TARGET_ALL) {
				Junction disj = Restrictions.disjunction();
				
				Junction junc = populateQueryWithKeywords(words, SearchParams.TARGET_TITLE, any);
				disj.add(junc);
				
				junc = populateQueryWithKeywords(words, SearchParams.TARGET_TEXT, any);
				disj.add(junc);
				
				rootJunction.add(disj);
			}
			else if (target == SearchParams.TARGET_TAG) {
				Junction junc = populateQueryWithKeywords(words, target, any);
				tagCrit.add(junc);
			}
			else {
				Junction junc = populateQueryWithKeywords(words, target, any);
				rootJunction.add(junc);
			}
		}
    	
    	output.add(rootJunction);
        return output;
    }

}
