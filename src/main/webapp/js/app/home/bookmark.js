function setContextMenuForBookmarks() {
	var menu = $("#bookmark-context-menu");
   	
	$(".each-bookmark").bind("contextmenu", function(event) {
	    showPopup(menu, event);
    	var target = $(event.target).closest(".each-bookmark");
    	menu.data("target-bookmark", target);
    	event.preventDefault();
    });

    $(document).bind("click", function(event) {
    	menu.hide();
    });
}

function confirmUnbookmarkingFragment() {
	var target = $("#bookmark-context-menu").data("target-bookmark");
	var fid = target.attr("_bid");
	var subMsg = "\n#"+fid + "  " + target.attr("_ft");
	$("#fragment-group-form\\:ok").click(function() {
	    addSubmitParam($("#fragment-group-form"), {fragmentId:fid});
	    document.forms["fragment-group-form"]["fragment-group-form:ok-unbookmark-fragment"].click();
	});
	showConfirmDlg(MSG.confirm_unbookmarking, subMsg, "fa-close", "orange");
}
