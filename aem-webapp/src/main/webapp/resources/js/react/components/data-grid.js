var DataGrid = React.createClass({

    render: function() {
        var jsonData = [{"name":"the-jvm-name-1","id":16,"host":"the-host-name-1","groupInfo":{"name":"the-group-name","id":5}},{"name":"test1","id":19,"host":"test1","groupInfo":{"name":"the-group-name","id":5}},{"name":"test","id":22,"host":"test","groupInfo":{"name":"Group 1","id":2}},{"name":"jvm10","id":23,"host":"host10","groupInfo":{"name":"Group 4","id":7}},{"name":"jvm11","id":24,"host":"host11","groupInfo":{"name":"Group 4","id":7}},{"name":"jvm13","id":27,"host":"host13","groupInfo":{"name":"Group 3","id":9}},{"name":"jvm12","id":28,"host":"jvm12","groupInfo":{"name":"Group 1","id":2}},{"name":"jvm15","id":32,"host":"host15","groupInfo":{"name":"Group 1","id":2}},{"name":"test2","id":36,"host":"test2","groupInfo":{"name":"Group 1","id":2}},{"name":"test7","id":40,"host":"host7","groupInfo":{"name":"Group 1","id":2}},{"name":"test10","id":42,"host":"host10","groupInfo":{"name":"Group 1","id":2}},{"name":"test12","id":45,"host":"host12","groupInfo":{"name":"Group 1","id":2}},{"name":"test13","id":47,"host":"host13","groupInfo":{"name":"Group 3","id":9}},{"name":"test14","id":48,"host":"host14","groupInfo":{"name":"Group 2","id":15}},{"name":"test20","id":49,"host":"host20","groupInfo":{"name":"Group 3","id":9}},{"name":"test21","id":50,"host":"host21","groupInfo":{"name":"Group 2","id":15}},{"name":"test22","id":52,"host":"host22","groupInfo":{"name":"Group 1","id":2}},{"name":"test30","id":54,"host":"host30","groupInfo":{"name":"Group 2","id":15}},{"name":"test31","id":57,"host":"host31","groupInfo":{"name":"Group 2","id":15}},{"name":"test33","id":61,"host":"host33","groupInfo":{"name":"Group 1","id":2}},{"name":"test34","id":62,"host":"host34","groupInfo":{"name":"Group 1","id":2}},{"name":"test35","id":63,"host":"host35","groupInfo":{"name":"Group 1","id":2}},{"name":"test37","id":64,"host":"host37","groupInfo":{"name":"Group 1","id":2}},{"name":"test38","id":66,"host":"test38","groupInfo":{"name":"Group 1","id":2}},{"name":"test39","id":68,"host":"host39","groupInfo":{"name":"Group 1","id":2}},{"name":"test40","id":69,"host":"host40","groupInfo":{"name":"Group 4","id":7}}];
        var rows = new Array();
        for (var i = 0; i < jsonData.length ; i++) {
            rows[i] = Row({data:jsonData[i]});
        }

        // The table has to be in a DIV so that css will work if this component is placed in
        // another table.
        return React.DOM.div({className:"dataGrid-" + this.props.theme},
                             React.DOM.table({className:"dataGrid-" + this.props.theme},
                             Header({header:this.props.header}),
                             rows));
    }

});

var Header = React.createClass({

    render: function() {
        var headers = this.props.header;
        var reactHeaders = new Array();
        for (var i = 0; i < headers.length; i++) {
            reactHeaders[i] = React.DOM.th(null, headers[i]);
        }

        return React.DOM.head(null,
                              React.DOM.th() /* this is for the checkbox */,
                              reactHeaders);
    }

});

var Row = React.createClass({

    render: function() {
        var jsonData = this.props.data;
        var cols = new Array();
        var idx = 0;

        $.each(jsonData, function(i, val) {
            cols[idx++] = Column({value:val});
        });

        return React.DOM.tr(null, Column({value:React.DOM.input({type:"checkbox"})}), cols)
    }

});

var Column = React.createClass({

    render: function() {
        return React.DOM.td(null, this.props.value)
    }

});