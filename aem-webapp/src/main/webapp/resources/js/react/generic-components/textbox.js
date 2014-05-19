/** @jsx React.DOM */
var TextBox = React.createClass({
    getInitialState: function() {
        return {
            theType: this.props.isPassword ? "password" : "text",
            hintClassName: this.props.hintClassName,
            inputClassName: "input-on-blur " + this.props.className,
            value: ""
        }
    },
    render: function() {
        if (this.props.hint === undefined) {
            return <div><input id={this.props.id}
                               className={this.props.className}
                               type={this.state.theType}/></div>
        }
        return  <div>
                    <label className={this.props.hintClassName}
                           htmlFor={this.props.id}>{this.props.hint}</label>
                    <input id={this.props.id}
                           name={this.props.name}
                           className={this.state.inputClassName}
                           type={this.state.theType}
                           value={this.state.value}
                           onFocus={this.handleFocus}
                           onBlur={this.handleBlur}
                           onChange={this.handleChange}/>
                </div>
    },
    handleFocus: function() {
        this.setState({inputClassName:"input-on-focus " + this.props.className});
    },
    handleBlur: function() {
        if (this.state.value.trim() === "") {
            this.setState({inputClassName:"input-on-blur " + this.props.className});
        }
    },
    handleChange: function(event) {
        this.setState({value: event.target.value});
    }
})