$(document).ready(function() {
	makeObjectsInsertableToTextArea();

	setupParser();
	
	translateFragments();

	setupFragmentOverlay();
	
	setupClickHandlerForTags($("#container"));
    
    setupFragmentResolutionSliders();
    
    setupDragAndDrop();
    
	setupPanelActivationButtons();
    
	setupFragmentEditor();
    
    autocompleteForTypingTags();
    
	setupTabViewsForTagPalette();
	
	setupContextMenus();
	
	makeSidebarToggleable();

	makeSidebarScrollable();
	
	makeSidebarTitleToggleable();
	
	$("#container").show();
	
	disableAutoSubmitOnEnterForForms("#fragment-editor-form", "#tag-palette-form", "#user-menu-dlg-form");
	
	setTimeout(function() {
    	// timeout for message display if any
    	$(".ui-messages-close").trigger("click");
    }, 15000);
    
    $(window).off("keyup.cvz_global_hotkey").on("keyup.cvz_global_hotkey", onGlobalHotkeys);
    
    setupSearchHistory();
});

$(window).load(function() {
    setCurrentTheme("ui-darkness");
    applyCurrentThemeToThemeSwitcher();
    setupFragmentCheckboxes();
});

function onGlobalHotkeys(e) {
    switch (e.keyCode) {
    case $.ui.keyCode.SPACE:
        if (e.ctrlKey && e.shiftKey) {
            if (!PF("searchDlg").isVisible()) {
                showSearchDialog(1, null);
                return false;
            }
        }
        break;
    }
}

