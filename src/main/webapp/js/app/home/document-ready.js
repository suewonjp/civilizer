$(document).ready(function() {
	makeObjectsInsertableToTextArea();

	setupParser();
	
	translateFragments();

	setupFragmentOverlay();
	
	setupClickHandlerForTags($("#container"));
    
    setupFragmentCheckboxes();
    
    setupFragmentResolutionSliders();
    
    setupDragAndDrop();
    
	setupPanelActivationButtons();
    
	setupFragmentEditor();
    
    autocompleteForTypingTags();
    
	setupTabViewsForTagPalette();
	
	setupContextMenus();
	
	makeSidebarToggleable();
	
	makeSidebarTitleToggleable();
	
	setCurrentTheme("ui-darkness");
	
	$("#container").show();
	
	disableAutoSubmitOnEnterForForms($("#fragment-editor-form"));
	disableAutoSubmitOnEnterForForms($("#tag-palette-form"));
	
	setTimeout(function() {
    	// timeout for message display if any
    	$(".ui-messages-close").trigger("click");
    }, 15000);
    
    $(window).off("keyup.cvz_global_hotkey").on("keyup.cvz_global_hotkey", onGlobalHotkeys);
});

$(window).load(function() {
    applyCurrentThemeToThemeSwitcher();
});

function onGlobalHotkeys(e) {
    switch (e.keyCode) {
    case $.ui.keyCode.SPACE:
        if (e.ctrlKey) {
            if (!PF("searchDlg").isVisible())
                showSearchDialog(0, null);
        }
        break;
    }
}

