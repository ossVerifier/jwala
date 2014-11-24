/**
 * A generic modal dialog box with ok and cancel buttons.
 *
 * Properties:
 *
 * 1. content - the content of the dialog box. This can be a react component.
 * 2. width - width of the dialog. If undefined it is set to auto.
 * 3. height - height of the dialog. If undefined it is set to auto.
 * 4. show - if true the dialog box is displayed, otherwise it is hidden.
 * 5. contentDivClassName - defines the div container class of the content.
 * 6. title - the title of the dialog box.
 * 7. okCallback - the callback that is called when the ok button is clicked.
 * 8. cancelCallback - the callback that is called when the cancel button is clicked.
 * 9. okLabel - the "ok" button label. If undefined the button label shows "Ok" by default.
 * 10. cancelLabel - the "cancel" button label. If undefined the button label shows "Cancel" by default.
 */
ModalDialogBox = React.createClass({
    getInitialState: function() {
        return {
            top: -1000,
            left: -1000
        }
    },
    render: function() {
        if (!this.props.show) {
            $(document).off("keydown");
            return React.DOM.div();
        }
        $(document).on("keydown", this.keyDownHandler);

        var height = this.props.height === undefined ? "auto" : this.props.height;
        var width = this.props.width === undefined ? "auto" : this.props.width;

        var theStyle = {zIndex:"998", position:"fixed",height:height,width:width,top:this.state.top + "px",left:this.state.left + "px",display:"block"};
        var contentDivStyle = {display:"block",width:"auto",maxHeight:"none",height:"100%"};
        var contentDivClassName = this.props.contentDivClassName !== undefined ? this.props.contentDivClassName : "";

        var theDialog = React.DOM.div({ref:"theDialog",
                                       className:"ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable",
                                       tabIndex:"-1",
                                       style:theStyle},
                                       React.DOM.div({className:"ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix",
                                                      onMouseDown:this.mouseDownHandler,
                                                      onMouseUp:this.mouseUpHandler},
                                                     React.DOM.span({className:"ui-dialog-title text-align-center"}, this.props.title),
                                                     RButton({title:"close",
                                                              className:"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close",
                                                              onClick:this.xBtnClick,
                                                              spanClassName:"ui-button-icon-primary ui-icon ui-icon-closethick"})),
                                       React.DOM.div({className:"ui-dialog-content ui-widget-content " + contentDivClassName, style:contentDivStyle}, this.props.content),
                                       React.DOM.div({className:"ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"},
                                                     React.DOM.div({className:"ui-dialog-buttonset"},
                                                     RButton({onClick:this.okCallback, label:this.props.okLabel === undefined ? "Ok" : this.props.okLabel}),
                                                     RButton({onClick:this.cancelCallback, label:this.props.cancelLabel === undefined ? "Cancel" : this.props.cancelLabel}))));

        return React.DOM.div({style:this.props.show ? {} : {display:"none"}},
                             React.DOM.div({className:"ui-widget-overlay ui-front"}, ""), theDialog);

    },
    keyDownHandler: function(e) {
        if (e.keyCode == 27) {
            this.props.cancelCallback();
        }
    },
    componentDidUpdate: function() {
        // Set the initial position if it is not yet set.
        if (this.refs.theDialog !== undefined) {
            var height = $(this.refs.theDialog.getDOMNode()).height();
            var width = $(this.refs.theDialog.getDOMNode()).width();
            if (this.state.top < 0) {
                this.setState({top: ($(window).height()/2) - (height/2),
                               left: ($(window).width()/2) - (width/2)});
            }
        }
    },
    xBtnClick: function() {
        this.props.cancelCallback();
    },
    okCallback: function() {
        this.props.okCallback();
    },
    cancelCallback: function() {
        this.props.cancelCallback();
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