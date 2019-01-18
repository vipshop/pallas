/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package ${package}.demo.source;

import com.vip.pallas.plugin.search.script.PallasSearchScript;
import org.elasticsearch.search.lookup.IndexField;
import org.elasticsearch.search.lookup.IndexFieldTerm;
import org.elasticsearch.search.lookup.IndexLookup;
import org.elasticsearch.search.lookup.TermPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import com.vip.pallas.plugin.search.script.AbstractPallasSearchScript;
import redis.clients.jedis.JedisPoolConfig;


import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class RunWithResSource extends AbstractPallasSearchScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunWithResSource.class);

	private String[] list;
	public static final String GOODS_NAME_SEPARATE = "goods_name_separate";

	float scorePlusWithStock = 0;

	private PallasSearchScript pallasSearchScript;

	protected static final int PARALLELISM = Integer.parseInt(System.getProperty("stocks.parallelism", "2"));
	protected static final String REDIS_HOST = getOSProperty("VIP_MPCSEARCH_REDIS_MASTER_01_HOST", "127.0.0.1");
	protected static final String REDIS_PORT = getOSProperty("VIP_MPCSEARCH_REDIS_MASTER_01_PORT", "6379");
	protected static final String OTHER = "other";

	public static JedisPool POOL;

	public static synchronized JedisPool getPool() {
		if (POOL == null) {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(PARALLELISM * 8);
			config.setMaxIdle(PARALLELISM * 2);
			config.setMaxWaitMillis(5000);
			config.setTestOnBorrow(true);
			config.setJmxEnabled(false);
			LOGGER.info("stock_redis host={}, port={}", REDIS_HOST, REDIS_PORT);
			POOL = new JedisPool(config, REDIS_HOST, Integer.parseInt(REDIS_PORT));

		}
		return POOL;
	}

	public RunWithResSource(PallasSearchScript pallasSearchScript, Map<String, Object> params) {
		super(pallasSearchScript, params);
		this.pallasSearchScript = pallasSearchScript;
		this.list = ((String)(params.get("terms"))).split("${symbol_escape}${symbol_escape}s+");
		this.scorePlusWithStock = (Integer)(params.get("scorePlus"));
	}

	private static String getOSProperty(String key, String defaultValue) {
		String val = System.getProperty(key,System.getenv(key));
		return (val == null) ? defaultValue : val;
	}

	@Override
	public Object run() {
		return runAsDouble();
	}

	public double runAsDouble() {
		float score = 0f;
		int length = list.length;
		for (String s : list) {
			IndexField indexField = pallasSearchScript.getIndexField(GOODS_NAME_SEPARATE);
			int[] positions = new int[length];
			IndexFieldTerm indexTermField = indexField.get(s, IndexLookup.FLAG_CACHE);
			int i = 0;
			if (indexTermField != null) {
				Iterator<TermPosition> iter = indexTermField.iterator();
				if (iter.hasNext()) {
					positions[i++] = iter.next().position;
				}
			}
			Arrays.sort(positions);
			for (i = 0; i < length; i++) {
				if (i != 0 && i != length - 1) {
					int dis = positions[i + 1] - positions[i];
					score += (dis == 1 ? 1 : (dis == 2 ? 0.5 : (dis == 3 ? 0.1 : 0)));
				}
				score += 8 / (0.6 + positions[i]);
			}
		}
		return score;
	}
}