(function () {

    var version = window.version = '?v=' + 26;

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
	
	loadCss(asset + "css/lib.css");
	loadCss(asset + "css/app.css");
	
	addScript(asset + 'js/lib.js');
	addScript(asset + 'js/app.js');
})();

