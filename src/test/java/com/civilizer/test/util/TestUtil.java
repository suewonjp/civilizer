package com.civilizer.test.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.dao.FileEntityDao;
import com.civilizer.domain.FileEntity;

public final class TestUtil {

    private static final Logger logger = newLogger();
    private static final Random random = newRandomGenerator();
    
    private static Logger newLogger() {
    	String log4jPropName = "log4j-test.properties";
    	URL url = ClassLoader.getSystemClassLoader().getResource(log4jPropName);
    	assertNotNull(url);
    	String log4jPropPath = url.getPath();
    	assertTrue(log4jPropPath != null && log4jPropPath.isEmpty() == false);
    	PropertyConfigurator.configure(log4jPropPath);

    	return newLogger(TestUtil.class);
    }

    public static Logger newLogger(Class<?> clazz) {
        Logger output = LoggerFactory.getLogger(clazz);
        assertNotNull(output);      
        return output;
    }
    
    private static Random newRandomGenerator() {
		Calendar cal = Calendar.getInstance();
        assertNotNull(cal);

        long seed = cal.getTimeInMillis();
//        seed = 1428894470142L;
        
        logger.info("random seed = " + seed);

        Random random = new Random(seed);
        assertNotNull(random);

        return random;
	}

    public static Random getRandom() {
        assertNotNull(random);
        return random;
    }

    public static String randomString(Random r, int minCount, int maxCount) {
        minCount = Math.max(Math.min(minCount, maxCount), 1);
        maxCount = Math.max(minCount, maxCount);
        final int stringCount = minCount + r.nextInt(maxCount - minCount);
        char[] s = new char[stringCount];

        for (int i = 0; i < stringCount; ++i) {
            s[i] = (char) (32 + r.nextInt(127 - 32));
        }

        return new String(s);
    }
    
    public static int[] randomIndices(Random r, int minCount, int maxCount) {
    	minCount = Math.max(Math.min(minCount, maxCount), 1);
        maxCount = Math.max(minCount, maxCount);
        List<Integer> tmp = new ArrayList<>(maxCount);
        for (int i = 0; i < maxCount; i++) {
        	tmp.add(i);
		}
        Collections.shuffle(tmp);
        final int outputCount = minCount + r.nextInt(maxCount - minCount);
        int[] output = new int[outputCount];
        for (int i = 0; i < outputCount; i++) {
        	output[i] = tmp.get(i);
		}
        return output;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> type) {
        assertNotNull(obj);
        assertSame(obj.getClass().getName(), type.getName());
        return (T) obj;
    }

    public static <T> void checkIfNoDuplicateExistsIn(Collection<T> coll) {
    	if (coll instanceof Set == false) {
    		Set<T> s = new HashSet<T>(coll);
    		assertEquals(s.size(), coll.size());
    	}
    }
    
    public static void configure() {
    	final String path = System.getProperty("user.dir") + "/test/private-home";
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, path);
    	new Configurator();
    }

    public static void unconfigure() {
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    }
    
    public static String getFilesHomePath() {
    	return System.getProperty(AppOptions.PRIVATE_HOME_PATH) + File.separatorChar + "files";
    }
    
    public static void touchTestFilesForFileBox(FileEntityDao fileEntityDao) {
    	final String filesHome = getFilesHomePath();
    	List<FileEntity> fileEntitiesFromDB = fileEntityDao.findAll();
		assertNotNull(fileEntitiesFromDB);
		
		for (FileEntity fe : fileEntitiesFromDB) {
			final File f = fe.toFile(filesHome);
			try {
				FileUtils.touch(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			assertEquals(true, f.isFile());
		}
    }
}
