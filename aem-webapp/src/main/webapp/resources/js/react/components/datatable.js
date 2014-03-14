var TocDataTable = React.createClass({
    getInitialState: function() {
        return {data: []};
    },
    refresh: function() {
        $.ajax({
            url: this.props.url,
            dataType: "json",
            success: function(data) {
                this.setState({data: data});
            }.bind(this),
            error: function(xhr, status, err) {

            }.bind(this)
        });
    },
    componentWillMount: function() {
        this.refresh();
    },
    componentDidMount: function() {
        var self = this;
        
        // build column definitions based on props
        var aoColumnDefs  = new Array();
        var aaSorting     = new Array();
        $(this.props.header).each(function(itemIndex, itemName, itemArray) {
          aoColumnDefs[itemIndex] = {
            "sTitle": itemName,
            "aTargets": [ itemIndex ] };
          aaSorting[itemIndex] = [itemIndex, 'asc'];
        });
        
        var props = this.props;
        
        $(this.props.headerExt).each(function(itemIndex, item, itemArray) {
          aoColumnDefs[itemIndex] = {
            "sTitle": item.sTitle,
            "mData": item.mData,
            "aTargets": [ itemIndex ] };
            
          if(item.tocType == 'link') { 
            aoColumnDefs[itemIndex].mRender = function ( data, type, full ) {
              if(self.isMounted()) {
                var capHtml = "";
                React.renderComponentToString(new Link({value:data,
                           editDialog:props.editDialog,
                           thisGrid:self,
                           jsonFormDataTransformerCallback:props.jsonFormDataTransformerCallback
                       }), function(html) { capHtml = html; } );
                return capHtml;
              } else { return ""; }
            }};
          aaSorting[itemIndex] = [itemIndex, 'asc'];
        });
        
        this.dataTable = 
          $(self.getDOMNode().children[1]).dataTable({
            "aaSorting": aaSorting,
            "aoColumnDefs": aoColumnDefs,
            "bJQueryUI": true
            });
    },
    render: function() {

       var jsonData;
       if (this.state.data.applicationResponseContent !== undefined) {
            jsonData = this.state.data.applicationResponseContent.content;
       } else {
            jsonData = this.state.data;
       }

        // The table has to be in a DIV so that css will work if this component is placed in
        // another table.
        var table = React.DOM.table({className:"dataGrid-" + this.props.theme});
        this.reactTable = table;
        
        var div = React.DOM.div({className:"dataGrid-" + this.props.theme},
                             this.props.editDialog,
                             table
                             );
                             
        if(this.dataTable !== undefined) {
          this.dataTable.fnClearTable(jsonData);
          this.dataTable.fnAddData(jsonData);
          this.dataTable.fnDraw();
        }
        return div;        
    },
    deleteClick: function() { // TODO - use dataTable API to identify selected row
        $("input:checkbox").each(function(i, obj) {
            if ($(this).is(":checked")) {
                dialogConfirm.show($(this).attr("value"));
           }
        });
    }
});

var Link = React.createClass({
    render: function() {
        var linkStyle = {"text-decoration":"underline", "background":"none", "color":"blue"};
        return React.DOM.a({href:"javascript:", style:linkStyle, onClick:this.linkClick}, this.props.value);
    },
    linkClick: function() {
        var editDialog = this.props.editDialog;
        var thisGrid = this.props.thisGrid;
        var jsonFormDataTransformerCallback = this.props.jsonFormDataTransformerCallback;
        $.getJSON("v1.0/jvm?name=" + this.props.value,
            function(data) {

                editDialog.show(jsonFormDataTransformerCallback(data), function(){
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

