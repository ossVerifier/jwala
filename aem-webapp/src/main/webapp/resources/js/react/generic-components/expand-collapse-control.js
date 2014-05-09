/** @jsx React.DOM */
var ExpandCollapseControl = React.createClass({
    currentIcon: "",
    render: function() {
        this.currentIcon = this.props.expandIcon;

        var dataSources = [];
        var childTableDetailsArray = [];

        if(Object.prototype.toString.call(this.props.childTableDetails) === "[object Array]") {
            for (var i = 0; i < this.props.childTableDetails.length; i++) {
                dataSources[i] = this.props.childTableDetails[i].dataCallback === undefined ?
                                    {jsonData:this.props.childTableDetails[i].data} :
                                    {dataCallback:this.props.childTableDetails[i].dataCallback};
                childTableDetailsArray[i] = this.props.childTableDetails[i];
            }
        } else {
                dataSources[0] = this.props.childTableDetails.dataCallback === undefined ?
                                    {jsonData:this.props.childTableDetails.data} :
                                    {dataCallback:this.props.childTableDetails.dataCallback};
                childTableDetailsArray[0] = this.props.childTableDetails;
        }

        $("#" + this.props.id).off("click");
        $("#" + this.props.id).on("click", this.onClick.bind(this, dataSources, childTableDetailsArray));

        return <img id={this.props.id}
                    src={this.props.expandIcon}/>
    },
    decorateTable: function(childTableDetails) {
        return decorateTableAsDataTable(childTableDetails.tableIdPrefix + this.props.id,
                                        childTableDetails.tableDef,
                                        false,
                                        false,
                                        null,
                                        null,
                                        this.props.expandIcon,
                                        this.props.collapseIcon,
                                        childTableDetails.childTableDetails,
                                        this.props.parentItemId,
                                        this.props.rootId);
    },
    drawDataTable: function(dataTable, data) {
        dataTable.fnClearTable(data);
        dataTable.fnAddData(data);
        dataTable.fnDraw();

        // If className is defined, add it to the row node's class
        var data = dataTable.fnGetData();
        var nodes = dataTable.fnGetNodes();
        for(var i = 0;i < data.length;i++) {
            if (data[i].className !== undefined) {
                $(nodes[i]).closest("tr").addClass(data[i].className);
            }
        }

    },
    onClick: function(dataSources, childTableDetailsArray) {

        var self = this;
        var dataTable = this.props.dataTable;

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + this.props.id).parent().parent().get(0);

        if (this.currentIcon === this.props.expandIcon) {
            this.currentIcon = this.props.collapseIcon;
            dataTable.fnOpen(nTr,
                             this.fnFormatDetails(),
                             this.props.rowSubComponentContainerClassName);

            for (var i = 0; i < dataSources.length; i++) {
                var subDataTable = this.decorateTable(childTableDetailsArray[i]);
                var data = dataSources[i].jsonData;
                if (data !== undefined && data !== null) {
                    this.drawDataTable(subDataTable, data);
                } else {
                    if (dataSources[i].dataCallback !== undefined) {
                            dataSources[i].dataCallback({rootId: this.props.rootId, parentId: this.props.parentItemId},
                                                        self.retrieveDataAndRenderTableCallback(subDataTable));
                    }

                }
            }

        } else {
            this.currentIcon = this.props.expandIcon;
            dataTable.fnClose(nTr);
        }
        $("#" + this.props.id).attr("src", this.currentIcon);

    },
    fnFormatDetails: function() {

        if(Object.prototype.toString.call(this.props.childTableDetails) === "[object Array]") {
            var tocDataTables = [];
            for (var i = 0; i < this.props.childTableDetails.length; i++) {
                tocDataTables[i] = new TocDataTable({tableId:this.props.childTableDetails[i].tableIdPrefix + this.props.id,
                                                     tableDef:this.props.childTableDetails[i].tableDef,
                                                     className:this.props.childTableDetails[i].className,
                                                     title:this.props.childTableDetails[i].title
                                                    });
            }
            return React.renderComponentToStaticMarkup(new React.DOM.div("", tocDataTables));
        } else {
            return React.renderComponentToStaticMarkup(<TocDataTable tableId={this.props.childTableDetails.tableIdPrefix + this.props.id}
                                                                     tableDef={this.props.childTableDetails.tableDef}
                                                                     className={this.props.childTableDetails.className}
                                                                     title={this.props.childTableDetails.title}/>)
        }

    },
    retrieveDataAndRenderTableCallback: function(subDataTable) {
        var self = this;
        return function(resp) {
            var data = resp.applicationResponseContent;
            if (data !== undefined) {
                self.drawDataTable(subDataTable, data);
            }
        }
    }
});