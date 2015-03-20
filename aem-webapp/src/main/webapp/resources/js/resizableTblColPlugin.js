/**
 * JQuery plugin to apply column resizing on HTML tables.
 *
 * @author: Jedd Anthony Cuison
 *
 * usage: {@link http://jsfiddle.net/jlkwison/auw373we/18/}
 *
  * @param tableWidth width "value" of the table e.g. 50 (not "50px").
  * @param minColWidth the allowable minimum column width "value". If set to zero, the column dissappears
  *                    if the column's width is set to 0. If not specified, min column width is 3.
  */
(function($) {

    $.fn.makeColumnsResizable = function(tableWidth, minColWidth) {
        var self = this;
        return this.each(function(idx){

            var mouseDrag = false;
            var colIdx;
            var COL_RESIZE_CURSOR = "col-resize";
            var origWidths = [0, 0];

            minColWidth = (minColWidth === undefined ? 5 : minColWidth);

            var onTdMouseMove = function(e) {
                if (mouseDrag) {
                    var newWidth = rightCellBorderPagePos - e.pageX;
                    var newWidthDiff = origWidths[1] - newWidth;

                    if (newWidth > minColWidth && (origWidths[0] + newWidthDiff) > minColWidth) {
                        $(self[idx]).find("thead th").eq(colIdx - 1).innerWidth(origWidths[0] + newWidthDiff);
                        $(self[idx]).find("thead th").eq(colIdx).innerWidth(newWidth);
                    }

                } else {
                    if ($(e.currentTarget).parent().children().index($(e.currentTarget))!==0){

                        if ((e.offsetX >= 0) &&
                            (e.offsetX <= 1)) {
                            $("html,body").css("cursor", COL_RESIZE_CURSOR);
                        } else {
                            $("html,body").css("cursor", "default");
                        }

                    } else {
                         $("html,body").css("cursor", "default");
                    }
                }
                e.preventDefault();
            }

            var onTdMouseDown = function(e) {
                if ($("html,body").css("cursor") === COL_RESIZE_CURSOR && !mouseDrag) {
                    mouseDrag = true;
                    colIdx = $(e.currentTarget).parent().children().index($(e.currentTarget));
                    rightCellBorderPagePos = $(e.currentTarget).offset().left + $(e.currentTarget).innerWidth();

                    origWidths[0] = $(e.currentTarget).prev().innerWidth();
                    origWidths[1] = $(e.currentTarget).innerWidth();

                    e.preventDefault();
                }
            }

            var onTdMouseUp = function() {
                $("html,body").css("cursor", "default");
                mouseDrag = false;
            }

            var onTbodyMouseLeave = function() {
                $("html,body").css("cursor", "default");
                mouseDrag = false;
            }

            if (tableWidth !== undefined) {
                $(self[idx]).width(tableWidth);
            }

            $(self[idx]).find("tbody td").mousemove(onTdMouseMove);
            $(self[idx]).find("tbody td").mousedown(onTdMouseDown);
            $(self[idx]).find("tbody td").mouseup(onTdMouseUp);
            $(self[idx]).find("tbody").mouseleave(onTbodyMouseLeave);

            $(self[idx]).addClass("adj-col");
            $(self[idx]).find("thead th").addClass("adj-col");
            $(self[idx]).find("tbody td").addClass("adj-col");
        });
    }

} (jQuery));