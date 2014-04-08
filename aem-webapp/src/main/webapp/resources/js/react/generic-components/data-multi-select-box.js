var DataMultiSelectBox = React.createClass({
    render: function() {
        var options = [];
        for (var idx in this.props.data) {

            var props;
            if (this.props.keyIsIdObject !== undefined && this.props.keyIsIdObject === true) {
                props = {name:this.props.name,type:"checkbox",value:this.props.data[idx][this.props.key]["id"]};
            } else {
                props = {name:this.props.name,type:"checkbox",value:this.props.data[idx][this.props.key]};
            }

// TODO: Once the JVM get service returns multiple groups already, complete the code below to show to what group the JVM is assinged to
//            if (this.props.selectedVal !== undefined &&
//                this.props.selectedVal === this.props.data[idx][this.props.val]) {
//                    props["checked"] = "checked";
//            }
            options.push(React.DOM.input(props, this.props.data[idx][this.props.val]));
            options.push(React.DOM.br());
        }
        return React.DOM.div({className:this.props.className}, options);
    }
})