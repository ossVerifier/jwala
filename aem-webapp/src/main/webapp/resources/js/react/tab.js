/** @jsx React.DOM */

var Tabs = React.createClass({
    getInitialState: function() {
        var items = this.props.items;
        return {
            tabs: items,
            active: 0
        };
    },
    render: function() {
        return <div>
                    <ol id="toc">
                        <TabsSwitcher items={this.state.tabs} active={this.state.active} onTabClick={this.handleTabClick}/>
                        <TabsContent items={this.state.tabs} active={this.state.active}/>
                    </ol>
               </div>;
    },
    handleTabClick: function(index) {
        this.setState({active: index})
    }
});

var TabsSwitcher = React.createClass({
    render: function() {
        var active = this.props.active;
        var items = this.props.items.map(function(item, index) {
            return <li className={'' + (active === index ? 'current' : '')}><a href="#" onClick={this.onClick.bind(this, index)}>
                {item.title}
            </a></li>;
        }.bind(this));
        return <div>{items}</div>;
    },
    onClick: function(index) {
        this.props.onTabClick(index);
    }
});

var TabsContent = React.createClass({
    render: function() {
        var active = this.props.active;
        var items = this.props.items.map(function(item, index) {
            return <div className={'tabs-panel ' + (active === index ? 'tabs-panel_selected' : '')}>{item.content}</div>;
        });
        return <div>{items}</div>;
    }
});