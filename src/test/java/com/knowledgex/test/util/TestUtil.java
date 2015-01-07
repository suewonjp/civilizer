package com.knowledgex.test.util;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class TestUtil {

    private static Log log = newLogger(TestUtil.class);
    private static Random random = newRandomGenerator();
    
    private static Random newRandomGenerator() {
		Calendar cal = Calendar.getInstance();
        assertNotNull(cal);

//        final long seed = 1404463439000L;
        final long seed = cal.getTimeInMillis();

        assertNotNull(log);
        log.info("random seed = " + seed);

        random = new Random(seed);
        assertNotNull(random);

        return random;
	}
    
    public static Log newLogger(Class<?> clazz) {
    	Log output = LogFactory.getLog(clazz);
        assertNotNull(output);    	
    	return output;
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

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> type) {
        assertNotNull(obj);
        assertSame(obj.getClass().getName(), type.getName());
        return (T) obj;
    }
}
