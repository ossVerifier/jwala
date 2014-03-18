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
        validator = null;
    },
    render: function() {
        return React.DOM.div({style:{"display":"none"}})
    },
    show: function(callback) {
        var thisComponent = this;
        var title = this.props.title;
        var formId = "#" + this.props.formId;

        $(this.getDOMNode()).append($(formId));

        $(this.getDOMNode()).dialog({
            resizable: false,
            modal: true,
            title: title,
            height: "auto",
            width: "auto",
            buttons: {
                "Ok": function () {
                    var thisDialog = this;
                    thisComponent.addItem([callback, function() {
                        thisComponent.destroy(thisDialog);
                    }]);
                },
                    "Cancel": function () {
                    thisComponent.destroy(this);
                }
            },
            close: function() {
                thisComponent.destroy(this);
            }
        });
    },
    addItem: function(callbacks) {
        var formId = "#" + this.props.formId;

        $(formId).one("submit", function(e) {
            var postData = serializedFormToJsonNoId($(formId).serializeArray());
            var formURL = $(formId).attr("action");
            $.ajax({url : formURL,
                    type: "POST",
                    cache: false,
                    data: postData,
                    contentType: "application/json",
                    dataType: "json",
                    success:function(data, textStatus, jqXHR) {
                        for (var i = 0; i < callbacks.length; i++) {
                            callbacks[i]();
                        }
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        var msg = jqXHR.responseText;
                        var msgStr = "";
                        if (msg !== undefined) {
                            var jsonObj = JSON.parse(jqXHR.responseText);
                            msgStr = jsonObj.message;
                        }
                        $.errorAlert(errorThrown + ": " + msgStr, "Error");
                    }
            });
            e.preventDefault(); // stop the default action
        });

        this.props.validator.cancelSubmit = true;
        this.props.validator.form();
        if (this.props.validator.numberOfInvalids() === 0) {
            $(formId).submit();
        }
    },
    destroy: function(theDialog) {
        this.props.validator.resetForm();
        $(theDialog).find("input").val("");
        $(theDialog).dialog("destroy");
    }
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