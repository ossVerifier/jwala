/** @jsx React.DOM */
var ExpandCollapseControl = React.createClass({
    dataTable: null,
    currentIcon: "", // Tried to use state then re-render but can't. I get firstChild of undefined rears for some reason.
                     // Upgrading to 0.10.0 should resolve this.
    render: function() {
        this.currentIcon = this.props.expandIcon;

        $("#" + this.props.id).off("click");
        $("#" + this.props.id).on("click", this.onClick);

        return <img id={this.props.id}
                    src={this.props.expandIcon}/>
    },
    onClick: function() {
        var dataTable = this.props.getDataTableCallback();

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + this.props.id).parent().parent().get(0);

        if (this.currentIcon === this.props.expandIcon) {
            this.currentIcon = this.props.collapseIcon;
            dataTable.fnOpen(nTr,
                             this.fnFormatDetails(),
                             this.props.rowSubComponentContainerClassName);

            this.dataTable = decorateTableAsDataTable(this.props.childTableDetails.tableIdPrefix + this.props.id,
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
        $("#" + this.props.id).attr("src", this.currentIcon);
    },
    fnFormatDetails: function() {
        return React.renderComponentToStaticMarkup(<TocDataTable tableId={this.props.childTableDetails.tableIdPrefix + this.props.id}
                                                    tableDef={this.props.childTableDetails.tableDef}
                                                    className={this.props.childTableDetails.className}/>)
    }
});