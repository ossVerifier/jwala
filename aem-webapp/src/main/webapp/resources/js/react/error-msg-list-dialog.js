var ErrorMsgListDialog = React.createClass({

    render: function() {
        var rows = [];
        var rowIdx = 1;
        for (var i = (this.props.msgList.length - 1);i >=0 ;i--) {
            var row = this.props.msgList[i];
            cols = [];
            for (var col in row) {
                cols.push(React.DOM.td(null, row[col]));
            }
            var trClassName = (rowIdx++ % 2 === 0) ? "even" : "odd";
            rows.push(React.DOM.tr({className:trClassName} , cols));
        }

        var errTable = React.DOM.table({className:"errMsgTable"}, rows);
        return React.DOM.div(null, errTable);
    },

    componentDidMount: function() {
        $(this.getDOMNode()).dialog({title:this.props.title,
                                     width:"auto",
                                     buttons: {"Close": function(){$(this).dialog("close")}}
                                    });
    }

})