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

package com.vip.pallas.bean;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.SerializedName;

public class EsMappings {
	
	private Mappings mappings;
	
	private Map<String, Object> settings;
	
	public static class Mappings{
		private Item item;
		
		public Item getItem() {
			return item;
		}

		public void setItem(Item item) {
			this.item = item;
		}
	}
	
	public static class Item{

		@SerializedName("include_in_all")
		@JsonProperty("include_in_all")
		private boolean includeInAll;


		@SerializedName("dynamic")
		@JsonProperty("dynamic")
		private boolean dynamic = false;

		private Map<String, Propertie> properties;

		public boolean isIncludeInAll() {
			return includeInAll;
		}

		public void setIncludeInAll(boolean includeInAll) {
			this.includeInAll = includeInAll;
		}

		public Map<String, Propertie> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, Propertie> properties) {
			this.properties = properties;
		}

		public boolean isDynamic() {
			return dynamic;
		}

		public void setDynamic(boolean dynamic) {
			this.dynamic = dynamic;
		}
	}

    public static class Propertie{
		@SerializedName("doc_values")
		private Boolean docValues;
		private String type;
		private Boolean index;
		private String format;
		private Boolean dynamic;
		private String analyzer;
		private String normalizer;

		private Map<String, Propertie> properties;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public boolean isIndex() {
			return index;
		}
		public void setIndex(Boolean index) {
			this.index = index;
		}
		public String getFormat() {
			return format;
		}
		public void setFormat(String format) {
			this.format = format;
		}
		public boolean isDocValues() {
			return docValues;
		}
		public void setDocValues(Boolean docValues) {
			this.docValues = docValues;
		}

		public String getAnalyzer() {
			return analyzer;
		}

		public void setAnalyzer(String analyzer) {
			this.analyzer = analyzer;
		}

		public String getNormalizer() {
			return normalizer;
		}

		public void setNormalizer(String normalizer) {
			this.normalizer = normalizer;
		}

		public Map<String, Propertie> getProperties() {
			return properties;
		}
		public void setProperties(Map<String, Propertie> properties) {
			this.properties = properties;
		}
		public boolean isDateType(){
			return "date".equals(type);
		}

		public Boolean getDynamic() {
			return dynamic;
		}

		public void setDynamic(Boolean dynamic) {
			this.dynamic = dynamic;
		}
	}
	
	public Mappings getMappings() {
		return mappings;
	}

	public void setMappings(Mappings mappings) {
		this.mappings = mappings;
	}

	public Map<String, Object> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}
}