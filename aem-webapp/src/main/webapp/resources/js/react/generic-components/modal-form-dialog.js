/** @jsx React.DOM */
var ModalFormDialog = React.createClass({
    isShowing: false,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    render: function() {
        if (this.props.show === true) {
            return <div className={this.props.className}>{this.props.form}</div>
        }
        return <div/>
    },
    componentDidUpdate: function () {
        if (this.props.show === true) {
            this.show();
            this.isShowing = true;
        } else {
            if (this.isShowing) {
                $(this.getDOMNode()).dialog("destroy");
            }
            this.isShowing = false;
        }
    },
    show: function() {
        var thisComponent = this;
        var title = this.props.title;

        $(this.getDOMNode()).dialog({
            resizable: false,
            modal: true,
            title: title,
            height: "auto",
            width: "auto",
            buttons: {
                "Ok": function () {
                    thisComponent.okClick();
                },
                "Cancel": function () {
                    thisComponent.destroy();
                }
            },
            close: function() {
                thisComponent.destroy();
            }
        });
    },
    okClick: function(callback) {
        var theForm = $(this.getDOMNode()).children("form:first");
        var validator = theForm.data("validator");

        if (validator !== null) {
            validator.cancelSubmit = true;
            validator.form();
            if (validator.numberOfInvalids() === 0) {
                theForm.submit();
            }
        } else {
            alert("There is no validator for the form!");
        }
    },
    destroy: function() {
        if (this.props.destroyCallback !== undefined) {
            this.props.destroyCallback();
        }
    }
});