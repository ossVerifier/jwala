var Tabs = React.createClass({displayName:"Tabs",
    getInitialState: function() {
        themeName = "tabs-" + this.props.theme;
        var items = this.props.items;
        return {
            tabs: items,
            active: 0
        };
    },
    render: function() {
        return React.DOM.div(null,
                    React.DOM.ol({className:themeName},
                        TabsSwitcher({items:this.state.tabs, active:this.state.active,
                                       onTabClick:this.handleTabClick}),
                        TabsContent({theme:themeName, items:this.state.tabs, active:this.state.active})
                    )
               );
    },
    handleTabClick: function(index) {
        this.setState({active: index})
    }
});

var TabsSwitcher = React.createClass({displayName:"TabsSwitcher",
    render: function() {
        var active = this.props.active;
        var items = this.props.items.map(function(item, index) {
            return React.DOM.li({className:"" + (active === index ? "current" : "")},
                   React.DOM.a({href:"#", onClick:this.onClick.bind(this, index)},
                item.title
            ));
        }.bind(this));
        return React.DOM.div(null, items);
    },
    onClick: function(index) {
        this.props.onTabClick(index);
    }
});

var TabsContent = React.createClass({displayName:"TabsContent",
    render: function() {
        var theme = this.props.theme;
        var active = this.props.active;
        var items = this.props.items.map(function(item, index) {
            return React.DOM.div({className:theme + "-panel" + (active === index ? "tabs-panel-selected" : "")},
                                  item.content);
        });
        return React.DOM.div({className:theme + "-panel-selected"}, items);
    }
});