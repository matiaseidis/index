package index;

import models.Cacho;

import org.junit.Test;

import play.test.UnitTest;

public class TestCacho extends UnitTest{

	@Test
	public void test(){
		new Cacho(0L,35L).save();
	}
}
