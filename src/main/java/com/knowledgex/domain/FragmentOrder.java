package com.knowledgex.domain;

import java.util.*;

import org.joda.time.DateTimeComparator;

public enum FragmentOrder {
    UPDATE_DATETIME
  , CREATION_DATETIME
  , TITLE
  , ID
  , COUNT
  ;    

    private static abstract class TransientComparator implements Comparator<Fragment> {}

    private static class TransientReverseComparator extends TransientComparator {
        final TransientComparator src;
        
        TransientReverseComparator(TransientComparator src) {
            this.src = src;
        }
        
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            return -src.compare(arg0, arg1);
        }
    }

    private static final class ComparatorForUpdateDatetime extends TransientComparator {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            DateTimeComparator comparator = DateTimeComparator.getInstance();
            return comparator.compare(arg0.getUpdateDatetime(), arg1.getUpdateDatetime());
        }
    }
    
    private static final class ComparatorForCreationDatetime extends TransientComparator {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            DateTimeComparator comparator = DateTimeComparator.getInstance();
            return comparator.compare(arg0.getCreationDatetime(), arg1.getCreationDatetime());
        }
    }

    private static final class ComparatorForTitle extends TransientComparator {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            String s0 = arg0.getTitle();
            String s1 = arg1.getTitle();
//            return s0.compareToIgnoreCase(s1);
            return s0.compareTo(s1);
        }
    }

    private static final class ComparatorForId extends TransientComparator  {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            Long id0 = arg0.getId();
            Long id1 = arg1.getId();
            return id0.compareTo(id1);
        }
    }
    
    private static final TransientComparator[] comparators;
    
    private static final TransientReverseComparator[] reverseComparators;
    
    static {
        final int c = FragmentOrder.COUNT.ordinal();
        
        comparators = new TransientComparator[c];
        comparators[FragmentOrder.UPDATE_DATETIME.ordinal()] = new ComparatorForUpdateDatetime();
        comparators[FragmentOrder.CREATION_DATETIME.ordinal()] = new ComparatorForCreationDatetime();
        comparators[FragmentOrder.TITLE.ordinal()] = new ComparatorForTitle();
        comparators[FragmentOrder.ID.ordinal()] = new ComparatorForId();
        
        reverseComparators = new TransientReverseComparator[c];
        for (int i=0; i<c; ++i) {
            reverseComparators[i] = new TransientReverseComparator(comparators[i]);
        }
    }
    
    public static final Comparator<Fragment> getComparator(FragmentOrder order, boolean ascending) {
        final int i = order.ordinal();
        if (ascending) {
            return comparators[i];
        }
        else {
            return reverseComparators[i];
        }
    }
    
}
