/** @jsx React.DOM */
var AdminTab = React.createClass({

	getInitialState: function() { 
		return { toEncrypt:"",
				 encrypted:"",
				 encryptLabel:"",
				 encryptLabelOr:"",
				 encryptProp:""};
	},
	doEncrypt: function() {
		ServiceFactory.getAdminService().encryptServerSide(
				this.state.toEncrypt, 
				this.onEncryption);
	},
	onChange: function(event) {
		this.setState({
			toEncrypt:event.target.value,
			encrypted:"",
			encryptProp:"",
			encryptLabel:"",
			encryptLabelOr:""});		
	},
	onEncryption: function(e) {
		this.setState({
			encrypted:e.applicationResponseContent,
			encryptProp:"${enc:"+e.applicationResponseContent+"}",
			encryptLabel:"Encryption Succeeded",
			encryptLabelOr:" or ",
			toEncrypt: ""});
	},
    render: function() {
        return <div>
                    <h3>Encryption Tool</h3>
                    <p>
                    <label>Enter data to be secured:</label> <input className="toEncrypt" length="100" value={this.state.toEncrypt} onChange={this.onChange} />
                    		<br/>                    		
                    		<GenericButton label=">>> Encrypt >>>" callback={this.doEncrypt}/>
                    </p>
                    <p>
                    		<h4>{this.state.encryptLabel}</h4> 
                    </p>
                    <p><span>{this.state.encrypted}</span> </p>
                    <p><label>{this.state.encryptLabelOr}</label></p>
                    <p><span>{this.state.encryptProp}</span></p>
               </div>
    }

	
});