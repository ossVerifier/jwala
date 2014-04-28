var DataMultiSelectBox = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {checkBoxData: undefined};
    },
    componentWillReceiveProps: function(nextProps) {
        var checkBoxData = [];
        for (var i = 0; i < nextProps.data.length; i++) {
            var valId = nextProps.data[i][nextProps.key][nextProps.keyPropertyName];
            checkBoxData.push({checked: this.getCheckedVal(nextProps.selectedValIds, valId), valId:valId});
        }
        this.setState({checkBoxData:checkBoxData});
    },
    getCheckedVal: function(selectedValIds, valId) {
        if (selectedValIds !== undefined) {
            for (var i = 0; i < selectedValIds.length; i++) {
                if (selectedValIds[i].id === valId) {
                    return "checked";
                }
            }
        }
        return "";
    },
    render: function() {
        var self = this;
        var options = [];
        for (var i = 0; i < this.props.data.length; i++) {
            var props;
            if (this.props.keyPropertyName !== undefined) {
                props = {name:this.props.name,type:"checkbox",
                         value:this.props.data[i][this.props.key][this.props.keyPropertyName],
                         checked:this.state.checkBoxData[i].checked,
                         onChange:this.changeHandler.bind(this, i)};
            } else {
                props = {name:this.props.name,type:"checkbox",
                         value:this.props.data[i][this.props.key],
                         checked:this.state.checkBoxData[i].checked,
                         onChange:this.changeHandler.bind(this, i)};
            }

            // We need to wrap the checkbox in a div to prevent this issue:
            // https://github.com/facebook/react/issues/997
            options.push(React.DOM.div(null, React.DOM.input(props, this.props.data[i][this.props.val])));
        }
        return React.DOM.div({className:this.props.className}, options);
    },
    changeHandler: function(i) {
        if (this.state.checkBoxData[i].checked === "") {
            this.state.checkBoxData[i].checked = "checked";
        } else {
            this.state.checkBoxData[i].checked = "";
        }
        this.setState({checkBoxData:this.state.checkBoxData});

        var selectedValIds = [];
        for (var i = 0; i < this.state.checkBoxData.length; i++) {
            if (this.state.checkBoxData[i].checked === "checked") {
                var obj = {id: this.state.checkBoxData[i].valId};

                // this is for the insert part since the rest service
                // post and get do not have the same name for the id
                obj[this.props.idKey] = this.state.checkBoxData[i].valId;

                selectedValIds.push(obj);
            }
        }

        this.props.onSelectCallback(selectedValIds);
    }
})