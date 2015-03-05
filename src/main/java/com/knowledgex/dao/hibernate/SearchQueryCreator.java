package com.knowledgex.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;

import com.knowledgex.domain.Pair;
import com.knowledgex.domain.SearchParams;

public final class SearchQueryCreator {
    
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
    
    private Criterion newQueryExpression(SearchParams.Keyword keyword) {
        return null;
    }
    
    private Criterion newQueryExpression(SearchParams.Keywords keywords) {
        return null;
    }
    
    public Criteria newQuery(SearchParams params) {
        return null;
    }

}
