package com.civilizer.test.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.FileEntity;

public class DomainFileTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEquals() {
		final FileEntity f0 = new FileEntity("whatever.txt");
		final FileEntity f1 = new FileEntity("whatever.txt");
		final FileEntity f2 = new FileEntity("another.txt");
		
		assertEquals(true, f0.equals(f1));
		assertEquals(false, f2.equals(f1));
	}

}
