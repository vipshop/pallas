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

var SelectedClusterNodeView = Backbone.View.extend({

    el: "#selectedClusterNode",

//    initialize: function() {
//        console.log("initialize",this);
//    },

    nodeId: function() {
        return this.options.nodeId;
    },

    render: function() {

        // Input is an array of objects having two properties: timestamp and value.
        // It update both properties timestamp and value and removes the first item.
        var normalizedDeltaToSeconds = function(items) {
            for (var i=(items.length - 1); i > 0 ; i--) {
                // delta value
                items[i].value -= items[i-1].value;
                // normalize value to seconds
                items[i].value = items[i].value / (
                    ( items[i].timestamp - items[i-1].timestamp ) <= 1000 ? 1 :
                        ( items[i].timestamp - items[i-1].timestamp ) / 1000
                    );
                // avg timestamp
                items[i].timestamp = Math.round( ( items[i].timestamp + items[i].timestamp ) / 2 );
            }
            items.shift();
        };

		var toThousands= function(num) {
    		return (num || 0).toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
		}

        var delta = function(items) {
            for (var i=(items.length - 1); i > 0 ; i--) {
                items[i].value -= items[i-1].value;
            }
            items.shift();
        };

        var _view = this;
        var nodeInfoModel = this.model.get("nodeInfo");
        var dispatcher = this.model.get("dispatcher");

        nodeInfoModel.fetch({

            nodeId: this.options.nodeId,
            success: function(model, response) {

                var selectedNodeInfo = response;
                var selectedNodeId = _view.options.nodeId;

                dispatcher.trigger("onAjaxResponse", response.cluster_name, "Node > Info", response);

                _view.renderNodeDetail(model);

                // Create all charts
                var chart_jvmThreads = bigdesk_charts.jvmThreads.chart(d3.select("#svg_jvmThreads"));
                var chart_jvmHeapMem = bigdesk_charts.jvmHeapMem.chart(d3.select("#svg_jvmHeapMem"));
                var chart_jvmGC = bigdesk_charts.jvmGC.chart(d3.select("#svg_jvmGC"));

				var chart_threadpoolSearch = bigdesk_charts.threadpoolSearch.chart(d3.select("#svg_threadpoolSearch"));
				var chart_threadpoolIndex = bigdesk_charts.threadpoolIndex.chart(d3.select("#svg_threadpoolIndex"));
				var chart_threadpoolBulk = bigdesk_charts.threadpoolBulk.chart(d3.select("#svg_threadpoolBulk"));
				var chart_threadpoolRefresh = bigdesk_charts.threadpoolRefresh.chart(d3.select("#svg_threadpoolRefresh"));

                var chart_channels = bigdesk_charts.channels.chart(d3.select("#svg_channels"));
                var chart_transport_txrx = bigdesk_charts.transport_txrx.chart(d3.select("#svg_transport_txrx"));

                var chart_indicesSearchReqs = bigdesk_charts.indicesSearchReqs.chart(d3.select("#svg_indicesSearchReqs"));
                var chart_indicesSearchTime = bigdesk_charts.indicesSearchTime.chart(d3.select("#svg_indicesSearchTime"));
                var chart_indicesGetReqs = bigdesk_charts.indicesGetReqs.chart(d3.select("#svg_indicesGetReqs"));
                var chart_indicesGetTime = bigdesk_charts.indicesGetTime.chart(d3.select("#svg_indicesGetTime"));
                var chart_indicesIndexingReqs = bigdesk_charts.indicesIndexingReqs.chart(d3.select("#svg_indicesIndexingReqs"));
                var chart_indicesCacheSize = bigdesk_charts.indicesCacheSize.chart(d3.select("#svg_indicesCacheSize"));
                var chart_indicesCacheEvictions = bigdesk_charts.indicesCacheEvictions.chart(d3.select("#svg_indicesCacheEvictions"));
                var chart_indicesIndexingTime = bigdesk_charts.indicesIndexingTime.chart(d3.select("#svg_indicesIndexingTime"));

                var chart_osMem = bigdesk_charts.osMem.chart(d3.select("#svg_osMem"));
                var chart_osPercent = bigdesk_charts.osPercent.chart(d3.select("#svg_osPercent"));

                var chart_indicesSegments = bigdesk_charts.indicesSegments.chart(d3.select("#svg_indicesSegments"));
                
               var nodesStatsCollection = _view.model.get("nodesStats");

                // function to update all node stats charts
                var updateCharts = function() {

                    // should the charts be animated?
                    var animatedCharts = $("#animatedCharts").prop("checked");
                    if (!animatedCharts) { animatedCharts = false; }

                    // get stats for selected node
                    var stats = [];
                    var stats_the_latest = undefined;

                    _.defer(function(){
                        stats = nodesStatsCollection.map(function(snapshot){
                            if (snapshot.get("nodes").get(_view.nodeId()))
                                return {
                                    id: snapshot.id, // this is timestamp
                                    node: snapshot.get("nodes").get(_view.nodeId()).toJSON()
                                }
                        });

                        stats = _.filter(stats, function(item){ return (item!=undefined)});

                        stats_the_latest = stats[stats.length - 1];

                        dispatcher.trigger("onNewData", "the latest node stats:", stats_the_latest);

                    });

                    // --------------------------------------------
                    // Channels

                    _.defer(function(){
                        var opened_http_channels = bigdesk_charts.channels.series1(stats);
                        var opened_transport_server_channels = bigdesk_charts.channels.series2(stats);

                        var theLatestTotalOpened = stats[stats.length-1].node.http.total_opened;

                        try { chart_channels.animate(animatedCharts).update(opened_http_channels, opened_transport_server_channels); } catch (ignore) {}

                        if (opened_http_channels.length > 0) {
                            $("#open_http_channels").text(opened_http_channels[opened_http_channels.length-1].value);
                        }
                        if (opened_transport_server_channels.length > 0) {
                            $("#open_transport_channels").text(opened_transport_server_channels[opened_transport_server_channels.length-1].value);
                        }
                        if (theLatestTotalOpened) {
                            $("#total_opened_http_channels").text(theLatestTotalOpened);
                        }
                    });

                    // --------------------------------------------
                    // JVM Info

                    _.defer(function(){
                        if (stats_the_latest && stats_the_latest.node) {
                            $("#jvm_uptime").text(stats_the_latest.node.jvm.uptime);
                        } else {
                            $("#jvm_uptime").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // JVM Threads

                    _.defer(function(){
                        var jvm_threads_count = bigdesk_charts.jvmThreads.series1(stats);
                        var jvm_threads_peak_count = bigdesk_charts.jvmThreads.series2(stats);

                        try { chart_jvmThreads.animate(animatedCharts).update(jvm_threads_count, jvm_threads_peak_count); } catch (ignore) {}

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#jvm_threads_peak").text(stats_the_latest.node.jvm.threads.peak_count);
                            $("#jvm_threads_count").text(stats_the_latest.node.jvm.threads.count);
                        } else {
                            $("#jvm_threads_peak").text("n/a");
                            $("#jvm_threads_count").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // JVM GC

                    _.defer(function(){
                        var jvm_gc_young_collection_count_delta = bigdesk_charts.jvmGC.series1(stats);
                        var jvm_gc_old_collection_count_delta = bigdesk_charts.jvmGC.series2(stats);
                        var jvm_gc_both_collection_time_delta = bigdesk_charts.jvmGC.series3(stats);
                        if (jvm_gc_old_collection_count_delta.length > 1 && jvm_gc_young_collection_count_delta.length > 1 && jvm_gc_both_collection_time_delta.length > 1) {

                            delta(jvm_gc_old_collection_count_delta);
                            delta(jvm_gc_young_collection_count_delta);
                            delta(jvm_gc_both_collection_time_delta);

                            try {
								chart_jvmGC.animate(animatedCharts).update(
									jvm_gc_young_collection_count_delta,
									jvm_gc_old_collection_count_delta,
									jvm_gc_both_collection_time_delta);
							} catch (ignore) {}
                        }

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#jvm_gc_time").text(
								stats_the_latest.node.jvm.gc.collectors.old.collection_time_in_millis + "ms / " + stats_the_latest.node.jvm.gc.collectors.young.collection_time_in_millis + "ms"
							);
                            $("#jvm_gc_count").text(
								stats_the_latest.node.jvm.gc.collectors.old.collection_count + " / " + stats_the_latest.node.jvm.gc.collectors.young.collection_count
							);
                        } else {
                            $("#jvm_gc_time").text("n/a");
                            $("#jvm_gc_count").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // JVM Heap Mem

                    _.defer(function(){
                        var jvm_heap_used_mem= bigdesk_charts.jvmHeapMem.series1(stats);
                        var jvm_heap_committed_mem= bigdesk_charts.jvmHeapMem.series2(stats);

                        try { chart_jvmHeapMem.animate(animatedCharts).update(jvm_heap_used_mem, jvm_heap_committed_mem); } catch (ignore) {}

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#jvm_heap_mem_committed").text(stats_the_latest.node.jvm.mem.heap_committed);
                            $("#jvm_heap_mem_used").text(stats_the_latest.node.jvm.mem.heap_used);
                        } else {
                            $("#jvm_heap_mem_committed").text("n/a");
                            $("#jvm_heap_mem_used").text("n/a");
                        }
                    });

					// --------------------------------------------
					// Threadpool Search

                    _.defer(function(){
                        var threadpool_search_count = bigdesk_charts.threadpoolSearch.series1(stats);
                        var threadpool_search_peak = bigdesk_charts.threadpoolSearch.series2(stats);
                        var threadpool_search_queue = bigdesk_charts.threadpoolSearch.series3(stats);

                        try { chart_threadpoolSearch.animate(animatedCharts).update(threadpool_search_count, threadpool_search_peak, threadpool_search_queue); } catch (ignore) {}

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#tp_search_count").text(stats_the_latest.node.thread_pool.search.active);
                            $("#tp_search_peak").text(stats_the_latest.node.thread_pool.search.largest);
                            $("#tp_search_queue").text(stats_the_latest.node.thread_pool.search.queue);
                        } else {
                            $("#tp_search_count").text("n/a");
                            $("#tp_search_peak").text("n/a");
                            $("#tp_search_queue").text("n/a");
                        }
                    });

					// --------------------------------------------
					// Threadpool Index

                    _.defer(function(){
                        var threadpool_index_count = bigdesk_charts.threadpoolIndex.series1(stats);
                        var threadpool_index_peak = bigdesk_charts.threadpoolIndex.series2(stats);
                        var threadpool_index_queue = bigdesk_charts.threadpoolIndex.series3(stats);

                        try { chart_threadpoolIndex.animate(animatedCharts).update(threadpool_index_count, threadpool_index_peak, threadpool_index_queue); } catch (ignore) {}

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#tp_index_count").text(stats_the_latest.node.thread_pool.index.active);
                            $("#tp_index_peak").text(stats_the_latest.node.thread_pool.index.largest);
                            $("#tp_index_queue").text(stats_the_latest.node.thread_pool.index.queue);
                        } else {
                            $("#tp_index_count").text("n/a");
                            $("#tp_index_peak").text("n/a");
                            $("#tp_index_queue").text("n/a");
                        }
                    });

					// --------------------------------------------
					// Threadpool Bulk

                    _.defer(function(){
                        var threadpool_bulk_count = bigdesk_charts.threadpoolBulk.series1(stats);
                        var threadpool_bulk_peak = bigdesk_charts.threadpoolBulk.series2(stats);
                        var threadpool_bulk_queue = bigdesk_charts.threadpoolBulk.series3(stats);

                        try { chart_threadpoolBulk.animate(animatedCharts).update(threadpool_bulk_count, threadpool_bulk_peak, threadpool_bulk_queue); } catch (ignore) {}

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#tp_bulk_count").text(stats_the_latest.node.thread_pool.bulk.active);
                            $("#tp_bulk_peak").text(stats_the_latest.node.thread_pool.bulk.largest);
                            $("#tp_bulk_queue").text(stats_the_latest.node.thread_pool.bulk.queue);
                        } else {
                            $("#tp_bulk_count").text("n/a");
                            $("#tp_bulk_peak").text("n/a");
                            $("#tp_bulk_queue").text("n/a");
                        }
                    });

					// --------------------------------------------
					// Threadpool Refresh

                    _.defer(function(){
                        var threadpool_refresh_count = bigdesk_charts.threadpoolRefresh.series1(stats);
                        var threadpool_refresh_peak = bigdesk_charts.threadpoolRefresh.series2(stats);
                        var threadpool_refresh_queue = bigdesk_charts.threadpoolRefresh.series3(stats);

                        try { chart_threadpoolRefresh.animate(animatedCharts).update(threadpool_refresh_count, threadpool_refresh_peak, threadpool_refresh_queue); } catch (ignore) {}

                        if (stats_the_latest && stats_the_latest.node) {
                            $("#tp_refresh_count").text(stats_the_latest.node.thread_pool.refresh.active);
                            $("#tp_refresh_peak").text(stats_the_latest.node.thread_pool.refresh.largest);
                            $("#tp_refresh_queue").text(stats_the_latest.node.thread_pool.refresh.queue);
                        } else {
                            $("#tp_refresh_count").text("n/a");
                            $("#tp_refresh_peak").text("n/a");
                            $("#tp_refresh_queue").text("n/a");
                        }
                    });


                    // --------------------------------------------
                    // OS Info

                    _.defer(function(){
                        if (stats_the_latest && stats_the_latest.node) {
                            $("#file_descriptors").text(stats_the_latest.node.process.open_file_descriptors+" / "+stats_the_latest.node.process.max_file_descriptors);
                            $("#load_average").text(stats_the_latest.node.os.load_average);
                        } else {
                            $("#file_descriptors").text("n/a");
                            $("#load_average").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // OS Mem

                    _.defer(function(){
                        // sigar & AWS check
                        if (stats_the_latest && stats_the_latest.node && stats_the_latest.node.os && stats_the_latest.node.os.mem) {

                            var os_mem_actual_used = bigdesk_charts.osMem.series1(stats);
                            var os_mem_actual_free = bigdesk_charts.osMem.series2(stats);

                            try { chart_osMem.animate(animatedCharts).update(os_mem_actual_used, os_mem_actual_free); } catch (ignore) {}

                            $("#os_mem_free").text(stats_the_latest.node.os.mem.free);
                            $("#os_mem_used").text(stats_the_latest.node.os.mem.used);
                        } else {
                            chart_osMem = bigdesk_charts.not_available.chart(chart_osMem.svg());
                            $("#os_mem_free").text("n/a");
                            $("#os_mem_used").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // OS percent

                    _.defer(function(){
                        if (stats_the_latest && stats_the_latest.node && stats_the_latest.node.os && stats_the_latest.node.process) {

                            var os_cpu_percent = bigdesk_charts.osPercent.series1(stats);
                            var os_mem_percent = bigdesk_charts.osPercent.series2(stats);
                            var os_100_percent = bigdesk_charts.osPercent.series3(stats);

                            try { chart_osPercent.animate(animatedCharts).update(os_cpu_percent, os_mem_percent,os_100_percent); } catch (ignore) {}

                            $("#cpu_percent").text(stats_the_latest.node.process.cpu.percent+"%");
                            $("#mem_percent").text(stats_the_latest.node.os.mem.used_percent+"%");
                        } else {
                            chart_osPercent = bigdesk_charts.not_available.chart(chart_osPercent.svg());
                            $("#cpu_percent").text("n/a");
                            $("#mem_percent").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // Indices

                    _.defer(function(){
                        if (stats_the_latest && stats_the_latest.node) {
	                        var indices_the_last=_view.model.get("indicesStatus");
	                        indices_the_last=indices_the_last.at(indices_the_last.length-1).toJSON();
	                        
                            $("#indices_docs_count").text(toThousands(stats_the_latest.node.indices.docs.count) + " / " + toThousands(indices_the_last.indices.docs.count));
                            $("#indices_docs_deleted").text(stats_the_latest.node.indices.docs.deleted + " / " + indices_the_last.indices.docs.deleted);
                            $("#indices_store_size").text(stats_the_latest.node.indices.store.size + " / " + indices_the_last.indices.store.size);
                            $("#indices_flush_total").text(stats_the_latest.node.indices.flush.total + ", " + stats_the_latest.node.indices.flush.total_time);
                            $("#indices_refresh_total").text(stats_the_latest.node.indices.refresh.total + ", " + stats_the_latest.node.indices.refresh.total_time);
                        } else {
                            $("#indices_docs_count").text("n/a");
                            $("#indices_docs_deleted").text("n/a");
                            $("#indices_store_size").text("n/a");
                            $("#indices_flush_total").text("n/a");
                            $("#indices_refresh_total").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // Indices: segments count

                    _.defer(function(){
                        var indices_segments_count = bigdesk_charts.indicesSegments.series1(stats);
                        var shards_count = bigdesk_charts.indicesSearchReqs.series2(stats);
                        if (indices_segments_count.length > 1 && shards_count.length > 1) {
	                        var clusterState_the_last=_view.model.get("clusterState");
	                        clusterState_the_last=clusterState_the_last.at(clusterState_the_last.length-1).toJSON();
  		                    var asc=clusterState_the_last.routing_nodes.nodes[_view.nodeId()].length;
  		                    //hack set value
  		                    for(ii=0;ii<shards_count.length;ii++)
  		                    	shards_count[ii].value=asc;
                            try { chart_indicesSegments.animate(animatedCharts).update(indices_segments_count, shards_count); } catch (ignore) {}
                            $("#indices_segments_count").text(stats_the_latest.node.indices.segments.count);
                            $("#shards_count").text(asc);
                        }
                    });
                    
                    // --------------------------------------------
                    // Indices: search requests

                    _.defer(function(){
                        var indices_fetch_reqs = bigdesk_charts.indicesSearchReqs.series1(stats);
                        var indices_query_reqs = bigdesk_charts.indicesSearchReqs.series2(stats);

                        if (indices_fetch_reqs.length > 1 && indices_query_reqs.length > 1) {

                            normalizedDeltaToSeconds(indices_fetch_reqs);
                            normalizedDeltaToSeconds(indices_query_reqs);

                            try { chart_indicesSearchReqs.animate(animatedCharts).update(indices_fetch_reqs, indices_query_reqs); } catch (ignore) {}

                            $("#indices_search_query_reqs").text(stats_the_latest.node.indices.search.query_total);
                            $("#indices_search_fetch_reqs").text(stats_the_latest.node.indices.search.fetch_total);
                        }
                    });

                    // --------------------------------------------
                    // Indices: search time

                    _.defer(function(){
                        var indices_fetch_time = bigdesk_charts.indicesSearchTime.series1(stats);
                        var indices_query_time = bigdesk_charts.indicesSearchTime.series2(stats);

                        if (indices_fetch_time.length > 1 && indices_query_time.length > 1) {

                            normalizedDeltaToSeconds(indices_fetch_time);
                            normalizedDeltaToSeconds(indices_query_time);

                            try { chart_indicesSearchTime.animate(animatedCharts).update(indices_fetch_time, indices_query_time); } catch (ignore) {}

                            $("#indices_search_query_time").text(stats_the_latest.node.indices.search.query_time);
                            $("#indices_search_fetch_time").text(stats_the_latest.node.indices.search.fetch_time);
                        }
                    });

                    // --------------------------------------------
                    // Indices: get requests

                    _.defer(function(){
                        var indices_get_reqs = bigdesk_charts.indicesGetReqs.series1(stats);
                        var indices_missing_reqs = bigdesk_charts.indicesGetReqs.series2(stats);
                        var indices_exists_reqs = bigdesk_charts.indicesGetReqs.series3(stats);

                        if (indices_get_reqs.length > 1 && indices_missing_reqs.length > 1 && indices_exists_reqs.length > 1) {

                            normalizedDeltaToSeconds(indices_get_reqs);
                            normalizedDeltaToSeconds(indices_missing_reqs);
                            normalizedDeltaToSeconds(indices_exists_reqs);

                            try { chart_indicesGetReqs.animate(animatedCharts).update(indices_get_reqs, indices_missing_reqs, indices_exists_reqs); } catch (ignore) {}

                            $("#indices_get_reqs").text(stats_the_latest.node.indices.get.total);
                            $("#indices_exists_reqs").text(stats_the_latest.node.indices.get.exists_total);
                            $("#indices_missing_reqs").text(stats_the_latest.node.indices.get.missing_total);
                        }
                    });

                    // --------------------------------------------
                    // Indices: get time

                    _.defer(function(){
                        var indices_get_time = bigdesk_charts.indicesGetTime.series1(stats);
                        var indices_missing_time = bigdesk_charts.indicesGetTime.series2(stats);
                        var indices_exists_time = bigdesk_charts.indicesGetTime.series3(stats);

                        if (indices_get_time.length > 1 && indices_missing_time.length > 1 && indices_exists_time.length > 1) {

                            normalizedDeltaToSeconds(indices_get_time);
                            normalizedDeltaToSeconds(indices_missing_time);
                            normalizedDeltaToSeconds(indices_exists_time);

                            try { chart_indicesGetTime.animate(animatedCharts).update(indices_get_time, indices_missing_time, indices_exists_time); } catch (ignore) {}

                            $("#indices_get_time").text(stats_the_latest.node.indices.get.get_time);
                            $("#indices_exists_time").text(stats_the_latest.node.indices.get.exists_time);
                            $("#indices_missing_time").text(stats_the_latest.node.indices.get.missing_time);
                        }
                    });

                    // --------------------------------------------
                    // Indices: indexing requests

                    _.defer(function(){
                        var indices_indexing_index_reqs = bigdesk_charts.indicesIndexingReqs.series1(stats);
                        var indices_indexing_delete_reqs = bigdesk_charts.indicesIndexingReqs.series2(stats);

                        if (indices_indexing_index_reqs.length > 1 && indices_indexing_delete_reqs.length > 1) {

                            normalizedDeltaToSeconds(indices_indexing_index_reqs);
                            normalizedDeltaToSeconds(indices_indexing_delete_reqs);

                            try { chart_indicesIndexingReqs.animate(animatedCharts).update(indices_indexing_index_reqs, indices_indexing_delete_reqs); } catch (ignore) {}

                            $("#indices_indexing_delete_reqs").text(stats_the_latest.node.indices.indexing.delete_total);
                            $("#indices_indexing_index_reqs").text(stats_the_latest.node.indices.indexing.index_total);
                        }
                    });

                    // --------------------------------------------
                    // Indices: indexing time

                    _.defer(function(){
                        var indices_indexing_index_time = bigdesk_charts.indicesIndexingTime.series1(stats);
                        var indices_indexing_delete_time = bigdesk_charts.indicesIndexingTime.series2(stats);

                        if (indices_indexing_index_time.length > 1 && indices_indexing_delete_time.length > 1) {

                            normalizedDeltaToSeconds(indices_indexing_index_time);
                            normalizedDeltaToSeconds(indices_indexing_delete_time);

                            try { chart_indicesIndexingTime.animate(animatedCharts).update(indices_indexing_index_time, indices_indexing_delete_time); } catch (ignore) {}

                            $("#indices_indexing_delete_time").text(stats_the_latest.node.indices.indexing.delete_time);
                            $("#indices_indexing_index_time").text(stats_the_latest.node.indices.indexing.index_time);
                        }
                    });

                    // --------------------------------------------
                    // Indices: cache size

                    _.defer(function(){
                        var indices_fielddata_cache_size = bigdesk_charts.indicesCacheSize.series1(stats);
                        var indices_query_cache_size = bigdesk_charts.indicesCacheSize.series2(stats);
                        var indices_request_cache_size = bigdesk_charts.indicesCacheSize.series3(stats);

                        try { chart_indicesCacheSize.animate(animatedCharts)
							.update(indices_fielddata_cache_size,indices_query_cache_size,indices_request_cache_size);
						} catch (ignore) {}

                        if (stats_the_latest.node && stats_the_latest.node.indices) {
                            $("#indices_fielddata_cache_size").text(stats_the_latest.node.indices.fielddata.memory_size);
                            $("#indices_query_cache_size").text(stats_the_latest.node.indices.query_cache.memory_size);
                            $("#indices_request_cache_size").text(stats_the_latest.node.indices.request_cache == undefined ? "n/a" : stats_the_latest.node.indices.request_cache.memory_size);
                        } else {
	                        chart_indicesCacheSize = bigdesk_charts.not_available.chart(chart_indicesCacheSize.svg());
                            $("#indices_fielddata_cache_size").text("n/a");
                            $("#indices_query_cache_size").text("n/a");
                            $("#indices_request_cache_size").text("n/a");
                        }
                    });

                    // --------------------------------------------
                    // Indices: cache evictions

                    _.defer(function(){
                        var indices_cache_fielddata_evictions= bigdesk_charts.indicesCacheEvictions.series1(stats);
                        var indices_cache_query_evictions = bigdesk_charts.indicesCacheEvictions.series2(stats);
                        var indices_cache_request_evictions = bigdesk_charts.indicesCacheEvictions.series3(stats);

                        if (indices_cache_fielddata_evictions.length > 1 && indices_cache_query_evictions.length > 1) {

                            normalizedDeltaToSeconds(indices_cache_fielddata_evictions);
                            normalizedDeltaToSeconds(indices_cache_query_evictions);
                            normalizedDeltaToSeconds(indices_cache_request_evictions);

                            try { chart_indicesCacheEvictions.animate(animatedCharts).update(indices_cache_fielddata_evictions, indices_cache_query_evictions,indices_cache_request_evictions); } catch (ignore) {}

                            $("#indices_cache_fielddata_evictions").text(stats_the_latest.node.indices.fielddata.evictions);
                            $("#indices_cache_query_evictions").text(stats_the_latest.node.indices.query_cache.evictions);
                            $("#indices_cache_request_evictions").text(stats_the_latest.node.indices.request_cache== undefined ? "n/a" : stats_the_latest.node.indices.request_cache.evictions);

                        }
                    });

                    // Transport: Tx Rx

                    _.defer(function(){
						if (stats_the_latest && stats_the_latest.node && stats_the_latest.node.transport) {
	                        var calcType = $("#transport_avg_calc_type").find(":selected").val();

	                        var transport_tx_delta = bigdesk_charts.transport_txrx.series1(stats);
	                        var transport_rx_delta = bigdesk_charts.transport_txrx.series2(stats);

	                        if (transport_tx_delta.length > 1 && transport_rx_delta.length > 1) {

	                            if (calcType == "weighted") {
	                                normalizedDeltaToSeconds(transport_tx_delta);
	                                normalizedDeltaToSeconds(transport_rx_delta);
	                            } else {
	                                delta(transport_tx_delta);
	                                delta(transport_rx_delta);
	                            }

	                            try { chart_transport_txrx.animate(animatedCharts).update(transport_tx_delta, transport_rx_delta); } catch (ignore) {}
	                        }
	                        var _t = stats_the_latest.node.transport;
	                        if (_t && _t.rx_size && _t.tx_size && _t.rx_count != undefined && _t.tx_count != undefined) {
	                            $("#transport_rx_size").text(stats_the_latest.node.transport.rx_size);
	                            $("#transport_tx_size").text(stats_the_latest.node.transport.tx_size);
	                            $("#transport_rx_count").text(stats_the_latest.node.transport.rx_count);
	                            $("#transport_tx_count").text(stats_the_latest.node.transport.tx_count);
	                        } else {
	                            chart_transport_txrx = bigdesk_charts.not_available.chart(chart_transport_txrx.svg());
	                            $("#transport_rx_size").text("n/a");
	                            $("#transport_tx_size").text("n/a");
	                            $("#transport_rx_count").text("n/a");
	                            $("#transport_tx_count").text("n/a");
	                        }
	                    }
                    });

                    // --------------------------------------------
                    // File system:

                    _.defer(function(){
                        if (stats_the_latest && stats_the_latest.node && stats_the_latest.node.fs && stats_the_latest.node.fs.data && stats_the_latest.node.fs.data.length > 0) {

                            var fs_section = $("#FileSystemSection");
                            var _fs_data_info = $("#fs_data_info");
                            _fs_data_info.empty();

                            var keys = _.keys(stats_the_latest.node.fs.data).sort();

                            if (keys.length > 0) {
                                for (var i = 0; i < keys.length; i++) {

                                    var fs_data = stats_the_latest.node.fs.data[keys[i]];
                                    // we need to keep key value for mustache processing
                                    fs_data.key = [keys[i]];
                                    var _fd_element = $("#fd_data_"+keys[i]);

                                    if (_fd_element.length == 0) {

                                        // render the row
                                        var fsInfo = Mustache.render(templates.selectedClusterNode.fsDataInfoTemplate, fs_data);
                                        var fsInfo_cnt = Mustache.render(templates.selectedClusterNode.fsDataInfo_cntTemplate, fs_data);
                                        var fsInfo_size = Mustache.render(templates.selectedClusterNode.fsDataInfo_sizeTemplate, fs_data);

                                        var fsp_data = _view.make("p", {}, fsInfo);
                                        var fsCol_data = _view.make("div", {"class":"twelvecol last"});

                                        var rowFsInfo = _view.make("div", {"class":"row nodeDetail", "id":"fd_data_" + keys[i]});

                                        $(rowFsInfo).append(fsCol_data);
                                        $(fsCol_data).append(fsp_data);
                                        
                                        fs_section.after(rowFsInfo);
                                    }

                                    $("#fs_disk_free_"+keys[i]).text(fs_data.free);
                                    $("#fs_disk_available_"+keys[i]).text(fs_data.available);
                               }
                            } else {
                            }
                        } else {
                            $("#fs_data_info").text("No data info available");
                        }
                    });

                };

                // add custom listener for the collection to update UI and charts on changes
                nodesStatsCollection.on("nodesStatsUpdated", function(){
                    updateCharts();
                });

                // update charts right now, do not wait for the nearest execution
                // of update interval to show the charts to the user
                updateCharts();
            }
        });
    },

    renderNodeDetail: function(model) {

        // Node info
        var jsonModel = model.toJSON();

        // Summary title

        var jvmTitleP = this.make("p", {}, "<h2>Summary</h2>");
        var jvmTitleCol = this.make("div", {"class":"twelvecol last"});
        var rowJvmTitle = this.make("div", {"class":"row nodeDetail newSection"});

        $(rowJvmTitle).append(jvmTitleCol);
        $(jvmTitleCol).append(jvmTitleP);

        // Summary detail row

        var jvmInfo1 = Mustache.render(templates.selectedClusterNode.summaryInfoTemplate, jsonModel);

        var jvmp1 = this.make("p", {}, jvmInfo1);

        var jvmCol1 = this.make("div", {"class":"twelvecol last"});

        var rowJvmInfo = this.make("div", {"class":"row nodeDetail", "id":"jvmInfo"});

        $(rowJvmInfo).append(jvmCol1);
        $(jvmCol1).append(jvmp1);

        // Summary row for charts
        var osPercent = Mustache.render(templates.selectedClusterNode.osPercent, jsonModel);
        var jvmHeapMem = Mustache.render(templates.selectedClusterNode.jvmHeapMem, jsonModel);

        var jvmpCharts1 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_osPercent' clip_id='clip_osPercent' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_jvmHeapMem' clip_id='clip_jvmHeapMem' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + osPercent + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + jvmHeapMem + "</div>" +
            "</div>"
        );

        var jvmGC = Mustache.render(templates.selectedClusterNode.jvmGC, jsonModel);
        var indicesSegments = Mustache.render(templates.selectedClusterNode.indicesSegments, jsonModel);

        var jvmpCharts2 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_jvmGC' clip_id='clip_jvmGC' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_indicesSegments' clip_id='clip_indicesSegments' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + jvmGC + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + indicesSegments + "</div>" +
            "</div>"
        );

        var jvmColCharts1 = this.make("div", {"class":"sixcol"});
        var jvmColCharts2 = this.make("div", {"class":"sixcol last"});

        var rowJvmCharts = this.make("div", {"class":"row nodeDetail"});

        $(rowJvmCharts).append(jvmColCharts1, jvmColCharts2);
        $(jvmColCharts1).append(jvmpCharts1);
        $(jvmColCharts2).append(jvmpCharts2);

        // ThreadPool title

        var tpTitleP = this.make("p", {}, "<h2>Thread Pools</h2>");
        var tpTitleCol = this.make("div", {"class":"twelvecol last"});
        var rowtpTitle = this.make("div", {"class":"row nodeDetail newSection"});

        $(rowtpTitle).append(tpTitleCol);
        $(tpTitleCol).append(tpTitleP);

		// Threadpool row for charts

        var tpSearch = Mustache.render(templates.selectedClusterNode.threadPoolSearch, jsonModel);
        var tpIndex = Mustache.render(templates.selectedClusterNode.threadPoolIndex, jsonModel);

        var tppCharts1 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_threadpoolSearch' clip_id='clip_threadpoolSearch' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_threadpoolIndex' clip_id='clip_threadpoolIndex' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + tpSearch + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + tpIndex + "</div>" +
            "</div>"
        );

		var tpBulk = Mustache.render(templates.selectedClusterNode.threadPoolBulk, jsonModel);
        var tpRefresh = Mustache.render(templates.selectedClusterNode.threadPoolRefresh, jsonModel);

        var tppCharts2 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_threadpoolBulk' clip_id='clip_threadpoolBulk' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_threadpoolRefresh' clip_id='clip_threadpoolRefresh' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + tpBulk + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + tpRefresh + "</div>" +
            "</div>"
        );

        var tpColCharts1 = this.make("div", {"class":"sixcol"});
        var tpColCharts2 = this.make("div", {"class":"sixcol last"});

        var rowTpCharts = this.make("div", {"class":"row nodeDetail"});

        $(rowTpCharts).append(tpColCharts1, tpColCharts2);
        $(tpColCharts1).append(tppCharts1);
        $(tpColCharts2).append(tppCharts2);

        // OS title

        var osTitleP = this.make("p", {}, "<h2>OS & JVM & Process & Transport</h2>");
        var osTitleCol = this.make("div", {"class":"twelvecol last"});
        var rowOsTitle = this.make("div", {"class":"row nodeDetail newSection"});

        $(rowOsTitle).append(osTitleCol);
        $(osTitleCol).append(osTitleP);

        // OS detail row
        var osInfo1 = Mustache.render(templates.selectedClusterNode.osInfoTemplate, jsonModel);

        var osp1 = this.make("p", {}, osInfo1);

        var osCol1 = this.make("div", {"class":"twelvecol last"});

        var rowOSInfo = this.make("div", {"class":"row nodeDetail", "id":"osInfo"});

        $(rowOSInfo).append(osCol1);
        $(osCol1).append(osp1);

        // OS row for charts
        var jvmThreads = Mustache.render(templates.selectedClusterNode.jvmThreads, jsonModel);
        var osMem = Mustache.render(templates.selectedClusterNode.osMem, jsonModel);

        var osCharts1 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_jvmThreads' clip_id='clip_jvmThreads' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_osMem' clip_id='clip_osMem' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + jvmThreads + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + osMem + "</div>" +
            "</div>"
        );


        var channels = Mustache.render(templates.selectedClusterNode.channelsTemplate, {});
        var transportRxTx = Mustache.render(templates.selectedClusterNode.transportRxTx, {});

        var avgTransportCalcType = Mustache.render(templates.avgCalculationType, { id: "transport_avg_calc_type" });
        transportRxTx = transportRxTx.replace("<!--#-->", avgTransportCalcType);

        var osCharts2 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_channels' clip_id='clip_channels' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_transport_txrx' clip_id='clip_transport_txrx' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>"+channels+"</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>"+transportRxTx+"</div>" +
            "</div>"
        );

        var osColCharts1 = this.make("div", {"class":"sixcol"});
        var osColCharts2 = this.make("div", {"class":"sixcol last"});

        var rowOsCharts = this.make("div", {"class":"row nodeDetail"});

        $(rowOsCharts).append(osColCharts1, osColCharts2);
        $(osColCharts1).append(osCharts1);
        $(osColCharts2).append(osCharts2);

        // Indices title

        var indicesTitleP = this.make("p", {}, "<h2>Indices</h2>");
        var indicesTitleCol = this.make("div", {"class":"twelvecol last"});
        var rowIndicesTitle = this.make("div", {"class":"row nodeDetail newSection"});

        $(rowIndicesTitle).append(indicesTitleCol);
        $(indicesTitleCol).append(indicesTitleP);

        // Indices info row
        var indicesInfo = Mustache.render(templates.selectedClusterNode.indicesTemplate, {});
        var indicesInfoP = this.make("p", {}, indicesInfo);

        var indicesInfoCol = this.make("div", {"class":"twelvecol last"});

        var rowIndicesInfo = this.make("div", {"class":"row nodeDetail"});

        $(rowIndicesInfo).append(indicesInfoCol);
        $(indicesInfoCol).append(indicesInfoP);

        // Indices charts row #1

        var indicesSearchReqs = Mustache.render(templates.selectedClusterNode.indicesSearchReqsTemplate, jsonModel);
        var indicesSearchTime = Mustache.render(templates.selectedClusterNode.indicesSearchTimeTemplate, jsonModel);

        var indicesCharts1p1 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_indicesSearchReqs' clip_id='clip_indicesSearchReqs' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_indicesSearchTime' clip_id='clip_indicesSearchTime' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + indicesSearchReqs + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + indicesSearchTime + "</div>" +
            "</div>"
        );

        var indicesIndexingReqs = Mustache.render(templates.selectedClusterNode.indicesIndexingReqsTemplate, jsonModel);
        var indicesIndexingTime = Mustache.render(templates.selectedClusterNode.indicesIndexingTimeTemplate, jsonModel);

        var indicesCharts1p2 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_indicesIndexingReqs' clip_id='clip_indicesIndexingReqs' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_indicesIndexingTime' clip_id='clip_indicesIndexingTime' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + indicesIndexingReqs + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + indicesIndexingTime + "</div>" +
            "</div>"
        );
        
        var indicesCharts1Col1 = this.make("div", {"class":"sixcol"});
        var indicesCharts1Col2 = this.make("div", {"class":"sixcol last"});

        var rowIndicesCharts1 = this.make("div", {"class":"row nodeDetail"});
        $(rowIndicesCharts1).append(indicesCharts1Col1, indicesCharts1Col2);
        $(indicesCharts1Col1).append(indicesCharts1p1);
        $(indicesCharts1Col2).append(indicesCharts1p2);

        // Indices charts row #2

        var indicesCacheSize = Mustache.render(templates.selectedClusterNode.indicesCacheSizeTemplate, jsonModel);
        var indicesCacheEvictions = Mustache.render(templates.selectedClusterNode.indicesCacheEvictionsTemplate, jsonModel);

        var indicesCharts2p1 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_indicesCacheSize' clip_id='clip_indicesCacheSize' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_indicesCacheEvictions' clip_id='clip_indicesCacheEvictions' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + indicesCacheSize + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + indicesCacheEvictions + "</div>" +
            "</div>"
        );

        var indicesGetReqs = Mustache.render(templates.selectedClusterNode.indicesGetReqsTemplate, jsonModel);
        var indicesGetTime = Mustache.render(templates.selectedClusterNode.indicesGetTimeTemplate, jsonModel);

        var indicesCharts2p2 = this.make("p", {},
            "<div style='overflow: auto;'>" +
                "<svg width='100%' height='160' class='whitebg'>" +
                    "<svg id='svg_indicesGetReqs' clip_id='clip_indicesGetReqs' width='46.5%' height='100%' x='0' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                    "<svg id='svg_indicesGetTime' clip_id='clip_indicesGetTime' width='46.5%' height='100%' x='54%' y='0' preserveAspectRatio='xMinYMid' viewBox='0 0 270 160'/>" +
                "</svg>" +
                "<div width='46.5%' style='margin-left: 0%; float: left;'>" + indicesGetReqs + "</div>" +
                "<div width='46.5%' style='margin-left: 54%;'>" + indicesGetTime + "</div>" +
            "</div>"
        );

        var indicesCharts2Col1 = this.make("div", {"class":"sixcol"});
        var indicesCharts2Col2 = this.make("div", {"class":"sixcol last"});

        var rowIndicesCharts2 = this.make("div", {"class":"row nodeDetail"});
        $(rowIndicesCharts2).append(indicesCharts2Col1, indicesCharts2Col2);
        $(indicesCharts2Col1).append(indicesCharts2p1);
        $(indicesCharts2Col2).append(indicesCharts2p2);

        // File system title

        var fsTitleP = this.make("p", {}, "<h2>File system</h2>");
        var fsTitleCol = this.make("div", {"class":"twelvecol last"});
        var rowFsTitle = this.make("div", {"class":"row nodeDetail newSection", "id":"FileSystemSection"});

        $(rowFsTitle).append(fsTitleCol);
        $(fsTitleCol).append(fsTitleP);

        this.$el.parent().append(

            rowJvmTitle,
            rowJvmInfo,
            rowJvmCharts,

            rowIndicesTitle,
            rowIndicesInfo,
            rowIndicesCharts1,
            rowIndicesCharts2,

			rowtpTitle,
			rowTpCharts,

            rowOsTitle,
            rowOSInfo,
            rowOsCharts,

            rowFsTitle

        );
    },

    clear: function() {
        this.$el.parent().find("div.row.nodeDetail").remove();

    },

    destroy: function() {

        // remove custom listeners first
        var nodesStatsCollection = this.model.get("nodesStats");
        nodesStatsCollection.off("nodesStatsUpdated");

        this.clear();
        this.undelegateEvents();
    }
});
