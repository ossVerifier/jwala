/**
 * sources: http://coding.abel.nu/2012/01/jquery-ui-replacement-for-alert/
 */
$.extend({ alert: function (message) {
  $("<div></div>").dialog( {
    buttons: { "Ok": function () { $(this).dialog("close"); } },
    close: function (event, ui) { $(this).remove(); },
    resizable: false,
    title: "Message",
    modal: true
  }).text(message);
}
});