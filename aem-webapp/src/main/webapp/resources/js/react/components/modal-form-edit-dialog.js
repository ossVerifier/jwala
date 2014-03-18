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
        submissionInProgress = false;
    },
    render: function() {
        return React.DOM.div({id:"tempId" ,style:{"display":"none"}})
    },
    show: function(data, callback) {
        var thisComponent = this;
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
                    thisComponent.updateItem([callback, function(){
                        thisComponent.destroy(thisDialog, data);
                    }]);
                },
                    "Cancel": function () {
                    thisComponent.destroy(this, data);
                }
            },
            create: function() {

                $.each(data, function(i, obj) {
                    $("#" + i).val(obj);
                });

            },
            close: function() {
                thisComponent.destroy(this, data);
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
    updateItem: function(callbacks) {
        var formId = "#" + this.props.formId;
        var serializedArr = $(formId).serializeArray();
        var urlData = "";
        $.each(serializedArr, function(i, obj) {
            urlData = urlData + "/" + escape(obj.value);
        });

        $(formId).one("submit", function(e) {

            if (submissionInProgress === false) {
                submissionInProgress = true;
                var postData = serializedFormToJson($(this).serializeArray());
                var formURL = $(formId).attr("action");
                    $.ajax({
                    url : formURL + urlData,
                    type: "PUT",
                    data: postData,
                    success:function(data, textStatus, jqXHR) {
                        submissionInProgress = false;
                        for (var i = 0; i < callbacks.length; i++) {
                            callbacks[i]();
                        }
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        submissionInProgress = false;
                        var msg = jqXHR.responseText;
                        var msgStr = "";
                        if (msg !== undefined) {
                            var jsonObj = JSON.parse(jqXHR.responseText);
                            msgStr = jsonObj.message;
                        }
                        $.errorAlert(errorThrown + ": " + msgStr, "Error");
                    }
                });
            }

            e.preventDefault();
        });

        this.props.validator.cancelSubmit = true;
        this.props.validator.form();
        if (this.props.validator.numberOfInvalids() === 0) {
            $(formId).submit();
        }
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
var serializedFormToJson = function(serializedArray) {
    var json = {};
    $.each(serializedArray, function() {
        json[this.name] = this.value;
    });
    return JSON.stringify(json);
}