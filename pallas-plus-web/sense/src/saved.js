(function () {

   var global = window;

   var saved_viewer, saved_popup;

   function getSavedKeys() {
      var keys = [];
      for (var i = 0; i < localStorage.length; i++) {
         var k = localStorage.key(i);
         if (k.indexOf("saved_elem") == 0) {
            keys.push(k);
         }
      }

      keys.sort();
      keys.reverse();
      return keys;
   }

   function getSaved() {
      var saved_items = [];
      $.each(getSavedKeys(), function (i, key) {
          saved_items.push(JSON.parse(localStorage.getItem(key)));
      });

      return saved_items;
   }

   function populateSavedElem(saved_elem) {
      var s = saved_elem.method + " " + saved_elem.endpoint + "\n" + (saved_elem.data || "");
      saved_viewer.setValue(s);
      saved_viewer.clearSelection();
   }

    function confirmDelete(li, saved_elem) {
        if(confirm('Are you sure you want to delete ' + saved_elem.title + '?')) {
            localStorage.removeItem('saved_elem_' + saved_elem.time);
            li.remove();
        }
    }

   function applySavedElem(saved_elem) {
      var session = sense.editor.getSession();
      var pos = sense.editor.getCursorPosition();
      var prefix = "";
      var suffix = "\n";
      if (sense.utils.isStartRequestRow(pos.row)) {
         pos.column = 0;
         suffix += "\n";
      }
      else if (sense.utils.isEndRequestRow(pos.row)) {
         var line = session.getLine(pos.row);
         pos.column = line.length;
         prefix = "\n\n";
      }
      else if (sense.utils.isInBetweenRequestsRow(pos.row)) {
         pos.column = 0;
      }
      else {
         pos = sense.utils.nextRequestEnd(pos);
         prefix = "\n\n";
      }

      var s = prefix + saved_elem.method + " " + saved_elem.endpoint;
      if (saved_elem.data) s += "\n" + saved_elem.data;

      s += suffix;

      session.insert(pos, s);
      sense.editor.clearSelection();
      sense.editor.moveCursorTo(pos.row + prefix.length, 0);
      sense.editor.focus();
   }

   function init() {
      saved_popup = $("#saved_popup");

      saved_popup.on('shown', function () {
         $('<div id="saved_viewer">No query available</div>').appendTo(saved_popup.find(".modal-body"));

         saved_viewer = ace.edit("saved_viewer");
         saved_viewer.getSession().setMode("ace/mode/sense");
         saved_viewer.getSession().setFoldStyle('markbeginend');
         saved_viewer.setReadOnly(true);
         saved_viewer.renderer.setShowPrintMargin(false);
         sense.editor.getSession().setUseWrapMode(true);

         showSaved(saved_popup);

         saved_popup.find(".modal-body .nav li:first a").click();

      });

       saved_popup.find("#saved_delete").click(function () {
           localStorage.removeItem(k);
       })

      saved_popup.on('hidden', function () {
         saved_popup.find('.modal-body #saved_viewer').remove();
         saved_popup.find('.modal-body .nav li').remove();
         saved_viewer = null;
      });

      saved_popup.find(".btn-primary").click(function () {
         saved_popup.find(".modal-body .nav li.active").trigger("apply");
      });

   }

   function showSaved(saved_popup) {

       $.each(getSaved(), function (i, saved_elem) {
           var li = $('<li><a href="#"><i class="icon-chevron-right"></i><span/></a></li>');
           var disc = '<span class="label ' + classByMethod(saved_elem.method) + '">' + saved_elem.method + '</span>  ' + saved_elem.title + '<br><small>' + saved_elem.endpoint;
           var date = moment(saved_elem.time);
           if (date.diff(moment(), "days") < -7)
               disc += " (" + date.format("MMM D") + ")";
           else
               disc += " (" + date.fromNow() + ")";
           disc += '</small><i class="icon-trash pull-right pointer" title="Delete this query"></i></button>';

           li.find("span").html(disc);
           li.attr("title", disc);

           li.find("a").click(function () {
               saved_popup.find('.modal-body .nav li').removeClass("active");
               li.addClass("active");
               $('#saved_delete').removeClass('hide');
               populateSavedElem(saved_elem);
               return false;
           });

           li.find(".icon-trash").click(function () {
               confirmDelete(li, saved_elem);
           });

           li.dblclick(function () {
               li.addClass("active");
               saved_popup.find(".btn-primary").click();
           });

           li.hover(function () {
               populateSavedElem(saved_elem);
               return false;
           }, function () {
               saved_popup.find(".modal-body .nav li.active a").click();
           });

           li.bind('apply', function () {
               applySavedElem(saved_elem);
           });


           li.appendTo(saved_popup.find(".modal-body .nav"));
       });
   }

   function classByMethod(method) {

      switch(method.toUpperCase()) {
          case 'GET':
              return 'label-info';
          case 'POST':
              return 'label-success';
          case 'PUT':
              return 'label-warning';
          case 'DELETE':
              return 'label-danger';
          default:
              return '';
      }
   }

   function saveQuery(server, endpoint, method, data, title) {
      var keys = getSavedKeys();

      var timestamp = new Date().getTime();
      var k = "saved_elem_" + timestamp;
      localStorage.setItem(k, JSON.stringify(
         { 'time': timestamp, 'server': server, 'endpoint': endpoint, 'method': method, 'data': data , 'title': title}));
   }

   global.sense.saved = {
      init: init,
      saveQuery: saveQuery
   };

})();





