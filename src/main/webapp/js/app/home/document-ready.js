$(document).ready(function() {
	makeObjectsInsertableToTextArea();

	setupParser();
	
	translateFragments();

	setupFragmentOverlay();
    
    setupFragmentCheckboxes();
    
    setupFragmentResolutionSliders();
    
    setupDragAndDrop();
    
	setupPanelActivationButtons();
    
	setupFragmentEditor();
    
    autocompleteForTypingTags();
    
	setupTabViewsForTagPalette();
	
	setContextMenuForFragments();
    setContextMenuForBookmarks();
    setContextMenuForTags();
    setContextMenuForSelections();
	setContextMenuForFiles();
	
	makeSidebarTitleToggleable();
	
	setCurrentTheme("ui-darkness");
	
	$("#container").show();
	
	disableAutoSubmitOnEnterForForms($("#fragment-editor-form"));
	disableAutoSubmitOnEnterForForms($("#tag-palette-form"));
	
	setTimeout(function() {
    	// timeout for message display if any
    	$(".ui-messages-close").trigger("click");
    }, 15000);
    
    $(".rclick-disabled").bind("contextmenu", function(e) {
        e.preventDefault(); 
    });
});

$(window).load(function() {
    applyCurrentThemeToThemeSwitcher();
});

