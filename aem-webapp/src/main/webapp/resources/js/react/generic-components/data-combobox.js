var DataCombobox = React.createClass({
    render: function() {
        var options = [];
        for (var idx in this.props.data) {
            var props = {value:this.props.data[idx][this.props.key]};
            if (this.props.selectedVal !== undefined &&
                this.props.selectedVal === this.props.data[idx][this.props.val]) {
                    props["selected"] = "selected";
            }
            options.push(React.DOM.option(props, this.props.data[idx][this.props.val]));
        }
        return React.DOM.select(null, options);
    }
})