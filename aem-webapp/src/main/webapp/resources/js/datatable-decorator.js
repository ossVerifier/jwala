/**
 * This method transforms a table to a JQuery DataTable.
 */
var decorateTableAsDataTable = function(tableId,
                                        tableDef,
                                        hideHeaderAndFooter,
                                        editCallback,
                                        rowSelectCallback,
                                        expandIcon,
                                        collapseIcon,
                                        getDataTableCallback,
                                        childTableDetails){

    var self = this;

    // build column definitions based on props
    var aoColumnDefs = [];
    var aaSorting = [];

    $(tableDef).each(function(itemIndex, item, itemArray) {

            aoColumnDefs[itemIndex] = {"sTitle": item.sTitle,
                                       "mData": item.mData,
                                       "aTargets": [itemIndex]};

            if(item.bVisible !== undefined) {
                aoColumnDefs[itemIndex].bVisible = item.bVisible;
            }

            if(item.tocType === "link") {
                aoColumnDefs[itemIndex].mRender = function(data, type, full) {
                    var id = tableId + "link" + data + full.id.id;
                    return React.renderComponentToStaticMarkup(new Anchor({id:id,
                                                                   valueId:full.id.id,
                                                                   value:data,
                                                                   callback:editCallback}));
                };

            } else if (item.tocType === "control") {

                self.expandCollapseEnabled = true;
                aoColumnDefs[itemIndex].mDataProp = null;
                aoColumnDefs[itemIndex].sClass = "control center";
                aoColumnDefs[itemIndex].sWidth = "20px";

                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                    var content = "";
                    if (data.length > 0) {
                        return React.renderComponentToStaticMarkup(
                                    new ExpandCollapseControl({controlId:full.id.id,
                                                               expandIcon:expandIcon,
                                                               collapseIcon:collapseIcon,
                                                               childTableDetails:childTableDetails,
                                                               getDataTableCallback:getDataTableCallback,
                                                               data:data}));
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

        var dataTableProperties = {"aaSorting": aaSorting,
                                   "aoColumnDefs": aoColumnDefs,
                                   "bJQueryUI": true,
                                   "bAutoWidth": false,
                                   "bStateSave": true,
                                   "aLengthMenu": [[25, 50, 100, 200, -1],
                                                   [25, 50, 100, 200, "All"]],
                                   "iDisplayLength": 25,
                                   "fnDrawCallback": rowSelectCallback};

        if (hideHeaderAndFooter === false) {
            dataTableProperties["sDom"] = "t";
        }

        return $("#" + tableId).dataTable(dataTableProperties);
}