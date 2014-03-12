/**
 * Widget that draws a "Delete" button and displays a confirmation dialog box.
 *
 *
 */
var DeleteItemWidget = React.createClass({
    getInitialState: function() {
        dialogConfirm = DialogConfirm({dataGrid:this.props.dataGrid, url:this.props.url});
    },
    render: function() {
        return React.DOM.div(null,
                            dialogConfirm,
                            React.DOM.input({value:"Delete", type:"button", onClick:this.onDeleteClick}));
    },
    onDeleteClick: function() {
        /**
         * When the data grid is replaced by jquery data table
         * the code below will have to be updated.
         */
        $("input:checkbox").each(function(i, obj) {
            if ($(this).is(":checked")) {
                dialogConfirm.show($(this).attr("value"));
            }
        });
    }
});

var DialogConfirm = React.createClass({
    render: function() {
        return React.DOM.div(null)
    },
    show: function(id) {
        var dialogConfirm = this;
        $(this.getDOMNode()).html("<h3>Are you sure you want to delete the selected item ?<h3>");

        // Define the Dialog and its properties.
        $(this.getDOMNode()).dialog({
            resizable: false,
            modal: true,
            title: "Confirmation Dialog Box",
            height: "auto",
            width: "auto",
            buttons: {
                "Yes": function () {
                    dialogConfirm.destroy(this);
                    dialogConfirm.deleteItem(id);
                },
                    "No": function () {
                    dialogConfirm.destroy(this);
                }
            }
        });
    },
    deleteItem: function(id) {
        var dataGrid = this.props.dataGrid;
        var url = this.props.url;
        $.ajax({
            type: "DELETE",
            dataType: "json",
            url: url + id,
            success: function(data, textStatus, jqXHR){
                dataGrid.refresh();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("Error deleting selected item! Cause: " + textStatus);
            },
        });
    },
    destroy: function(obj) {
        $(obj).html("");
        $(obj).dialog("destroy");

        // reset checkboxes
        $("input:checkbox").each(function(i, obj) {
            $(this).prop("checked", false);
        });
    }
});
