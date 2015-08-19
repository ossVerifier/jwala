/**
 * Displays hierarchical data in a single branch tree like list.
 *
 * Properties:
 *
 * 1. data
 * 2. treeMetaData = describes (in JSON) how data will be presented in a tree structure.
 *
 *    Example:
 *
 *     {propKey: "name",
 *      children:[{entity: "webServers",
 *                 propKey: "name",
 *                 selectable: true},
 *                {entity: "jvms",
 *                 propKey: "jvmName",
 *                 selectable: true, children:[{entity: "webApps",
 *                                              propKey: "name",
 *                                              selectable: true}]
 *                }]
 *     }
 *
 * 3. expandIcon
 * 4. collapseIcon
 * 5. selectNodeCallback - Callback that is called when a node is clicked.
 *
 * TODO: Write unit tests.
 */
var RTreeList = React.createClass({
    getInitialState: function() {
        return {selectedNodeKey: null};
    },
    render: function() {
        var nodes = this.createTreeNodes(this.props.data, this.props.treeMetaData, 0, "");
        return React.createElement("div", {className:"tree-list-content ui-widget-content"}, React.createElement("ul", {className: "root-node-ul"}, nodes));
    },
    onSelectNode: function(data) {
        this.props.selectNodeCallback(data);
    },
    createTreeNodes: function(data, meta, level, parent) {
        var self = this;
        var nodes = [];

        for (var i = 0; i < data.length; i++) {
            var childNodes = [];
            if (meta.children !== undefined) {
                meta.children.forEach(function(child){
                    if (data[i][child.entity] !== undefined && data[i][child.entity].length > 0) {
                        childNodes.push(self.createTreeNodes(data[i][child.entity], child, level + 1, data[i][meta.propKey]));
                    }
                });
            }

            var key = parent + data[i][meta.propKey] + level;
            nodes.push(React.createElement(Node, {label:data[i][meta.propKey],
                                                  collapsedByDefault:false,
                                                  expandIcon: this.props.expandIcon,
                                                  collapseIcon: this.props.collapseIcon,
                                                  selectable: meta.selectable,
                                                  data: data[i],
                                                  theTree: this,
                                                  key: key /* React use */,
                                                  nodeKey: key,
                                                  selectedNodeKey: this.state.selectedNodeKey,
                                                  parent: parent,
                                                  entity: meta.entity}, childNodes));
        }
        return nodes;
    }
});

/**
 * A tree node.
 */
var Node = React.createClass({
    getInitialState: function() {
        return {
            isCollapsed: this.props.collapsedByDefault,
            mouseOver: false
        }
    },
    isSelected: function() {
        return this.props.nodeKey === this.props.selectedNodeKey;
    },
    render: function() {
        var children;
        var expandCollapseIcon = null;
        var liClassName = this.props.children.length === 0 ? "tree-list-style" : "";

        var spanClassName = "";
        if (this.isSelected()) {
            spanClassName = spanClassName + "span-style " + Node.HIGHLIGHT_CLASS_NAME;
        } else if (this.state.mouseOver && this.props.selectable) {
            spanClassName = spanClassName + "span-style " + Node.FOCUS_CLASS_NAME;
        }

        if (this.props.children.length > 0) {
            expandCollapseIcon = React.createElement("img", {ref: "expandCollapseIcon",
                                                             src: (this.state.isCollapsed ? this.props.expandIcon : this.props.collapseIcon),
                                                             onClick:this.onClickIconHandler, className: "expand-collapse-padding"});
            if (!this.state.isCollapsed) {
                children = React.createElement("ul", {className: "tree-list-style"}, this.props.children);
            }
        }
        return React.createElement("li", {className: liClassName}, expandCollapseIcon,
                                          React.createElement("span", {onClick: this.onClickNodeHandler,
                                                                       onMouseOver: this.onMouseOver,
                                                                       onMouseOut: this.onMouseOut,
                                                                       className: spanClassName}, this.props.label), children);
    },
    onMouseOver: function(event) {
        event.preventDefault();
        this.setState({mouseOver: true});
    },
    onMouseOut: function(event) {
        event.preventDefault();
        this.setState({mouseOver: false});
    },
    onClickIconHandler: function() {
        this.setState({isCollapsed:!this.state.isCollapsed});
    },
    onClickNodeHandler: function() {
        if (this.props.selectable === true) {
            this.props.theTree.setState({selectedNodeKey: this.props.nodeKey});

            // Add RTreeList specific data
            this.props.data["rtreeListMetaData"] = {parent: this.props.parent, entity: this.props.entity};

            this.props.theTree.onSelectNode(this.props.data);
        }
    },
    statics: {
            HIGHLIGHT_CLASS_NAME: "ui-state-active",
            FOCUS_CLASS_NAME: "ui-state-focus"
    }
});