/** @jsx React.DOM */
var ConfirmDeleteModalDialog = React.createClass({
    render: function() {
        if (this.props.show === true) {
            return <div><h3>Are you sure you want to delete the selected item ?</h3></div>
        }
        return <div/>
    },
    componentDidUpdate: function () {
        if (this.props.show === true) {
            this.show();
        }
    },
    show: function() {
        var dialogConfirm = this;

        // Define the Dialog and its properties.
        $(this.getDOMNode()).dialog({
            resizable: false,
            modal: true,
            title: "Confirmation Dialog Box",
            height: "auto",
            width: "auto",
            buttons: {
                "Yes": function () {
                    $(dialogConfirm.getDOMNode()).dialog("destroy");
                    dialogConfirm.props.btnClickedCallback("yes");
                },
                    "No": function () {
                    $(dialogConfirm.getDOMNode()).dialog("destroy");
                    dialogConfirm.props.btnClickedCallback("no");
                }
            }
        });
    }
})