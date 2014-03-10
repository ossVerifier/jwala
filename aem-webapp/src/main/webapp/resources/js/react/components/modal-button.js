var ModalButton = React.createClass({
    render: function() {
        return React.DOM.div(null,
                             this.props.modalDialog,
                             React.DOM.input({type:"button", onClick:this.handleClick, value:this.props.label}));
    },
    handleClick: function() {
        this.props.modalDialog.show();
    }
});