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
	
	private static final String WORD_BOUNDARY = "[^a-z0-9_]";
    
    public static String newPattern(SearchParams.Keyword keyword) {
        final Pair<String, Character> tmp =
                SearchParams.Keyword.escapeSqlWildcardCharacters(keyword.getWord());
        String word = tmp.getFirst();
        
        if (! keyword.isAsIs()) {
            word = word.replace('?', '_').replace('*', '%');
        }
        
        if (! keyword.isWholeWord()) {
            word = "%" + word + "%";
        }
        
        return word;
    }
    
    private static void populateQueryWithKeywords(Criteria output, List<Keyword> words, int target, boolean any) {
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
			final String pattern = newPattern(w);
			
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
			else if (target == SearchParams.TARGET_URL) {
				final Disjunction disj = Restrictions.disjunction();
				String p = null;
				
				p = "%http://" + pattern + "%";
				disj.add(w.isCaseSensitive() ? 
						Restrictions.like(column, p) : Restrictions.ilike(column, p));
				p = "%https://" + pattern + "%";
				disj.add(w.isCaseSensitive() ? 
						Restrictions.like(column, p) : Restrictions.ilike(column, p));
				junction.add(disj);
			}
			else {
				junction.add(w.isCaseSensitive() ?
						Restrictions.like(column, pattern) : Restrictions.ilike(column, pattern));
			}
		}
    	
    	output.add(junction);
    }
    
    public static Criteria newQuery(SearchParams params, Session session) {
    	final Criteria output = session.createCriteria(Fragment.class);
    	Criteria tagCrit = null;
    	
    	if (params.hasTarget(SearchParams.TARGET_ALL) || params.hasTarget(SearchParams.TARGET_TAG)) {
    		tagCrit = output.createCriteria("tags");
    	}
    	
    	for (SearchParams.Keywords keywords : params.getKeywords()) {
    		final int target = keywords.getTarget();
    		final List<Keyword> words = keywords.getWords();
    		final boolean any = keywords.isAny();
    		
			if (target == SearchParams.TARGET_ALL) {
				populateQueryWithKeywords(tagCrit, words, SearchParams.TARGET_TAG, any);
				populateQueryWithKeywords(output, words, SearchParams.TARGET_TITLE, any);
				populateQueryWithKeywords(output, words, SearchParams.TARGET_TEXT, any);
			}
			else if (target == SearchParams.TARGET_TAG) {
				populateQueryWithKeywords(tagCrit, words, target, any);
			}
			else {
				populateQueryWithKeywords(output, words, target, any);
			}
		}
    	
        return output;
    }

}
