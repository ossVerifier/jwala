/**
 * Component that displays a list of error messages in a table.
 * It has a pull down feature (specified as a pullDown property)
 * which can contain detailed description of listed error messages.
 */
var ErrorMsgList = React.createClass({
    statics: {
        getPropertyCount: function(o) {
            var count = 0;
            for (var prop in o) {
                count++;
            }
            return count;
        }
    },
    getInitialState: function() {
        return {
            pullDownVisible: {}
        }
    },
    render: function() {
        var rows = [];
        var rowIdx = 1;
        var propertyCount;

        for (var i = (this.props.msgList.length - 1);i >=0 ;i--) {
            var row = this.props.msgList[i];
            var pullDownMsg = null;
            cols = [];
            for (var col in row) {
                if (col !== "pullDown") {
                    cols.push(React.DOM.td(null, row[col]));
                } else {
                   var theKey = "key" + i;
                    cols.push(React.DOM.td(null,
                                           React.DOM.span({className:"ui-icon ui-icon-link cursorPointer",
                                           onClick:this.clickPullDown.bind(this, theKey)}, "")));

                        if (propertyCount === undefined) {
                            propertyCount = ErrorMsgList.getPropertyCount(row);
                        }
                        pullDownMsg = React.DOM.td({colSpan:propertyCount}, row[col])

                }
            }
            var trClassName = (rowIdx++ % 2 === 0) ? "even" : "odd";
            rows.push(React.DOM.tr({className:trClassName} , cols));

            var rowStyle = this.state.pullDownVisible["key" + i] === true ? {} : {display:"none"};
            rows.push(React.DOM.tr({style:rowStyle}, pullDownMsg));
        }

        return React.DOM.table({className:"errMsgTable"}, rows);
    },
    clickPullDown: function(key) {
        if (this.state.pullDownVisible[key]) {
            this.state.pullDownVisible[key] = false;
        } else {
            this.state.pullDownVisible[key] = true;
        }
        this.setState({pullDownVisible:this.state.pullDownVisible});
    }
})