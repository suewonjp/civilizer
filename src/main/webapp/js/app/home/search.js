var SC = createSearchController();

function createSearchController() {
    var ctrr = new Object();
    var dlg, panelBtns, qsInput;
    var curPanelId = 1;
    
    function getDialog() {
        return dlg || (dlg = PF("searchDlg"));
    }

    function getPanelButtons() {
        return panelBtns || (panelBtns = $("#target-panels-on-search-dlg").buttonset());
    }
    
    function getQuickSearchInput() {
        return qsInput || (qsInput = $("#fragment-group-form\\:search-panel\\:quick-search-input"));
    }
    
    function fixEscKeyProblem() {
        // Hide the dialog with a ESC key press except when the quick search input is focused.
        // Without this code, pressing ESC key to hide autocomplete will hide the dialog,
        // which will be a very awkward user experience.
        var dlg = getDialog();
        var en = 'keyup.dialog_' + dlg.id;
        $(document).off(en).on(en, function(e) {
            active = parseInt(dlg.jq.css('z-index')) === PrimeFaces.zindex;
            if (e.which === $.ui.keyCode.ESCAPE && dlg.isVisible() && active) {
                var tgt = $(e.target);
                if (tgt.attr("id") == "fragment-group-form:search-panel:quick-search-input" ||
                    tgt.attr("id") == "fragment-group-form:search-panel:tag-keywords")
                    tgt.blur();
                else
                    dlg.hide();
            };
        });
    }
    
    function setupHandlers() {
        getDialog().jq.off(".cvz_sch_dlg")
        .on("change.cvz_sch_dlg", "#target-panels-on-search-dlg input[type=radio]", function(e) {
            var pid = $(e.currentTarget).attr("_pid");
            curPanelId = pid;
        })
        .on("keyup.cvz_sch_dlg", function(e) {
            if (e.ctrlKey && e.shiftKey && e.which == $.ui.keyCode.SPACE) {
                curPanelId = (curPanelId + 1) % 3;
                $("#panel-radio-on-search-dlg-"+curPanelId).prop("checked", true);
                getPanelButtons().buttonset("refresh");
            }
        })
        .on("keypress.cvz_sch_dlg", "#fragment-group-form\\:search-panel\\:quick-search-input", function(e) {
            if (e.which == $.ui.keyCode.ENTER) {
                // Quick search tab responds to the enter key
                if ($(this).val().trim()) {
                    searchFragmentsForPanel(curPanelId);
                }
            }
            else if (e.which == $.ui.keyCode.ESCAPE) {
                $(e.target).blur();
            }
        })
        .on("click.cvz_sch_dlg", "#fragment-group-form\\:go-search", function() {
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
                    getQuickSearchInput().val(null);
                }
            }
            else {
                // Quick search tab is focused
                if (getQuickSearchInput().val().trim()) {
                    hasSomeToSearch = true;
                }
            }
            if (hasSomeToSearch) {
                searchFragmentsForPanel(curPanelId);
            }
        })
        .on("click.cvz_sch_dlg", "#search-hist li a", function() {
            var $this = $(this);
            var idx = $this.attr("_idx");
            if (idx) {
                removeSearchHistoryEntity(idx);
            }
            else {
                var phrase = $(this).next("input").val().trim();
                getQuickSearchInput().val(phrase);
                searchFragmentsForPanel(curPanelId);
            }
        })
        .on("keypress.cvz_sch_dlg", "#search-hist li input", function(e) {
            if (e.which == $.ui.keyCode.ENTER) {
                var phrase = $(this).val().trim();
                getQuickSearchInput().val(phrase);
                searchFragmentsForPanel(curPanelId);
            }
        })
        ;
    }
    
    ctrr.init = function() {
        if (dlg)
            return;
        getDialog().jq.find(".ui-dialog-title").text(MSG.search);
        
        fixEscKeyProblem();
        
        setupHandlers();
        
        $("#fragment-group-form\\:search-panel\\:tag-keywords").watermark(MSG.how_to_input_tags);
    }
    
    ctrr.showDialog = function(panelId, qsPhrase) {
        this.init();
        $("#panel-radio-on-search-dlg-"+panelId).prop("checked", true);
        getPanelButtons().buttonset("refresh");
        curPanelId = panelId;
        getQuickSearchInput().val(qsPhrase);
        dlg.show();
    }    
    
    return ctrr;
}

function showSearchDialog(panelId, qsPhrase) {
    SC.showDialog(panelId, qsPhrase);
}

function searchWithHelpFromLastSearch(e, panelId, widget) {
    if (e.which == $.ui.keyCode.ENTER) {
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

function setupSearchHistoryUI(hist) {    
    var list = $("#search-hist").empty();
    for (var i=0, j=hist.length; i<j; ++i) {
        list.append(
            "<li><a href='#' class='ui-panel ui-widget-content ui-corner-all fa fa-search button-link'/><input type='text' value='"
            + hist[i]
            + "'/><a href='#' _idx='" + i + "' class='fa fa-minus-circle button-link'/></li>");
    }    
}

function setupSearchHistory() {
    var histStr = localStorage.getItem("srch-hist");
    var hist = JSON.parse(histStr) || [];
    $(".last-search-phrase").each(function(idx) {        
        var phrase = $(this).text();
        if ($("#last-search-phrase-"+idx).is(":visible") && phrase) {
            var idx = hist.indexOf(phrase);
            if (idx > -1) {
                hist.splice(idx, 1);
            }
            hist.unshift(phrase);
            if (hist.length > 7)
                hist.pop();
        }
    });

    setupSearchHistoryUI(hist);
    
    localStorage.setItem("srch-hist", JSON.stringify(hist));
}

function removeSearchHistoryEntity(idx) {
    var histStr = localStorage.getItem("srch-hist");
    var hist = JSON.parse(histStr) || [];
    
    hist.splice(idx, 1);
    
    setupSearchHistoryUI(hist);
    
    localStorage.setItem("srch-hist", JSON.stringify(hist));
}

