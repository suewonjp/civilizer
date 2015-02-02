package com.knowledgex.domain;

import java.util.Comparator;

import org.joda.time.DateTimeComparator;

public final class FragmentComparator {
    
    private static final class ComparatorForCreationDatetime implements Comparator<Fragment> {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            DateTimeComparator comparator = DateTimeComparator.getInstance();
            return comparator.compare(arg0.getCreationDatetime(), arg1.getCreationDatetime());
        }
    }

    private static final class ComparatorForUpdateDatetime implements Comparator<Fragment> {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            DateTimeComparator comparator = DateTimeComparator.getInstance();
            return comparator.compare(arg0.getUpdateDatetime(), arg1.getUpdateDatetime());
        }
    }

    private static final class ComparatorForTitle implements Comparator<Fragment> {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            String s0 = arg0.getTitle();
            String s1 = arg1.getTitle();
            return s0.compareToIgnoreCase(s1);
        }
    }

    private static final class ComparatorForId implements Comparator<Fragment> {
        @Override
        public int compare(Fragment arg0, Fragment arg1) {
            Long id0 = arg0.getId();
            Long id1 = arg1.getId();
            return id0.compareTo(id1);
        }
    }
    
    public static Comparator<Fragment> newComparatorForCreationDatetime() {
        return new ComparatorForCreationDatetime();
    }
    
    public static Comparator<Fragment> newComparatorForUpdateDatetime() {
        return new ComparatorForUpdateDatetime();
    }
    
    public static Comparator<Fragment> newComparatorForTitle() {
        return new ComparatorForTitle();
    }
    
    public static Comparator<Fragment> newComparatorForId() {
        return new ComparatorForId();
    }

}
