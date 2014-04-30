/** @jsx React.DOM */
var ExpandCollapseControl = React.createClass({
    dataTable: null,
    currentIcon: "", // Tried to use state then re-render but can't. I get firstChild of undefined rears for some reason.
                     // Upgrading to 0.10.0 should resolve this.
    render: function() {
        this.currentIcon = this.props.expandIcon;
        var controlFullId = "ExpandCollapseControl_" + this.props.controlId;

        $("#" + controlFullId).off("click");
        $("#" + controlFullId).on("click", this.onClick);

        return <img id={controlFullId}
                    src={this.props.expandIcon}/>
    },
    onClick: function() {
        var dataTable = this.props.getDataTableCallback();
        var controlFullId = "ExpandCollapseControl_" + this.props.controlId;

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + controlFullId).parent().parent().get(0);

        if (this.currentIcon === this.props.expandIcon) {
            this.currentIcon = this.props.collapseIcon;
            dataTable.fnOpen(nTr,
                             this.fnFormatDetails(),
                             this.props.rowSubComponentContainerClassName);

            this.dataTable = decorateTableAsDataTable(this.props.childTableDetails.tableIdPrefix + this.props.controlId,
                                                      this.props.childTableDetails.tableDef,
                                                      false,
                                                      false);

            if (this.dataTable !== null) {
                this.dataTable.fnClearTable(this.props.data);
                this.dataTable.fnAddData(this.props.data);
                this.dataTable.fnDraw();
            }

        } else {
            this.currentIcon = this.props.expandIcon;
            dataTable.fnClose(nTr);
        }
        $("#" + controlFullId).attr("src", this.currentIcon);
    },
    fnFormatDetails: function() {
        return React.renderComponentToStaticMarkup(<TocDataTable tableId={this.props.childTableDetails.tableIdPrefix + this.props.controlId}
                                                    tableDef={this.props.childTableDetails.tableDef}
                                                    className={this.props.childTableDetails.className}/>)
    }
});