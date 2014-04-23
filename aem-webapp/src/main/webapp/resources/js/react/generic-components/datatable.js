/** @jsx React.DOM */
var TocDataTable = React.createClass({
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

                aoColumnDefs[itemIndex].mRender = function(data, type, full) {
                    var id = self.props.tableId + "link" + data + full.id.id;
                    return React.renderComponentToStaticMarkup(new Anchor({id:id,
                                                                   valueId:full.id.id,
                                                                   value:data,
                                                                   callback:self.props.editCallback}));
                };

            } else if (item.tocType === "control") {
                self.expandCollapseEnabled = true;
                aoColumnDefs[itemIndex].mDataProp = null;
                aoColumnDefs[itemIndex].sClass = "control center";
                aoColumnDefs[itemIndex].sWidth = "20px";

                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                    var content = "";
                    if (data.length > 0) {
                        return React.renderComponentToStaticMarkup (new ExpandCollapseController({controlId:full.id.id,
                                                                        expandIcon:self.props.expandIcon,
                                                                        collapseIcon:self.props.collapseIcon,
                                                                        colHeaders:self.props.colHeaders,
                                                                        data:data,
                                                                        getDataTableCallback:self.getDataTable,
                                                                        rowSubComponentContainerClassName:self.props.rowSubComponentContainerClassName}));
                        }
                    return content;
                }

            } else if (item.tocType === "array") {
                  aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                      var str = "";
                      /* would be better with _Underscore.js : */
                      for (var idx = 0; idx < data.length; idx=idx+1) {
                          str = str + (str === "" ? "" : ", ") + data[idx][item.displayProperty];
                      }
                      return str;
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
                                        "aLengthMenu": [[25, 50, 100, 200, -1],
                                                        [25, 50, 100, 200, "All"]],
                                        "iDisplayLength": 25,
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

var Anchor = React.createClass({
    render: function() {
        $("#" + this.props.id).off("click");
        $("#" + this.props.id).click(this.linkClick);
        return <a id={this.props.id} href="">{this.props.value}</a>
    },
    linkClick: function() {
        this.props.callback(this.props.valueId);
        return false;
    }
});

var ExpandCollapseController = React.createClass({
    currentIcon: "", // Tried to use state then re-render but can't. I get firstChild of undefined rears for some reason.
                     // Upgrading to 0.10.0 should resolve this.
    render: function() {
        this.currentIcon = this.props.expandIcon;
        var controlFullId = "ExpandCollapseController_" + this.props.controlId;

        $("#" + controlFullId).off("click");
        $("#" + controlFullId).click(this.onClick);

        return <img id={controlFullId}
                    src={this.props.expandIcon}/>
    },
    onClick: function() {
        var dataTable = this.props.getDataTableCallback();
        var controlFullId = "ExpandCollapseController_" + this.props.controlId;

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + controlFullId).parent().parent().get(0);

        if (this.currentIcon === this.props.expandIcon) {
            this.currentIcon = this.props.collapseIcon;
            dataTable.fnOpen(nTr,
                             this.fnFormatDetails(this.props.data),
                             this.props.rowSubComponentContainerClassName);
        } else {
            this.currentIcon = this.props.expandIcon;
            dataTable.fnClose(nTr);
        }
        $("#" + controlFullId).attr("src", this.currentIcon);
    },
    fnFormatDetails: function(data) {
        return React.renderComponentToStaticMarkup (new SimpleDataTable({className:"simple-data-table",
                                                        colHeaders:this.props.colHeaders,
                                                        displayColumns:["jvmName", "hostName"],
                                                        data:data}));
    }
});