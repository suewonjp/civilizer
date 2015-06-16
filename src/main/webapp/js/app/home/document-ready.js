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
	
	$("#container").show();
	
	disableAutoSubmitOnEnterForForms($("#tag-palette-form"));
	
	setTimeout(function() {
    	// timeout for message display if any
    	$(".ui-messages-close").trigger("click");
    }, 15000);
});
