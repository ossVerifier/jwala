/**
 * sources: http://coding.abel.nu/2012/01/jquery-ui-replacement-for-alert/
            http://stackoverflow.com/questions/702510/jquery-dialog-theme-and-style
 */
$.extend({ errorAlert: function (message, dlgTitle, modal, content) {
        /**
         * Exclude React minified exceptions since it is not very informative.
         * It only says there was an error in the minified React source code.
         * Developers should test and troubleshoot in dev mode to get better and helpful error/warning messages.
         * Actual users don't need to see it as well. If the problem is critical it will manifest itself as a
         * problem in the UI's functionality.
         */
        if (message.indexOf("Minified exception occurred;") === -1) {
            // check if message is in html, if it is, extract main cause.
            if (message !== undefined && message !== null) {
                if (message.indexOf("<html>") > -1) {
                    message = message.substring(message.indexOf("<h1>") + 4, message.indexOf("</h1>"));
                }
            }

            if (content) {
                message += "<div class='textAlignLeft'><ul>";
                for (var property in content) {
                    if (content.hasOwnProperty(property)) {
                        message += "<li>";
                        message += property + ": " + content[property];
                        message += "</li>"
                    }
                }
                message += "</ul></div>"
            }

            $("<div style='font-size:14px'></div>").dialog( {
                buttons: { "Ok": function () { $(this).dialog("close"); } },
                close: function (event, ui) { $(this).remove(); },
                resizable: true,
                width: 500,
                title: dlgTitle || "ERROR",
                modal: modal !== undefined ? modal : true,
                open: function () {
                    $(this).parents(".ui-dialog:first").find(".ui-dialog-titlebar").addClass("ui-state-error");
                    $(this).parents(".ui-dialog:first").zIndex(999);
                }}).html($.parseHTML(message));
        }
    }
});