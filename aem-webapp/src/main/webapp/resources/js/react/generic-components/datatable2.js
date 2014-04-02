/** @jsx React.DOM */
var TocDataTable2 = React.createClass({
    dataTable: null,
    render: function() {
        return <div>
                    <table/>
               </div>
    },
    componentDidMount: function() {
        var self = this;

        // build column definitions based on props
        var aoColumnDefs = [];
        var aaSorting = [];
        var props = this.props;

        $(this.props.headerExt).each(function(itemIndex, item, itemArray) {

            aoColumnDefs[itemIndex] = {"sTitle": item.sTitle,
                                       "mData": item.mData,
                                       "aTargets": [itemIndex]};

            if(item.bVisible !== undefined) {
                aoColumnDefs[itemIndex].bVisible = item.bVisible;
            }

            if(item.tocType === "link") {

                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                if(self.isMounted()) {
                    var capHtml = "";
                    React.renderComponentToString(new Link2({value:data, callback:self.props.editCallback}),
                                                  function(html) {capHtml = html;});
                        return capHtml;
                    } else { return ""; }
                };
            }
            aaSorting[itemIndex] = [itemIndex, 'asc'];

        });

        this.dataTable =
        $(this.getDOMNode().children[0]).dataTable({"aaSorting": aaSorting,
                                        "aoColumnDefs": aoColumnDefs,
                                        "bJQueryUI": true,
                                        "bAutoWidth": false,
                                        "bStateSave": true,
                                        "fnDrawCallback": function(){
                                                var theDataTable = this;
                                                $(theDataTable).find("tr").off("click").on("click", function(e) {
                                                    if ($(this).hasClass("row_selected") ) {
                                                        $(this).removeClass("row_selected");
                                                    } else {
                                                        $(theDataTable).find("tr").removeClass("row_selected");
                                                        $(this).addClass("row_selected");

                                                        var cell = theDataTable.find("tbody tr.row_selected")[0];
                                                        if (cell !== undefined) {
                                                            var i = theDataTable.fnGetPosition(cell);
                                                            var item = theDataTable.fnGetData(i);
                                                            self.props.selectItemCallback(item);
                                                        }

                                                    }
                                                });
                                            }
                                        });
    },
    componentDidUpdate: function() {
        if (this.dataTable !== null) {
            this.dataTable.fnClearTable(this.props.data);
            this.dataTable.fnAddData(this.props.data);
            this.dataTable.fnDraw();
        }
    }
});

var Link2 = React.createClass({
    render: function() {
        return <a href="" onClick={this.linkClick}>{this.props.value}</a>
    },
    linkClick: function(e) {
        // next 3 lines stop the browser navigating
        e.preventDefault();
        e.stopPropagation();

        this.props.callback(this.props.value);

        return false;
    }
});