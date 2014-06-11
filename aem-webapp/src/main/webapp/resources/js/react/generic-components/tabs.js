var Tabs = React.createClass({displayName:"Tabs",
    getInitialState: function() {
        this.hashRegex = /#\/([^/]*)\/(([^/]*)\/)?(([^/]*)\/)?/; // zero-three levels deep, prefix with / suffixes with /, indexes 1 3 5, null zero match

        var activeTabIndex = this.lookupIndexFromHash(window.location.hash, this.props.depth /*nesting depth*/) || 0;
        var items = this.props.items;
        return {
            titlePrefix: "Tomcat Operations Center - ",
            tabs: items,
            active: activeTabIndex,
            themeName: "tabs-" + this.props.theme
        };
    },
    handleBack: function() {
   	    if(this.isMounted()) {
          var newHash = location.hash;
          var newTabIndex = this.lookupIndexFromHash(newHash, this.props.depth /*nesting depth*/);
          if(newTabIndex !== undefined && newTabIndex != this.state.active) { 
  	  	    this.handleTabClick(newTabIndex);

            // unwind this item off of the history
            if(history.back) { 
              history.back();
            }  	  	    
          }
        } else { 
          /* higher level tab managed the state change */
          $(window).off('hashchange', this.handleBack.bind(this));
        }
    },
    componentWillUnmount: function() {
       $(window).off('hashchange', this.handleBack.bind(this));
    },
    componentDidMount: function() { 
       $(window).on('hashchange', this.handleBack.bind(this));
       document.title = this.state.titlePrefix + this.props.items[this.state.active].title;
    },
    render: function() {
        return React.DOM.div(null,
                    React.DOM.ol({className:this.state.themeName},
                        TabsSwitcher({items:this.state.tabs, active:this.state.active,
                                      onTabClick:this.handleTabClick})
                    ),
                    TabsContent({theme:this.state.themeName,
                                 items:this.state.tabs,
                                 active:this.state.active})
               );
    },
    handleTabClick: function(index) {
        this.setState({active: index})
        var newhash = this.mergeIndexIntoHash(index, window.location.hash, this.props.depth);
        var title = this.state.titlePrefix + this.props.items[index].title;
        if(history.pushState) {
        	history.pushState(null,title, newhash);
        } else {
        	window.location.hash = newhash;
        }
      	document.title = title;
        return true;
    },
	/* Merge into the hash in the URL by mapping this tab into existing fragments */
    mergeIndexIntoHash: function(index, currentHash, depth) {
    	var matches = this.hashRegex.exec(currentHash);
    	var newList = "#/";
    	if(matches == null) {
    		for(var i = 0; i < depth; i=i+1) {
    			newList = newList + "/";
    		}
    		newList = newList + this.props.items[index].title + "/"
    	} else {
    		for(var j = 0; j <= depth; j=j+1) {
    			if(j == depth) {
    				newList = newList + this.props.items[index].title + "/";
    			} else if(matches[1+j*2] == null) {
    				newList = newList + "/"
    			} else {
    				newList = newList + matches[1+j*2] + "/"
    			}
    		}
    	}

  		if(matches != null) {
  			return currentHash.replace(this.hashRegex,newList);
  		} else return newList;

    },
		/* Map hashtag fragments into an index for this tab at this depth. */
    lookupIndexFromHash: function(currentHash, depth) {
			if(!this.hashRegex.test(currentHash)) {
				return 0;
			}
			/* 1+depth*2 is a calculation that looks at the nesting level of the tabs
			   and converts it to an group index for the regular expression
			   allowing us to extract the window location hash component corresponding
			   to this particular tab component.
			*/
    	var localHash = this.hashRegex.exec(currentHash)[1+depth*2];
    	var localIndex = undefined;
    	this.props.items.every(function(itemName, itemIndex, harray) {
    		 if(itemName.title == localHash) {
    		 	localIndex = itemIndex; return false;
    		 }
    		 return true; });
   		return localIndex;
    }

});

var TabsSwitcher = React.createClass({displayName:"TabsSwitcher",
    render: function() {
        var active = this.props.active;
        var items = this.props.items.map(function(item, index) {
            return React.DOM.li({key:"li"+index, className:"" + (active === index ? "current" : "")},
                   React.DOM.a({key:"a"+index, onClick:this.onClick.bind(this, index)},
                item.title
            ));
        }.bind(this));
        return React.DOM.div(null, items);
    },
    onClick: function(index) {
        this.props.onTabClick(index);
    }
});

var TabsContent = React.createClass({
    displayName: "TabsContent",
    render: function() {
        var theme = this.props.theme;
        var active = this.props.active;

        var items = this.props.items.map(function(item, index) {
            if (index === active) {
                return React.DOM.span({key:"tc.span"+index}, item.content);
            }
        });

        return React.DOM.div({
          key:"tc.t"+this.props.active,
          className:theme + "-panel-selected"}, items);
    }
});