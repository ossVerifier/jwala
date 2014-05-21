var DataCombobox = React.createClass({
    render: function() {
        var options = [];
        for (var idx in this.props.data) {
            var props = {value:eval("this.props.data[idx]." + this.props.key)};
            if (this.props.selectedVal !== undefined &&
                this.props.selectedVal === eval("this.props.data[idx]." + this.props.key)) {
                    props["selected"] = "selected";
            }
            options.push(React.DOM.option(props, eval("this.props.data[idx]." + this.props.val)));
        }
        return React.DOM.select({
          onChange:this.props.onchange,
          name:this.props.name
         }, options);
    }
})