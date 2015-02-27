/** @jsx React.DOM */
var AdminTab = React.createClass({

	getInitialState: function() { 
		ServiceFactory.getAdminService().viewProperties(
				this.onPropertiesRx);

		return { toEncrypt:"",
				 encrypted:"",
				 encryptLabel:"",
				 encryptLabelOr:"",
				 encryptProp:"",
				 properties:""};
	},
	doEncrypt: function() {
		ServiceFactory.getAdminService().encryptServerSide(
				this.state.toEncrypt, 
				this.onEncryption);
	},
	doReload: function() {
		ServiceFactory.getAdminService().reloadProperties(
				this.onPropertiesRx);
	},
	onPropertiesRx: function(e) {
		var str = "";		
		$.each( e.applicationResponseContent, function(key, value) {
			str = str + (key+"="+value+"\n");
		});
		this.setState({
			properties: str
		});
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
                    <br />                    
                    <h3>Properties Management</h3>   
                    <p>
                    <label>Reload toc.properties and logging configuration.</label><br />  
                    <GenericButton label=">>> Reload >>>" callback={this.doReload} />                   
                    </p>
                    <p><textarea readonly="true" disabled="true" spellcheck='false' cols="100" rows="15" value={this.state.properties}></textarea></p>
               </div>
    }

	
});