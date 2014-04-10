/** @jsx React.DOM */
var TocDataTable2 = React.createClass({
    dataTable: null,
    anOpen: [],
    expandCollapseEnabled: false,
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
                    React.renderComponentToString(new Link2({valueId:full.id.id,
                                                             value:data,
                                                             callback:self.props.editCallback}),
                                                  function(html) {capHtml = html;});
                        return capHtml;
                    } else { return ""; }
                };
            } else if (item.tocType === "control") {
                self.expandCollapseEnabled = true;
                aoColumnDefs[itemIndex].mDataProp = null;
                aoColumnDefs[itemIndex].sClass = "control center";
                aoColumnDefs[itemIndex].sWidth = "20px";

                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                    var content = "";
                    if (data.length > 0) {
                        React.renderComponentToString(new ExpandCollapseController({controlId:full.id.id,
                                                      expandIcon:"public-resources/img/react/components/details_open.png",
                                                      collapseIcon:"public-resources/img/react/components/details_close.png",
                                                      data:data,
                                                      getDataTableCallback:self.getDataTable}),
                                                      function(html) {
                                                        content = html;
                                                     });
                    }
                    return content;
                }

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
        var self = this;
        if (this.dataTable !== null) {
            this.dataTable.fnClearTable(this.props.data);
            this.dataTable.fnAddData(this.props.data);
            this.dataTable.fnDraw();
        }
    },
    getDataTable: function() {
        return this.dataTable;
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

        this.props.callback(this.props.valueId);

        return false;
    }
});

var ExpandCollapseController = React.createClass({
    currentIcon: "", // Tried to use state then re-render but can't get firstChild of undefined rears it's ungly head out.
    render: function() {
        this.currentIcon = this.props.expandIcon;
        var controlFullId = "ExpandCollapseController_" + this.props.controlId;
        return <img id={controlFullId}
                    src={this.props.expandIcon}
                    onClick={this.onClick}/>
    },
    onClick: function() {
        var dataTable = this.props.getDataTableCallback();
        var controlFullId = "ExpandCollapseController_" + this.props.controlId;

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + controlFullId).parent().parent().get(0);

        if (this.currentIcon === this.props.expandIcon) {
            this.currentIcon = this.props.collapseIcon;
            dataTable.fnOpen(nTr, this.fnFormatDetails(this.props.data), "details");
        } else {
            this.currentIcon = this.props.expandIcon;
            dataTable.fnClose(nTr);
        }
        $("#" + controlFullId).attr("src", this.currentIcon);
    },
    fnFormatDetails: function(data) {
        var sOut = "";
        React.renderComponentToString(new SimpleDataTable({className:"simple-data-table",
                                                           displayColumns:["jvmName", "hostName"],
                                                           data:data}),
                                  function(html) {sOut = html;});

        return sOut;
    }
});