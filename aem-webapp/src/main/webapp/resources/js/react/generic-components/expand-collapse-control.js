/** @jsx React.DOM */
var ExpandCollapseControl = React.createClass({
    render: function() {
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

        return <span id={this.props.id} className="ui-icon ui-icon-triangle-1-e"/>
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
                                        this.props.rootId,
                                        childTableDetails.initialSortColumn,
                                        this.props.parentItemName);
    },
    drawDataTable: function(dataTable, data, defaultSorting, isCollapsible, headerComponents) {
        dataTable.fnClearTable(data);
        dataTable.fnAddData(data);
        dataTable.fnDraw();

        // indiscriminately show the table.
        // I didn't feel the need to check if the Datatable is hidden to avoid additional logic
        // without significant performance gains.
        // The table's parent's parent is the div that holds the header and the table container.
        dataTable.parent().parent().show();

        // If className is defined, add it to the row node's class
        var data = dataTable.fnGetData();
        var nodes = dataTable.fnGetNodes();
        for(var i = 0;i < data.length;i++) {
            if (data[i].className !== undefined) {
                $(nodes[i]).closest("tr").addClass(data[i].className);
            }
        }

        if (defaultSorting !== undefined) {
            dataTable.fnSort([[defaultSorting.col, defaultSorting.sort]]);
        }

        if (isCollapsible) {
            // Make the table collapsible by applying an accordion ui
            // on its top level container (the parent of the parent)
            if (!dataTable.parent().parent().hasClass("ui-accordion")) {
                dataTable.parent().parent().accordion({
                    header: headerComponents === undefined ? "h3" : "div.accordion-title",
                    collapsible: true,
                    heightStyle: "content",
                    beforeActivate:function(event, ui){
                        var fromIcon = $(event.originalEvent.target).is('.ui-accordion-header > .ui-icon');
                        return fromIcon;
                    }
                });

                var self = this;
                if (headerComponents !== undefined) {
                    // Accordion button specific codes
                    // attach handlers to the buttons
                    for (var i = 0; i < headerComponents.length; i++) {
                        var component = headerComponents[i];
                        var buttonSelector = dataTable.selector + "_" + component.id;

                        // Remove span to prevent span within a span due to the application of JQuery button.
                        // This fixes bug in ie8 where the button label goes outside the button container.
                        var label = $(buttonSelector).find("span").html();
                        $(buttonSelector).find("span").remove();
                        $(buttonSelector).html(label);
                        $(buttonSelector).attr("title", component.sTitle);
                        $(buttonSelector).button();
                        $(buttonSelector).off("click");
                        $(buttonSelector).on("click",
                                       {id:self.props.parentItemId,
                                        name:self.props.parentItemName,
                                        buttonSelector: buttonSelector, },
                                        component.btnCallback);
                        $(buttonSelector).find("span").attr("class", component.customSpanClassName);
                        $(buttonSelector).addClass("ui-button-height");

                    }
                }

            }
        }
    },
    onClick: function(dataSources, childTableDetailsArray) {

        var self = this;
        var dataTable = this.props.dataTable;

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + this.props.id).parent().parent().get(0);

        if (!dataTable.fnIsOpen(nTr)) {
            dataTable.fnOpen(nTr,
                             this.fnFormatDetails(),
                             this.props.rowSubComponentContainerClassName);

            for (var i = 0; i < dataSources.length; i++) {
                var subDataTable = this.decorateTable(childTableDetailsArray[i]);
                var data = dataSources[i].jsonData;
                if (data !== undefined && data !== null) {

                    data.forEach(function(o) {
                        o["parentItemId"] = self.props.parentItemId;
                    });

                    this.drawDataTable(subDataTable,
                                       data,
                                       childTableDetailsArray[i].defaultSorting,
                                       childTableDetailsArray[i].isCollapsible,
                                       childTableDetailsArray[i].headerComponents);
                } else {
                    if (dataSources[i].dataCallback !== undefined) {
                            dataSources[i].dataCallback({rootId: this.props.rootId, parentId: this.props.parentItemId},
                                                        self.retrieveDataAndRenderTableCallback(subDataTable,
                                                        childTableDetailsArray[i].defaultSorting,
                                                        childTableDetailsArray[i].isCollapsible,
                                                        childTableDetailsArray[i].headerComponents));
                    }

                }
            }

            $("#" + this.props.id).removeClass("ui-icon-triangle-1-e");
            $("#" + this.props.id).addClass("ui-icon-triangle-1-s");
        } else {
            $("#" + this.props.id).removeClass("ui-icon-triangle-1-s");
            $("#" + this.props.id).addClass("ui-icon-triangle-1-e");
            dataTable.fnClose(nTr);
        }

    },
    fnFormatDetails: function() {

        if(Object.prototype.toString.call(this.props.childTableDetails) === "[object Array]") {
            var tocDataTables = [];
            for (var i = 0; i < this.props.childTableDetails.length; i++) {
                tocDataTables[i] = new TocDataTable({tableId:this.props.childTableDetails[i].tableIdPrefix + this.props.id,
                                                     tableDef:this.props.childTableDetails[i].tableDef,
                                                     className:this.props.childTableDetails[i].className,
                                                     title:this.props.childTableDetails[i].title,
                                                     headerComponents: this.props.childTableDetails[i].headerComponents,
                                                     hide:true // hide (show later) to prevent from screwing up the display when data is not yet available because of the header components
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
    retrieveDataAndRenderTableCallback: function(subDataTable, defaultSorting, isCollapsible, headerComponents) {
        var self = this;
        return function(resp) {
            var data = resp.applicationResponseContent;
            if (data !== undefined) {
                self.drawDataTable(subDataTable, data, defaultSorting, isCollapsible, headerComponents);
            }
        }
    }
});