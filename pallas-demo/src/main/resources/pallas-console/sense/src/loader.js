(function () {

    var version = window.version = '?v=' + 21;

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
	
	loadCss(asset + "sense.css");

	addScript(asset + 'lib/jquery-1.8.3.min.js');
	addScript(asset + 'lib/jqueryui/jquery-ui-1.9.2.custom.min.js');
	addScript(asset + 'lib/bootstrap/js/bootstrap.min.js');
	addScript(asset + 'lib/moment.min.js');
	addScript(asset + 'lib/src-noconflict/ace.js');
	addScript(asset + 'src/utils.js');
	addScript(asset + 'src/kb.js');
	addScript(asset + 'kb/aliases.js');
	addScript(asset + 'kb/cluster.js');
	addScript(asset + 'kb/filter.js');
	addScript(asset + 'kb/facets.js');
	addScript(asset + 'kb/globals.js');
	addScript(asset + 'kb/indices.js');
	addScript(asset + 'kb/query.js');
	addScript(asset + 'kb/search.js');
	addScript(asset + 'kb/settings.js');
	addScript(asset + 'kb/templates.js');
	addScript(asset + 'kb/warmers.js');
	addScript(asset + 'kb/mappings.js');
	addScript(asset + 'kb/misc.js');
	addScript(asset + 'src/curl.js');
	addScript(asset + 'src/base.js');
	addScript(asset + 'src/mappings.js');
	addScript(asset + 'src/history.js');
	addScript(asset + 'src/autocomplete.js');
})();

