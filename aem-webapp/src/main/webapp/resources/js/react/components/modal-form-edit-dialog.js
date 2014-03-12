/**
 * Form edit modal dialog box.
 *
 * Properties:
 *
 * 1. formId - id of the form that the modal dialog will contain
 * 2. title - title of the modal dialog form
 *
 */
var ModalFormEditDialog = React.createClass({
    getInitialState: function() {
    },
    render: function() {
        return React.DOM.div({style:{"display":"none"}})
    },
    show: function(data, callback) {
        var modalDialog = this;
        var title = this.props.title;

        $(this.getDOMNode()).append($("#" + this.props.formId));

        $(this.getDOMNode()).dialog({
            resizable: false,
            modal: true,
            title: title,
            height: "auto",
            width: "auto",
            buttons: {
                "Ok": function () {
                    modalDialog.updateItem(callback);
                    modalDialog.destroy(this, data);
                },
                    "Cancel": function () {
                    modalDialog.destroy(this, data);
                }
            },
            create: function() {

                $.each(data, function(i, obj) {
                    $("#" + i).val(obj);
                });

            },
            close: function() {
                modalDialog.destroy(this, data);
            }
        });
    },
    destroy: function(theDialog, data) {
        // reset form data
        $.each(data, function(i, obj) {
            $("#" + i).val("");
        });

        $(theDialog).dialog("destroy");
    },
    updateItem: function(callback) {
        var formId = "#" + this.props.formId;
        var serializedArr = $(formId).serializeArray();
        var urlData = "";
        $.each(serializedArr, function(i, obj) {
            urlData = urlData + "/" + escape(obj.value);
        });

        $(formId).one("submit", function(e) {
            var postData = serializedFormToJson($(this).serializeArray());
            var formURL = $(this).attr("action");
                $.ajax({
                url : formURL + urlData,
                type: "PUT",
                data: postData,
                success:function(data, textStatus, jqXHR) {
                    callback();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    // TODO: Display error message in another modal dialog.
                    alert(textStatus);
                }
            });
            e.preventDefault(); // stop the default action
        });
        $(formId).submit();
    },

});

/**
 * Form data converted to a serialized array will have a constant name-value pair i.e.
 * [{name:value}...] therefore using JSON stringify on it will not produce
 * the desired JSON format thus the need for the function serializedFormToJson
 * to correctly build the JSON data.
 *
 * Example of form data converted to a serialized array then "stringified"
 *
 * [{"name":"jvmName","value":"default-jvm-name"},{"name":"hostName","value":"default-host-name"}]
 *
 * NOTE: This is not what we want! It should be like this:
 *
 * [{"jvmName":"default-jvm-name"},{"hostName":"default-host-name"}]
 *
 */
var serializedFormToJson = function(serializedArray) {
    var json = {};
    $.each(serializedArray, function() {
        json[this.name] = this.value;
    });
    return JSON.stringify(json);
}