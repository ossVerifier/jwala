var TocDataTable = React.createClass({
    getInitialState: function() {
        return {data: {applicationResponseContent:[]}};
    },
    refresh: function() {
        $.ajax({
            url: this.props.url,
            dataType: "json",
            cache: false,
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
        var aoColumnDefs  = [];
        var aaSorting     = [];
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
            };
          }
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
      
        // Table must be in a DIV so that css will work with a table as container
        var table = React.DOM.table({className:"tocDataTable-" + this.props.theme});
        
        var div = React.DOM.div({className:"tocDataTable-" + this.props.theme},
                             this.props.editDialog,
                             table
                             );
                             
        if(this.dataTable !== undefined) {
          this.dataTable.fnClearTable(this.state.data.applicationResponseContent);
          this.dataTable.fnAddData(this.state.data.applicationResponseContent);
          this.dataTable.fnDraw();
        }
        return div;        
    }
});

var Link = React.createClass({
    render: function() {
        // TODO: Remove inline style
        var linkStyle = {"text-decoration":"underline", "background":"none", "color":"blue"};
        return React.DOM.a({href:"", style:linkStyle, onClick:this.linkClick}, this.props.value);
    },
    linkClick: function(e) {
        var editDialog = this.props.editDialog;
        var thisGrid = this.props.thisGrid;
        var jsonFormDataTransformerCallback = this.props.jsonFormDataTransformerCallback;
        $.getJSON("v1.0/jvm?name=" + this.props.value,
            function(data) {

                editDialog.show(jsonFormDataTransformerCallback(data), function(){
                    thisGrid.refresh();
                });

         });
        // next 3 lines stop the browser navigating
        e.preventDefault();
        e.stopPropagation();
        return false; 
    }
});
