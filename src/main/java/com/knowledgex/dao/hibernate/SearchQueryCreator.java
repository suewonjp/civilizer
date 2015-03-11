package com.knowledgex.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Pair;
import com.knowledgex.domain.SearchParams;
import com.knowledgex.domain.SearchParams.Keyword;

public final class SearchQueryCreator {
	
    public static String getPatternFromKeyword(SearchParams.Keyword keyword) {
        String word = keyword.getWord().replace("'", "''");

        if (keyword.isTrivial()) {
            final Pair<String, Character> tmp =
                SearchParams.Keyword.escapeSqlWildcardCharacters(word);
            word = "%" + tmp.getFirst() + "%";
        }

        return word;
    }
    
    private static Junction buildQueryWithKeywords(List<Keyword> words, int target, boolean any) {
    	final String[] targetColumns = {
    	        null, "TAG_NAME", "TITLE", "CONTENT", "CONTENT"
    	};
    	
    	final String column = targetColumns[target];
    	if (column == null) {
    		throw new IllegalArgumentException();
    	}
    	
    	final Junction junction = any ?
    			Restrictions.disjunction() : Restrictions.conjunction();
    			
		for (SearchParams.Keyword w : words) {
			final String pattern = getPatternFromKeyword(w);
			String sql = null;
			
			if (w.isTrivial()) {
                if (w.isCaseSensitive()) {
                    sql = column + " like " + "'" + pattern + "'";
                }
                else {
                    sql = "lower(" + column + ") like " + "'" + pattern.toLowerCase() + "'";
                }
            }
			else {
				if (w.isRegex()) {
					sql = column + " regexp " + "'" + pattern + "'";
				}
				else if (w.isWholeWord()) {
			        if (w.isCaseSensitive()) {
			            sql = column + " regexp " + "'\\b" + pattern + "\\b'";
			        }
			        else {
			            sql = "lower(" + column + ") regexp " + "'\\b" + pattern.toLowerCase() + "\\b'";
			        }
			    }
				else if (w.isBeginningWith()) {
					if (w.isCaseSensitive()) {
						sql = column + " regexp " + "'\\b" + pattern + "'";
					}
					else {
						sql = "lower(" + column + ") regexp " + "'\\b" + pattern.toLowerCase() + "'";
					}
				}
				else if (w.isEndingWith()) {
					if (w.isCaseSensitive()) {
						sql = column + " regexp " + "'" + pattern + "\\b'";
					}
					else {
						sql = "lower(" + column + ") regexp " + "'" + pattern.toLowerCase() + "\\b'";
					}
				}
			    else {
			        throw new UnsupportedOperationException();
			    }
			}
			
			if (w.isInverse()) {
				sql = "not " + sql;
			}
			
			// [TODO] research compatibility issues with other DBMS vendors
			junction.add(Restrictions.sqlRestriction(sql));
		}
		
		return junction;
    }
    
    public static Criteria buildQuery(SearchParams params, Session session) {
    	final Criteria output = session.createCriteria(Fragment.class);
    	Criteria tagCrit = output.createCriteria("tags");
    	output.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    	
    	Junction rootJunction = Restrictions.conjunction();
    	
    	for (SearchParams.Keywords keywords : params.getKeywords()) {
    		final int target = keywords.getTarget();
    		final List<Keyword> words = keywords.getWords();
    		final boolean any = keywords.isAny();
    		
			if (target == SearchParams.TARGET_ALL) {
				Junction disj = Restrictions.disjunction();
				
				Junction junc = buildQueryWithKeywords(words, SearchParams.TARGET_TITLE, any);
				disj.add(junc);
				
				junc = buildQueryWithKeywords(words, SearchParams.TARGET_TEXT, any);
				disj.add(junc);
				
				rootJunction.add(disj);
			}
			else if (target == SearchParams.TARGET_TAG) {
				Junction junc = buildQueryWithKeywords(words, target, any);
				tagCrit.add(junc);
			}
			else {
				Junction junc = buildQueryWithKeywords(words, target, any);
				rootJunction.add(junc);
			}
		}
    	
    	output.add(rootJunction);
        return output;
    }

}
