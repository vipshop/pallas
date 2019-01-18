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

var templates = {

    avgCalculationType: [
        "<form>",
            "Series: ",
            "<select id='{{id}}'>",
                "<option value='avg'>average</option>",
                "<option value='weighted' selected='selected'>weighted avg</option>",
            "</select>",
        "</form>"
    ].join(""),

    nodesViewTemplate: [
        "<div class='row'>",
            "<div class='threecol'>",
                "<p id='clusterHealth'></p>",
            "</div>",
            "<div class='ninecol last'>",
                "<p id='clusterNodes'></p>",
            "</div>",
        "</div>",
        "<div id='selectedClusterNode' class='row invisible'>",
            "<div class='twelvecol last'/>",
        "</div>"
    ].join(""),

    clusterViewTemplate: [
        "<div class='row'>",
            "<div class='threecol'>",
                "<p id='clusterHealth'></p>",
            "</div>",
            "<div class='ninecol last'>",
                "<p id='clusterNodesAllocation'></p>",
            "</div>",
        "</div>",
        "<div class='row'>",
            "<div class='twelvecol last'>",
                "<h2>Experimental cluster Pack diagram:</h2>",
            "</div>",
        "</div>",
        "<div class='row'>",
            "<div class='twelvecol last'>",
                "<p id='clusterChart'>Loading cluster chart...</p>",
            "</div>",
        "</div>"
    ].join(""),

    selectedClusterNode : {

        jvmHeapMem: [
            "<div>Committed: <span id='jvm_heap_mem_committed'>n/a</span></div>",
            "<div>Used: <span id='jvm_heap_mem_used'>n/a</span></div>"
        ].join(""),

        jvmThreads: [
            "<div>Peak: <span id='jvm_threads_peak'>n/a</span></div>",
            "<div>Count: <span id='jvm_threads_count'>n/a</span></div>"
        ].join(""),

        jvmGC: [
            "<div>Total time (O/Y): <span id='jvm_gc_time'>n/a</span></div>",
            "<div>Total count (O/Y): <span id='jvm_gc_count'>n/a</span></div>"
        ].join(""),

		threadPoolSearch: [
            "<div>Queue: <span id='tp_search_queue'>n/a</span></div>",
			"<div>Peak: <span id='tp_search_peak'>n/a</span></div>",
            "<div>Count: <span id='tp_search_count'>n/a</span></div>"
		].join(""),

		threadPoolIndex: [
            "<div>Queue: <span id='tp_index_queue'>n/a</span></div>",
			"<div>Peak: <span id='tp_index_peak'>n/a</span></div>",
            "<div>Count: <span id='tp_index_count'>n/a</span></div>"
		].join(""),

		threadPoolBulk: [
            "<div>Queue: <span id='tp_bulk_queue'>n/a</span></div>",
			"<div>Peak: <span id='tp_bulk_peak'>n/a</span></div>",
            "<div>Count: <span id='tp_bulk_count'>n/a</span></div>"
		].join(""),

		threadPoolRefresh: [
            "<div>Queue: <span id='tp_refresh_queue'>n/a</span></div>",
			"<div>Peak: <span id='tp_refresh_peak'>n/a</span></div>",
            "<div>Count: <span id='tp_refresh_count'>n/a</span></div>"
		].join(""),

        osMem: [
            "<div>Free: <span id='os_mem_free'>n/a</span></div>",
            "<div>Used: <span id='os_mem_used'>n/a</span></div>"
        ].join(""),

        osPercent: [
            "<div>Mem: <span id='mem_percent'>n/a</span></div>",
            "<div>Cpu: <span id='cpu_percent'>n/a</span></div>"
        ].join(""),

        channelsTemplate: [
            "<div>Transport: <span id='open_transport_channels'>n/a</span></div>",
            "<div>HTTP: <span id='open_http_channels'>n/a</span></div>",
            "<div>HTTP total opened: <span id='total_opened_http_channels'>na</span></div>"
        ].join(""),

        transportRxTx: [
            "<!--#-->",
            "<div>Rx: <span id='transport_rx_size'>n/a</span>, #<span id='transport_rx_count'>n/a</span></div>",
            "<div>Tx: <span id='transport_tx_size'>n/a</span>, #<span id='transport_tx_count'>n/a</span></div>"
        ].join(""),

        TDBTemplate: [
            "<svg width='100%' height='90'>" +
                "<rect x='0' y='0' width='100%' height='100%' fill='#eee' stroke-width='1' />" +
            "</svg>"
        ].join(""),

        summaryInfoTemplate: [
            "Elasticsearch version: {{version}}"+
            "&nbsp;&nbsp;&nbsp;Host name: {{host}}"+ 
            "&nbsp;&nbsp;&nbsp;OS name: {{os.name}}"+
            "&nbsp;&nbsp;&nbsp;Java version: {{jvm.version}}"+
            "&nbsp;&nbsp;&nbsp;VM PID: {{jvm.pid}}"+
            "&nbsp;&nbsp;&nbsp;VM Uptime: <span id='jvm_uptime'>n/a</span>"
        ].join("<br>"),

        osInfoTemplate: [
            "OS Arch: {{os.arch}}"+
            "&nbsp;&nbsp;&nbsp;Available processors: {{os.available_processors}}"+
            "&nbsp;&nbsp;&nbsp;Load average: <span id='load_average'>n/a</span>"+
            "&nbsp;&nbsp;&nbsp;File descriptors: <span id='file_descriptors'>n/a</span>",
            "VM name: {{jvm.vm_name}}"+
            "&nbsp;&nbsp;&nbsp;VM vendor: {{jvm.vm_vendor}}"
        ].join("<br>"),

        indicesTemplate: [
       		"Store size: <span id='indices_store_size'>n/a</span>"+
            "&nbsp;&nbsp;&nbsp;Docs count: <span id='indices_docs_count'>n/a</span>"+
            "&nbsp;&nbsp;&nbsp;Docs deleted: <span id='indices_docs_deleted'>n/a</span>"+
            "&nbsp;&nbsp;&nbsp;Flush: <span id='indices_flush_total'>n/a</span>"+
            "&nbsp;&nbsp;&nbsp;Refresh: <span id='indices_refresh_total'>n/a</span>"
        ].join("<br>"),
        
        indicesSegments: [
        	"Shards count: <span id='shards_count'>n/a</span>",
            "Segments count: <span id='indices_segments_count'>n/a</span>"
        ].join("<br>"),
        
        indicesSearchReqsTemplate: [
            "Query: <span id='indices_search_query_reqs'>n/a</span>",
            "Fetch: <span id='indices_search_fetch_reqs'>n/a</span>"
        ].join("<br>"),

        indicesSearchTimeTemplate: [
            "Query: <span id='indices_search_query_time'>n/a</span>",
            "Fetch: <span id='indices_search_fetch_time'>n/a</span>"
        ].join("<br>"),

        indicesGetReqsTemplate: [
            "Get: <span id='indices_get_reqs'>n/a</span>",
            "Exists: <span id='indices_exists_reqs'>n/a</span>",
            "Missing: <span id='indices_missing_reqs'>n/a</span>"
        ].join("<br>"),

        indicesGetTimeTemplate: [
            "Get: <span id='indices_get_time'>n/a</span>",
            "Exists: <span id='indices_exists_time'>n/a</span>",
            "Missing: <span id='indices_missing_time'>n/a</span>"
        ].join("<br>"),

        indicesCacheSizeTemplate: [
            "Request: <span id='indices_request_cache_size'>n/a</span>",
            "Query: <span id='indices_query_cache_size'>n/a</span>",
            "Fielddata: <span id='indices_fielddata_cache_size'>n/a</span>"
        ].join("<br>"),

        indicesCacheEvictionsTemplate: [
            "Request: <span id='indices_cache_fielddata_evictions'>n/a</span>",
            "Query: <span id='indices_cache_query_evictions'>n/a</span>",
            "Fielddata: <span id='indices_cache_request_evictions'>n/a</span>"
        ].join("<br>"),

        indicesIndexingReqsTemplate: [
            "Delete: <span id='indices_indexing_delete_reqs'>n/a</span>",
            "Index: <span id='indices_indexing_index_reqs'>n/a</span>"
        ].join("<br>"),

        indicesIndexingTimeTemplate: [
            "Delete: <span id='indices_indexing_delete_time'>n/a</span>",
            "Index: <span id='indices_indexing_index_time'>n/a</span>"
        ].join("<br>"),

        fsDataInfoTemplate: [
            "<div>Type: {{#type}}<span class='pre'>{{type}}</span>{{/type}}{{^type}}n/a{{/type}}"+
            "&nbsp;&nbsp;&nbsp;Mount: {{#mount}}<span class='pre'>{{mount}}</span>{{/mount}}{{^mount}}n/a{{/mount}}"+
            "&nbsp;&nbsp;&nbsp;Free: <span id='fs_disk_free_{{key}}'>{{free}}</span>"+
            "&nbsp;&nbsp;&nbsp;Available: <span id='fs_disk_available_{{key}}'>{{available}}</span>"+
            "&nbsp;&nbsp;&nbsp;Total: {{total}}</div>",
            "<div>Path: <span class='pre'>{{path}}</span></div>"
        ].join(""),

        fsDataInfo_cntTemplate: [
            "<div>Writes: <span id='fs_disk_writes_{{key}}'>n/a</span></div>",
            "<div>Reads: <span id='fs_disk_reads_{{key}}'>n/a</span></div>"
        ].join(""),

        fsDataInfo_sizeTemplate: [
            "<div>Write: <span id='fs_disk_write_size_{{key}}'>n/a</span></div>",
            "<div>Read: <span id='fs_disk_read_size_{{key}}'>n/a</span></div>"
        ].join("")

    }

};
