function setContextMenuForBookmarks() {
	var menu = $("#bookmark-context-menu");
   	
	$(".each-bookmark").bind("contextmenu", function(event) {
	    showPopup(menu, event);
    	var target = $(event.target).closest(".each-bookmark");
    	menu.data("target-bookmark", target);
    	$("#fragment-group-form\\:id-placeholder-for-fragment").val(target.attr("_bid"));
    	event.preventDefault();
    });

    $(document).bind("click", function(event) {
    	menu.hide();
    });
}

function confirmUnbookmarkingFragment() {
	$("#fragment-group-form\\:ok").click(function() {
		document.forms["fragment-group-form"]["fragment-group-form:ok-unbookmark-fragment"].click();
	});
	var target = $("#bookmark-context-menu").data("target-bookmark");
	var subMsg = "\n#"+target.attr("_bid") + "  " + target.attr("_ft");
	showConfirmDlg(MSG.confirm_unbookmarking, subMsg, "fa-close", "orange");
}
