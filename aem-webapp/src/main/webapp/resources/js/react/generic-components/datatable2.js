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
                aoColumnDefs[itemIndex].sDefaultContent = "<img src='public-resources/img/react/components/details_open.png'/>";
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

            if (self.expandCollapseEnabled === true) {
                    $(this.getDOMNode()).find("td.control").on("click", function () {
                        var nTr = this.parentNode;
                        var i = $.inArray(nTr, self.anOpen);

                        if ( i === -1 ) {
                            $("img", this).attr("src", "public-resources/img/react/components/details_close.png");
                            self.dataTable.fnOpen(nTr, self.fnFormatDetails(self.dataTable, nTr), "details");
                            self.anOpen.push(nTr);
                        } else {
                            $("img", this).attr("src", "public-resources/img/react/components/details_open.png");
                            self.dataTable.fnClose(nTr);
                            self.anOpen.splice(i, 1);
                        }
                    });
            }

        }
    },
    // TODO: This should be a component
    fnFormatDetails: function (oTable, nTr) {
        var oData = oTable.fnGetData(nTr);

        var sOut = "";
        React.renderComponentToString(new SimpleDataTable({className:"simple-data-table",
                                                           displayColumns:["jvmName", "hostName"],
                                                           data:[{"id":{"id":275},"hostName":"host01","group":{"name":"Group 2","id":{"id":15}},"jvmName":"jvm01"},{"id":{"id":276},"hostName":"host02","group":{"name":"Group 2","id":{"id":15}},"jvmName":"jvm02"}]}),
                                  function(html) {sOut = html;});

        return sOut;
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