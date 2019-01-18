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

import com.vip.pallas.plugin.search.script.AbstractPallasLongSearchScript;
import com.vip.pallas.plugin.search.script.PallasSearchScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RunAsLongSource extends AbstractPallasLongSearchScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunAsLongSource.class);

	private final static char separatorNnderLine = '_';
	private final static char separatorColon = ':';
	private String[] sizes;
	private String priceField;
	private String agioField;

	private PallasSearchScript pallasSearchScript;

	public RunAsLongSource(PallasSearchScript pallasSearchScript, Map<String, Object> params) {
		super(pallasSearchScript, params);
		this.pallasSearchScript = pallasSearchScript;

		String sizesStr =  (String)params.get("sizes");

		if (sizesStr != null) {
			this.sizes = sizesStr.split("${symbol_escape}${symbol_escape}|");
		}

		this.priceField = (String)params.get("priceField");
		this.agioField = (String)params.get("agioField");
	}
	@Override
	public long runAsLong() {
		String tempPrice = pallasSearchScript.docFieldStrings(priceField).getValue();
		String tempAgio = pallasSearchScript.docFieldStrings(agioField).getValue();
		if (tempPrice == null || tempAgio == null) {
			return 0;
		}
		int end = 0;
		int index = 0;
		int m=0;
		List<Double> priceList = new ArrayList<>();
		List<Integer> agioList = new ArrayList<>();
		if (sizes != null) {
			for (String s : sizes) {
				index = tempPrice.indexOf(s);
				if (index != -1) {
					double v = 0;
					m = tempPrice.indexOf(separatorColon, index)+1;
					end = tempPrice.indexOf(separatorNnderLine, m);
					if (end == -1) {
						v = Double.parseDouble(tempPrice.substring(m));
					} else {
						v = Double.parseDouble(tempPrice.substring(m, end));
					}
					priceList.add(v);
				}else{
					continue;
				}
				
				int a=0;
				index = tempAgio.indexOf(s);
				if (index != -1) {
					m = tempAgio.indexOf(separatorColon, index)+1;
					end = tempAgio.indexOf(separatorNnderLine, m);
					if (end == -1) {
						a = Integer.parseInt(tempAgio.substring(m));
					} else {
						a = Integer.parseInt(tempAgio.substring(m, end));
					}
					agioList.add(a);
				}
			}
	
			if (priceList.size()==0 || priceList.size() != agioList.size()) {
				return 0;
			}
	
			Double[] pr = priceList.toArray(new Double[priceList.size()]);
			Integer[] ag = agioList.toArray(new Integer[agioList.size()]);
			int len = pr.length;
	
			double temp = 0;
			int key = 0;
			for (int i = 0; i < len; i++) {
				for (int j = 1 + i; j < len; j++) {
					if (pr[i] < pr[j]) {
						temp = pr[j];
						pr[j] = pr[i];
						pr[i] = temp;
	
						key = ag[j];
						ag[j] = ag[i];
						ag[i] = key;
					}
				}
			}
			temp = pr[0];
			int ind = 0;
			for (int k = 1; k < len; k++) {
				if (pr[k] == temp) {
					ind = k;
				} else {
					break;
				}
			}
			long one = 0;
			try {
				if (ind == 0) {
					return ag[0];
				}
				int agioTemp = 0;
				for (int n = 1; n < ind + 1; n++) {
					agioTemp = ag[n];
					if (one < agioTemp) {
						one = agioTemp;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
}