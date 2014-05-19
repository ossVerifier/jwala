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
    handleBlur: function() {
        if (this.state.value === "") {
            this.setState({inputClassName:"input-on-blur " + this.props.className});
        }
    },
    handleChange: function(event) {
        var className;
        if (event.target.value === "") {
            className = "input-on-blur " + this.props.className;
        } else {
            className = "input-on-focus " + this.props.className;
        }
        this.setState({inputClassName: className, value: event.target.value});
    }
})