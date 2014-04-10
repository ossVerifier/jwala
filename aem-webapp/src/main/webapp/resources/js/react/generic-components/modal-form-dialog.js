/** @jsx React.DOM */
var ModalFormDialog = React.createClass({
    render: function() {
        if (this.props.show === true) {
            return <div className={this.props.className}>{this.props.form}</div>
        }
        return <div/>
    },
    componentDidUpdate: function () {
        if (this.props.show === true) {
            this.show();
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
                    thisComponent.okClick(function(){
                        if (thisComponent.props.successCallback() === true) {
                            thisComponent.destroy();
                        }
                    });
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
        this.props.form.submit(
            function() {
                // You need to destroy this component in a callback
                // to prevent has no method isMounted error if
                // the component is destroyed here
                callback();
            },
            function(errMsg) {
                $.errorAlert(errMsg, "Error");
            }
        );
    },
    destroy: function() {
        if (this.props.destroyCallback !== undefined) {
            this.props.destroyCallback();
        }
        $(this.getDOMNode()).dialog("destroy");
    }
});