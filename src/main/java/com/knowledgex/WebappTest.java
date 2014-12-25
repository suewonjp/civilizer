package com.knowledgex;

import java.util.List;

import org.springframework.context.support.GenericXmlApplicationContext;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;

public class WebappTest {

    public static void main(String[] args) {

        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:datasource-context.xml");
        ctx.refresh();

        FragmentDao fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        List<Fragment> fragments = fragmentDao.findAll();
        listFragments(fragments);
        
        TagDao tagDao = ctx.getBean("tagDao", TagDao.class);
        List<Tag> tags = tagDao.findAll();
        listTags(tags);
    }

    private static void listFragments(List<Fragment> fragments) {
        System.out.println("");
        System.out.println("Listing fragments without details:");
        for (Fragment frgm : fragments) {
            System.out.println(frgm);
            System.out.println();
        }
    }

    private static void listTags(List<Tag> tags) {
        System.out.println("");
        System.out.println("Listing Tags without details:");
        for (Tag t : tags) {
            System.out.println(t);
            System.out.println();
        }
    }

}
