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
    submissionInProgress: false,
    render: function() {
        if (this.props.show) {
            return React.DOM.div({className:this.props.className}, this.props.form)
        }
        return React.DOM.div({style:{"display":"none"}})
    },
    componentDidUpdate: function () {
        this.show();
    },
    show: function(callback) {
        var thisComponent = this;
        var title = this.props.title;
        var formId = "#" + this.props.formId;

        if (this.props.form === undefined) {
            // remove this once JVM config is "Reactified" (Refactored to follow the ways of React)
            $(this.getDOMNode()).append($(formId));
        }

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
            if (submissionInProgress === false) {
                submissionInProgress = true;

                var postData = $(formId).serializeArray();

                // NOTE: This operation is JVM specific because the modal-form-add-dialog
                // will actually be deprecated. It is only used by JVM CRUD.
                // All other CRUDs should use modal-form-dialog.
                ServiceFactory.getJvmService().insertNewJvm(postData).then(
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
        $(theDialog).find("input[type=text]").val("");
        $(theDialog).find("input[type=checkbox]").removeAttr("checked");
        $(theDialog).dialog("destroy");
    }
});