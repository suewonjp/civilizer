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
    
    private void populateQueryWithKeywords(Criteria output, List<Keyword> words, int target, boolean any) {
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
    
    public Criteria newQuery(SearchParams params, Session session) {
    	final Criteria output = session.createCriteria(Fragment.class);
    	Criteria tagCrit = null;
    	
    	for (SearchParams.Keywords keywords : params.getKeywords()) {
    		Criteria crit = output;
			if (keywords.getTarget() == SearchParams.TARGET_TAG) {
				if (tagCrit == null) {
					tagCrit = output.createCriteria("tags");
				}
				crit = tagCrit;
			}
			populateQueryWithKeywords(crit, keywords.getWords(), keywords.getTarget(), keywords.isAny());
		}
        return output;
    }

}
