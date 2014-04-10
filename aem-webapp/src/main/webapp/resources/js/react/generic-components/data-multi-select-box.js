var DataMultiSelectBox = React.createClass({
    render: function() {
        var options = [];
        for (var i = 0; i < this.props.data.length; i++) {

            var props;
            if (this.props.keyPropertyName !== undefined) {
                props = {name:this.props.name,type:"checkbox",
                         value:this.props.data[i][this.props.key][this.props.keyPropertyName]};
            } else {
                props = {name:this.props.name,type:"checkbox",
                         value:this.props.data[i][this.props.key]};
            }

            options.push(React.DOM.input(props, this.props.data[i][this.props.val]));
            options.push(React.DOM.br());
        }
        return React.DOM.div({className:this.props.className}, options);
    }
})