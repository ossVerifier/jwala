/**
 * Form add modal dialog box.
 *
 * Properties:
 *
 * 1. formId - id of the form that the modal dialog will contain
 * 2. title - title of the modal dialog form
 *
 */
var ModalFormAddDialog = React.createClass({
    getInitialState: function() {
    },
    render: function() {
        return React.DOM.div({style:{"display":"none"}})
    },
    show: function(callback) {
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
                    var thisDialog = this;
                    modalDialog.addItem(callback, function() {
                        $(thisDialog).dialog("destroy");
                    });
                },
                    "Cancel": function () {
                    $(this).dialog("destroy");
                }
            },
            close: function() {
                $(this).dialog("destroy");
            }
        });
    },
    addItem: function(callback, callerCallback) {
        var formId = "#" + this.props.formId;

        $(formId).validate({
            ignore: ":hidden",
            submitHandler: function (form) {

                var postData = serializedFormToJsonNoId($(formId).serializeArray());
                var formURL = $(form).attr("action");
                $.ajax({url : formURL,
                        type: "POST",
                        data: postData,
                        contentType: "application/json",
                        dataType: "json",
                        success:function(data, textStatus, jqXHR) {
                            callback();
                            callerCallback();
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            // TODO: Display error message in another modal dialog.
                            alert(textStatus);
                        }
                });
                return false;  //e.preventDefault(); // stop the default action
            }

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
var serializedFormToJsonNoId = function(serializedArray) {
    var json = {};
    $.each(serializedArray, function() {
        if (this.name !== "id") {
            json[this.name] = this.value;
        }
    });
    return JSON.stringify(json);
}