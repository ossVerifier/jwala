var ErrorMsgListDialog = React.createClass({

    render: function() {
        var rows = [];
        var i = 1;
        this.props.msgList.forEach(
            function(row) {
                cols = [];
                for (var col in row) {
                    cols.push(React.DOM.td(null, row[col]));
                }
                var trClassName = (i++ % 2 === 0) ? "even" : "odd";
                rows.push(React.DOM.tr({className:trClassName} , cols));
            }
        );

        var errTable = React.DOM.table({className:"errMsgTable"}, rows);
        return React.DOM.div(null, errTable);
    },

    componentDidMount: function() {
        $(this.getDOMNode()).dialog({title:this.props.title,
                                     width:"auto"});
    }

})