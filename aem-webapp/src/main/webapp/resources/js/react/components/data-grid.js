var DataGrid = React.createClass({
    getInitialState: function() {
        return {data: []};
    },
    refresh: function() {
        $.ajax({
            url: this.props.url,
            dataType: 'json',
            success: function(data) {
                this.setState({data: data});
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    componentWillMount: function() {
        this.refresh();
    },
    render: function() {

       var jsonData;
       if (this.state.data.applicationResponseContent !== undefined) {
            jsonData = this.state.data.applicationResponseContent.content;
       } else {
            jsonData = this.state.data;
       }

        var rows = new Array();
        for (var i = 0; i < jsonData.length ; i++) {
           rows[i] = Row({data:jsonData[i], editDialog:this.props.editDialog, thisGrid:this});
        }

        // The table has to be in a DIV so that css will work if this component is placed in
        // another table.
        return React.DOM.div({className:"dataGrid-" + this.props.theme},
                             this.props.editDialog,
                             React.DOM.table({className:"dataGrid-" + this.props.theme},
                             Header({header:this.props.header}),
                             rows));
    },
    deleteClick: function() {
        $("input:checkbox").each(function(i, obj) {
            if ($(this).is(":checked")) {
                dialogConfirm.show($(this).attr("value"));

                $("#jvmName").val("test");
            }
        });

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
        var id;
        var editDialog = this.props.editDialog;
        var thisGrid = this.props.thisGrid;
        $.each(jsonData, function(i, val) {
            if (i === "id") {
                id = val;
            }

            // Custom code...
            if (idx == 0) {
                cols[idx++] = Column({value:val, type:"link", editDialog:editDialog, thisGrid:thisGrid});
            } else {
                cols[idx++] = Column({value:val});
            }
        });

        return React.DOM.tr(null, Column({value:Checkbox({value:id})}), cols)
    }

});

var Column = React.createClass({

    render: function() {
        if (this.props.type === undefined) {
            return React.DOM.td(null, this.props.value);
        } else if (this.props.type === "link") {
            return React.DOM.td(null, Link({value:this.props.value,
                                            editDialog:this.props.editDialog, thisGrid:this.props.thisGrid}));
        }
    }

});

var Link = React.createClass({
    render: function() {
        var linkStyle = {"text-decoration":"underline", "background":"none", "color":"blue"};
        return React.DOM.a({href:"#", style:linkStyle, onClick:this.linkClick}, this.props.value);
    },
    linkClick: function() {
        var editDialog = this.props.editDialog;
        var thisGrid = this.props.thisGrid;
        $.getJSON("v1.0/jvm?name=" + this.props.value,
            function(data) {

                var filteredJson = {};

                var nameMap = {};
                nameMap["id"] = "jvmId";
                nameMap["name"] = "jvmNameId";
                nameMap["host"] = "hostNameId";

                // Custom code...
                // TODO: Callback json decorator/parser

                $.each(data.applicationResponseContent.content, function(i, obj) {
                    if (nameMap[i] !== undefined) {
                          filteredJson[nameMap[i]] = obj;
                    }
                });
                console.log(JSON.stringify(filteredJson));
                editDialog.show(filteredJson, function(){
                    thisGrid.refresh();
                });

         });

    }
});

var Checkbox = React.createClass({
    render: function() {
        return React.DOM.input({type:"checkbox", value:this.props.value, onClick:this.checkboxClicked})
    },
    /**
     * Allow only one item to be checked.
     */
    checkboxClicked: function() {
        var id = this.props.value;
        $("input:checkbox").each(function(i, obj) {
            if ($(this).attr("value") != id) {
                $(this).prop("checked", false);
            }
        });
    }
});

