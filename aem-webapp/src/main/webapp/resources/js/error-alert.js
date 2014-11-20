/**
 * sources: http://coding.abel.nu/2012/01/jquery-ui-replacement-for-alert/
            http://stackoverflow.com/questions/702510/jquery-dialog-theme-and-style
 */
$.extend({ errorAlert: function (message, dlgTitle) {
      $("<div style='font-size:16px'></div>").dialog( {
        buttons: { "Ok": function () { $(this).dialog("close"); } },
        close: function (event, ui) { $(this).remove(); },
        resizable: false,
        title: dlgTitle || "ERROR",
        modal: true,
        open: function () {
            $(this).parents(".ui-dialog:first").find(".ui-dialog-titlebar").addClass("ui-state-error");
            $(this).parents(".ui-dialog:first").zIndex(999);
        }
      }).text(message);
    }
});