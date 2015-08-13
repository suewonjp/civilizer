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
		sanitize: true,
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
    
    // add a tooltip message of updated and created time
    fg.find(".fragment-header .fa-clock-o, .fragment-header .fa-birthday-cake").each(function() {
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
        , modalCSS:{ position:"fixed", bottom: '34px', right: '2%' }
    });
	
	var titleBar = $("#fragment-overlay-title-bar");
	if (titleBar.next().is(":visible") == false) {
    	toggleWindow(overlayFrame, titleBar);
    }
	
	$("#fragment-overlay-title").text("");
	
	// Set up a link to the previous fragment if any.
	var overlayContent = $("#fragment-overlay-content");
	var prevHdr = overlayContent.find(".fragment-header");
	var backBtn = $("#fragmen-overlay-back-button");
	if (prevHdr.length > 0) {
		backBtn.show().attr("href", "fragment/"+prevHdr.attr("_fid"))
		    .addClass("-cvz-frgm");
	}
	else {
		backBtn.hide();
	}
	
	overlayContent.html(data);
	
	$("#fragment-overlay-content .fragment-header .fa-clock-o, #fragment-overlay-content .fragment-header .fa-birthday-cake")
	.each(function() {
    	var $this = $(this);
    	$this[0].title = formatDatetime($this.prev("span").text());
    });
	
	// translate Markdown formatted fragment contents into HTML format
	overlayContent.find(".fragment-content").each(function(){
        var $this = $(this);
		$this.html(translateFragmentContent($this.text()));
		postprocessFragmentContent($this);
    });
	
	// make titles on the overlay window also draggable/droppable
	setupDraggableForFragmentTitle();
	
	setContextMenuForFragments();
	
	setContextMenuForTags();
	
	overlayContent.find(".each-tag").draggable(baseDraggableSettings)
	.each(function() {formatTagsOnFragmentHeader($(this))});
	
	overlayFrame.off("click.cvz_frg_overlay").on("click.cvz_frg_overlay", ".-cvz-frgm", triggerFragmentOverlay);
	setupClickHandlerForTags(overlayContent);
}

function triggerFragmentOverlay(event) {
	var href=$(this).attr('href');
	$.get(href, "", populateFragmentOverlay);
	
	event.preventDefault();
	
	return false; // stop the link
}

function setupFragmentOverlay() {
    $("#container").off("click.cvz_frg_overlay").on("click.cvz_frg_overlay", ".-cvz-frgm", triggerFragmentOverlay);
    
	$("#fragment-overlay-title-bar").dblclick(function() {
    	toggleWindow($("#fragment-overlay"), $(this));
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
    fragmentCheckboxMaster = PrimeFaces.widgets["fragmentCheckboxMaster"+panelId];
    if (fragmentCheckboxMaster) {
    	fragmentCheckboxMaster.jq.click(generateClickHandler(panelId));
    }
    panelId = 1;
    fragmentCheckboxMaster = PrimeFaces.widgets["fragmentCheckboxMaster"+panelId];
    if (fragmentCheckboxMaster) {
    	fragmentCheckboxMaster.jq.click(generateClickHandler(panelId));
    }
    panelId = 2;
    fragmentCheckboxMaster = PrimeFaces.widgets["fragmentCheckboxMaster"+panelId];
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

function setupFragmentResolutionSliders() {
    $("#fragment-group .fragment-header").dblclick(function() {
        var tgt = $(this).closest(".each-fragment").find("tbody");
        showOrHide(tgt, !tgt.is(":visible"));
    });

    var allClasses = "frg-content-reso0 frg-content-reso1 frg-content-reso2 frg-content-reso3";
    
    function applyResolution(pid, reso) {
        var panel = $("#fragment-panel-"+pid);
        var tbody = panel.find(".each-fragment tbody").show();
        if (reso === undefined) reso = 3;
        if (reso == 0) tbody.hide();
        panel.find(".fragment-content").removeClass(allClasses).addClass("frg-content-reso"+reso);
        localStorage.setItem("frg-content-reso-"+pid, reso);
        return reso;
    }
    
    var resos = [
                 applyResolution(0, localStorage.getItem("frg-content-reso-0")),
                 applyResolution(1, localStorage.getItem("frg-content-reso-1")),
                 applyResolution(2, localStorage.getItem("frg-content-reso-2")),
                 ];
    
    function reso2value(reso) {
        switch (parseInt(reso, 10)) {
        case 0: return 0;
        case 1: return 30;
        case 2: return 70;
        }
        return 100;
    }
    
    var baseSettings = {
        max:100, min:0,
        change:function(event, ui) {
            var reso = 3;
            if (ui.value < 20) reso = 0;
            else if (ui.value < 50) reso = 1;
            else if (ui.value < 90) reso = 2;

            applyResolution($(this).attr("_pid"), reso);
        }
    };
    
    for (var i=0; i<3; ++i) {
        var value = reso2value(resos[i]);
        var obj = $("#frg-reso-slider"+i).slider($.extend(baseSettings, {value:value}))
            .find(".ui-slider-handle").css({left:value.toString()+'%'});
    }
}

function translateCustomMarkupRules(html) {
	return html
    	// {([keyword] ... text ... )} --- translated to a <span>
	    // used for one special purpose; highlighting search phrase
        .replace(/\{\(\[(.+?)\] /g, function(match, p1, pos, originalText) {
            return "<span class='-cvz-" + p1 + "'>"; 
        })
        .replace(/ \)\}/g, function(match, pos, originalText) {
            return "</span>";
        })
        // {{{[keyword] ... text ... }}} --- translated to a <div> block
    	.replace(/\{\{\{\[(.+?)\]/g, function(match, p1, pos, originalText) {
    	    return "<div class='-cvz-" + p1 + "'>"; 
    	})
    	.replace(/\}\}\}/g, function(match, pos, originalText) {
    	    return "</div>";
    	})
        // {{[keyword] ... text ... }} --- translated to a <span>
    	.replace(/\{\{\[(.+?)\]/g, function(match, p1, pos, originalText) {
    		return "<span class='-cvz-" + p1 + "'>"; 
    	})
    	.replace(/\}\}/g, function(match, pos, originalText) {
    		return "</span>";
    	})
    	;
}

function translateFragmentContent(content) {
    // translate Markdown text into HTML;
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
}

function setupTagLinks(content) {
    content.find("span.-cvz-tag").each(function() {
        var $this = $(this);
        var tagId = $this.text().trim();
        if (tagId) {
            var srcTag = $("#tag-palette-flat").find("[_tid="+tagId+"]");
            var newElem = $("<a href='#' class='-cvz-tag tag-button each-tag' _tid='"+tagId+"'>" + srcTag.find(".each-tag-name").text() + "</a>");
            $this.replaceWith(newElem);
        }
    });
}

function setupClickHandlerForTags(container) {
    container.off("click.cvz_tag").on("click.cvz_tag", ".-cvz-tag", function(e) {
        fetchFragmentsByTag($(this), null);
        return false;
    });
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

function unsanitizeHtml(content) {
    content.find(".-cvz-html").each(function() {
        var $this = $(this);
        var res = $this.text()
            .replace(/<(\/?)(.+)>/g, function(match, cg1, cg2, pos, originalText) {
//                var cg1 = RegExp.$1;
//                var cg2 = RegExp.$2;
                if (cg2.indexOf("html") == 0
                   || cg2.indexOf("head") == 0
                   || cg2.indexOf("body") == 0
                   || cg2.indexOf("link") == 0
                   || cg2.indexOf("script") == 0
                   )
                    // these tags should be sanitized for safety
                    return "&lt;"+cg1+cg2+"&gt";
                return match;
            });
        $this.text(null).html(res);
    });
}

function postprocessFragmentContent(content) {
	// translate embedded fragemnts
	// Rule - {{[frgm-embed]...}}
	processEmbeddedFragments(content);
	
	// translate fragemnt links
	// Rule - {{[frgm]...}}
	setupFragmentLinks(content);

	// translate tag links
	// Rule - {{[tag]...}}
	setupTagLinks(content);
	
	// convert links to images into <img> tags
	content.find("a").each(function() {
	    var $this = $(this);
	    var href = $this.attr("href");
	    if (isImage(href.substring(href.lastIndexOf("."))))
	        $this.replaceWith("<img src='"+href+"'>");
	});
	
    // translate file box elements into HTML links
    // Rule - {{[file]...}}
    processFileClasses(content);
    
    // unsanitize HTML code if any
    // Rule - {{[html]...}}
    unsanitizeHtml(content);
}

function setupDragAndDrop() {
	setupDraggableForFragmentTitle();
	setupDraggableForTags();
	setupDraggableForFiles();
	
	setupDndForRelatingFragments();
	setupDndForFragmentFetch();
	setupDndForBookmarking();
	setupDndForTrashing();
	setupDndToDropDataToFrgEditor();
}

function setupDraggableForFragmentTitle() {
	$(".fragment-title").draggable(baseDraggableSettings);
}

function setupDraggableForTags() {
	var tagPalettePanel = $("#tag-palette-panel");
	var overflowOption = tagPalettePanel.css("overflow");
	$("#tag-palette-panel .each-tag").draggable(baseDraggableSettings).off("dragstart dragstop")
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
	$(".fb-file").draggable(baseDraggableSettings).off("dragstart dragstop")
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
        	confirmTrashingFragments(frgId, deleting);
        }
        else if (from.hasClass("each-tag")) {
        	confirmTrashingTag(from.attr("_tid"), Boolean(from.attr("_frgCount") == 0));
        }
    };    
    $("#trashcan").droppable(droppable);
}

function setupDndToDropDataToFrgEditor() {
    var droppable = newBaseDroppable(["each-tag", "fb-file"]);
    droppable.drop = function(event, ui) {
        var from = ui.draggable;
        var to = $(event.target);
        if (from.hasClass("each-tag")) {
            var id = from.attr("_tid");
            var encoded = "{{[tag] " + id + " }}";
            $(this).insertAtCaret(encoded);
        }
        else if (from.hasClass("fb-file")) {
            var ids = from.attr("id");
            var id = ids.substr(ids.lastIndexOf("-") + 1);
            var encoded = "{{[file] " + id + " }}";
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
	sessionStorage.setItem("panel-"+targetPanelId, "on");
    $("#panel-activation-buttons").buttonset("refresh");
    filterByTag([ {name:"tagId", value:from.attr("_tid")}, {name:"panelId", value:targetPanelId} ]);
}

function showSortOptionDialog(panelId) {
	PF("sortOptionDlg" + panelId).show();
}

function getFragmentTitle(frgId) {
	var title = $("#fragment-group").find(".fragment-title[_fid=" + frgId + "]");
	if (!title || title.length == 0) {
	    title = $("#fragment-overlay-content").find(".fragment-title");
	}
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
	if (mainMsg instanceof jQuery) {
	    msg.empty().append(mainMsg).next().remove();
	}
	else {
	    msg.empty().text(mainMsg).next().remove();
	}
	
    if (! color)
		color = "deepskyblue";
    
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
    	var p = $("<p class='ui-panel ui-widget-content ui-corner-all' style='margin-top:3px;white-space:pre;color:" + color + "'>").text(subMsg);
    	msg.after(p);
    }
    
    dlg.show();
}

function confirmEmptyingTrash() {
    $("#fragment-group-form\\:ok").click(function() {
        document.forms["fragment-group-form"]["fragment-group-form:ok-empty-trash"].click();
    });
    
    var mainMsg = $("<span style='color:orangered'>"+MSG.confirm_emptying_trash+"</span>");
    
    showConfirmDlg(mainMsg, null, "fa-trash", "orangered");
}

function confirmTrashingFragments(frgId, deleting) {
    var op = deleting ? "delete" : "trash";
	$("#fragment-group-form\\:ok").click(function() {
	    addSubmitParam($("#fragment-group-form"), {fragmentId:frgId});
		document.forms["fragment-group-form"]["fragment-group-form:ok-"+ op +"-fragment"].click();
	});
	
	var mainMsg;
	if (deleting)
	    mainMsg = $("<span style='color:orangered'>"+MSG.confirm_deleting+"</span>");
	else
	    mainMsg = MSG.confirm_trashing;	
	var subMsg = "\n#" + frgId + "  " + getFragmentTitle(frgId);
	showConfirmDlg(mainMsg, subMsg, "fa-trash", "orangered");
}

function confirmRestoringFragments(frgId) {
    $("#fragment-group-form\\:ok").click(function() {
        addSubmitParam($("#fragment-group-form"), {fragmentId:frgId});
        document.forms["fragment-group-form"]["fragment-group-form:ok-restore-fragment"].click();
    });
    
    var subMsg = "\n#" + frgId + "  " + getFragmentTitle(frgId);
    showConfirmDlg(MSG.confirm_restoring, subMsg, "fa-recycle", "orange");
}

function confirmTrashingTag(tagId, deleting) {
	var op = deleting ? "delete" : "trash";	
	$("#fragment-group-form\\:ok").click(function() {
        addSubmitParam($("#fragment-group-form"), {tagId:tagId});
		document.forms["fragment-group-form"]["fragment-group-form:ok-"+ op +"-tag"].click();
	});
	var subMsg = "\n#" + tagId + "  " + getTagName(tagId);
    showConfirmDlg(deleting ? MSG.confirm_deleting : MSG.confirm_trashing, subMsg, "fa-trash", "orangered");
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
	    addSubmitParam($("#fragment-group-form"), {frgId0:frgId0,frgId1:frgId1})
		document.forms["fragment-group-form"]["fragment-group-form:ok-unrelate-fragments"].click();
	});
	var subMsg = "\n#"+frgId0 + "   " + getFragmentTitle(frgId0) +
		"\n#"+frgId1 + "   " + getFragmentTitle(frgId1);
    showConfirmDlg(MSG.confirm_unrelating, subMsg, "fa-unlink", "orange");
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

function confirmSignout() {
    $("#fragment-group-form\\:ok").click(function() {
        document.forms["fragment-group-form"]["fragment-group-form:ok-signout"].click();
    });
  
    showConfirmDlg(MSG.confirm_signout, null, "fa-sign-out", "orangered");
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

	confirmTrashingFragments(frgId, deleting);
}

function restoreFragmentFromCtxtMenu() {
    var menu = $("#frg-context-menu");
    var target = menu.data("target-frg");
    var frgId = target.attr("_fid");
    
    confirmRestoringFragments(frgId);
}

function showSearchDialog(panelId, qsPhrase) {
    var dlg = PF("searchDlg");
    
    var panelBtns = $("#target-panels-on-search-dlg");
    panelBtns.buttonset();
    $("#panel-radio-on-search-dlg-"+panelId).prop("checked", true);
    panelBtns.buttonset("refresh").find("input[type=radio]").off("change").on("change", function(e) {
        var pid = $(e.currentTarget).attr("_pid");
        dlg.cvzCurPanelId = pid;
    });

	dlg.show();
	dlg.jq.off("keyup").on("keyup", function(e) {
	    if (e.ctrlKey && e.which == $.ui.keyCode.SPACE) {
            dlg.cvzCurPanelId = (dlg.cvzCurPanelId + 1) % 3;
            $("#panel-radio-on-search-dlg-"+dlg.cvzCurPanelId).prop("checked", true);
            panelBtns.buttonset("refresh");
        }
	}).find(".ui-dialog-title").text(MSG.label_search);
	dlg.cvzCurPanelId = panelId;
	
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
		    searchFragmentsForPanel(dlg.cvzCurPanelId);
		}
	});
	qsInput.keypress(function(event) {
		// Quick search tab responds to the enter key
		if (event.which == $.ui.keyCode.ENTER) {
			if ($(this).val().trim()) {
			    searchFragmentsForPanel(dlg.cvzCurPanelId);
			}
		}
	});
}

function searchWithHelpFromLastSearch(event, panelId, widget) {
	if (event.which == $.ui.keyCode.ENTER) {
		var qsPhrase = $(widget).val().trim();
		if (qsPhrase) {
			$("#fragment-group-form\\:search-panel\\:quick-search-input").val(qsPhrase);
			searchFragmentsForPanel(panelId);
		}
	}
}

function fetchFragments(panelId, fragmentIds) {
	var ids = "id:";
	for (var i=0; i<fragmentIds.length; ++i) {
		ids += fragmentIds[i] + " ";
	}
	$("#fragment-group-form\\:search-panel\\:quick-search-input").val(ids);
	searchFragmentsForPanel(panelId);
}

function searchFragmentsForPanel(panelId) {
    sessionStorage.setItem("panel-"+panelId, "on");
    searchFragments([{name:'panelId',value:panelId}]);
}

function makeSidebarTitleToggleable() {
    var bookmarkPanel = PF('bookmarkPanel');
    var tagPalettePanel = PF('tagPalettePanel');
    var fileBoxPanel = PF('fileBoxPanel');
    var selectionBoxPanel = PF('selectionBoxPanel');
    var iconClass="toggle-icon";

    var bmLink = addToggler($("#bookmark-title"), iconClass, function() {
		bookmarkPanel.toggle();
		localStorage.setItem("bookmarkOpen", bookmarkPanel.cfg.collapsed ? "no":"yes");
	});
	var tpLink = addToggler($("#tag-palette-title"), iconClass, function() {
	    tagPalettePanel.toggle();
	    localStorage.setItem("tagPaletteOpen", tagPalettePanel.cfg.collapsed ? "no":"yes");
	});
	var sbLink = addToggler($("#selection-box-title"), iconClass, function() {
	    selectionBoxPanel.toggle();
	    localStorage.setItem("selectionBoxOpen", selectionBoxPanel.cfg.collapsed ? "no":"yes");
	});
	var fbLink = addToggler($("#file-box-title"), iconClass, function() {
	    fileBoxPanel.toggle();
	    localStorage.setItem("fileBoxOpen", fileBoxPanel.cfg.collapsed ? "no":"yes");
	});
	
	if (localStorage.getItem("bookmarkOpen") === "no")
	    bmLink.trigger("click");
	if (localStorage.getItem("tagPaletteOpen") === "no")
	    tpLink.trigger("click");
	if (localStorage.getItem("selectionBoxPanel") === "no")
	    sbLink.trigger("click");
	if (localStorage.getItem("fileBoxOpen") === "no")
	    fbLink.trigger("click");
}

function sortFragments(fragments, panelId) {
    var optIdx = PF("frgSortOpt"+panelId).jq.find(".ui-state-highlight").index();
    var sign = PF("orderAsc"+panelId).input[0].checked*2 - 1;
    var fmt = MSG.date_time_format_js;
    var locale = $("html").attr("lang");
    switch (optIdx) {
    case 0: // sort by updated time
        fragments.sort(function(a, b) {
            var aa = moment($(a).find(".-cvz-data-ut").text(), fmt, locale);
            var bb = moment($(b).find(".-cvz-data-ut").text(), fmt, locale);
            return sign*aa.diff(bb, "minutes");
        });
        break;
    case 1: // sort by created time
        fragments.sort(function(a, b) {
            var aa = moment($(a).find(".-cvz-data-ct").text(), fmt, locale);
            var bb = moment($(b).find(".-cvz-data-ct").text(), fmt, locale);
            return sign*aa.diff(bb, "minutes");
        });
        break;
    case 2: // sort by title
        fragments.sort(function(a, b) {
            var aa = $(a).find(".-cvz-data-title").text().toLowerCase();
            var bb = $(b).find(".-cvz-data-title").text().toLowerCase();
            return sign*((aa < bb) ? -1 : ((aa > bb) ? 1 : 0));
        });
        break;
    case 3: // sort by id
        fragments.sort(function(a, b) {
            var aa = parseInt($(a).find(".fragment-header").attr("_fid"), 10);
            var bb = parseInt($(b).find(".fragment-header").attr("_fid"), 10);
            return sign*(aa - bb);
        });
        break;
    default:
        break;
    }
    fragments.detach().appendTo("#fragment-panel-"+panelId);
}

function onClickGoSort(panelId) {
    var dlg = PF("sortOptionDlg" + panelId)
    var cbCurPageOnly = dlg.jq.find("#cb-curpageonly"+panelId);
    if (cbCurPageOnly.prop("checked")) {
        sortFragments($("#fragment-panel-"+panelId+" .each-fragment-container"), panelId);
        dlg.hide();
    }
    else {
        document.forms["fragment-group-form"]["fragment-group-form:go-sort-action"+panelId].click()
    }
}

function _touchFragment() {
    var menu = $("#frg-context-menu");
    var target = menu.data("target-frg");
    var frgId = target.attr("_fid");
    
    touchFragment([{name:'fragmentId', value:frgId}]);
}
