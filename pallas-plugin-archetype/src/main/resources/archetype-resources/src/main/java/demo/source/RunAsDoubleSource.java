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

import com.vip.pallas.plugin.search.script.AbstractPallasDoubleSearchScript;
import com.vip.pallas.plugin.search.script.PallasSearchScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RunAsDoubleSource extends AbstractPallasDoubleSearchScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunAsDoubleSource.class);

	private final static char separatorNnderLine = '_';
	private final static char separatorColon = ':';
	private String[] sizes;
	private String fieldName;

	private PallasSearchScript pallasSearchScript;

	public RunAsDoubleSource(PallasSearchScript pallasSearchScript, Map<String, Object> params) {
		super(pallasSearchScript, params);
		this.pallasSearchScript = pallasSearchScript;

		String sizesStr =  (String)params.get("sizes");

		if (sizesStr != null) {
			this.sizes = sizesStr.split("${symbol_escape}${symbol_escape}|");
		}

		this.fieldName = (String)params.get("fieldName");
	}
	@Override
	public double runAsDouble() {
		// 取宽表的值
		String temp = pallasSearchScript.docFieldStrings(fieldName).getValue();
		if (temp == null) {
			return 0;
		}
		double def = 0;
		int end = 0;
		int index = 0;
		int m = 0;
		if (sizes != null) {
			for (String s : sizes) {
				double v = 0;
				index = temp.indexOf(s);
				if (index != -1) {
					m = temp.indexOf(separatorColon, index) + 1;
					// m=index+7;
					end = temp.indexOf(separatorNnderLine, m);
					if (end == -1) {
						v = Double.parseDouble(temp.substring(m));
					} else {
						v = Double.parseDouble(temp.substring(m, end));
					}
					if (v > def) {
						def = v;
					}
				}
			}
		}
		return def;
	}
}