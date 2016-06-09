package com.civilizer.test.config

import spock.lang.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.dao.FragmentDao;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.FsUtil;

@Subject(Configurator)
class ConfigSpec extends spock.lang.Specification {
    
    def cleanup() {
        System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
        System.clearProperty(AppOptions.DB_FILE_PREFIX);
        System.clearProperty(AppOptions.FILE_BOX_HOME);
        System.clearProperty(AppOptions.OVERRIDE_OPTION_FILE);
    }
    
    def "Default private home"() {
        given:
            TestUtil.unconfigure();
            
        when: "Simulate setting up the default private home"
            // Notice that the real name of default private home is ".civilizer"
            final String defaultPrivateHomeName = ".civilizer~";
            final String defaultPrivateHomePath = Configurator.getDefaultPrivateHomePath(defaultPrivateHomeName);
            File privateHome = new File(defaultPrivateHomePath);
            assert privateHome && privateHome.isAbsolute();
            FileUtils.deleteQuietly(privateHome);
            assert ! privateHome.isDirectory();
            new Configurator(defaultPrivateHomeName);
        then: "The default private home has been created?"
            privateHome.isDirectory()
        
        cleanup: ""
            FileUtils.deleteQuietly(privateHome)
    }
    
    def "Private home provided at runtime"() {
        given: "Store the private home path as a system property" 
            final String path = TestUtil.getPrivateHomePath();
            System.setProperty(AppOptions.PRIVATE_HOME_PATH, path);
            
        when: "Configure"
            new Configurator();
        then: "The private home has been created?"        
            final File f = new File(System.getProperty(AppOptions.PRIVATE_HOME_PATH));
            f && f.isDirectory() && f.isAbsolute()
    }
    
    def "Relative file path rules"() {
        given: "The private home path"
            final String homePath = TestUtil.getPrivateHomePath();
            System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
        and: '''Set the DB path and File Box path as the system properties.
                Mostly, these paths are set as absolute paths.
                However, it should work even though they are set as relative ones.
                Also, notice that AppOptions.OVERRIDE_OPTION_FILE should be true,
                otherwise, those paths can't be set via the system properties. 
             '''
            System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
            System.setProperty(AppOptions.DB_FILE_PREFIX, "db/file/prefix");
            System.setProperty(AppOptions.FILE_BOX_HOME, "file/box/home");
        
        when: "Configure"
            new Configurator();
        then: "The paths are set up based on the provided private home path"
            "true" == System.getProperty(AppOptions.OVERRIDE_OPTION_FILE)
            FsUtil.normalizePath(homePath + "/db/file/prefix") ==
                System.getProperty(AppOptions.DB_FILE_PREFIX)
            FsUtil.normalizePath(homePath + "/file/box/home") ==
                System.getProperty(AppOptions.FILE_BOX_HOME)
        
        cleanup: 
            FileUtils.deleteQuietly(new File(homePath + "/file"));
    }
    
    def "Absolute file path rules"() {
        given: "The private home path"
            final String homePath = TestUtil.getPrivateHomePath();
            System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);            
        and: "The user home path"
            final String userHomePath = System.getProperty("user.home");            
        and: '''Set the DB path as the system properties.
                Set it as an absolute path. (the most general case)
             '''
            System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
            System.setProperty(AppOptions.DB_FILE_PREFIX, userHomePath + "/db/file/prefix");
        
        when: "Configure"
            new Configurator();
        then: '''The paths are set up as specified.
                 Notice that the private home path and the DB path is not related in this case.
              '''
            "true" == System.getProperty(AppOptions.OVERRIDE_OPTION_FILE)
            FsUtil.normalizePath(userHomePath + "/db/file/prefix") ==
                System.getProperty(AppOptions.DB_FILE_PREFIX)
    }
    
    def "Home alias (~) file path rules"() {
        given: "The private home path"
            final String homePath = TestUtil.getPrivateHomePath();
            System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);           
        and: "The user home path"
            final String userHomePath = System.getProperty("user.home");            
        and: '''Set the DB path as the system properties.
                Set it using a ~ tilde notation
             '''
            System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
            System.setProperty(AppOptions.DB_FILE_PREFIX, "~/db/file/prefix");
            
        when: "Configure"
            new Configurator();
        then: "The path should be translated as a normal absolute path"
            FsUtil.normalizePath(userHomePath + "/db/file/prefix") ==
                System.getProperty(AppOptions.DB_FILE_PREFIX)
        
    }
    
    def "File path separator convention"() {
        given: "The private home path"
            final String homePath = TestUtil.getPrivateHomePath();
            System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
        and: "The user home path"
            final String userHomePath = System.getProperty("user.home");
        and: "Set the DB path. Notice we use the Windows separator convention here"
            System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
            System.setProperty(AppOptions.DB_FILE_PREFIX, userHomePath + "\\db\\file\\prefix");
        
        when: "Configure"
            new Configurator()
        then: "The path should be translated as a Unix path convention"
            ! System.getProperty(AppOptions.PRIVATE_HOME_PATH).contains("\\")
            FsUtil.normalizePath(userHomePath + "/db/file/prefix") == System.getProperty(AppOptions.DB_FILE_PREFIX)
    }
    
    def "Test if we can access the data source after new Configurator() call"() {
        given: "Configure"
            TestUtil.configure();
        and: "Spring application context"
            def ctx = new GenericXmlApplicationContext();
        and: "Load the data source context"
            ctx.load("classpath:datasource-context-h2-url.xml");
            ctx.refresh();
            
        when: "Access a data access object"
            FragmentDao fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        then: "The DAO is valid"
            fragmentDao
            
        cleanup:
            ctx.close();
    }
    
    def "Configurator.equals"() {
        given: "An arbitrary property key"
            final String option = "Whatever Option";
        expect:
            ! Configurator.equals(option, "true", false)
            
        when: "Set value for the key with various forms"
            System.setProperty(option, value)
        then:
            result == Configurator.equals(option, "true", false)
            
        cleanup:
            System.clearProperty(option)
            
        where:
            value   || result
            "TRUE"  || true
            "True"  || true
            "true"  || true
            "false" || false
    }
    
    def "Configurator.isTrue"() {
        given: "An arbitrary property key"
            final String option = "Boolean Option";
        expect:
            ! Configurator.isTrue(option);
            
        when: "Set value for the key with various forms"
            System.setProperty(option, value)
        then:
            result == Configurator.isTrue(option)
            
        cleanup:
            System.clearProperty(option)
            
        where:
            value   || result
            "TRUE"  || true
            "true"  || true
            "yes"   || true
            "ON"    || true
    }

}
