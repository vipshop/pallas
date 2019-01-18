/*
   Copyright 2011-2014 Lukas Vlcek

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

// Override jQuery AJAX settings to use jsonp
// Add some helper methods into both Model and Collection to support "baseUrl"

var currentHostIndex = 0;
Backbone.Model = Backbone.Model.extend({
    sync: function(method, model, options) {
		var _this = this;
//    options.timeout = 10000; // required, or the application won't pick up on 404 responses
        //options.dataType = 'jsonp';
        //options.url = this.getBaseUrl() + this.url();
        options.url = "/pallas/esproxy" + this.url();
		var urlbase = this.getBaseUrl();
		var cluster = this.getCluster();
		var currentHost = urlbase;
		var hosts = urlbase;
		var hostList = [currentHost];
		if(hosts.indexOf(",") >= 0){
			var arr = hosts.split(",");
			hostList = [];
			for(var i=0;i<arr.length;i++){
				if(arr[i]){
					if(arr[i].indexOf("http") < 0){
						arr[i] = "http://"+arr[i];
					}
					hostList.push(arr[i]);
				}
			}
			currentHost = hostList[currentHostIndex];
		}
		
		if(!options.retryCount){
			options.retryCount = 0;
		}
		options.retryCount = options.retryCount + 1;
		
        options.beforeSend = function(xhr){
			xhr.setRequestHeader('es-cluster', cluster);
			xhr.setRequestHeader('es-request', true);
        }
	  
		var error = options.error;
		options.error = function(msg){
			currentHostIndex = (currentHostIndex + 1)%hostList.length;
			if(options.retryCount >= hostList.length){
				error && error(msg);
				return;
			}
			_this.sync(method, model, options);
		}
        return Backbone.sync(method, model, options);
    },
    getBaseUrl: function() {
        return this.get("baseUrl");
    },
    setBaseUrl: function(url) {
        this.set({baseUrl: url});
    },
    getCluster: function() {
        return this.get("cluster");
    },
    setCluster: function(cluster) {
        this.set({cluster: cluster});
    },
    initialize: function(attributes, options) {
        if (options && options.baseUrl) {
            this.setBaseUrl(options.baseUrl);
        }
        if (options && options.cluster) {
            this.setCluster(options.cluster);
        }
    }
});

Backbone.Collection = Backbone.Collection.extend({
    sync: function(method, model, options) {
		var _this = this;
        //options.dataType = 'jsonp';
        options.url = "/pallas/esproxy" + this.url();
        var urlbase = this.getBaseUrl();
				var currentHost = urlbase;
		var hosts = urlbase;
		var cluster = this.getCluster();
		var hostList = [currentHost];
		if(hosts.indexOf(",") >= 0){
			var arr = hosts.split(",");
			hostList = [];
			for(var i=0;i<arr.length;i++){
				if(arr[i]){
				    if(arr[i].indexOf("http") < 0){
						arr[i] = "http://"+arr[i];
					}
					hostList.push(arr[i]);
				}
			}
			currentHost = hostList[currentHostIndex];
		}
		
		if(!options.retryCount){
			options.retryCount = 0;
		}
		options.retryCount = options.retryCount + 1;
		
        options.beforeSend = function(xhr){
			xhr.setRequestHeader('es-cluster', cluster);
			xhr.setRequestHeader('es-request', true);
        }
	  
		var error = options.error;
		options.error = function(msg){
			currentHostIndex = (currentHostIndex + 1)%hostList.length;
			if(options.retryCount >= hostList.length){
				error && error(msg);
				return;
			}
			_this.sync(method, model, options);
		}
        return Backbone.sync(method, model, options);
    },
    // Did not find much information about how to store metadata with collection.
    // Direct set/get of property seems to work fine.
    getBaseUrl: function() {
        return this.baseUrl;
    },
    setBaseUrl: function(url) {
        this.baseUrl = url;
    },
    getCluster: function() {
        return this.cluster;
    },
    setCluster: function(cluster) {
        this.cluster = cluster;
    },
    initialize: function(models, options) {
        if (options && options.baseUrl) {
            this.setBaseUrl(options.baseUrl);
        }
        if (options && options.cluster) {
            this.setCluster(options.cluster);
        }
    }
});
