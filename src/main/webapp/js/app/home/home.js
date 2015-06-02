// [NOTE] this is a default basic setting for all jQuery UI Draggable objects;
// It should remain as IMMUTABLE
var baseDraggableSettings = {
    cursor:"move",
    scroll: false,
    helper: "clone",
    zIndex: 10000,
    containment: "document",
    appendTo: "body",
};

function setupParser() {
	// prepare *Marked* (a Markdown parser) library; see https://github.com/chjj/marked
	marked.setOptions({
		renderer: new marked.Renderer(),
		gfm: true,
		tables: true,
		breaks: false,
		pedantic: false,
		sanitize: SYSPROP.sanitizeHtml,
		smartLists: true,
		smartypants: false,
	});
}

function formatDatetime(src) {
	return moment(src, MSG.date_time_format_js, $("html").attr("lang")).fromNow() + "; " + src;
}

function formatTagsOnFragmentHeader(tgtTag) {
    var tid = tgtTag.attr("_tid");
    var srcTag = $("#tag-palette-flat").find("[_tid="+tid+"]");
    var fc = srcTag.attr("_frgCount");
    tgtTag[0].title = fc + " " + MSG.label_fragments + "; " + MSG.rclick_for_menu;
    if (srcTag.hasClass("special-tag"))
    	tgtTag.addClass("special-tag");
}

function translateFragments() {
    var fg = $("#fragment-group");
    
	// apply Civilizer's custom markup rules to fragment titles;
    fg.find(".fragment-title").each(function() {
    	var $this = $(this);
    	$this.html(translateCustomMarkupRules($this.html()));
    });
    
    // translate Markdown formatted fragment contents into HTML format
    fg.find(".fragment-content").each(function() {
    	var $this = $(this);
    	$this.html(translateFragmentContent($this.text()));
    	postprocessFragmentContent($this);
    });
    
    // add a tooltip message of updated time to a clock icon
    fg.find(".fragment-header").find(".fa-clock-o").each(function() {
    	var $this = $(this);
    	$this[0].title = formatDatetime($this.prev("span").text());
    });
    
    fg.find(".each-tag").each(function() {
    	formatTagsOnFragmentHeader($(this));
    });
}

function populateFragmentOverlay(data) {
	// show the fragment overlay as a popup
	var overlayFrame = $("#fragment-overlay");
	overlayFrame.lightbox_me({
        centered:false
        , showOverlay:false
        , lightboxSpeed:"fast"
        , closeSelector:"#fragment-overlay-close-button"
        , closeEsc:true
        , modalCSS:{ position:"fixed", bottom: '2%', right: '2%' }
    });
	
	var titleBar = $("#fragment-overlay-title-bar");
	if (titleBar.next().is(":visible") == false) {
    	toggleFragmentEditor(overlayFrame, titleBar);
    }
	
	$("#fragment-overlay-title").text("");
	
	// Set up a link to the previous fragment if any.
	var overlayContent = $("#fragment-overlay-content");
	var prevHdr = overlayContent.find(".fragment-header");
	var backBtn = $("#fragmen-overlay-back-button");
	if (prevHdr.length > 0) {
		backBtn.show();
		backBtn.attr("href", "fragment/"+prevHdr.attr("_fid"))
			.off("click").on("click", triggerFragmentOverlay);
	}
	else {
		backBtn.hide();
	}
	
	overlayContent.html(data);
	
	$("#fragment-overlay-content .fragment-header .fa-clock-o").each(function() {
    	var $this = $(this);
    	$this[0].title = formatDatetime($this.prev("span").text());
    });
	
	// translate Markdown formatted fragment contents into HTML format
	overlayContent.find(".fragment-content").each(function(){
        var $this = $(this);
		$this.html(translateFragmentContent($this.text()));
		postprocessFragmentContent($this);
    });
	
	overlayContent.find(".related-fragment-container a.-cvz-frgm").on("click", triggerFragmentOverlay);
	
	// make titles on the overlay window also draggable/droppable
	setupDraggableForFragmentTitle();
	
	setContextMenuForFragments();
	
	setContextMenuForTags();
	
	overlayContent.find(".each-tag").draggable(baseDraggableSettings)
	.each(function() {formatTagsOnFragmentHeader($(this))});
}

function triggerFragmentOverlay(event) {
	var href=$(this).attr('href');
	$.get(href, "", populateFragmentOverlay);
	
	event.preventDefault();
	
	return false; // stop the link
}

function setupFragmentOverlay() {
	$("#fragment-group").find(".-cvz-frgm").on("click", triggerFragmentOverlay);
    $("#bookmark-form\\:bookmark-panel").find(".-cvz-frgm").on("click", triggerFragmentOverlay);
    
    $("#fragment-overlay").draggable({
    	handle:"#fragment-overlay-title-bar",
        cursor:"move",
    });
    
	$("#fragment-overlay-title-bar").dblclick(function() {
    	toggleFragmentEditor($("#fragment-overlay"), $(this));
    });
}

function setupFragmentCheckboxes() {
	function generateClickHandler(panelId) {
    	var fragmentCount = $("#fragment-panel-" + panelId + " .each-fragment-container").length;
        if (fragmentCount > 0) {
            var fragmentCheckboxSlaves = [];    
            
            for (var j=0; j<fragmentCount; ++j) {
                var cb = PF("fragmentCheckboxSlave" + panelId + "_" + j);
                fragmentCheckboxSlaves.push(cb);
            }
        
            var fragmentCheckboxMaster = PF("fragmentCheckboxMaster" + panelId);
            
            // Align the master checkbox with its slaves in horizontal position
            fragmentCheckboxMaster.jq.offset({ left:fragmentCheckboxSlaves[0].jq.offset().left });
            
            return function (e) {
                if (fragmentCheckboxMaster.isChecked()) {
                    for (var i=0; i<fragmentCount; ++i) {
                        fragmentCheckboxSlaves[i].check();
                    }
                }
                else {
                    for (var i=0; i<fragmentCount; ++i) {
                        fragmentCheckboxSlaves[i].uncheck();
                    }
                }
            }
        }
    }
    
    var fragmentCheckboxMaster;
    var panelId;
    
    panelId = 0;
    fragmentCheckboxMaster = PF("fragmentCheckboxMaster" + panelId)
    if (fragmentCheckboxMaster) {
    	fragmentCheckboxMaster.jq.click(generateClickHandler(panelId));
    }
    panelId = 1;
    fragmentCheckboxMaster = PF("fragmentCheckboxMaster" + panelId)
    if (fragmentCheckboxMaster) {
    	fragmentCheckboxMaster.jq.click(generateClickHandler(panelId));
    }
    panelId = 2;
    fragmentCheckboxMaster = PF("fragmentCheckboxMaster" + panelId)
    if (fragmentCheckboxMaster) {
    	fragmentCheckboxMaster.jq.click(generateClickHandler(panelId));
    }
    
    // Attach a tooltip message to a checkbox on each fragment panel menu
    for (var i=0; i<3; ++i) {
    	var cb = $("#fragment-group-form\\:checkbox-for-all-fragments-" + i);
    	if (cb.length > 0) {
    		cb[0].title = MSG.label_check_uncheck_all;
    	}
    }
}

function makeObjectsInsertableToTextArea() {
	// [NOTE] original source code can be found at:
    // http://skfox.com/2008/11/26/jquery-example-inserting-text-with-drag-n-drop/
    $.fn.insertAtCaret = function (myValue) {
		return this.each(function(){
			//IE support
			if (document.selection) {
				this.focus();
				sel = document.selection.createRange();
				sel.text = myValue;
				this.focus();
			}
			//MOZILLA / NETSCAPE support
			else if (this.selectionStart || this.selectionStart == '0') {
				var startPos = this.selectionStart;
				var endPos = this.selectionEnd;
				var scrollTop = this.scrollTop;
				this.value = this.value.substring(0, startPos)+ myValue+ this.value.substring(endPos,this.value.length);
				this.focus();
				this.selectionStart = startPos + myValue.length;
				this.selectionEnd = startPos + myValue.length;
				this.scrollTop = scrollTop;
			}
			else {
				this.value += myValue;
				this.focus();
			}
		});
    };
}

function translateCustomMarkupRules(html) {
	// format  =>   {{[keyword] ... text ... }}
	var found = false;
	html = html.replace(/\{\{\[(.+?)\]/g, function(match, pos, originalText) {
		found = true;
		return "<span class='-cvz-" + RegExp.$1 + "'>"; 
	});
	if (found) {
		html = html.replace(/\}\}/g, function(match, pos, originalText) {
			return "</span>"; 
		});
	}
	return html;
// 	var patt = /\{\{\[(.+?)\](.*?)\}\}/g;
// 	return html.replace(patt, function(match, pos, originalText) {
// 		var cg1 = RegExp.$1;
// 		var cg2 = RegExp.$2;
// 		while (patt.test(cg2)) {
// 			cg2 = cg2.replace(patt, function(match_, pos_, originalText_) {
// 				return "<span class='-cvz-" + RegExp.$1 + "'>" + RegExp.$2 + "</span>";
// 			});
// 		}
//         return "<span class='-cvz-" + cg1 + "'>" + cg2 + "</span>";
//     });
}

function translateFragmentContent(content) {
    // translate Markdown text into HTML;
    // [NOTE] HTML code would be sanitized at this time;
	var outputHtml = parseMarkdown(content);
	
	// take care of custom style rules
	return translateCustomMarkupRules(outputHtml);
}

function setupFragmentLinks(content) {
    content.find("span.-cvz-frgm").each(function() {
        var $this = $(this);
        var txt = $this.text();
        var patt = /\s*(\d+)\s+(.*)/g;
        if (txt.match(patt)) {
            var newTag = $("<a class='-cvz-frgm' href='fragment/" + RegExp.$1 + "'>" + RegExp.$2 + "</a>");
            $this.replaceWith(newTag);
        }
    });
    
    content.find("a.-cvz-frgm").on("click", triggerFragmentOverlay);
}

function processEmbeddedFragments(content) {
	content.find(".-cvz-frgm-embed").each(function() {
		var $this = $(this);
        $.get("fragment/"+$this.text().trim(), "", function(data) {
        	var content = $(data).find(".fragment-content").text();
        	$this.html(translateFragmentContent(content));
            postprocessFragmentContent($this);
        });
	})
}

function postprocessFragmentContent(content) {
	// translate embedded fragemnts
	// Rule - {{[frgm-embed]...}}
	processEmbeddedFragments(content);
	
	// translate fragemnt links
	// Rule - {{[frgm]...}}
	setupFragmentLinks(content);
	
    // translate file box elements into HTML links
    // Rule - {{[file]...}}
    processFileClasses(content);
}

function setupDragAndDrop() {
	setupDraggableForFragmentTitle();
	setupDraggableForTags();
	setupDraggableForFiles();
	
	setupDndForRelatingFragments();
	setupDndForFragmentFetch();
	setupDndForBookmarking();
	setupDndForTrashing();
	setupDndForEmbeddingFile();
}

function setupDraggableForFragmentTitle() {
	$(".fragment-title").draggable(baseDraggableSettings);
}

function setupDraggableForTags() {
	var tagPalettePanel = $("#tag-palette-panel");
	var overflowOption = tagPalettePanel.css("overflow");
	$("#tag-palette-panel .each-tag").draggable(baseDraggableSettings)
	.on("dragstart", function(event, ui) {
		// [NOTE] the helper object disappears at the outside of the panel unless doing this
		tagPalettePanel.css({overflow:"initial"});
	})
	.on("dragstop", function(event, ui) {
		tagPalettePanel.css({overflow:overflowOption});
	});
	
	$("#fragment-group .each-tag").draggable(baseDraggableSettings);
}

function setupDraggableForFiles() {
	var fileBoxPanel = $("#file-box-form\\:file-box-panel");
	var overflowOption = fileBoxPanel.css("overflow");
	$(".fb-file").draggable(baseDraggableSettings)
	.on("dragstart", function(event, ui) {
		fileBoxPanel.css({overflow:"initial"});
	})
	.on("dragstop", function(event, ui) {
		// [NOTE] the overflow style should be identical between the tag palette and file box
		fileBoxPanel.css({overflow:overflowOption});
	});
}

function setupDndForRelatingFragments() {
	var droppable = newBaseDroppable(["fragment-title"]);
    droppable.drop = function(event, ui) {
        var from = ui.draggable;
        var to = $(event.target);
        if (from.hasClass("fragment-title")) {
            var fromId = from.attr("_fid");
            var toId = to.find(".fragment-title").attr("_fid");
            if (fromId != toId) {
            	confirmRelatingFragments(fromId, toId);
            }
        }
    };
    $(".fragment-header").droppable(droppable);
}

function setupDndForFragmentFetch() {
	var droppable = newBaseDroppable(["fragment-title", "each-tag"]);
    droppable.drop = function(event, ui) {
        var from = ui.draggable;
        var to = $(event.target);
        if (from.hasClass("each-tag")) {
            fetchFragmentsByTag(from, to);
        }
        else if (from.hasClass("fragment-title")) {
            fetchFragments(findPanel(to), [from.attr("_fid")]);
        }
    };
    $('[id^="fragment-group-form\\:fragment-panel-toolbar-"]').droppable(droppable);
    $("#panel-activation-buttons label").droppable(droppable);
}

function setupDndForBookmarking() {
	var droppable = newBaseDroppable(["fragment-title"]);
    droppable.drop = function(event, ui) {
        var from = ui.draggable;
        var to = $(event.target);
        if (from.hasClass("fragment-title")) {
        	var frgId = from.attr("_fid");
        	bookmarkFragment([ {name:"fragmentId", value:frgId} ]);
        }
    };    
    $("#bookmark-form\\:bookmark-panel").droppable(droppable);
}

function setupDndForTrashing() {
	var droppable = newBaseDroppable(["fragment-title", "each-tag"]);
    droppable.drop = function(event, ui) {
        var from = ui.draggable;
        var to = $(event.target);
        if (from.hasClass("fragment-title")) {
	        var panelId = findPanel(from);
	       	var deleting = FRAGMENT_DELETABLE[panelId];
        	var frgId = from.attr("_fid");
        	confirmTrashingFragments(frgId, deleting, false, null);
        }
        else if (from.hasClass("each-tag")) {
        	confirmTrashingTag(from.attr("_tid"), Boolean(from.attr("_frgCount") == 0));
        }
    };    
    $("#trashcan").droppable(droppable);
}

function setupDndForEmbeddingFile() {
	var droppable = newBaseDroppable("", ["fb-file"]);
    droppable.drop = function(event, ui) {
        var from = ui.draggable;
        var to = $(event.target);
        if (from.hasClass("fb-file")) {
        	var ids = from.attr("id");
        	var id = ids.substr(ids.lastIndexOf("-") + 1);
        	var encoded = "{{[file]" + id + "}}";
        	$(this).insertAtCaret(encoded);
        }
    };    
    $("#fragment-content-editor").droppable(droppable);
}

function newBaseDroppable(acceptableClasses) {
	var weakFocus = "ui-weak-focus";
	var strongFocus = "ui-strong-focus";
	var output = {
		over: function(event, ui) {
			var from = ui.draggable;
            var to = $(event.target);
			if (hasAnyClass(from, acceptableClasses)) {
	            to.addClass(strongFocus);
	            to.removeClass(weakFocus);
			}
        },
        out: function(event, ui) {
        	var from = ui.draggable;
            var to = $(event.target);
            if (hasAnyClass(from, acceptableClasses)) {
	            to.removeClass(strongFocus);
	            to.addClass(weakFocus);
            }
        },
        activate: function(event, ui) {
        	var from = ui.draggable;
            var to = $(event.target);
            if (hasAnyClass(from, acceptableClasses)) {
	            to.addClass(weakFocus);
			}
        },
        deactivate: function(event, ui) {
            var to = $(event.target);
            to.removeClass(strongFocus);
            to.removeClass(weakFocus);
        },
	};
	return output;
}

function hasAnyClass(obj, classes) {
	for (var i=0; i<classes.length; ++i) {
		if (obj.hasClass(classes[i])) {
			return true;
		}
	}
	return false;
}

function findPanel(obj) {
	if (!obj) {
		return -1;
	}
	var pid = obj.attr("_pid");
	if (pid) {
		return pid;
	}
	var panelParent = obj.parents('[id^="fragment-panel-"]');
	if (panelParent.length > 0) {
		return panelParent.attr("_pid");
	}
	return -1;
}

function fetchFragmentsByTag(from, to) {
	var targetPanelId = findPanel(to);
	if (targetPanelId < 0) {
		if ($("#panel-toggler-1").prop("checked") == false) targetPanelId = 1;
		else if ($("#panel-toggler-2").prop("checked") == false) targetPanelId = 2;
		else targetPanelId = 0;
	}
    $("#fragment-group-form\\:id-placeholder-for-panel").val(targetPanelId);
    $("#panel-toggler-" + targetPanelId).prop("checked", true);
    $("#panel-activation-buttons").buttonset("refresh");
    filterByTag([ {name:"tagId", value:from.attr("_tid")}, {name:"panelId", value:targetPanelId} ]);
}

function showSortOptionDialog(panelId) {
	PF("sortOptionDlg" + panelId).show();
}

function fragmentCheckBoxesAreChecked(panelId) {
	var fragmentCount = $("#fragment-panel-" + panelId + " .each-fragment-container").length;
    if (fragmentCount > 0) {
        for (var j=0; j<fragmentCount; ++j) {
            var cb = PF("fragmentCheckboxSlave" + panelId + "_" + j);
            if (cb.isChecked()) {
            	return true;
            }
        }
    }
    return false;
}

function getFragmentTitle(frgId) {
	var title = $("#fragment-group").find(".fragment-title[_fid=" + frgId + "]");
	return (title.length > 0) ? title.eq(0).text() : "";
}

function getTagName(tagId) {
	var tag = $("#tag-palette-flat").find(".each-tag[_tid=" + tagId + "]");
	return (tag.length > 0) ? tag.find(".each-tag-name").text() : "";
}

function getFilePath(fileId) {
	var file = $("#file-path-tree").find(".each-file[_id=" + fileId + "]");
	return (file.length > 0) ? file.attr("_fp") : "";
}

function showError(message) {
	$('#fragment-group-form\\:error-msg').text(message);
	PF("errorMsgDlg").show();
}

function showConfirmDlg(mainMsg, subMsg, icon, color) {
	var dlg = PF("confirmDlg");
	var msg = dlg.jq.find(".ui-confirm-dialog-message");
    msg.text(mainMsg).next().remove();
    
    if (! color)
		color = "aqua";
    
    var iconTag = msg.prev().prev();
    if (iconTag.hasClass("fa")) {
    	iconTag.remove();
    }
    if (icon) {
    	if (typeof icon  == "string")
    		iconTag = $("<span class='fa fa-lg pull-left " + icon + "' style='color:" + color + "'>");
    	else
    		iconTag = icon;
    	msg.prev().before(iconTag);
    }
    
    if (subMsg) {
    	var p = $("<p style='margin-top:3px;white-space:pre;color:" + color + "'>").text(subMsg);
    	msg.after(p);
    }
    
    dlg.show();
}

function confirmTrashingFragments(frgId, deleting, bulk, panelId) {
	if (bulk) {
        if (fragmentCheckBoxesAreChecked(panelId) == false) {
            showError(MSG.no_item_is_selected);
            return;
        }
    }
	
    var op = deleting ? "delete" : "trash";
    var s = bulk ? "s" : "";
	$("#fragment-group-form\\:ok").click(function() {
		document.forms["fragment-group-form"]["fragment-group-form:ok-"+ op +"-fragment" + s].click();
	});
	
	var subMsg = "\n#" + frgId + "  " + getFragmentTitle(frgId);
	showConfirmDlg(deleting ? MSG.confirm_deleting : MSG.confirm_trashing, subMsg, "fa-trash", "orangered");
    
    if (bulk) {
	    $("#fragment-group-form\\:id-placeholder-for-panel").val(panelId);
    }
    else {
	    $("#fragment-group-form\\:id-placeholder-for-fragment").val(frgId);
    }
}

function confirmRestoringFragments(frgId) {
    $("#fragment-group-form\\:ok").click(function() {
        document.forms["fragment-group-form"]["fragment-group-form:ok-restore-fragment"].click();
    });
    
    var subMsg = "\n#" + frgId + "  " + getFragmentTitle(frgId);
    showConfirmDlg(MSG.confirm_restoring, subMsg, "fa-recycle", "orange");
    
    $("#fragment-group-form\\:id-placeholder-for-fragment").val(frgId);
}

function confirmTrashingTag(tagId, deleting) {
	var op = deleting ? "delete" : "trash";	
	$("#fragment-group-form\\:ok").click(function() {
		document.forms["fragment-group-form"]["fragment-group-form:ok-"+ op +"-tag"].click();
	});
	var subMsg = "\n#" + tagId + "  " + getTagName(tagId);
    showConfirmDlg(deleting ? MSG.confirm_deleting : MSG.confirm_trashing, subMsg, "fa-trash", "orangered");
    $("#fragment-group-form\\:id-placeholder-for-trashed-tag").val(tagId);
}

function confirmTrashingTagFromCtxtMenu() {
	var menu = $("#tag-context-menu");
	var target = menu.data("target-tag");
	var tagId = target.attr("_tid");
	var deleting = target.attr("_frgCount") == 0;
	confirmTrashingTag(tagId, deleting);
}

function confirmRelatingFragments(fromId, toId) {
	$("#fragment-group-form\\:ok").click(function() {
		relateFragments([ {name:"from", value:fromId}, {name:"to", value:toId} ]);
	});
	var subMsg = "\n#"+fromId + "   " + getFragmentTitle(fromId) +
		"\n#"+toId + "   " + getFragmentTitle(toId);
	showConfirmDlg(MSG.confirm_relating, subMsg, "fa-link");
}

function confirmUnrelatingFragments(frgId0, frgId1) {
	$("#fragment-group-form\\:ok").click(function() {
		document.forms["fragment-group-form"]["fragment-group-form:ok-unrelate-fragments"].click();
	});
	var subMsg = "\n#"+frgId0 + "   " + getFragmentTitle(frgId0) +
		"\n#"+frgId1 + "   " + getFragmentTitle(frgId1);
    showConfirmDlg(MSG.confirm_unrelating, subMsg, "fa-unlink", "orange");
    $("#fragment-group-form\\:id-placeholder-for-fragment0").val(frgId0);
    $("#fragment-group-form\\:id-placeholder-for-fragment1").val(frgId1);
}

function confirmDeletingFile() {
    $("#fragment-group-form\\:ok").click(function() {
		document.forms["fragment-group-form"]["fragment-group-form:ok-delete-files"].click();
	});
  
    var menu = $("#file-context-menu");
	var target = menu.data("target-file");
	var fileId = target.attr("_id");
    $("#fragment-group-form\\:id-placeholder-for-file").val(fileId);

    var subMsg = "\n" + getFilePath(fileId);
    showConfirmDlg(MSG.confirm_deleting, subMsg, "fa-trash", "orangered");
}

function setContextMenuForFragments() {
	var menu = $("#frg-context-menu");
	
	$(".fragment-header, .rcllick-hint").bind("contextmenu", function(event) {
    	var target = $(event.target);
    	if (target.hasClass("rclick-hint")) {
    		target = target.closest(".fragment-header");    		
    	}
    	if (target.hasClass("fragment-header")) {
	    	menu.css({ left:event.pageX, top:event.pageY }).show();
	    	menu.data("target-frg", target);
	    	if (target.attr("_deletable") === "true") {
	    		menu.find("#fragment-group-form\\:bookmark").hide();
	    		menu.find("#fragment-group-form\\:trash").hide();
	    		menu.find("#fragment-group-form\\:delete").show();
	    		menu.find("#fragment-group-form\\:restore").show();
	    	}
	    	else {
	    		menu.find("#fragment-group-form\\:bookmark").show();
	    		menu.find("#fragment-group-form\\:trash").show();
	    		menu.find("#fragment-group-form\\:delete").hide();
	    		menu.find("#fragment-group-form\\:restore").hide();
	    	}
	    	event.preventDefault();
    	}
	});
	
	$(document).bind("click", function(event) {
    	menu.hide();
    });
}

function bookmarkFragmentFromCtxtMenu() {
	var menu = $("#frg-context-menu");
	var target = menu.data("target-frg");
	var frgId = target.attr("_fid");
	bookmarkFragment([ {name:"fragmentId", value:frgId} ]);
}

function trashFragmentFromCtxtMenu(deleting) {
	var menu = $("#frg-context-menu");
	var target = menu.data("target-frg");
	var frgId = target.attr("_fid");
	var panelId = findPanel(target);
	
	confirmTrashingFragments(frgId, deleting, false, panelId);
}

function restoreFragmentFromCtxtMenu() {
    var menu = $("#frg-context-menu");
    var target = menu.data("target-frg");
    var frgId = target.attr("_fid");
    
    confirmRestoringFragments(frgId);
}

function showSearchDialog(panelId, qsPhrase) {
	var dlg = PF("searchDlg");
	dlg.show();
	dlg.jq.find(".ui-dialog-title").text(MSG.label_search+" ~~~> "+MSG.label_panel+" "+panelId);
	
	var qsInput = $("#fragment-group-form\\:search-panel\\:quick-search-input").val(qsPhrase);
	
	$("#fragment-group-form\\:search-panel\\:tag-keywords").watermark(MSG.how_to_input_tags);
	
	$("#fragment-group-form\\:go-search").click(function() {
	    var activeTabId = $("#fragment-group-form\\:search-panel_active").val();
		var hasSomeToSearch = false;
		if (activeTabId == 1) {
			// Normal search tab is focused
			$("#fragment-group-form\\:search-panel\\:t1").find("input[type='text']").each(function() {
				if ($(this).val().trim()) {
					hasSomeToSearch = true;
					return false;
				}
				return true;
			});
			if (hasSomeToSearch) {
				// the quick search input has higher priority so we need to clear it
				qsInput.val(null);
			}
		}
		else {
			// Quick search tab is focused
			if (qsInput.val().trim()) {
				hasSomeToSearch = true;
			}
		}
		if (hasSomeToSearch) {
			searchFragments([{name:'panelId',value:panelId}]);
		}
	});
	qsInput.keyup(function(event) {
		// Quick search tab responds to the enter key
		if (event.keyCode == 13) {
			if ($(this).val().trim()) {
	            searchFragments([{name:'panelId',value:panelId}]);
			}
		}
	});
}

function searchWithHelpFromLastSearch(event, panelId, widget) {
	if (event.keyCode == 13) {
		var qsPhrase = $(widget).val().trim();
		if (qsPhrase) {
			$("#fragment-group-form\\:search-panel\\:quick-search-input").val(qsPhrase);
            searchFragments([{name:'panelId',value:panelId}]);
		}
	}
}

function fetchFragments(panelId, fragmentIds) {
	var ids = "id:";
	for (var i=0; i<fragmentIds.length; ++i) {
		ids += fragmentIds[i] + " ";
	}
	$("#fragment-group-form\\:search-panel\\:quick-search-input").val(ids);
	searchFragments([{name:'panelId',value:panelId}]);
}

function addToggler(target, toggler) {
	var collapseIcon = "fa-minus-square";
	var expandIcon = "fa-plus-square";
	var link = $("<a>").attr("href", "#");
	var icon = $("<span>").addClass("fa " + collapseIcon);
	link.prepend(icon).click(function (event) {
		toggler();
// 		if (icon.hasClass(collapseIcon)) {
// 			icon.removeClass(collapseIcon).addClass(expandIcon);
// 		}
// 		else {
// 			icon.removeClass(expandIcon).addClass(collapseIcon);
// 		}
		icon.toggleClass(collapseIcon + " " + expandIcon);
		event.preventDefault();
	});	
	target.before(link);
	return link;
}

function makeSidebarTitleToggleable() {
    var bookmarkPanel = PF('bookmarkPanel');
    var tagPalettePanel = PF('tagPalettePanel');
    var fileBoxPanel = PF('fileBoxPanel');
    
	var bmLink = addToggler($("#bookmark-title"), function() {
		bookmarkPanel.toggle();
		sessionStorage.setItem("bookmarkOpen", bookmarkPanel.cfg.collapsed ? "no":"yes");
	});
	var tpLink = addToggler($("#tag-palette-title"), function() {
	    tagPalettePanel.toggle();
	    sessionStorage.setItem("tagPaletteOpen", tagPalettePanel.cfg.collapsed ? "no":"yes");
	});
	var fbLink = addToggler($("#file-box-title"), function() {
	    fileBoxPanel.toggle();
	    sessionStorage.setItem("fileBoxOpen", fileBoxPanel.cfg.collapsed ? "no":"yes");
	});
	
	if (sessionStorage.getItem("bookmarkOpen") === "no")
	    bmLink.trigger("click");
	if (sessionStorage.getItem("tagPaletteOpen") === "no")
	    tpLink.trigger("click");
	if (sessionStorage.getItem("fileBoxOpen") === "no")
	    fbLink.trigger("click");
}

function onChangeFragmentCheckbox(fid, pid) {
	var checked = fragmentCheckBoxesAreChecked(pid);
	var tgt = $("#fragment-group-form\\:fragment-panel-toolbar-" + pid).find(".fa-trash");
	if (checked) {
		tgt.show();
	}
	else {
		tgt.hide();
	}
}
