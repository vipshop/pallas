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

package com.vip.pallas.plugin.search.script.similarity;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.similarity.AbstractSimilarityProvider;

public class VipSearchSimilarityProvider extends AbstractSimilarityProvider {
	private final VipSearchSimilarity similarity;

	@Inject
	public VipSearchSimilarityProvider(@Assisted String name, @Assisted Settings settings) {
		super(name);
		this.similarity = new VipSearchSimilarity(settings);
	}

	public VipSearchSimilarity get() {
		return similarity;
	}
}