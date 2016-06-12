package com.civilizer.test.domain

import spock.lang.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import com.civilizer.domain.*;

@Subject(Fragment)
class FragmentSpec extends spock.lang.Specification {
    
    List<Fragment> fragments;
    
    def setup() {
        fragments = buildFragments(16);
    }
    
    def buildFragmentTitleList(Collection<Fragment> fragments) {
        List<String> fragmentNames = new ArrayList<String>();
        for (Fragment f : fragments) {
            fragmentNames.add(f.getTitle());
        }
        assert fragmentNames.size() == fragments.size()
        fragmentNames;
    }
    
    def buildFragments(int count) {
        List<Fragment> fragments = new ArrayList<Fragment>(count);
        for (int i = 0; i < count; i++) {
            Fragment frg = new Fragment(
                "fragment " + fragments.size(),
                "Some content " + fragments.size(),
                null);
            assert frg
            frg.setId(new Long(i));
            fragments.add(frg);
        }
        fragments;
    }
    
    def "IDs are valid"() {
        expect:
            ! fragments.isEmpty()
            
        fragments.each {
            assert it.getId() != null
        }
    }
        
    def "Equality"() {
        expect:
            ! fragments.isEmpty()
            
        fragments.each {
            def x = it;            
            //  REFLEXIVE:
            //  For any non-null reference value x,
            //  x.equals(x) should return true.        
            assert x.equals(x)
            
            Fragment y = new Fragment();
            y.setId(x.getId());
            Fragment z = new Fragment();
            z.setId(x.getId());

            // SYMMETRIC:
            // For any non-null reference values x and y, 
            // x.equals(y) should return true
            // if and only if y.equals(x) returns true.
            assert x.equals(y)
            assert y.equals(x)
                            
            // TRANSITIVE: 
            // For any non-null reference values x, y, and z,
            // if x.equals(y) returns true and y.equals(z) returns true,
            // then x.equals(z) should return true.
            assert y.equals(z)
            assert x.equals(z)
            
            // CONSISTENT:
            // For any non-null reference values x and y,
            // multiple invocations of x.equals(y) consistently return true or consistently return false,
            // provided no information used in equals comparisons on the objects is modified.
            assert x.equals(y)                
            assert x.equals(y)                
            assert x.equals(y)
            
            // For any non-null reference value x, x.equals(null) should return false.
            assert ! x.equals(null)
        }
    }
    
    def "Fragment.getFragmentTitleCollectionFrom"() {
        given: "Fragment titles built from the method in question"
            Collection<String> actual = 
                Fragment.getFragmentTitleCollectionFrom(fragments);
        and: "Fragment titles from the test code"
            Collection<String> expected = buildFragmentTitleList(fragments);
        expect:
            expected == actual
    }
    
    def "Sort"() {
        given: "Fragments to sort"
            fragments.each {
                assert it.getCreationDatetime()
                assert it.getUpdateDatetime()
                assert it.getTitle()
                assert it.getId() != null
            }
        and: "DateTimeComparator : necessary to check if the sort regarding DATETIME is done correctly"
            DateTimeComparator dtCmptr = DateTimeComparator.getInstance();
        expect:
            dtCmptr
            0 == FragmentOrder.UPDATE_DATETIME.ordinal()
            1 == FragmentOrder.CREATION_DATETIME.ordinal()
            2 == FragmentOrder.TITLE.ordinal()
            3 == FragmentOrder.ID.ordinal()
            4 == FragmentOrder.COUNT.ordinal()
            
        when: "Sort in order by update time"
            Collections.shuffle(fragments);
            Fragment.sort(fragments, FragmentOrder.UPDATE_DATETIME, false);
        then: "Properly sorted?"
            for (int i=1; i<fragments.size(); ++i) {
                def dt0 = fragments.get(i - 1).getUpdateDatetime();
                def dt1 = fragments.get(i).getUpdateDatetime();
                assert dtCmptr.compare(dt0, dt1) >= 0;
            }
        
        when: "Sort in order by creation time"
            Collections.shuffle(fragments);
            Fragment.sort(fragments, FragmentOrder.CREATION_DATETIME, false);
        then: "Properly sorted?"
            for (int i=1; i<fragments.size(); ++i) {
                def dt0 = fragments.get(i - 1).getCreationDatetime();
                def dt1 = fragments.get(i).getCreationDatetime();
                assert dtCmptr.compare(dt0, dt1) >= 0;
            }
            
        when: "Sort in order by title"
            Collections.shuffle(fragments);
            Fragment.sort(fragments, FragmentOrder.TITLE, true);
        then: "Properly sorted?"
            for (int i=1; i<fragments.size(); ++i) {
                def t0 = fragments.get(i - 1).getTitle();
                def t1 = fragments.get(i).getTitle();
                assert t0.compareToIgnoreCase(t1) <= 0
            }
                
        when: "Sort in order by id"
            Collections.shuffle(fragments);
            Fragment.sort(fragments, FragmentOrder.ID, true);
        then: "Properly sorted?"
            for (int i=1; i<fragments.size(); ++i) {
                def id0 = fragments.get(i - 1).getId();
                def id1 = fragments.get(i).getId();
                assert id0 < id1
            }
    }

}

