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
    submissionInProgress: false,
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
            console.log(submissionInProgress);
            if (submissionInProgress === false) {
                submissionInProgress = true;

                var postData = $(this).serializeArray();

                // NOTE: This operation is JVM specific because the modal-form-add-dialog
                // will actually be deprecated. It is only used by JVM CRUD.
                // All other CRUDs should use modal-form-dialog.

                ServiceFactory.getJvmService().updateJvm(postData).then(
                    function(){
                        submissionInProgress = false;
                        for (var i = 0; i < callbacks.length; i++) {
                            callbacks[i]();
                        }
                    },
                    function(response) {
                        submissionInProgress = false;
                        $.errorAlert(JSON.parse(response.responseText).applicationResponseContent, "Error");
                    }
                );
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