/** @jsx React.DOM */
var TextBox = React.createClass({
    getInitialState: function() {
        return {
            hintClassName: this.props.hintClassName,
            typeName: "text",
            value: this.props.hint
        }
    },
    render: function() {
        if (this.props.hint === undefined) {
            return <input type={this.props.isPassword ? "password" : "text"}/>
        }
        return <input type={this.state.typeName}
                      className={this.state.hintClassName}
                      value={this.state.value}
                      onFocus={this.handleFocus}
                      onBlur={this.handleBlur}
                      onChange={this.handleChange}/>
    },
    handleFocus: function() {
        if (this.state.value === this.props.hint) {
            if (this.props.isPassword) {
                this.setState({typeName:"password", hintClassName:"", value:""});
            } else {
                this.setState({hintClassName:"", value:""});
            }
        }
    },
    handleBlur: function() {
        if (this.state.value.trim() === "") {
            this.setState({typeName:"text", hintClassName:this.props.hintClassName, value:this.props.hint});
        }
    },
    handleChange: function(event) {
        this.setState({value: event.target.value});
    }
})