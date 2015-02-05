@NamedQueries({
    @NamedQuery(name = "Fragment.findById",
        query = "select distinct f from Fragment f where f.id = :id"),
    @NamedQuery(name = "Fragment.findByIdWithRelatedOnes",
        query = "select distinct f from Fragment f left join fetch f.relatedOnes where f.id = :id"),
    @NamedQuery(name = "Fragment.findByIdWithTags",
        query = "select distinct f from Fragment f left join fetch f.tags where f.id = :id"),
    @NamedQuery(name = "Fragment.findIdsOrderByUpdateDatetime",
        query = "select f.id from Fragment f order by f.updateDatetime desc"),
    @NamedQuery(name = "Fragment.findNonTrashedWithTagsOrderByUpdateDatetime",
        query = "from Fragment f left join fetch f.tags where f.id not in ( select distinct f.id from Fragment f join f.tags t where t.id = 0 ) order by f.updateDatetime desc"),
    
    @NamedQuery(name = "Tag.findAllWithChildren",
        query = "select distinct t from Tag t left join fetch t.children"),
    @NamedQuery(name = "Tag.findById",
        query = "select distinct t from Tag t where t.id = :id"),
    @NamedQuery(name = "Tag.findByIdWithChildren",
        query = "select distinct t from Tag t left join fetch t.children where t.id = :id"),
    @NamedQuery(name = "Tag.findByIdWithFragments",
        query = "select distinct t from Tag t left join fetch t.fragments where t.id = :id"),
    @NamedQuery(name = "Tag.findFragments",
//        query = "select t.fragments from Tag t where t.id = :id"),
        query = "select f from Tag t join t.fragments as f left join fetch f.tags where t.id = :id"),
    @NamedQuery(name = "Tag.findNonTrashedFragments",
        query = "select distinct f from Fragment f left join fetch f.tags t where t.id = :id and f.id not in ( select f2.id from Tag t2 join t2.fragments f2 where t2.id = 0 )"),
    @NamedQuery(name = "Tag.findFragmentsWithIdFilter",
        query = "select distinct f from Tag t join t.fragments as f left join fetch f.tags where t.id in (:ids)"),
    @NamedQuery(name = "Tag.findParentTags",
        query = "select distinct t from Tag t inner join t.children child where child.id = :id"),

    @NamedQuery(name = "Tag2Fragment.findTrashedFragmentIds",
    	query = "select distinct t2f.fragmentId from Tag2Fragment t2f where t2f.tagId = 0"),
//	@NamedQuery(name = "Tag2Fragment.findNonTrashedFragmentIds",
//		query = "select distinct t2f.fragmentId from Tag2Fragment t2f where t2f.tagId != 0"),
}) 

package com.knowledgex.domain;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

