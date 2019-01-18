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

package $

import java.util.Random;{package}.demo;

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

	private PallasSearchScript pallasSearchScript;

	private Random r = new Random();


	public RunWithResSource(PallasSearchScript pallasSearchScript, Map<String, Object> params) {
		super(pallasSearchScript, params);
		this.pallasSearchScript = pallasSearchScript;
	}

	@Override
	public Object run() {
		return runAsDouble();
	}

	public double runAsDouble() {
		return r.nextDouble();
	}
}