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

import com.vip.pallas.plugin.search.script.AbstractPallasDoubleSearchScript;
import com.vip.pallas.plugin.search.script.PallasSearchScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RunAsDoubleSource extends AbstractPallasDoubleSearchScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunAsDoubleSource.class);

	private PallasSearchScript pallasSearchScript;

	private Random r = new Random();

	public RunAsDoubleSource(PallasSearchScript pallasSearchScript, Map<String, Object> params) {
		super(pallasSearchScript, params);
		this.pallasSearchScript = pallasSearchScript;
	}
	@Override
	public double runAsDouble() {
		return r.nextDouble();
	}
}