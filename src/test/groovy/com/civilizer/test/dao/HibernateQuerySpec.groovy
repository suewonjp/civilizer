package com.civilizer.test.dao

import spock.lang.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

@Ignore
class HibernateQuerySpec extends DaoSpecBase {
    
    static final DateTimeComparator dtCmptr = DateTimeComparator.getInstance();
    
    Session session;
    
    def setupSpec() {
        assert dtCmptr
        DaoSpecBase.setupApplicationContext(
            "classpath:datasource-context-h2-embedded.xml");
    }
    
    def cleanupSpec() {
        DaoSpecBase.cleanupApplicationContext();
    }
    
    @Override
    void doSetup() {
        super.doSetup();
        def sessionFactory = ctx.getBean("sessionFactory", SessionFactory.class);
        assert sessionFactory
        session = SessionFactoryUtils.getSession(sessionFactory, true);
        assert session
    }
    
    def "Simple criteria query"() {
        given: "A Hibernate Criteria for Fragment entity"
            final Criteria crit = session.createCriteria(Fragment.class);
        when: "Do simple fetch"
            final def fragments = crit.list();
        then: "The result of the fetch is valid"
            ! fragments.isEmpty()
            fragments.every {
                it.getId() != null
            }
            
        when: "Do a single fetch"
            def id = fragments.get(TestUtil.getRandom().nextInt(fragments.size())).getId();
            crit.add(Restrictions.eq("id", id));
            final def f0 = (Fragment) crit.uniqueResult();
        then:
            f0 == fragmentDao.findById(id, false, false)
    }
    
    def "Criteria query by order"() {
        given: "The 1st data; The order option is embedded in the query"
            final def fragments0 = session
                .createQuery("from Fragment f order by updateDatetime desc")
                .list();
            assert ! fragments0.isEmpty()
        and: "The 2nd data; The order option is specified by the Criteria API"
            final def crit = session.createCriteria(Fragment.class);
            crit.addOrder(Order.desc("updateDatetime"));
            
            // [NOTE] to get distinct results, use either of the following methods
            // - 1. lazy fetching of the specified child selections unless it is in lazy fetching mode
            //      crit.setFetchMode("tags", FetchMode.SELECT);
            // - 2. using distinct transformer
            //      crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            final def fragments1 = crit.list();
        expect: "Order in both of data is identical"
            fragments0.size() == fragments1.size()
            [ fragments0, fragments1 ].transpose().every {
                it[0] == it[1]
            }
            
            for (int i=1; i<fragments1.size(); ++i) {
                DateTime dt0 = fragments1[i - 1].getUpdateDatetime();
                DateTime dt1 = fragments1[i].getUpdateDatetime();
                assert dtCmptr.compare(dt0, dt1) >= 0
            }
    }
    
    def "Criteria projection"() {
        given: "Fragments sorted in ascending order of id"
            final def fragments = session.createCriteria(Fragment.class)
                .addOrder(Order.asc("id"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
        when: "Fetch data by projection query"
            final def results = session.createCriteria(Fragment.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .setProjection(Projections.projectionList()
                    .add(Projections.rowCount())
                    .add(Projections.min("id"))
                    .add(Projections.max("id"))
                )
                .list();
        then:
            1 == results.size()
            def prjList = results[0]
            prjList[0] == fragments.size()
            prjList[1] == fragments[0].getId()
            prjList[2] == fragments[-1].getId()
    }
    
    def "Criteria association"() {
        given: "Name of target tag"
            final String tgtTagName = "#trash";
        and: "Criteria for Fragment entity"
            final Criteria crit = session.createCriteria(Fragment.class);
        and: "Criteria for tags associated with each fragment"
            final Criteria tagCrit = crit.createCriteria("tags");
            assert tagCrit
        and: "Set a restriction so that we get fragments only associated with the target tag"
            tagCrit.add(Restrictions.eq("tagName", tgtTagName));
        when: "Execute the query"
            final def fragments = crit.list();
        then: "Check if the above restriction is satisfied"
            ! fragments.isEmpty()
            final def f0 = fragments[0];
            f0.getId() != null
            ! f0.getTags().isEmpty()
            f0.containsTagName(tgtTagName)
    }
    
    def "Detached criteria"() {
        given: "All fragments"
            final def fragments = fragmentDao.findAll(true);
            assert ! fragments.isEmpty()
        and: "A detached criteria with an arbitrary query"
            final def query = DetachedCriteria.forClass(Fragment.class);
            final long id = fragments.get(TestUtil.getRandom().nextInt(fragments.size())).getId();
            query.add(Property.forName("id").eq(id));
        when: "Execute the query"
            final def crit = query.getExecutableCriteria(session);
            final def f0 = (Fragment) crit.uniqueResult();
        then: "The result matches another form of query"
            f0 == fragmentDao.findById(id)
    }
    
    def "Queries for Tag2Fragment"() {
        given:
            final def fragments = fragmentDao.findAll(true);
            final def tags = tagDao.findAll();
            
        when: "Fetch all rows from Tag2Fragment table"
            final def t2fs = session.createQuery("from Tag2Fragment t2f").list();
        then: "Check data integrity"
            ! t2fs.isEmpty()
            t2fs.every { row ->
                Tag.containsId(tags, row.getTagId()) &&
                    Fragment.containsId(fragments, row.getFragmentId())
            }
        when: "Fetch ids of trashed fragments by a named query"
            final def trashedFragmentIds =
                session.getNamedQuery("Tag2Fragment.findTrashedFragmentIds").list();
        then: "Check data integrity"
            trashedFragmentIds.every { id ->
                def f = fragmentDao.findById(id, true, false);
                f.getTags().any {
                    it.getId() == Tag.TRASH_TAG_ID
                }
            }
    }
    
    def "Query with HQL"() {
        given: "An arbitrary tag id"
            final def tgtTagId = 13L;
        when: "Fetch fragments associated with the above tag by HQL query"
            final def fragments = session
                .createQuery('''
                    select distinct f 
                    from Fragment f 
                      left join fetch f.tags t 
                    where t.id = :id and f.id not in (
                      select f2.id 
                      from Tag t2 
                        join t2.fragments f2 
                      where t2.id = 0 
                    )
                    '''
                 )
                .setParameter("id", tgtTagId)
                .list();
        then: "The fetched data matches the conditions of our query"
            fragments.every { f ->
                def tags = f.getTags();
                Tag.containsId(tags, tgtTagId) && ! Tag.containsId(tags, Tag.TRASH_TAG_ID)
            }
    }
    
    @Ignore
    def "Export database as SQL script"() {
        given: "Path where to export data"
            final String tmpPath = TestUtil.getTempFolderPath();
            com.civilizer.utils.FsUtil.createUnexistingDirectory(new File(tmpPath));
        when: "Export content of the DB as a SQL script file"
            // The query creates a SQL script from the database.
            // [NOTE] The syntax is H2 database specific.
            final String queryString = 
                "script to '${tmpPath}${com.civilizer.utils.FsUtil.SEP}test.sql'";
            session.createSQLQuery(queryString).list();
        then: ""
            new File("${tmpPath}${com.civilizer.utils.FsUtil.SEP}test.sql").exists()
        cleanup:
            org.apache.commons.io.FileUtils.deleteQuietly(new File(tmpPath));
    } 

}
