function formatDatetime(src) {
	return moment(src, MSG.date_time_format_js, $("html").attr("lang")).fromNow() + "; " + src;
}

function formatTagsOnFragmentHeader(tgtTag) {
    var tid = tgtTag.attr("_tid");
    var srcTag = getTag(tid);
    tgtTag[0].title =
        srcTag.attr("_frgCnt")+" ("+srcTag.attr("_frgCntWtHrc")+") "
        +MSG.fragments+"; "+MSG.rclick_for_menu;
    if (srcTag.hasClass("special-tag"))
    	tgtTag.addClass("special-tag");
}

function fragmentTrashed($frgHdr) {
    var fid = $frgHdr.attr("_fid");
    if (fid) {
        var ids = $("#trashed-fragment-ids span"), i, c;
        for (i=0,c=ids.length; i<c; ++i) {
            if ($(ids[i]).text() === fid) return true;
        }
        return false;
    }
    return $frgHdr.attr("_deletable") === "true";
}

function triggerFragmentOverlay(e) {
	var href=$(this).attr('href');
	$.get(href, "", populateFragmentOverlay);
	e.preventDefault();
	return false;
}

function setupFragmentOverlay() {
    $("body").off("click.cvz_frg_overlay")
    .on("click.cvz_frg_overlay", ".-cvz-frgm", triggerFragmentOverlay);
    
	$("#fragment-overlay-title-bar").dblclick(function() {
    	toggleWindow($("#fragment-overlay"), $(this));
    });
}

function setupRelatedFragments() {
    $(".related-fragment-container .small-fragment-box").each(function() {
        var $this = $(this);
        if (fragmentTrashed($this))
            $this.addClass("trashed-fragment-title");
    });
}

function setupFragmentCheckboxes() {
	function generateClickHandler(panelId) {
    	var fragmentCount = $("#fragment-panel-" + panelId + " .each-fragment-container").length;
        if (fragmentCount > 0) {
            var fragmentCheckboxSlaves = [];    
            
            for (var j=0; j<fragmentCount; ++j) {
                var cbName = "fragmentCheckboxSlave" + panelId + "_" + j,
                    cb = PF(cbName);
                cb.jq.closest(".fragment-header").data("pfCheckbox", cbName);
                fragmentCheckboxSlaves.push(cb);
            }
        
            var fragmentCheckboxMaster = PF("fragmentCheckboxMaster" + panelId);
            
            // Align the master checkbox with its slaves in horizontal position
            fragmentCheckboxMaster.jq.offset({ left:fragmentCheckboxSlaves[0].jq.offset().left });
            
            return function (e) {
                var icon = fragmentCheckboxMaster.jq.find(".ui-chkbox-icon");
                if (icon.hasClass("ui-icon-cvz-check")) {
                    for (var i=0; i<fragmentCount; ++i) {
                        fragmentCheckboxSlaves[i].uncheck();
                    }
                    icon.removeClass("ui-icon-cvz-check ui-icon-check").addClass("ui-icon-blank");
                }
                else {
                    for (var i=0; i<fragmentCount; ++i) {
                        fragmentCheckboxSlaves[i].check();
                    }
                    icon.removeClass("ui-icon-blank").addClass("ui-icon-cvz-check ui-icon-check");
                }
            }
        }
    }
    
    var fragmentCheckboxMaster, panelId;
    
    for (panelId=0; panelId<3; ++panelId) {
        fragmentCheckboxMaster = PrimeFaces.widgets["fragmentCheckboxMaster"+panelId];
        if (fragmentCheckboxMaster) {
            fragmentCheckboxMaster.jq.find("input").click(generateClickHandler(panelId));
            fragmentCheckboxMaster.jq.title = MSG.check_uncheck_all; // Attach a tooltip message
        }
    }
}

function setupFragmentResolutionSliders() {
    $("body").on("dblclick.frg_hdr", "#fragment-group .fragment-header", function() {
        var tgt = $(this).closest(".each-fragment").find("tbody");
        showOrHide(tgt, !tgt.is(":visible"));
        return false;
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
        change:function(e, ui) {
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

function setupTagLinks(content) {
    content.find("span.-cvz-tag").each(function() {
        var $this = $(this);
        var tagId = $this.text().trim();
        if (tagId) {
            var srcTag = getTag(tagId);
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

function findAvailablePanel() {
    var targetPanelId = 0;
    if ($("#panel-toggler-1").prop("checked") == false) targetPanelId = 1;
    else if ($("#panel-toggler-2").prop("checked") == false) targetPanelId = 2;
    return targetPanelId;
}

function fetchFragmentsByTag(from, to) {
	var targetPanelId = findPanel(to);
	if (targetPanelId < 0)
	    targetPanelId = findAvailablePanel();
	sessionStorage.setItem("panel-"+targetPanelId, "on");
    $("#panel-activation-buttons").buttonset("refresh");
    filterByTag([ {name:"tagId", value:from.attr("_tid")}, {name:"panelId", value:targetPanelId} ]);
}

function showSortOptionDialog(panelId) {
	PF("sortOptionDlg" + panelId).show();
}

function getFragmentTitle(frgId) {
	var title = $("#fragment-group").find(".fragment-title[_fid=" + frgId + "]");
	if (title.length == 0)
	    title = $("#fragment-overlay-content").find(".fragment-title[_fid=" + frgId + "]");
	if (title.length == 0)
	    return $(".small-fragment-box[_fid=" + frgId + "]").attr("_ft") || "";
	else
	    return (title.length > 0) ? title.eq(0).text() : "";
}

function getTag(tagId) {
    return $("#tag-palette-flat").find(".each-tag[_tid=" + tagId + "]").first();
}

function getTagName(tagId) {
	var tag = getTag(tagId);
	return (tag.length > 0) ? tag.find(".each-tag-name").first().text() : "";
}

function getTagByName(name) { // returns the exact tag for the name;
    var tags = getTagsByName(name);
    name = name.trim();
    for (var i=0; i<tags.length; ++i) {
        if (tags.eq(i).text().trim() == name)
            return tags.eq(i);
    }
}

function getTagsByName(name) { // returns multiple tags containing the name;
    name = name.trim();
    return $("#tag-palette-flat").find(".each-tag-name:contains('"+name+"')").closest(".each-tag");
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

function confirmSignout() {
    $("#fragment-group-form\\:ok").click(function() {
        document.forms["fragment-group-form"]["fragment-group-form:ok-signout"].click();
    });
  
    showConfirmDlg(MSG.confirm_signout, null, "fa-sign-out", "orangered");
}

function makeSidebarTitleToggleable() {
    var bookmarkPanel = PF('bookmarkPanel'),
        tagPalettePanel = PF('tagPalettePanel'),
        fileBoxPanel = PF('fileBoxPanel'),
        selectionBoxPanel = PF('selectionBoxPanel'),
        iconClass="toggle-icon",
        bmLink = addToggler($("#bookmark-title"), iconClass),
        tpLink = addToggler($("#tag-palette-title"), iconClass),
        sbLink = addToggler($("#selection-box-title"), iconClass),
        fbLink = addToggler($("#file-box-title"), iconClass);
    
    $("#sidebar").off("click").on("click", ".sidebar-title a", function(e) {
        var $this = $(this),
            id = $this.next("span").attr("id");
        $this.find("span").toggleClass("fa-plus-square fa-minus-square");
        e.preventDefault();
        switch (id) {
        case "bookmark-title":
            bookmarkPanel.toggle();
            localStorage.setItem("bookmarkOpen", bookmarkPanel.cfg.collapsed ? "no":"yes");
            break;
        case "tag-palette-title":
            tagPalettePanel.toggle();
            localStorage.setItem("tagPaletteOpen", tagPalettePanel.cfg.collapsed ? "no":"yes");
            break;
        case "selection-box-title":
            selectionBoxPanel.toggle();
            localStorage.setItem("selectionBoxOpen", selectionBoxPanel.cfg.collapsed ? "no":"yes");
            break;
        case "file-box-title":
            fileBoxPanel.toggle();
            localStorage.setItem("fileBoxOpen", fileBoxPanel.cfg.collapsed ? "no":"yes");
            break;
        }
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
        document.forms["fragment-group-form"]["fragment-group-form:go-sort-action"+panelId].click();
    }
}

function setupPanelToolbarArea() {
    var ptbs = $("#fragment-group-form\\:fragment-panel-toolbar-0, #fragment-group-form\\:fragment-panel-toolbar-1, #fragment-group-form\\:fragment-panel-toolbar-2");
    
    function onHoverOnPanelToolbarArea(pageX) {
        for (var i=0; i<3; ++i) {
            var panel = $("#fragment-panel-" + i);
            if (!panel.is(":visible"))
                continue;
            var panelToolbar = ptbs.eq(i);
            var left = panel.offset().left;
            if (left > pageX || pageX > left+panel.width()) {
                panelToolbar.css("visibility", "hidden");
                continue;
            }
            panelToolbar.css({
                visibility : "visible",
                left : panel.offset().left - $(window).scrollLeft(),
            });
        }
    }
    
    $("#panel-toolbar-area").off(".cvz_tba").on("mousemove.cvz_tba", function(e) {
        onHoverOnPanelToolbarArea(e.pageX);
        return false;
    });

    $("#content").off("click").on("click", function(e) {
        var tgt = $(e.target);
        if (tgt.is("#panel-toolbar-area, #panel-toolbar-area i") || ptbs.has(tgt).length)
            return false;
        else if ($("#content").has(tgt).length)
            ptbs.css("visibility", "hidden");
    });
}

function onStartReminder() {
    // Suspend polling while the message appears (to save network traffic)
    var poll = PF("reminderPoll"), timer = poll.restartTimer;
    poll.stop();

    // Make sure there be only zero or one timeout callback in any case
    if (timer)
        clearTimeout(timer);
    timer = setTimeout(function() {
        // Close the message and restart the polling
        $("#fragment-group-form\\:reminder-messages .ui-messages-close").trigger("click");
        var poll = PF("reminderPoll");
        if (poll.cfg.frequency > 0)
            poll.start();
        clearTimeout(poll.restartTimer);
    }, 1000*60*10); // Let it stay for 10 minutes unless the user closes it

    poll.restartTimer = timer;
}

function onClickReminderSetting(btn) {
    var parent = $(btn).closest(".ui-messages-info"),
        settingArea = parent.find(".reminder-setting-box");
    if (settingArea.length) {
        showOrHide(settingArea);
    }
    else {
        var interval = localStorage.getItem("reminder_interval") || SYSPROP.defReminderInterval,
            slider = $("<div class='reminder-setting-slider'>").slider({
                max: 24, min: 1, value: interval,
                change: function(e, ui) {
                    localStorage.setItem("reminder_interval", ui.value);
                    PF("reminderPoll").cfg.frequency = 3600 * ui.value;
                    settingArea.find("b").text(ui.value + " hours");
                }
            });
        settingArea = $("<div class='reminder-setting-box'><span>" + MSG.reminder_interval + " : </span><b>" + interval + " hours</b></div>")
        .append(slider);
        parent.append(settingArea);
    }
}

// [DEV]
if (SYSPROP.dev === "true") {
    var remindMeRightNow = function() {
        var poll = PF("reminderPoll");
        poll.cfg.frequency = 1;
        poll.start();
    }
}
