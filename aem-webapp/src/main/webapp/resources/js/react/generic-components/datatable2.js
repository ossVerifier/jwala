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
                    var capHtml = React.renderComponentToString(new Link2({value:data, callback:self.props.editCallback}));
                        return capHtml;
                    } else { return ""; }
                };
            } else if (item.tocType === "control") {
                self.expandCollapseEnabled = true;
                aoColumnDefs[itemIndex].mDataProp = null;
                aoColumnDefs[itemIndex].sClass = "control center";
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

      // TODO: Use REACT to string approach here
      var sOut =
        '<div class="innerDetails">'+
          '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">'+
            '<tr><td>Rendering engine:</td><td>'+oData.engine+'</td></tr>'+
            '<tr><td>Browser:</td><td>'+oData.browser+'</td></tr>'+
            '<tr><td>Platform:</td><td>'+oData.platform+'</td></tr>'+
            '<tr><td>Version:</td><td>'+oData.version+'</td></tr>'+
            '<tr><td>Grade:</td><td>'+oData.grade+'</td></tr>'+
          '</table>'+
        '</div>';

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

        this.props.callback(this.props.value);

        return false;
    }
});