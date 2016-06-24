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
 * 11. top - the dialog's top position. If not set top will be computed to position the dialog at the middle of the screen.
 * 12. left - the dialog's left position. If not set left will be computed to position the dialog at the center of the screen.
 *
 * Usage Example (in JSX)
 *
 * <ModalDialogBox title="Edit JVM"
 *                 show={this.state.showModalFormEditDialog}
 *                 okCallback={this.okEditCallback}
 *                 cancelCallback={this.cancelEditCallback}
 *                 content={<JvmConfigForm ref="jvmEditForm"
 *                                         data={this.state.selectedJvmForEditing}/>}
 */
var ModalDialogBox = React.createClass({
    getInitialState: function() {

        var top = this.props.top === undefined ? -10000 : this.props.top;
        var left = this.props.left === undefined ? -10000 : this.props.left;

        return {
            top: top,
            left: left,
            show: this.props.show,
            title: this.props.title,
            content: this.props.content,
            okCallback: this.props.okCallback,
            cancelCallback: this.props.cancelCallback
        }
    },
    componentWillReceiveProps: function(nextProps) {
        if (this.props.show !== nextProps.show) {
            this.setState({show:nextProps.show, content:nextProps.content});
        }
    },
    render: function() {
        if (!this.state.show) {
            return React.DOM.div();
        }

        var height = this.props.height === undefined ? "auto" : this.props.height;
        var width = this.props.width === undefined ? "auto" : this.props.width;

        var theStyle = {overflow:"visible", zIndex:"998", position:"absolute",height:height,width:width,top:this.state.top + "px",left:this.state.left + "px",display:"block"};
        var contentDivStyle = {display:"block",width:"auto",maxHeight:"none",height:"auto"};
        var contentDivClassName = this.props.contentDivClassName !== undefined ? this.props.contentDivClassName : "";

        var theDialog = React.DOM.div({ref:"theDialog",
                                       className:"ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable",
                                       tabIndex:"-1",
                                       style:theStyle,
                                       onKeyDown:this.keyDownHandler},
                                       React.DOM.div({className:"ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix",
                                                      onMouseDown:this.mouseDownHandler,
                                                      onMouseUp:this.mouseUpHandler},
                                                     React.DOM.span({className:"ui-dialog-title text-align-center"}, this.state.title),
                                                     RButton({ref:"xBtn",
                                                              title:"close",
                                                              className:"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close",
                                                              onClick:this.xBtnClick,
                                                              spanClassName:"ui-button-icon-primary ui-icon ui-icon-closethick"})),
                                       React.DOM.div({className:"ui-dialog-content ui-widget-content " + contentDivClassName, style:contentDivStyle}, this.state.content),
                                       React.DOM.div({className:"ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"},
                                                     React.DOM.div({className:"ui-dialog-buttonset"},
                                                     this.state.okCallback ? RButton({ref:"okBtn", onClick:this.okCallback, label:this.props.okLabel === undefined ? "Ok" : this.props.okLabel}) : null,
                                                     RButton({ref:"cancelBtn", onClick:this.cancelCallback, label:this.props.cancelLabel === undefined ? "Cancel" : this.props.cancelLabel}))));

                                       // Text area that would do the resizing for us, the only problem is that we need to resize the content along with it.
                                       // React.createElement("textArea", {style: {width: "100%", height: "100%", position: "absolute", top: 0, left: 0, zIndex: -999}})

        return React.DOM.div({style:this.state.show ? {} : {display:"none"}},
                             React.DOM.div({className:"ui-widget-overlay ui-front"}, ""), theDialog);

    },
    keyDownHandler: function(e) {
        if (e.keyCode === 27) {
            this.state.cancelCallback();
        } else if (e.keyCode === 13) {
            if (this.state.okCallback() !== false) {
                e.preventDefault();
            }
        }
    },
    componentDidMount: function() {
        // This is for scenario where show is set to true initially.
        // Initiate re-render if top and left is not defined.
        if (this.state.show && this.state.top < 0) {
            this.setState({show:true}); // Initiates render which computes top and left
        }
    },
    componentDidUpdate: function() {
        // Set the initial position if it is not yet set. Position the dialog at the center of the screen.
        if (this.refs.theDialog !== undefined) {
            if (this.state.top === -10000) {
                var height = $(this.refs.theDialog.getDOMNode()).height();
                var width = $(this.refs.theDialog.getDOMNode()).width();

                var offsetX = $(window).width()/2 - $(this.getDOMNode()).parent().offset().left;
                var offsetY = $(document).height()/2 - $(this.getDOMNode()).parent().offset().top;

                this.setState({top:offsetY - height/2,left:offsetX - width/2});

                if (this.props.modal === true) {
                    $(this.getDOMNode()).parent().append(this.divOverlay);
                }
            }
        }
    },
    xBtnClick: function() {
        if (this.state.cancelCallback !== undefined) {
            this.state.cancelCallback();
        } else {
            this.setState({show:false});
        }
    },
    okCallback: function() {
        this.state.okCallback();
    },
    cancelCallback: function() {
        this.xBtnClick();
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
    },
    show: function(title, content, okCallback, cancelCallback) {
        var states = {show: true};

        if (title) {
            states["title"] = title;
        }

        if (content) {
            states["content"] = content;
        }

        if (okCallback) {
            states["okCallback"] = okCallback;
        }

        if (cancelCallback) {
            states["cancelCallback"] = cancelCallback;
        }

        this.setState(states);
    },
    close: function() {
        this.setState({show:false});
    }
});