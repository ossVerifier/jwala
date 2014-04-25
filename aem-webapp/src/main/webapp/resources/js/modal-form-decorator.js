/**
 * This method makes (or "decorates") a React form contained in a div as a JQuery UI Modal Dialog Box.
 * This replaces the "Modal Dialog Component" concept and it aims to clearly separate JQuery node mutation and
 * manipulation on a React modal form component.
 */
var decorateNodeAsModalFormDialog = function(divNode, title, okCallback, cancelCallback, closeCallback) {
    $(divNode).dialog({
        resizable: false,
        modal: true,
        title: title,
        height: "auto",
        width: "auto",
        buttons: {
            "Ok": function () {
                okCallback();
            },
            "Cancel": function () {
                cancelCallback();
            }
        },
        close: function() {
            closeCallback();
        }
    });
}