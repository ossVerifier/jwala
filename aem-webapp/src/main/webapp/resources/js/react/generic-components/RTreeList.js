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
            selectedNodeKey: null
        }
    },
    render: function() {
        var self = this;
        var nodes = [];

        if (this.props.data.length > 0) {
            for (var i = 0; i < this.props.data.length; i++) {
                var key = Node.createKey("na", 0, i);
                nodes.push(React.createElement(Node, {key: key, // used by React
                                                      ref: key,
                                                      nodeKey: key, // used for node comparison
                                                      level: 0,
                                                      data: this.props.data[i],
                                                      treeMetaData: this.props.treeMetaData,
                                                      collapsedByDefault: false,
                                                      expandIcon: this.props.expandIcon,
                                                      collapseIcon: this.props.collapseIcon,
                                                      selectNodeCallback: this.props.selectNodeCallback,
                                                      theTree: this,
                                                      selectedNodeKey: this.state.selectedNodeKey}));
            }

            var ul = React.createElement("ul", {className: "tree-list-style"}, nodes);
            return React.createElement("div", {className: "tree-list-content"}, ul);
        }
        return React.createElement("div", {className: "tree-list-content"}, "The tree is empty...");
    },
    selectNode: function(name) {
        var self = this;
        if (this.props.treeMetaData !== null) {
            for (var i = 0; i < this.props.data.length; i++) {
            var level = 0;
                this.props.treeMetaData.forEach(function(metaDataItem){
                    if (self.props.data[i][metaDataItem["propKey"]] === name) {
                        var parent = (level === 0 ? "na" : metaDataItem[level - 1]);
                        var key = Node.createKey(parent, level, i);
                        self.setState({selectedNodeKey:key});
                        return;
                    }
                    level++;
                });
            }
        }
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

            if (this.props.data[metaData.entity] !== undefined) {
                this.props.data[metaData.entity].forEach(function(item) {
                    var key = Node.createKey(self.props.nodeKey, nextLevel, nodeIdx);
                    childNodes.push(React.createElement(Node, {key: key, // used by React
                                                               nodeKey: key, // used for node comparison
                                                               ref: key,
                                                               data: item,
                                                               treeMetaData: self.props.treeMetaData,
                                                               level: nextLevel,
                                                               collapsedByDefault: false,
                                                               expandIcon: self.props.expandIcon,
                                                               collapseIcon: self.props.collapseIcon,
                                                               selectNodeCallback: self.props.selectNodeCallback,
                                                               theTree: self.props.theTree,
                                                               selectedNodeKey: self.props.selectedNodeKey}));
                    nodeIdx++;
                });
            }
        }

        var selectableClassName = this.props.treeMetaData[level].selectable === true ? "selectable" : "";

        var selectedClassName = "";
        if (this.isSelected()) {
            selectedClassName = Node.HIGHLIGHT_CLASS_NAME;
            this.props.selectNodeCallback(this.props.data);
        }

        var nodeLabel = this.props.data[this.props.treeMetaData[level].propKey];

        if (childNodes.length > 0) {
            return React.createElement("li", {className: "li-style " + selectableClassName},
                       React.createElement("img", {ref: "expandCollapseIcon", src: (this.state.isCollapsed ? this.props.expandIcon : this.props.collapseIcon), onClick:this.onClickIconHandler, className: "expand-collapse-padding"}),
                       React.createElement("span", {ref: "nodeLabel", className: "tree-list-style " + selectedClassName, onClick: this.onClickNodeHandler.bind(this, this.props.treeMetaData[level].selectable, this.props.data)}, nodeLabel),
                       React.createElement("li", {className: "tree-list-style " + (this.state.isCollapsed ? "li-display-none" : "")},
                           React.createElement("ul", {className: "tree-list-style"}, childNodes)));
        }
        return React.createElement("li", {className: "tree-list-style li-style " + selectableClassName, onClick:this.onClickIconHandler},
                   React.createElement("span", {ref: "nodeLabel", className: selectedClassName, onClick: this.onClickNodeHandler.bind(this, this.props.treeMetaData[level].selectable, this.props.data)}, nodeLabel));
    },
    onClickNodeHandler: function(isSelectable, data) {
        if (isSelectable) {
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