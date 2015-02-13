//@formatter:off
@NamedQueries({
    @NamedQuery(name = "Fragment.countAll",
        query = "select count(*) "
        	  + "from Fragment f"
              ),
    @NamedQuery(name = "Fragment.countAllNonTrashed",
        query = "select count(*) "
              + "from Fragment f "
              + "where f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              ),
    @NamedQuery(name = "Fragment.findById",
        query = "select distinct f "
              + "from Fragment f "
              + "where f.id = :id "
              ),
    @NamedQuery(name = "Fragment.findByIdWithAll",
        query = "select distinct f "
              + "from Fragment f "
              + "  left join fetch f.tags "
              + "  left join fetch f.relatedOnes "
              + "where f.id = :id "
              ),
    @NamedQuery(name = "Fragment.findByIdWithRelatedOnes",
        query = "select distinct f "
              + "from Fragment f "
              + "  left join fetch f.relatedOnes "
              + "where f.id = :id "
              ),
    @NamedQuery(name = "Fragment.findByIdWithTags",
        query = "select distinct f "
              + "from Fragment f "
              + "  left join fetch f.tags "
              + "where f.id = :id "
              ),
    @NamedQuery(name = "Fragment.findIds",
        query = "select f.id "
              + "from Fragment f "
              + "order by f.updateDatetime desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedOrderByUpdateDatetime",
        query = "select f.id "
              + "from Fragment f "
              + "where f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by f.updateDatetime desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedOrderByCreationDatetime",
        query = "select f.id "
              + "from Fragment f "
              + "where f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by f.creationDatetime desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedOrderByTitle",
        query = "select f.id "
              + "from Fragment f "
              + "where f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by lower(f.title) desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedOrderById",
        query = "select f.id "
              + "from Fragment f "
              + "where f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by f.id desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedByTagIdOrderByUpdateDatetime",
        query = "select f.id "
              + "from Fragment f "
              + "  inner join f.tags t "
              + "where t.id = :tagId and f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by f.updateDatetime desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedByTagIdOrderByCreationDatetime",
        query = "select f.id "
              + "from Fragment f "
              + "  inner join f.tags t "
              + "where t.id = :tagId and f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by f.creationDatetime desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedByTagIdOrderByTitle",
        query = "select f.id "
              + "from Fragment f "
              + "  inner join f.tags t "
              + "where t.id = :tagId and f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by lower(f.title) desc "
              ),
    @NamedQuery(name = "Fragment.findIdsNonTrashedByTagIdOrderById",
        query = "select f.id "
              + "from Fragment f "
              + "  inner join f.tags t "
              + "where t.id = :tagId and f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              + "order by f.id desc "
              ),
    
    @NamedQuery(name = "Tag.countAll",
        query = "select count(*) "
        	  + "from Tag t"
              ),
    @NamedQuery(name = "Tag.findIdsOrderByTagName",
		query = "select t.id "
		      + "from Tag t "
		      + "order by lower(t.tagName) asc "
		      ),
    @NamedQuery(name = "Tag.findById",
        query = "select distinct t "
              + "from Tag t "
              + "where t.id = :id"
              ),
    @NamedQuery(name = "Tag.findByIdWithChildren",
        query = "select distinct t "
              + "from Tag t "
              + "  left join fetch t.children "
              + "where t.id = :id "
              ),
    @NamedQuery(name = "Tag.findByIdWithFragments",
        query = "select distinct t "
              + "from Tag t "
              + "  left join fetch t.fragments "
              + "where t.id = :id "
              ),
    @NamedQuery(name = "Tag.countFragments",
        query = "select count(f) "
              + "from Tag t "
              + "  inner join t.fragments as f "
              + "where t.id = :id "
              ),
    @NamedQuery(name = "Tag.countNonTrashedFragments",
        query = "select count(f) "
              + "from Tag t "
              + "  inner join t.fragments as f "
              + "where t.id = :id and f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              ),
    @NamedQuery(name = "Tag.findFragmentIds",
        query = "select distinct f.id "
              + "from Tag t "
              + "  inner join t.fragments as f "
              + "where t.id = :id "
              ),
    @NamedQuery(name = "Tag.findFragments",
        query = "select distinct f "
              + "from Tag t "
              + "  inner join t.fragments as f "
              + "  left join fetch f.tags "
              + "where t.id = :id "
              ),
    @NamedQuery(name = "Tag.findNonTrashedFragments",
        query = "select distinct f "
              + "from Fragment f "
              + "  inner join f.tags t "
              + "  left join fetch f.tags "
              + "where t.id = :id and f.id not in ( "
              + "  select t2f.fragmentId "
              + "  from Tag2Fragment t2f "
              + "  where t2f.tagId = 0 "
              + ") "
              ),
    @NamedQuery(name = "Tag.findFragmentsWithIdFilter",
        query = "select distinct f "
              + "from Tag t "
              + "  inner join t.fragments as f "
              + "  left join fetch f.tags "
              + "where t.id in (:ids) "
              ),
    @NamedQuery(name = "Tag.findParentTags",
        query = "select distinct t "
              + "from Tag t "
              + "  inner join t.children child "
              + "where child.id = :id "
              ),

    @NamedQuery(name = "Tag2Fragment.findTrashedFragmentIds",
        query = "select t2f.fragmentId "
              + "from Tag2Fragment t2f "
              + "where t2f.tagId = 0 "
              ),
}) 

package com.knowledgex.domain;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

