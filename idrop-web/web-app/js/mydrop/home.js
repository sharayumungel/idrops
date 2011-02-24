/**
 * Javascript for home page and data browser (refactor data browser?)
 * 
 * author: Mike Conway - DICE
 */

/**
 * Global var holds jquery ref to the dataTree
 */
var dataTree;

/**
 * Initialize the tree control for the first view by issuing an ajax directory
 * browser request for the root directory.
 * 
 * @return
 */
function retrieveBrowserFirstView() {
	var url = "/browse/ajaxDirectoryListingUnderParent";
	lcSendValueAndCallbackWithJsonAfterErrorCheck(url, "dir=/", "#dataTreeDiv",
			browserFirstViewRetrieved);
}

// get rid of
function useAjaxToRetrieveATreeNode(node, path) {
	var url = "/browse/loadTree";
	strPath = "";
	// make path into a parameter for 'dir'
	for (i = 0; i < path.length; i++) {
		if (path[i] == "/") {
			// skip
		} else {
			strPath += "/";
			strPath += path[i];
		}
	}

	parms = {
		"dir" : strPath
	};

	lcSendValueAndCallbackWithJsonAfterErrorCheck(url, parms, "#dataTreeDiv",
			function(data) {
				nodeTreeRetrievedViaAjax(node, data);
			});

}

// get rid of
function nodeTreeRetrievedViaAjax(targetNode, jsonData) {

	var directoryList = jsonData.directoryList;

	if (directoryList == null) {
		synchronizeDetailView(targetNode, path);
		return;
	}

	for (i = 0; i < directoryList.length; i++) {
		var entry = directoryList[i];
		var nodeData = {}
		if (entry.file == true) {
			nodeData = {

				"data" : entry.data.substring(entry.data.lastIndexOf("/") + 1)
			};
		} else {
			nodeData = {
				"state" : "closed",
				"data" : entry.data.substring(entry.data.lastIndexOf("/") + 1)
			};
		}

		$.jstree._reference(targetNode).create_node(targetNode, "last",
				nodeData, nodeLoadedCallback);
	}

	synchronizeDetailView(targetNode, path);
	$.jstree._reference(targetNode).toggle_node(targetNode, false, false);

}

/**
 * handy method to build the parms for an ajax request for the given path
 * 
 * @param n
 * @return
 */
// get rid of
function buildDataForNodeRequest(n) {
	var nodeData = {
		"dir" : "/"
	};
	return nodeData;
}

/**
 * Callback to initialize a browser tree for the first time, set to the root
 * node as indicated in the data
 * 
 * @param data
 *            ajax response from browse controller containing the JSON
 *            representation of the collections and files underneath the given
 *            path
 * @return
 */
function browserFirstViewRetrieved(data) {

	dataTree = $("#dataTreeDiv").jstree({
		"core" : {
			"initially_open" : [ "/" ]
		},
		"json_data" : {
			"data" : [ data ],
			"ajax" : {
				"url" : context + "/browse/ajaxDirectoryListingUnderParent",
				"data" : function(n) {
					return {
						dir : n.attr ? n.attr("id") : 0
					};
				}
			}
		},
		"types" : {
			"types" : {
				"file" : {
					"valid_children" : "none",
					"icon" : {
						"image" : context + "/images/file.png"
					}
				},
				"folder" : {
					"valid_children" : [ "default", "folder", "file" ],
					"icon" : {
						"image" : context + "/images/folder.png"
					}
				}
			}

		},

		"themes" : {
			"theme" : "default",
			"url" : context + "/css/style.css",
			"dots" : false,
			"icons" : true
		},
		"plugins" : [ "json_data", "types", "ui", "crmm", "themes" ]
	});

	$("#dataTreeDiv").bind("select_node.jstree", function(e, data) {
		// alert(data.rslt.obj); // this is the object that was clicked
		nodeSelected(e, data.rslt.obj);
	});

	/*
	 * dataTree.live("click", function(event, data) { nodeSelected(event, data)
	 * });
	 */

}

/**
 * Event callback when a tree node has finished loading.
 * 
 * @return
 */
function nodeLoadedCallback() {
}

/**
 * called when a tree node is selected. Toggle the node as appropriate, and if
 * necessary retrieve data from iRODS to create the children
 * 
 * @param event
 *            javascript event containing a reference to the selected node
 * @return
 */
function nodeSelected(event, data) {
	// given the path, put in the node data

	var id = data[0].id;
	lcSendValueAndCallbackHtmlAfterErrorCheck("/browse/fileInfo?absPath=" + id,
			"#infoDiv", "#infoDiv", null);

}

/**
 * Event callback to synchronize a detail view with the selected node as the
 * parent
 * 
 * @param node
 *            tree node that should be synchronized to
 * @param path
 *            path array of the given node
 * @return
 */
function synchronizeDetailView(node, path) {
	// alert("synch");
	// put build of data tree table view
}

/**
 * Determine if this node has been loaded (that the dir contents are retrieved
 * via ajax call)
 * 
 * @param node
 *            tree node in question
 * @return true if node is loaded already
 */
function isThisNodeLoaded(node) {

	return obj == -1 || !obj || obj.is(".jstree-open, .jstree-leaf")
			|| obj.children("ul").children("li").size() > 0;

}

function updateTags() {
	alert("updating tags");
}
