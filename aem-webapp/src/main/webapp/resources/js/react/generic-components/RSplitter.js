/**
 * Movable splitter divided container component.
 *
 * Import Note!!! Components that will be contained by the splitter should be wrapped with a div with position
 *                set to absolute so that component hiding works.
 *
 * Properties:
 *
 * 1. orientation - Dictates how the child components are arranged. Possible values VERTICAL_ORIENTATION or
 *                  HORIZONTAL_ORIENTATION or any which defaults to horizontal.
 * 2. components - components held by the splitter component.
 *
 * TODO: Update js docs
 */
var RSplitter = React.createClass({
    getInitialState: function() {
        var panelStates = [];
        var childContainerSize = (100 / this.props.components.length) + "%";

        if (this.props.panelDimensions === undefined) {
            var width = this.props.orientation === RSplitter.VERTICAL_ORIENTATION ?
                                                        "100%" : childContainerSize;
            var height = this.props.orientation === RSplitter.VERTICAL_ORIENTATION ?
                                                        childContainerSize : "100%";

            this.props.components.forEach(function(item) {
                panelStates.push({width: width, height: height});
            });
        } else {
            this.props.panelDimensions.forEach(function(dimension) {
                panelStates.push({width: dimension.width, height: dimension.height});
            });
        }

        return {
            mouseOnSplitter: false,
            grabSplitter: false,
            splitterRef: null,
            splitterIdx: -1,
            mousePos: -1,
            panelStates: panelStates
        }
    },

    render: function() {
        var divs = [];
        var self = this;
        var i = 0;
        var orientationClassName;
        var dividerClassName;

        if (this.props.orientation === RSplitter.VERTICAL_ORIENTATION) {
            orientationClassName = "vert";
            dividerClassName = "horz-divider";
        } else {
            orientationClassName = "horz";
            dividerClassName = "vert-divider";
        };

        this.props.components.forEach(function(item) {
            ++i;
            var key = RSplitter.getKey(i);

            var cursor = "auto";
            if (self.state.mouseOnSplitter || self.state.grabSplitter) {
                if (self.state.splitterIdx === (i - 1)) {
                    cursor = ((self.props.orientation === RSplitter.VERTICAL_ORIENTATION) ? "row-resize" : "col-resize");
                }
            }

            divs.push(React.createElement(RPanel, {key: key,
                                                   ref: key,
                                                   className:(i > 1 ? dividerClassName : "") + " rsplitter childContainer " + orientationClassName,
                                                   style:{width: self.state.panelStates[i - 1].width,
                                                          height: self.state.panelStates[i - 1].height,
                                                          cursor: cursor},
                                                   mouseMoveHandler: self.mouseMoveHandler.bind(self, key, i - 1),
                                                   mouseDownHandler: self.mouseDownHandler.bind(self, key, i - 1),
                                                   mouseUpHandler: self.mouseUpHandler}, item));

        });

        return React.createElement("div", {ref: "mainContainer", className:"rsplitter container " + orientationClassName}, divs);
    },

    mouseMoveHandler: function(ref, idx, e) {

        var pagePos = this.props.orientation === RSplitter.VERTICAL_ORIENTATION ? e.pageY : e.pageX;

        if (pagePos !== this.state.mousePos && this.state.grabSplitter) {
            if (this.props.orientation === RSplitter.VERTICAL_ORIENTATION) {
                var topDivHeight =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx)].getDOMNode()).height();
                var currentDivHeight =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx + 1)].getDOMNode()).height();
                var dif = pagePos - this.state.mousePos;

                var topDivHeight = topDivHeight + dif;
                var bottomDivHeight = currentDivHeight - dif;

                // Prevent the 2 concerned panels height to affect other divs beside them
                if (topDivHeight > 0 && bottomDivHeight > 0) {
                    this.state.panelStates[this.state.splitterIdx - 1].height = topDivHeight;
                    this.state.panelStates[this.state.splitterIdx].height = bottomDivHeight;
                }

            } else {
                var leftDivWidth =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx)].getDOMNode()).width();
                var currentDivWidth =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx + 1)].getDOMNode()).width();
                var dif = pagePos - this.state.mousePos;
                this.state.panelStates[this.state.splitterIdx - 1].width = leftDivWidth + dif;
                this.state.panelStates[this.state.splitterIdx].width = currentDivWidth - dif;
            }
            this.setState({mousePos: pagePos});
            e.preventDefault();
        } else if (idx > 0) {
            var divO = $(this.refs[ref].getDOMNode());

            var relPos = pagePos - (this.props.orientation === RSplitter.VERTICAL_ORIENTATION ? divO.offset().top : divO.offset().left);

            if (relPos < RSplitter.SPLITTER_DRAG_AREA_SIZE) {
                this.setState({mousePos: pagePos, splitterIdx: idx, mouseOnSplitter: true});
            } else {
                this.setState({mouseOnSplitter: false});
                return;
            }
            e.preventDefault();
        }
    },

    mouseDownHandler: function(ref, idx, e) {
        if (idx > 0 && this.state.mouseOnSplitter && !this.state.grabSplitter) {
            this.setState({splitterRef: ref, splitterIdx: idx, grabSplitter: true});
            e.preventDefault();
        }
    },

    mouseUpHandler: function(e) {
        if (this.state.grabSplitter) {
            this.setState({grabSplitter: false});
            e.preventDefault();
        }
    },

    statics: {
        SPLITTER_DRAG_AREA_SIZE: 5,
        CHILD_CONTAINER_PREFIX: "cc",
        VERTICAL_ORIENTATION: "vert",
        HORIZONTAL_ORIENTATION: "horz",
        UID: "uid-" + Date.now(),
        getKey: function(someOtherRefCode) {
            return RSplitter.UID + "-" + someOtherRefCode;
        }
    }

});

/**
 * A panel component.
 *
 * Note: Refactor in the future in such a way that the panel maintains its dimensions and other stuff as state ?
 *       In doing so, we would have to devise a way to make panels know that the panel beside it has changed
 *       and that it should react accordingly e.g. adjust it's width or height.
 */
var RPanel = React.createClass({
    render: function() {
        var i = this.props.panelIdx;
        return React.createElement("div", {className: this.props.className,
                                           style: this.props.style,
                                           onMouseMove: this.props.mouseMoveHandler,
                                           onMouseDown: this.props.mouseDownHandler,
                                           onMouseUp: this.props.mouseUpHandler}, this.props.children);
    }
});