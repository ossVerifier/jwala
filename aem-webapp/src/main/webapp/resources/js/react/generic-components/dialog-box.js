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
        var theStyle = {position:"fixed",height:"auto",width:this.state.width + "px",top:this.state.top + "px",left:this.state.left + "px",display:"block"};
        var contentDivStyle = {display:"block",width:"auto",maxHeight:"none",height:"100%"};
        var contentDivClassName = this.props.contentDivClassName !== undefined ? this.props.contentDivClassName : "";
        return React.DOM.div({className:"ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable",
                              tabIndex:"-1",
                              style:theStyle},
                              React.DOM.div({className:"ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix", onMouseDown:this.mouseDownHandler, onMouseUp:this.mouseUpHandler, onMouseMove:this.mouseMoveHandler},
                                React.DOM.span({className:"ui-dialog-title"}, this.props.title)),
                              React.DOM.div({className:"ui-dialog-content ui-widget-content " + contentDivClassName, style:contentDivStyle}, this.props.content),
                              React.DOM.div({className:"ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"},
                                React.DOM.div({className:"ui-dialog-buttonset"},
                                    React.DOM.button({className:"ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only", onClick:this.closeCallback},
                                        React.DOM.span({className:"ui-button-text"}, "Close")))));
    },

    componentDidMount: function() {

    },

    closeCallback: function() {
        React.unmountComponentAtNode($(this.getDOMNode()).parent().get(0));
    },
    mouseDown : false,
    mouseDownXDiff: 0,
    mouseDownYDiff: 0,
    mouseDownHandler: function(e) {
        this.mouseDown = true;
        this.mouseDownXDiff = e.pageX - this.state.left;
        this.mouseDownYDiff = e.pageY - this.state.top;
    },
    mouseUpHandler: function() {
        this.mouseDown = false;
    },
    mouseMoveHandler: function(e) {
        if (this.mouseDown) {
            this.setState({top:e.pageY - this.mouseDownYDiff, left:e.pageX - this.mouseDownXDiff});
        }
    }

});