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
