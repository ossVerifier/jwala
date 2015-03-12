/**
 * JQuery plugin to apply column resizing on HTML tables.
 *
 * @author: Jedd Anthony Cuison
 *
 * usage: {@link http://jsfiddle.net/jlkwison/auw373we/14/}
 *
 * TODO: Prevent columns from totally collapsing.
 */
(function($) {

    $.fn.makeColumnsResizable = function() {
        var self = this;
        return this.each(function(idx){

            var mouseDrag = false;
            var colIdx;
            var COL_RESIZE_CURSOR = "col-resize";
            var origWidths = [0, 0];

            var onTdMouseMove = function(e) {
                if (mouseDrag) {
                    var newWidth = rightCellBorderPagePos - e.pageX;
                    var newWidthDiff = origWidths[1] - newWidth;
                    $(self[idx]).find("thead th").eq(colIdx - 1).innerWidth(origWidths[0] + newWidthDiff);
                    $(self[idx]).find("thead th").eq(colIdx).innerWidth(newWidth);
                } else {
                    if ($(e.currentTarget).parent().children().index($(e.currentTarget))!==0){

                        if ((e.offsetX >= 0) &&
                            (e.offsetX <= 1)) {
                            $("html,body").css("cursor", COL_RESIZE_CURSOR);
                        } else {
                            $("html,body").css("cursor", "default");
                        }

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