(function () {

    var version = window.version = '?v=' + 13;

    var asset = window.asset = '';
	if(!window.navmode){window.navmode="";}
	

    var addTag = function(name, attributes, sync) {
        var el = document.createElement(name),
            attrName;

        for (attrName in attributes) {
            el.setAttribute(attrName, attributes[attrName]);
        }

        sync ? document.write(outerHTML(el)) : headEl.appendChild(el);
    };

    var outerHTML = function (node) {
        return node.outerHTML || (function (n) {
                var div = document.createElement('div'), h;
                div.appendChild(n);
                h = div.innerHTML;
                div = null;
                return h;
            })(node);
    };
	
	var addScript = function(src){
		 addTag('script', {src:src+version}, true);
	};
	
	window.cssCache = [];
	var loadCss = function(c){
		var idx = c.indexOf("?");
		if(idx > -1){
			c = c.substring(0,idx);
		}
		c = c + window.version;
		
		for(var i in window.cssCache){
			if(c == window.cssCache[i]) return;
		}

		window.cssCache.push(c);
		var b=document.getElementsByTagName("head")[0],
		a=document.createElement("link");
		a.rel="stylesheet";
		a.href=c;
		b.appendChild(a);
	}

	loadCss(asset + "css/CssGrid_2/1140.css");
	loadCss(asset + "css/CssGrid_2/styles.css");
    loadCss(asset + "css/bigdesk.css");
    loadCss(asset + "js/charts/common.css");
    loadCss(asset + "js/charts/not-available/not-available-chart.css");
   	loadCss(asset + "js/charts/time-series/time-series-chart.css");
    loadCss(asset + "js/charts/time-area/time-area-chart.css");
   	loadCss(asset + "js/charts/pack/pack.css");
	
	
	addScript(asset + 'js/lib/css3-mediaqueries/css3-mediaqueries.js');
	addScript(asset + 'js/lib/jquery/jquery-1.7.1.min.js');
	addScript(asset + 'js/lib/tinysort/jquery.tinysort.min.js');
	addScript(asset + 'js/lib/mustache/mustache.js');
	addScript(asset + 'js/lib/underscore/underscore-min.js');
	addScript(asset + 'js/lib/backbone/backbone-min.js');
	addScript(asset + 'js/lib/D3-v2.8.1/d3.v2.min.js');
	addScript(asset + 'js/util/bigdesk_extension.js');

	addScript(asset + 'js/models/Hello.js');
	addScript(asset + 'js/models/cluster/IndicesStatus.js');
	addScript(asset + 'js/models/cluster/ClusterState.js');
	addScript(asset + 'js/models/cluster/ClusterHealth.js');
	addScript(asset + 'js/models/cluster/NodesState.js');
	addScript(asset + 'js/models/cluster/NodesStats.js');
	addScript(asset + 'js/models/cluster/NodeInfo.js');

	addScript(asset + 'js/views/templates.js');
	addScript(asset + 'js/views/ClusterHealthView.js');
	
	addScript(asset + 'js/views/ClusterNodesListView.js');
    addScript(asset + 'js/views/SelectedClusterNodeView.js');
    addScript(asset + 'js/views/ClusterStateView.js');

    addScript(asset + 'js/charts/not-available/not-available-chart.js');
    addScript(asset + 'js/charts/time-series/time-series-chart.js');
    addScript(asset + 'js/charts/time-area/time-area-chart.js');
    addScript(asset + 'js/charts/bigdesk_charts.js');
    addScript(asset + 'js/store/BigdeskStore.js');
   	addScript(asset + 'js/bigdeskApp.js');
})();

