<div id="topToolbar"
	style="height: 100%; overflow: visible; margin-left: auto; margin-right: auto;">

	<ul id="topToolbarMenu" class="sf-menu">
		
		<li id="menuFileDetails" class="detailsToolbarMenuItem"><a
			href="#file"><g:message code="text.file" /></a>
			<ul>
			<li id="menuRefresh"><a href="#refresh" onclick="refreshTree()"><g:message
					code="text.refresh" /></a></li>
				<li id="menuNewFolderDetails"><a href="#newFolderDetails"
					onclick="newFolderViaBrowseDetailsToolbar()"><g:message
							code="text.new.folder" /></a></li>
				<!--  <li id="menuRenameDetails"><a href="#renameDetails"
					onclick="renameViaBrowseDetailsToolbar()"><g:message
							code="text.rename" /></a></li>
				<li id="menuDeleteDetails"><a href="#deleteDetails"
					onclick="deleteViaBrowseDetailsToolbar()"><g:message
							code="default.button.delete.label" /></a></li>-->
				
			</ul></li>
			<li id="menuFile" class="toolbarMenuItem"><a href="#file"><g:message
					code="text.file" /></a>
			<ul>
			<li id="menuRefresh"><a href="#refresh" onclick="refreshTree()"><g:message
					code="text.refresh" /></a></li>
				<li id="menuNewFolder"><a href="#newFolder"
					onclick="newFolderViaToolbar()"><g:message
							code="text.new.folder" /></a></li>
				<li id="menuRename"><a href="#rename"
					onclick="renameViaToolbar()"><g:message code="text.rename" /></a></li>
				<li id="menuDelete"><a href="#delete"
					onclick="deleteViaToolbar()"><g:message
							code="default.button.delete.label" /></a></li>
			</ul></li>
			
		<li id="menuView"><a href="#view"><g:message code="text.view" /></a>
			<ul>
				<li id="menuBrowseView"><a href="#browseView"
					onclick="browseView()"><g:message
							code="text.browse" /></a></li>
				<li id="menuInfoView"><a href="#infoView" onclick="infoView()"> <g:message code="text.info" /></a></li>
				<li id="menuSharingView"><a href="#sharingView"
					onclick="sharingView()"><g:message
							code="text.sharing" /></a></li>
				<li id="menuMetadataView"><a href="#metadataView"
					onclick="metadataView()"><g:message
							code="text.metadata" /></a></li>
				<li id="menuGalleryView"><a href="#galleryView"
					onclick="galleryView()"><g:message
							code="text.gallery" /></a></li>
				<li id="menuAuditView"><a href="#auditView"
					onclick="auditView()"><g:message
							code="text.audit" /></a></li>
				<g:if test="${grailsApplication.config.idrop.config.use.tickets==true}">
				<li id="menuTicketView"><a href="#ticketView"
					onclick="ticketView()"><g:message
							code="text.tickets" /></a></li>
				</g:if>
			</ul></li>

		<!--  details toolbar -->

		<li id="menuUploadDownloadDetails" class="detailsToolbarMenuItem"><a
			href="#uploadDownloadDetails"><g:message
					code="text.upload.and.download" /></a>
			<ul>
				<li id="menuUploadDetails"><a href="#uploadDetails"
					onclick="showBrowseDetailsUploadDialog()"><g:message
							code="text.upload" /></a></li>
			
					<li id="menuBulkUploadDetails" class="idropLiteBulkUpload"><a href="#bulkuploadDetails"
						onclick="showBrowseDetailsIdropLite()"><g:message
								code="text.bulk.upload" /></a></li>

					<li id="menuAddToCartDetails"><a href="#addToCartDetails"
						onclick="addToCartViaBrowseDetailsToolbar()"><g:message
								code="text.add.to.cart" /></a></li>

			
			</ul></li>
			
			<li id="menuToolsDetails" class="detailsToolbarMenuItem"><a href="#menuToolsDetails"><g:message code="text.tools"/></a>
				<ul>
					<li id="menuToolsDetailsMakePublicLink"><a href="#makePublicLinkDetails" onclick="makePublicLinkAtPath()"><g:message code="text.create.public.link" /></a></li>
				</ul>
			</li>
				
			
			<li id="menuBulkActionDetails" class="detailsToolbarMenuItem"><a href="#applyActionToAllDetails"><g:message code="text.apply.to.all"/></a>
				<ul>

					<li id="menuAddToCartDetails"><a href="#addAllToCartDetails" onclick="addSelectedToCart()"><g:message code="text.add.all.to.cart" /></a></li>
					<li id="menuDeleteDetails"><a href="#deleteAllDetails" onclick="deleteSelected()"><g:message code="text.delete.all" /></a></li>
				</ul>
			</li>
			
			
				
			
	<!--  info toolbar -->

	<li id="menuTools" class="toolbarMenuItem"><a href="#menuToolsD"><g:message code="text.tools"/></a>
				<ul>
					<li id="menuToolsMakePublicLink"><a href="#makePublicLink" onclick="makePublicLinkAtPath()"><g:message code="text.create.public.link" /></a></li>
				</ul>
			</li>
		
		<li id="menuUploadDownload" class="toolbarMenuItem"><a
			href="#uploadDownload"><g:message code="text.upload.and.download" /></a>
			<ul>
				<li id="menuUpload"><a href="#upload"
					onclick="showUploadDialogFromInfoToolbar()"><g:message
							code="text.upload" /></a></li>
				<li id="menuDownload"><a href="#download"
					onclick="downloadAction()"><g:message code="text.download" /></a></li>
					<li id="menuBulkUpload" class="idropLiteBulkUpload"><a href="#bulkupload"
						onclick="showIdropLite()"><g:message code="text.bulk.upload" /></a></li>

					<li id="menuAddToCart"><a href="#addToCart"
						onclick="addToCartViaToolbar()"><g:message
								code="text.add.to.cart" /></a></li>
		

			</ul></li>
		</ul>
</div>


<script type="text/javascript">
	var showLite = false;

	$(function() {
		$(".toolbarMenuItem").hide();
		$(".detailsToolbarMenuItem").hide();
		$("ul.sf-menu").superfish();
	});

	function setDefaultView(view) {
		if (view == null) {
			return false;
		}

		browseOptionVal = view;

		var state = {};

		state["browseOptionVal"] = browseOptionVal;
		$.bbq.pushState(state);

	}

	/**
	 * audit view selected
	 */
	function auditView() {
		setDefaultView("audit");
		showAuditView(selectedPath);

	}

	/**
	 * browse view selected
	 */
	function browseView() {
		setDefaultView("browse");
		showBrowseView(selectedPath);

	}

	/**
	 * Show the info view
	 */
	function infoView() {
		setDefaultView("info");
		showInfoView(selectedPath);
	}

	/**
	 * Show the sharing (ACL) view
	 */
	function sharingView() {
		setDefaultView("sharing");
		showSharingView(selectedPath);
	}

	/**
	 * Show the metadata (AVU) view
	 */
	function metadataView() {
		setDefaultView("metadata");
		showMetadataView(selectedPath);
	}

	/**
	 * Show the ticket view
	 */
	function ticketView() {
		setDefaultView("ticket");
		showTicketView(selectedPath);
	}

	/**
	 * Show the gallery (photo) view
	 */
	function galleryView() {
		setDefaultView("gallery");
		showGalleryView(selectedPath);
	}

	// browse details toolbar

	/**
	 * Start a bulk action to add selected files to the shopping cart
	 */
	function addSelectedToCart() {
		answer = confirm("Add the selected files to the cart?"); //FIXME: i18n
		if (!answer) {
			return false;
		}

		addToCartBulkAction();
	}

	/**
	 * Start a bulk action to delete the selected files from the shopping cart
	 */
	function deleteSelected() {
		answer = confirm("Delete the selected files?"); //FIXME: i18n
		if (!answer) {
			return false;
		}
		deleteFilesBulkAction();
	}

	function sharingSelectedFromBrowseDetailsToolbar() {
		showSharingView(selectedPath);
	}

	// browse toolbar scripts

	function showInTreeClickedFromToolbar() {
		var path = $("#infoAbsPath").val();
		$(tabs).tabs('select', 0); // switch to home tab
		splitPathAndPerformOperationAtGivenTreePath(path, null, null, function(
				path, dataTree, currentNode) {

			$.jstree._reference(dataTree).open_node(currentNode);
			$.jstree._reference(dataTree).select_node(currentNode, true);
			// updateBrowseDetailsForPathBasedOnCurrentModel(data);

		});
	}

	function downloadAction() {
		var path = $("#infoAbsPath").val();
		downloadViaToolbar(path);
	}

	function showBulkShareDialogFromToolbar() {
		var path = $("#infoAbsPath").val();
		showBulkShareDialog(path);
	}

	function showIdropLiteFromToolbar() {
		var path = $("#infoAbsPath").val();
		showBulkShareDialog(path);
	}

	/**
	 * Show the dialog to allow upload of data using the abs path in the info pane
	 */
	function showUploadDialogFromInfoToolbar() {
		var uploadPath = $("#infoAbsPath").val();
		if (uploadPath == null) {
			showErrorMessage("No path was found to upload, application error occurred");
			return;
		}

		showUploadDialogUsingPath(uploadPath);

	}

	function sharingSelectedFromToolbar() {
		var path = $("#infoAbsPath").val();
		showSharingView(path);
	}

	/*
	* Cause a dialog to appear that has a link for a public path for the current path
	*/
	function makePublicLinkAtPath() {
		$("#browseDialogArea").html();
		var path = selectedPath;
		if (selectedPath == null) {
			return false;
		}

		// show the public link dialog
		var url = "/browse/preparePublicLinkDialog";
		var params = {
			absPath : path
		}

		lcSendValueWithParamsAndPlugHtmlInDiv(url, params, "", function(data) {
			fillInPublicLinkDialog(data);
		});
		
	}

	/*
	*Given the contents of the 'create public link' dialog, 
	*/
	function fillInPublicLinkDialog(data) {
		$("#browseDialogArea").html(data);
		$("#browseDialogArea").show("slow");
		//$("#browseDialogArea").dialog({width:500, modal:true});
	}
	
</script>