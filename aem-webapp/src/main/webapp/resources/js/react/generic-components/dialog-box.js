/**
 * A generic non-modal dialog box with a close button.
 * This can/will be enhanced iteratively.
 */
DialogBox = React.createClass({
    getInitialState: function() {
        var width = (this.props.width === undefined || parseInt(this.props.width) === NaN) ? 900 : this.props.width;
        var height = (this.props.height === undefined || parseInt(this.props.height) === NaN) ? 400 : this.props.height;
        return {
            width: width,
            height: height,
            top: (screen.height/2) - (height/2),
            left: (screen.width/2) - (width/2)
        }
    },
    render: function() {
        var theStyle = {zIndex:"999", position:"fixed",height:"auto",width:this.state.width + "px",top:this.state.top + "px",left:this.state.left + "px",display:"block"};
        var contentDivStyle = {display:"block",width:"auto",maxHeight:"none",height:"100%"};
        var contentDivClassName = this.props.contentDivClassName !== undefined ? this.props.contentDivClassName : "";
        return React.DOM.div({className:"ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable",
                              tabIndex:"-1",
                              style:theStyle},
                              React.DOM.div({className:"ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix", onMouseDown:this.mouseDownHandler, onMouseUp:this.mouseUpHandler},
                                React.DOM.span({className:"ui-dialog-title"}, this.props.title)),
                              React.DOM.div({className:"ui-dialog-content ui-widget-content " + contentDivClassName, style:contentDivStyle}, this.props.content),
                              React.DOM.div({className:"ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"},
                                React.DOM.div({className:"ui-dialog-buttonset"},
                                    React.DOM.button({ref:"xBtn", className:"ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only", onClick:this.closeCallback},
                                        React.DOM.span({className:"ui-button-text"}, "Close")))));
    },
    divOverlay: $('<div class="ui-widget-overlay ui-front"></div>'),
    componentDidMount: function() {
        if (this.props.modal === true) {
            $(this.getDOMNode()).parent().append(this.divOverlay);
        }
    },
    closeCallback: function() {
        $(this.refs.xBtn.getDOMNode()).removeAttr("title");
        React.unmountComponentAtNode($(this.getDOMNode()).parent().get(0));
    },
    mouseDownXDiff: 0,
    mouseDownYDiff: 0,
    mouseDownHandler: function(e) {
        e.preventDefault();
        this.mouseDown = true;
        this.mouseDownXDiff = e.pageX - this.state.left;
        this.mouseDownYDiff = e.pageY - this.state.top;
        $(document).on("mousemove", this.mouseMoveHandler);
    },
    mouseUpHandler: function(e) {
        e.preventDefault();
        $(document).off("mousemove", this.mouseMoveHandler);
    },
    mouseMoveHandler: function(e) {
        e.preventDefault();
        this.setState({top:e.pageY - this.mouseDownYDiff, left:e.pageX - this.mouseDownXDiff});
    }

});