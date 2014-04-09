/**
 * Widget that draws a "Delete" button and displays a confirmation dialog box.
 */
var DeleteItemWidget = React.createClass({
    getInitialState: function() {
        dataTable = $(this.props.dataGrid.getDOMNode()).find("table").dataTable();
        dialogConfirm = DialogConfirm({dataGrid:this.props.dataGrid, url:this.props.url});
        submissionInProgress = false;
    },
    render: function() {
        return React.DOM.div(null,
                            dialogConfirm,
                            React.DOM.input({value:"Delete", type:"button", onClick:this.onDeleteClick}));
    },
    onDeleteClick: function() {
        var cell = dataTable.find("tbody tr.row_selected")[0];
        if (cell !== undefined) {
            var i = dataTable.fnGetPosition(cell);
            var idx = dataTable.fnGetData(i);
            dialogConfirm.show(idx.id.id);
        }
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
        if (submissionInProgress === false) {
            submissionInProgress = true;

            ServiceFactory.getJvmService().deleteJvm(id).then(
                function(){
                    submissionInProgress = false;
                    dataGrid.refresh();
                },
                function(response) {
                    submissionInProgress = false;
                    dataGrid.refresh();
                    if (response.status !== 200) {
                        $.errorAlert("Error deleting selected item! The item may not be in the database anymore.");
                    }
                }
            );

        }

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