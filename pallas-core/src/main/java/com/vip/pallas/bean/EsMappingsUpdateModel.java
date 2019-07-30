package com.vip.pallas.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import com.vip.pallas.bean.EsMappings.Propertie;

public class EsMappingsUpdateModel {

        private Map<String, Propertie> properties;

        public Map<String, Propertie> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Propertie> properties) {
            this.properties = properties;
        }
}
