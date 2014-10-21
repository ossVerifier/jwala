var RDataTable = React.createClass({

    render: function() {
        var self = this;
        var rows = [];
        var i = 0
        this.props.data.forEach(function(item) {
            i++;
            var oddEvenClassName = (i % 2 === 0) ? "even" : "odd";
            rows.push(new RDataTableRow({className:oddEvenClassName, rowItem:item, colDefinitions:self.props.colDefinitions}));
        });

        return React.DOM.div({className:"dataTables_wrapper"}, new RDataTableHeader(),
                              React.DOM.table({className:"dataTable"},
                              new RDataTableHeaderRow({colDefinitions:self.props.colDefinitions}),
                              rows),
                              new RDataTableFooter());
    }

});

var RDataTableHeader = React.createClass({
    render: function() {
        return React.DOM.div({className:"fg-toolbar ui-toolbar ui-widget-header ui-corner-tl ui-corner-tr ui-helper-clearfix"},
                             React.DOM.div({className:"dataTables_length"}, React.DOM.label(null, "Show",
                             React.DOM.select({size:"1"}, React.DOM.option({value:"25", selected:"selected"}, 25)), " entries")));
    }
});

var RDataTableFooter = React.createClass({
    render: function() {
        return React.DOM.div({className:"fg-toolbar ui-toolbar ui-widget-header ui-corner-bl ui-corner-br ui-helper-clearfix"});
    }
});

var RDataTableHeaderRow = React.createClass({

    render: function() {
        var headerCols = [];
        this.props.colDefinitions.forEach(function(item) {
            if (item.type === undefined) {
                headerCols.push(new RDataTableHeaderColumn({title:item.title}));
            }
        });

        return React.DOM.tr({role:"row"}, headerCols);
    }

});

var RDataTableHeaderColumn = React.createClass({

    render: function() {
        return React.DOM.th({className:"ui-state-default"}, this.props.title,
                            React.DOM.span({className:"DataTables_sort_icon css_right ui-icon ui-icon-carat-2-n-s"}));
    }

});

var RDataTableRow = React.createClass({

    render: function() {
        var self = this;
        var cols = [];
        this.props.colDefinitions.forEach(function(item) {
            if (item.type === undefined) {
                cols.push(new RDataTableColumn({data:self.getEndData(self.props.rowItem, item.key)}));
            }
        });

        return React.DOM.tr({className:this.props.className}, cols);
    },
    /**
     * Gets the "end" data as specified by item.key e.g. if item.key = "person.lastName", end data is the lastName
     */
    getEndData: function(rowItem, itemKey) {
        var keys = itemKey.split(".");
        var endData = rowItem;
        keys.forEach(function(key) {
            endData = endData[key];
        });
        return endData;
    }

});

var RDataTableColumn = React.createClass({
    render: function() {
        return React.DOM.td(null, this.props.data);
    }
});