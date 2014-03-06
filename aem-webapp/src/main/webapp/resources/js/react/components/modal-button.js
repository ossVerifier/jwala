var modalDialog;

var ModalButton = React.createClass({
    render: function() {
        modalDialog = this.props.modalDialog;
        return React.DOM.div(null,
                              modalDialog,
                              React.DOM.input({type:"button", onClick:this.handleClick, value:this.props.label}));
    },
    handleClick: function() {
        modalDialog.show();
    }

});