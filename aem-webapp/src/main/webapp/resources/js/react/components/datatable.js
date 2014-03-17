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
            "aTargets": [ itemIndex ]};
            
          if(item.bVisible !== undefined) {
            aoColumnDefs[itemIndex].bVisible = item.bVisible;
          }
          
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
            "bJQueryUI": true,
            "fnDrawCallback": function(){

                dataTable = this;
                $(dataTable).find("tr").off("click").on("click", function(e) {
                    if ($(this).hasClass("row_selected") ) {
                        $(this).removeClass("row_selected");
                    } else {
                        $(dataTable).find("tr").removeClass("row_selected");
                        $(this).addClass("row_selected");
                    }
                });

            }
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
        var table = React.DOM.table({className:"tocDataTable-" + this.props.theme});
        this.reactTable = table;
        
        var div = React.DOM.div({className:"tocDataTable-" + this.props.theme},
                             this.props.editDialog,
                             table
                             );
                             
        if(this.dataTable !== undefined) {
          this.dataTable.fnClearTable(jsonData);
          this.dataTable.fnAddData(jsonData);
          this.dataTable.fnDraw();
          var dataTable = this.getDOMNode();
        }
        return div;        
    }
});

var Link = React.createClass({
    render: function() {
        // TODO: Remove inline style
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