function setupPanelActivationButtons() {
	$("#panel-activation-buttons").buttonset();

    var activeCount = 0;
    for (var i = 0; i < 3; i++) {
        if (sessionStorage.getItem('panel-' + i) === "on") {
            ++activeCount;
            $("#panel-toggler-" + i).prop("checked", true);
        }
    }
    if (activeCount === 0) {
        $("#panel-toggler-0").prop("checked", true);
    }
    
    var phPanel = $("#fragment-group-form\\:id-placeholder-for-panel");
    var idSpecified = phPanel.val();
    if (idSpecified > -1) {
        $("#panel-toggler-" + idSpecified).prop("checked", true);
        phPanel.val(null);
    }

    $("#panel-activation-buttons").buttonset("refresh");
    
    onPanelActivationChange(false);
}

function onPanelActivationChange(explicit) {
    var panels = [ $("#fragment-panel-0"), $("#fragment-panel-1"), $("#fragment-panel-2") ];
    var activeCount = 0;
    
    for (var i=0; i<3; ++i) {
        if ($("#panel-toggler-" + i).prop("checked")) {
            ++activeCount;
            panels[i].show();
            $("fragment-group-form\\:fragment-panel-toolbar-"+i).offset({
                left: panels[i].offset().left
            });
            sessionStorage.setItem('panel-' + i, 'on');
        }
        else {
            panels[i].hide();
            sessionStorage.setItem('panel-' + i, 'off');
            if (explicit)
                document.forms["panel-button-form"]["panel-button-form:id-cleaner"].click();
        }
    }
    
    var w = '100%';
    switch (activeCount) {
    case 2:
        w = '45%';
        break;
    case 3: 
        w = '30%';
        break;
    }
    for (var i=0; i<3; ++i) {
        panels[i].css({ width: w });
    }
}

function toggleUserProfileDlgSaveBtn() {
    var dlg = PF("userProfileDlg");
    var inplace = PF("userName");
    var saveBtn = PF("userProfileDlgSave");
    (inplace.jq.data("modified") || dlg.jq.find("input[name='enable_password_change']").prop("checked"))
    ? saveBtn.enable() : saveBtn.disable();
}

function showAboutDialog() {
    var dlg = PF("aboutDlg");
    dlg.show();
}

function createDataBrokerController() {
    var helper = new Object();
    var dlg;
    var wzd;
    
    function getDialog() {
        return dlg || (dlg = PF('dataBrokerDlg'));
    }

    function getWizard() {
        return wzd || (wzd = PF('dataBrokerWizard'));
    }
    
    helper.showDialog = function() {
        var w = getWizard();
        w.loadStep(w.cfg.steps[0], false);
        
        var d = getDialog();
        d.jq.find(".ui-wizard-nav-next").hide();
        d.show();
    }
    
    helper.hideDialog = function() {
        getDialog().hide();
    }
    
    helper.setMode = function(exportMode) {
        $("#user-menu-form\\:data-broker-export-mode").val(exportMode);
        if (exportMode)
            getDialog().jq.find(".ui-dialog-title").text(MSG.export_data);
        else
            getDialog().jq.find(".ui-dialog-title").text(MSG.import_data);
    }
    
    helper.onTypePw = function(pwInput, e) {
        var nextBtn = dlg.jq.find(".ui-wizard-nav-next");
        if (event.which == $.ui.keyCode.ENTER) {
            nextBtn.hide();
        }
        else
            showOrHide(nextBtn, $(pwInput).val());
    }
    
    helper.onClickNext = function(btn) {
        wzd.next();
        $(btn).hide();
    }
    
    helper.onCompleteNext = function(xhr, status, args) {
        if (args.authFailed === true) {
            $("#user-menu-form\\:dbw-pw").effect("shake");
        }
        else if (args.exportReady === true) {
            requestExportDataFile();
        }
    }
    
    helper.onExportDataReady = function(xhr, status, args) {
        if (args.exportReady === true) { // success
            wzd.next();
        }
    }
    
    return helper;
}

var DBC = createDataBrokerController();

function showProfileDialog() {
    var dlg = PF("userProfileDlg");
    var changePwCb = dlg.jq.find("input[type=checkbox]").prop("checked", false);
    togglePasswordChange(changePwCb);
    $("#user-menu-form\\:pwd1").val(null);
    $("#user-menu-form\\:pwd2").val(null);
    var inplace = PF("userName");
    
    function onInplaceCommit(val, text) {
        inplace.jq.data("modified", val !== text);
        toggleUserProfileDlgSaveBtn();
    }
    
    initPfInplaceWidget(
            inplace
            , inplace.jq.next("span").text()
            , dlg.jq.find("div.ui-panel").eq(0)
            , onInplaceCommit
    );    
    onInplaceCommit("", "");
    
    dlg.show();
}

function togglePasswordChange(widget) {
    var checked = $(widget).prop("checked");
    showOrHide($("#new-password-box"), checked);
    var dlg = PF("userProfileDlg");
    dlg.jq.find(".ui-messages").hide();
    toggleUserProfileDlgSaveBtn();
}

function setCurrentTheme(defaultTheme) {
    var theme = localStorage.getItem("theme");
    if (! theme) {
        theme = defaultTheme;
        localStorage.setItem("theme", theme);
    }
    PrimeFaces.changeTheme(theme);
}

function applyCurrentThemeToThemeSwitcher() {
    var theme = localStorage.getItem("theme");
    if (theme) {
        selectItemOnPfListbox(theme, PF('themeSwitcher'));
    }
}

function onChangeTheme() {
    var switcher = PF('themeSwitcher');
    var theme = switcher.getSelectedValue();
    if (theme) {
        localStorage.setItem("theme", theme);
    }
}

