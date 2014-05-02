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
    getDataTableCallback: function() {
        return this.dataTable;
    },
    drawDataTable: function() {

        this.dataTable = decorateTableAsDataTable(this.props.childTableDetails.tableIdPrefix + this.props.id,
                                                  this.props.childTableDetails.tableDef,
                                                  false,
                                                  false,
                                                  null,
                                                  null,
                                                  this.props.expandIcon,
                                                  this.props.collapseIcon,
                                                  this.getDataTableCallback,
                                                  this.props.childTableDetails.childTableDetails,
                                                  this.props.rootId);

        if (this.dataTable !== null) {
            this.dataTable.fnClearTable(this.props.data);
            this.dataTable.fnAddData(this.props.data);
            this.dataTable.fnDraw();
        }

    },
    onClick: function() {
        var self = this;
        var dataTable = this.props.getDataTableCallback();

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + this.props.id).parent().parent().get(0);

        if (this.currentIcon === this.props.expandIcon) {
            this.currentIcon = this.props.collapseIcon;
            dataTable.fnOpen(nTr,
                             this.fnFormatDetails(),
                             this.props.rowSubComponentContainerClassName);

            if (this.props.data !== null) {
                this.drawDataTable();
            } else {
                this.props.childTableDetails.getDataCallback(this.props.rootId,
                                                             function(resp){
                                                                self.props.data = resp.applicationResponseContent[0];
                                                                if (self.props.data !== undefined) {
                                                                    self.drawDataTable();
                                                                }
                                                             });
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