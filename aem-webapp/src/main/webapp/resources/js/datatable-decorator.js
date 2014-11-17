//TODO The functions in here need to be name-spaced
/**
 * Creates an id delimited by dash delimeter
 * e.g. ["btn", "1"] returns btn-1
 */
var createDelimitedId = function(idFragments, delimiter) {
    var tmpId;
    for (var i = 0; i < idFragments.length; i++) {
        if (tmpId === undefined) {
            tmpId = idFragments[i];
        } else {
            tmpId = tmpId + delimiter + idFragments[i]
        }
    }
    return tmpId;
};

/**
 * This method transforms a table to a JQuery DataTable.
 */
var decorateTableAsDataTable = function(tableId,
                                        tableDef,
                                        applyThemeRoller,
                                        hideHeaderAndFooter,
                                        editCallback,
                                        rowSelectCallback,
                                        expandIcon,
                                        collapseIcon,
                                        childTableDetails,
                                        parentItemId /* e.g. group id. This is used to retrieve child data via the data callback method when the expand-collapse control is clicked */,
                                        rootId /* This is the first element id in a hierarchy */,
                                        initialSortColumn,
                                        parentItemName){

    var self = this;

    // build column definitions based on props
    var aoColumnDefs = [];
    var aaSorting;
    if (initialSortColumn !== undefined) {
        aaSorting = initialSortColumn;
    } else {
        aaSorting = [];
    }

    $(tableDef).each(function(itemIndex, item, itemArray) {
            if (!isArray(item)) {
                aoColumnDefs[itemIndex] = {"sTitle": item.sTitle,
                                           "mData": item.mData,
                                           "aTargets": [itemIndex]};

                if(item.bVisible !== undefined) {
                    aoColumnDefs[itemIndex].bVisible = item.bVisible;
                }

                if (item.tocType === "control") {
                    self.expandCollapseEnabled = true;
                    aoColumnDefs[itemIndex].mDataProp = null;
                    aoColumnDefs[itemIndex].sClass = "control center";
                    aoColumnDefs[itemIndex].sWidth = "20px";
                }

                if (item.mRender !== undefined ||
                    item.tocType === "button"  ||
                    item.tocType === "control" ||
                    item.tocType === "emptyColumn") {
                        aoColumnDefs[itemIndex].bSortable = false;
                }

                if (item.colWidth !== undefined) {
                    aoColumnDefs[itemIndex].sWidth = item.colWidth;
                }

                if (item.sClass !== undefined) {
                    aoColumnDefs[itemIndex].sClass = aoColumnDefs[itemIndex].sClass + " " + item.sClass;
                }

            } else {

                /**
                 * Only 1 field related to data can be merged to one cell!
                 * And that includes the ExpandCollapseControl which has an mData!
                 * This is because every cell can only have one mData.
                 */
                var theItem;
                var colWidth = 0;
                // look for the item with the mData
                for (var i = 0; i < item.length; i++) {
                    if (theItem === undefined && item[i].mData !== undefined && item[i].tocType === undefined) {
                        theItem = item[i];
                    }
                    colWidth += item[i].colWidth !== undefined ? item[i].colWidth : 0;
                }

                if (theItem === undefined) {
                    // Init to empty item
                    theItem = {"sTitle": null, "mData": null};
                }

                aoColumnDefs[itemIndex] = {"sTitle": theItem.sTitle,
                                           "mData": theItem.mData,
                                           "aTargets": [itemIndex]};

                aoColumnDefs[itemIndex].sClass = "nowrap";
                aoColumnDefs[itemIndex].bSortable = false;

                if (colWidth !== 0) {
                    aoColumnDefs[itemIndex].sWidth = colWidth + "px";
                }
            }

            if (!isArray(item) && item.mRender !== undefined) {
                // If mRender is set to a function
                aoColumnDefs[itemIndex].mRender = item.mRender;
            } else if (!isArray(item) && item.tocType === "custom") {
                if(item.tocRenderer == 'undefined') {
                    alert('You set tocType to custom, but you did not set tocRenderCfgFn to a function(dataTable, data, aoColumnDefs, i) { aoColumnDefs[i].mRender = function(data, type, full){}}!');
                } return item.tocRenderCfgFn(self, item, aoColumnDefs, itemIndex);
            } else {
                aoColumnDefs[itemIndex].mRender = function(data, type, full) {
                   var renderStr = "";
                   if (isArray(item)) {
                        for (var i = 0; i < item.length; i++) {
                            renderStr += renderComponents(tableId,
                                                          parentItemId,
                                                          rootId,
                                                          childTableDetails,
                                                          item[i],
                                                          data,
                                                          type,
                                                          full,
                                                          expandIcon,
                                                          collapseIcon,
                                                          editCallback,
                                                          parentItemName);
                        }
                    } else {
                        renderStr = renderComponents(tableId,
                                                     parentItemId,
                                                     rootId,
                                                     childTableDetails,
                                                     item,
                                                     data,
                                                     type,
                                                     full,
                                                     expandIcon,
                                                     collapseIcon,
                                                     editCallback,
                                                     parentItemName);
                        return renderStr;
                    }
                    return "<div style='overflow:hidden;text-align:right'>" + renderStr + "</div>"
                }
            }
        });

        var dataTableProperties = {"aaSorting": aaSorting,
                                   "aoColumnDefs": aoColumnDefs,
                                   "bJQueryUI": applyThemeRoller === undefined ? true : applyThemeRoller,
                                   "bAutoWidth": false,
                                   "bStateSave": true,
                                   "aLengthMenu": [[25, 50, 100, 200, -1],
                                                   [25, 50, 100, 200, "All"]],
                                   "iDisplayLength": 25,
                                   "fnDrawCallback": rowSelectCallback,
                                   "sPaginationType": "toc"};

        if (hideHeaderAndFooter === false) {
            dataTableProperties["sDom"] = "t";
        }

        var decorated = $("#" + tableId).dataTable(dataTableProperties);
        return decorated;
};

var TocPager = {

    init: function() {
        $.fn.dataTableExt.oPagination.toc = this;

    },
    "fnInit": function ( oSettings, nPaging, fnCallbackDraw )
    {
    $.fn.dataTableExt.oPagination.two_button.fnInit(oSettings, nPaging, fnCallbackDraw);
    },

    attachExpanderClickEvent: function(idx, obj) {
        var expander = this.allExpanders[obj.id];
        if(expander !== undefined) {
            $("#" + obj.id).off("click");
            $("#" + obj.id).on("click",
                expander.component.onClick.bind(expander.component,
                                                expander.dataSources,
                                                expander.childTableDetailsArray));
        }
    },

    attachButtonMouseOverAndClickEvents: function(idx, obj) {
        var reactBtn = this.allButtons[obj.id];
        var btn = $("#" + obj.id);
        if(reactBtn !== undefined) {
            DataTableButton.bindEvents(reactBtn);
        }
    },

   "fnUpdate": function ( oSettings, fnCallbackDraw )
    {
       $.fn.dataTableExt.oPagination.two_button.fnUpdate(oSettings, fnCallbackDraw);
       // Need to bind onclick to expand collapse options in this method

       var decorated = oSettings.nTable;

       if(decorated !== null) {
           var self = this;
           $('img', decorated).each(function(idx,obj) {self.attachExpanderClickEvent(idx, obj)});
           $('.ui-button').each(function(idx,obj) {self.attachButtonMouseOverAndClickEvents(idx, obj)});
       }
    },
    allExpanders : {},
    allButtons : {}
};

TocPager.init();

var renderComponents = function(tableId,
                                parentItemId,
                                rootId,
                                childTableDetails,
                                item,
                                data,
                                type,
                                full,
                                expandIcon,
                                collapseIcon,
                                editCallback,
                                parentItemName) {
    var renderedComponent;

    if (item.tocType === "space") {
        renderedComponent = "&nbsp;";
    } else if (item.tocType === "link") {
        renderedComponent = renderLink(item, tableId, data, type, full, editCallback);
    } else if (item.tocType === "control") {
        renderedComponent = renderExpandCollapseControl(tableId,
                                                        parentItemId,
                                                        rootId,
                                                        childTableDetails,
                                                        data,
                                                        type,
                                                        full,
                                                        expandIcon,
                                                        collapseIcon,
                                                        parentItemName);
    } else if (item.tocType === "array") {
        renderedComponent = renderArray(item, data);
        if (renderedComponent.length > item.maxDisplayTextLen) {
            renderedComponent = "<span title=" + renderedComponent.replace(/ /gi, "&nbsp;").replace(/,/gi, "&#10;") + ">"  +
                                renderedComponent.substring(0, item.maxDisplayTextLen) + "..." +
                                "</span>";
        }
    } else if (item.tocType === "button") {
        renderedComponent = renderButton(tableId, item, data, type, full, parentItemId);
    } else if (item.tocType === "emptyColumn") {
        renderedComponent = "";
    } else {
        renderedComponent = data;
    }
    return renderedComponent;
};

var isArray = function(val) {
    return (val instanceof Array)
};

var renderButton = function(tableId, item, data, type, full, parentItemId) {

    var btnClassifier = item.id !== undefined ? item.id : item.btnLabel;
    var id = tableId + "btn" + btnClassifier.replace(/[\. ,:-]+/g, '') +  full.id.id;

    var extraData;
    if (item.extraDataToPassOnCallback instanceof Array) {
        extraData = {};
        item.extraDataToPassOnCallback.forEach(function(property){
            extraData[property] = full[property];
        });
    } else {
        extraData = full[item.extraDataToPassOnCallback];
    }

    var reactBtn = new DataTableButton({id:id,
                                        sTitle:item.sTitle,
                                        className:item.className,
                                        customBtnClassName:item.customBtnClassName,
                                        customSpanClassName:item.customSpanClassName,
                                        clickedStateClassName:item.clickedStateClassName,
                                        itemId:full.id.id,
                                        label:item.btnLabel,
                                        callback:item.btnCallback,
                                        isToggleBtn:item.isToggleBtn,
                                        label2:item.label2,
                                        callback2:item.callback2,
                                        expectedState:item.expectedState,
                                        isBusyCallback:item.isBusyCallback,
                                        buttonClassName:item.buttonClassName,
                                        extraDataToPassOnCallback:extraData,
                                        busyStatusTimeout:item.busyStatusTimeout,
                                        parentItemId:parentItemId});

    TocPager.allButtons[id] = reactBtn;

    return React.renderComponentToStaticMarkup(reactBtn);

};

var renderLink = function(item, tableId, data, type, full, editCallback) {
    if (item.hRefCallback === undefined) {
        var linkLabelPartId = item.linkLabel !== undefined ? item.linkLabel.replace(/[\. ,:-]+/g, '')
                                                           : data.replace(/[\. ,:-]+/g, '');
        var id = createDelimitedId([tableId, "link",  linkLabelPartId, full.id.id], "_");
        return React.renderComponentToStaticMarkup(new Anchor({id:id,
                                                               data:full,
                                                               value:item.linkLabel !== undefined ?
                                                                     item.linkLabel :
                                                                     data,
                                                               callback:item.onClickCallback !== undefined ?
                                                                        item.onClickCallback :
                                                                        editCallback,
                                                               waitForResponse:item.waitForResponse}));
    }  else {
        return "<a href='" + item.hRefCallback(full) + "' target='_blank'>" + item.linkLabel + "</a>";
    }
};

var renderExpandCollapseControl = function(tableId, parentItemId, rootId, childTableDetails, data, type, full, expandIcon, collapseIcon) {
    var parentItemId = (parentItemId === undefined ? full.id.id : parentItemId);

    if(Object.prototype.toString.call(childTableDetails) === "[object Array]") {
        for (var i = 0; i < childTableDetails.length; i++) {
            childTableDetails[i]["data"] = data;
        }
    } else {
        childTableDetails["data"] = data;
    }

    var theRootId = (rootId === undefined ? full.id.id : rootId);
    var delimitedId = createDelimitedId([tableId,
                                         full.id.id], "_");

    var dataSources = [];
    var childTableDetailsArray = [];

    if(Object.prototype.toString.call(childTableDetails) === "[object Array]") {
        for (var i = 0; i < childTableDetails.length; i++) {
            dataSources[i] = childTableDetails[i].dataCallback === undefined ?
                                {jsonData:childTableDetails[i].data} :
                                {dataCallback:childTableDetails[i].dataCallback};
            childTableDetailsArray[i] = childTableDetails[i];
        }
    } else {
            dataSources[0] = childTableDetails.dataCallback === undefined ?
                                {jsonData:childTableDetails.data} :
                                {dataCallback:childTableDetails.dataCallback};
            childTableDetailsArray[0] = childTableDetails;
    }

    var expander = new ExpandCollapseControl({id:delimitedId,
                                               expandIcon:expandIcon,
                                               collapseIcon:collapseIcon,
                                               childTableDetails:childTableDetails,
                                               rowSubComponentContainerClassName:"row-sub-component-container",
                                               parentItemId:full.id.id,
                                               parentItemName:full.name,
                                               dataTable:$("#" + tableId).dataTable(),
                                               rootId:theRootId});

    var renderedComponent = React.renderComponentToStaticMarkup(expander);

    TocPager.allExpanders[delimitedId] = { "component": expander, "dataSources": dataSources, "childTableDetailsArray": childTableDetailsArray };

    return renderedComponent;
};

var renderArray = function(item, data) {

    var str = "";
    /* would be better with _Underscore.js : */
    for (var idx = 0; idx < data.length; idx=idx+1) {
        str = str + (str === "" ? "" : ",") + data[idx][item.displayProperty];
    }
    return str;
};