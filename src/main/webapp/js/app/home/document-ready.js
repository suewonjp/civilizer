$(document).ready(function() {
	makeObjectsInsertableToTextArea();

	setupParser();
	
	translateFragments();

	setupFragmentOverlay();
	
	setupRelatedFragments();
	
	setupClickHandlerForTags($("body"));
    
    setupFragmentResolutionSliders();
    
    setupDragAndDrop();
    
	setupPanelActivationButtons();
    
	setupFragmentEditor();
    
    autocompleteForTypingTags();
    
	setupTabViewsForTagPalette();
	
	setupFileInfo();
	
	setupContextMenus();
	
	makeSidebarToggleable();

	makeSidebarScrollable();
	
	makeSidebarTitleToggleable();
	
	setupPanelToolbarArea();
	
    $(".each-fragment th").addClass("ui-state-default");

    setCurrentTheme("aristo");
	
	$("#container").show();
	
	setupSearchHistory();
	
	disableAutoSubmitOnEnterForForms("#fragment-editor-form", "#tag-palette-form", "#user-menu-dlg-form");
	
	setTimeout(function() {
    	// timeout for message display if any
        $(".auto-closable .ui-messages-close").trigger("click");
    }, 15000);

    $(window).off("keyup.cvz_global_hotkey").on("keyup.cvz_global_hotkey", onGlobalHotkeys);
    
//    var appUrl = location.protocol + '//' + location.hostname;
//    $('a[href^="http://"], a[href^="https://"]').not('[href^="'+appUrl+'"]').attr('target','_blank');
});

$(window).load(function() {
    setupFragmentCheckboxes();
    applyCurrentThemeToThemeSwitcher();
    PF("reminderPoll").cfg.frequency = (localStorage.getItem("reminder_interval") || SYSPROP.defReminderInterval)*3600;
});

var curPanel = 0;

function onGlobalHotkeys(e) {
    switch (parseInt(e.keyCode)) {
    case $.ui.keyCode.SPACE: // Ctrl + Shift + Space; show the search dialog
        if (e.ctrlKey && e.shiftKey) {
            if (!PF("searchDlg").isVisible()) {
                showSearchDialog(1, null);
                return false;
            }
        }
        break;
    case 49: // Ctrl + Shift + 1; toggle panel 0
        if (e.ctrlKey && e.shiftKey) {
            $("#panel-toggler-0").click();
            return false;
        }
        break;
    case 50: // Ctrl + Shift + 2; toggle panel 1
    case 222: // For Safari
        if (e.ctrlKey && e.shiftKey) {
            $("#panel-toggler-1").click();
            return false;
        }
        break;
    case 51: // Ctrl + Shift + 3; toggle panel 2
        if (e.ctrlKey && e.shiftKey) {
            $("#panel-toggler-2").click();
            return false;
        }
        break;
    case 52: // Ctrl + Shift + 4; ; enable a panel and disable all others in turn
        if (e.ctrlKey && e.shiftKey) {
            if ($("#fragment-panel-0").is(":visible"))
                $("#panel-toggler-0").click();
            if ($("#fragment-panel-1").is(":visible"))
                $("#panel-toggler-1").click();
            if ($("#fragment-panel-2").is(":visible"))
                $("#panel-toggler-2").click();
            $("#panel-toggler-" + (++curPanel)%3).click();
            return false;
        }
        break;
    }
}

