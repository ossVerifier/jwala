/**
 * A simple data table intended to be used as a child table.
 */
var SimpleDataTable = React.createClass({
    render: function() {
        var rowArray = [];
        var self = this;
        $.each(this.props.data, function(i, obj) {
            var colArray = [];
            for (var idx = 0; idx < self.props.displayColumns.length; ++idx) {
                colArray.push(React.DOM.td(null, obj[self.props.displayColumns[idx]]));
            }
            rowArray.push(React.DOM.tr(null, colArray));
        });
        return React.DOM.table({className:this.props.className}, rowArray);
    }
})