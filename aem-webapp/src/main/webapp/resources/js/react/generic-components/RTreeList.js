/**
 * Displays hierarchical data in a single branch tree like list.
 *
 * Properties:
 *
 * 1. title - the tree list header title (will be deprecated in the future, this should be placed in the container)
 * 2. data -
 * 3. treeMetaData
 * 4. expandIcon
 * 5. collapseIcon
 * 6. selectNodeCallback
 *
 * Sample usage using jsx:
 *
 * <RTreeList title = "JVMs"
 *            data = {sampleTreeListData.applicationResponseContent}
 *            treeMetaData = {treeMetaData}
 *            expandIcon = "css/images/plus.png"
 *            collapseIcon = "css/images/minus.png"
 *            selectNodeCallback = {selectNodeCallback} />
 *
 * Where:
 *
 * 1. sampleTreeListData.applicationResponseContent = [{name: "Some Name", jvms: [{jvmName: "Some JVM Name"}]}];
 * 2. treeMetaData - describes the data in a hierarchical fashion e.g. [{propKey: "name"}, {entity: "jvms", propKey: "jvmName", selectable: true}];
 *
 * TODO: Write unit tests.
 */
var RTreeList = React.createClass({
    getInitialState: function() {
        return {
            selectedNodeKey: ""
        }
    },
    render: function() {
        var self = this;
        var nodes = [];

        for (var i = 0; i < this.props.data.length; i++) {
            nodes.push(React.createElement(Node, {key: Node.createKey("na", 0, i), // used by React
                                                  nodeKey: Node.createKey("na", 0, i), // used for node comparison
                                                  data: this.props.data[i],
                                                  treeMetaData: this.props.treeMetaData,
                                                  collapsedByDefault: false,
                                                  expandIcon: this.props.expandIcon,
                                                  collapseIcon: this.props.collapseIcon,
                                                  selectNodeCallback: this.props.selectNodeCallback,
                                                  theTree: this,
                                                  selectedNodeKey: this.state.selectedNodeKey}));
        }

        var ul = React.createElement("ul", {className: "list-style-none "}, nodes);
        return React.createElement("div", {className: "tree-list-content"}, ul);
    }
});

var Node = React.createClass({
    getInitialState: function() {
        return {
            isCollapsed: this.props.collapsedByDefault
        }
    },

    isSelected: function() {
        return this.props.nodeKey === this.props.selectedNodeKey;
    },

    render: function() {
        var self = this;
        var level = this.props.level === undefined ? 0 : this.props.level;
        var nextLevel = level + 1;

        var metaData = this.props.treeMetaData[level + 1]; // Get child meta data
        var childNodes = [];
        if (metaData !== undefined) {
            var nodeIdx = 0;
            this.props.data[metaData.entity].forEach(function(item) {
                childNodes.push(React.createElement(Node, {key: Node.createKey(self.props.nodeKey, nextLevel, nodeIdx), // used by React
                                                           nodeKey: Node.createKey(self.props.nodeKey, nextLevel, nodeIdx), // used for node comparison
                                                           data: item,
                                                           treeMetaData: self.props.treeMetaData, level:nextLevel,
                                                           collapsedByDefault: true,
                                                           selectNodeCallback: self.props.selectNodeCallback,
                                                           theTree: self.props.theTree,
                                                           selectedNodeKey: self.props.selectedNodeKey}));
                nodeIdx++;
            });
        }

        var selectableClassName = this.props.treeMetaData[level].selectable === true ? "selectable" : "";
        var selectedClassName = this.isSelected() ? Node.HIGHLIGHT_CLASS_NAME : "";
        var nodeLabel = this.props.data[this.props.treeMetaData[level].propKey];
        if (childNodes.length > 0) {
            return React.createElement("li", {className: "li-style li-width"},
                       React.createElement("img", {src: (this.state.isCollapsed ? this.props.expandIcon : this.props.collapseIcon), onClick:this.onClickIconHandler}),
                       " ",
                       React.createElement("span", {className: selectedClassName, onClick: this.onClickNodeHandler.bind(this, this.props.treeMetaData[level].selectable, this.props.data)}, nodeLabel),
                       React.createElement("li", {className: "list-style-none " + (this.state.isCollapsed ? "li-display-none" : "")},
                           React.createElement("ul", {className: "list-style-none"}, childNodes)));
        }
        return React.createElement("li", {className: "li-style li-width " + selectableClassName, onClick:this.onClickIconHandler},
                   React.createElement("span", {className: selectedClassName, onClick: this.onClickNodeHandler.bind(this, this.props.treeMetaData[level].selectable, this.props.data)}, nodeLabel));
    },
    onClickNodeHandler: function(isSelectable, data) {
        if (isSelectable) {
            this.props.selectNodeCallback(data);
            this.props.theTree.setState({selectedNodeKey: this.props.nodeKey});
        }
    },
    onClickIconHandler: function() {
        this.setState({isCollapsed:!this.state.isCollapsed});
    },
    statics: {
        PREFIX: "node",
        HIGHLIGHT_CLASS_NAME: "ui-state-highlight",
        createKey: function(parentNodeKey, level, nodeIdx) {
            return Node.PREFIX + "-" + parentNodeKey + "-" + level + "-" + nodeIdx;
        }
    }
});