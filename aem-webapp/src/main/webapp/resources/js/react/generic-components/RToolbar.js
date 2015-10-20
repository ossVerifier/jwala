/**
 * A toolbar component.
 */
var RToolbar = React.createClass({
    render: function() {
        var self = this;
        var btns = [];
        this.props.metaData.forEach(function(meta){
            btns.push(React.createElement(RToolbarButton, {key: meta.title, meta: meta,
                btnClassName: self.props.btnClassName, busyBtnClassName: self.props.busyBtnClassName}));
        });

        return React.createElement("div", {className: this.props.className}, btns);
    }
});

/**
 * The toolbar button.
 */
var RToolbarButton = React.createClass({
    timeout: null,
    getInitialState: function() {
        return {clicked: false, hover: false}
    },
    render: function() {
        var hoverClassName = this.state.hover ? "ui-state-hover " : "";
        if (!this.state.clicked) {
            return React.createElement("button", {className:"ui-button ui-widget ui-state-default ui-corner-all " + hoverClassName +
                                                  this.props.btnClassName, onClick:this.onClick, onMouseMove: this.onMouseMove,
                                                  onMouseOut: this.onMouseOut},
                       React.createElement("span", {className:"ui-icon " + this.props.meta.icon, title: this.props.meta.title}))
        }
        return React.createElement("button", {className:"ui-button ui-widget ui-corner-all " + this.props.busyBtnClassName});
    },
    onClick: function() {
        if (this.props.busyBtnClassName !== undefined) {
            this.setState({clicked: true});
            // TODO: Make hard coded timeout definable.
            this.timeout = setTimeout(this.timeout, 60000);
        }
        this.props.meta.onClickCallback(this.ajaxProcessDoneCallback)
    },
    timeout: function() {
        this.setState({clicked: false});
    },
    onMouseMove: function() {
        if (!this.state.hover) {
            this.setState({hover: true});
        }
    },
    onMouseOut: function() {
        this.setState({hover: false});
    },
    ajaxProcessDoneCallback: function() {
        this.setState({clicked: false});
    }
});
