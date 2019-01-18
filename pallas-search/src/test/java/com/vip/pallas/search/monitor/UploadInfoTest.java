package com.vip.pallas.search.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UploadInfoTest {
	
	@Test
	public void testUpdateInfo() throws Exception {
		assertThat(new ServerWatch().buildAllInfo().get("IP")).isNotNull();
	}
	
}
