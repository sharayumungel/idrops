package org.irods.mydrop.controller


import grails.converters.JSON

import org.irods.jargon.core.connection.IRODSAccount
import org.irods.jargon.core.exception.JargonException
import org.irods.jargon.core.protovalues.FilePermissionEnum
import org.irods.jargon.core.pub.CollectionAO
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO
import org.irods.jargon.core.pub.DataObjectAO
import org.irods.jargon.core.pub.IRODSAccessObjectFactory
import org.irods.jargon.core.pub.UserAO
import org.irods.jargon.core.pub.domain.DataObject
import org.irods.jargon.core.utils.LocalFileUtils
import org.irods.jargon.core.utils.MiscIRODSUtils

class SharingController {

	IRODSAccessObjectFactory irodsAccessObjectFactory
	IRODSAccount irodsAccount

	/**
	 * Interceptor grabs IRODSAccount from the SecurityContextHolder
	 */
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if(!session["SPRING_SECURITY_CONTEXT"]) {
			redirect(controller:"login", action:"login")
			return false
		}
		irodsAccount = session["SPRING_SECURITY_CONTEXT"]
	}


	def afterInterceptor = {
		log.debug("closing the session")
		irodsAccessObjectFactory.closeSession()
	}

	/**
	 * Load the acl details area, this will show the main form, and subsequently, the table will be loaded via AJAX
	 */
	def showAclDetails = {

		def absPath = params['absPath']
		if (absPath == null) {
			log.error "no absPath in request for showAclDetails()"
			def message = message(code:"error.no.path.provided")
			response.sendError(500,message)
		}

		log.info("showAclDetails for absPath: ${absPath}")

		try {
			CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount)
			def retObj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(absPath)
			def isDataObject = retObj instanceof DataObject
			boolean getThumbnail = false

			if (isDataObject) {
				String extension = LocalFileUtils.getFileExtension(retObj.dataName).toUpperCase()
				log.info("extension is:${extension}")

				if (extension == ".JPG" || extension == ".GIF" || extension == ".PNG" || extension == ".TIFF" ||   extension == ".TIF") {
					getThumbnail = true
				}
			}

			render(view:"aclDetails",model:[retObj:retObj, isDataObject:isDataObject, absPath:absPath, getThumbnail:getThumbnail])
		} catch (org.irods.jargon.core.exception.FileNotFoundException fnf) {
			log.info("file not found looking for data, show stand-in page", fnf)
			render(view:"/browse/noInfo")
		}
	}

	def renderAclDetailsTable = {

		def absPath = params['absPath']

		if (!absPath) {
			log.error "no absPath in request for renderAclDetailsTable()"
			def message = message(code:"error.no.path.provided")
			response.sendError(500,message)
		}

		log.info("renderAclDetailsTable for absPath: ${absPath}")
		def acls

		try {

			CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount)

			def retObj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(absPath)

			def isDataObject = retObj instanceof DataObject

			if (isDataObject) {
				log.debug("retrieving ACLs for a data object")
				DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount)
				acls = dataObjectAO.listPermissionsForDataObject(retObj.collectionName + "/" + retObj.dataName)
			} else {
				log.debug("retrieveing ACLs for collection")
				CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount)
				acls = collectionAO.listPermissionsForCollection(retObj.collectionName)
			}
		} catch (Exception je){
			log.error("exception getting acl data ${je}", je)
			response.sendError(500,je.getMessage())
		}

		def homeZone = MiscIRODSUtils.getZoneInPath(absPath)

		render(view:"aclTable", model:[acls:acls, homeZone:homeZone])
	}

	/**
	 * Display an Acl dialog for an add or edit
	 */
	def prepareAclDialog = {
		log.info "prepareAclDialog"
		log.info "params: ${params}"

		// if a user is provided, this will be an edit, otherwise, it's a create
		def userName = params['userName']
		def absPath = params['absPath']
		def isCreate = params['create']

		if (!absPath) {
			log.error "no absPath in request for prepareAclDialog()"
			def message = message(code:"error.no.path.provided")
			response.sendError(500,message)
			return
		}

		render(view:"aclDialog", model:[absPath:absPath, userName:userName, userPermissionEnum:FilePermissionEnum.listAllValues(), isCreate:isCreate])

	}

	def processAddAclDialog = {
		log.info("processAddAclDialog")
		log.info(params)
		def absPath = params['absPath']
		def acl = params['acl']
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount)
		def retObj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(absPath)
		def isDataObject = retObj instanceof DataObject
		log.info("adding ACLs for a data object")

		def DataObjectAO dataObjectAO
		def CollectionAO collectionAO

		if (isDataObject) {
			dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount)
		} else {
			collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount)
		}

		if (!absPath) {
			log.error "no path provided"
			def errorMessage = message(code:"error.no.path.provided")
			response.sendError(500,errorMessage)
			return
		}

		if (!acl) {
			log.error "no acl provided"
			def errorMessage = message(code:"error.no.acl.provided")
			response.sendError(500,errorMessage)
			return
		}

		def selectedUsers = params['selectuser']

		// if nothing selected, just jump out and return a message
		if (!selectedUsers) {
			log.info("no users to add")
			def errorMessage = message(code:"error.nothing.selected")
			response.sendError(500,errorMessage)
			return
		}

		log.info("users to add: ${selectedUsers}")

		// based on the type of file at absPath, get the appropriate access object
		if (isDataObject) {
			dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount)
		} else {
			collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount)
		}

		if (selectedUsers instanceof Object[]) {
			log.debug "is array"
			selectedUsers.each{
				log.info "selecteduser: ${it}"

				def theUserName = MiscIRODSUtils.getUserInUserName(it)
				def theZone = MiscIRODSUtils.getZoneInUserName(it)

				if (isDataObject) {
					log.info "add user to data object"
					try {
						updateACLValueForDataObject(theZone,absPath, acl, theUserName, dataObjectAO)
					} catch (Exception e) {
						log.error("Exception during acl processing", e)
						response.sendError(500,e.message)
						return
					}

				} else {
					log.info("add user to collection")
					try {
						updateACLValueForCollection(theZone,absPath, acl, theUserName, collectionAO)
					} catch (Exception e) {
						log.error("Exception during acl processing", e)
						response.sendError(500,e.message)
						return
					}
				}
			}

		} else {
			log.debug "not array"
			log.info "adding: ${selectedUsers}"
			def theUserName = MiscIRODSUtils.getUserInUserName(selectedUsers)
			def theZone = MiscIRODSUtils.getZoneInUserName(selectedUsers)
			if (isDataObject) {
				log.info "add user to data object"
				try {
					updateACLValueForDataObject(theZone,absPath, acl, theUserName, dataObjectAO)
				} catch (Exception e) {
					log.error("Exception during acl processing", e)
					response.sendError(500,e.message)
					return
				}

			} else {
				log.info("add user to collection")
				try {
					updateACLValueForCollection(theZone,absPath, acl, theUserName, collectionAO)
				} catch (Exception e) {
					log.error("Exception during acl processing", e)
					response.sendError(500,e.message)
					return
				}
			}
		}

		render "OK"
	}

	/**
	 * FIXME: refactor to service object
	 * @param zone
	 * @param absPath
	 * @param aclString
	 * @param userName
	 * @param dataObjectAO
	 * @return
	 */
	private updateACLValueForDataObject(String zone, String absPath, String aclString, String userName, DataObjectAO dataObjectAO) {
		if (aclString == "READ") {
			dataObjectAO.setAccessPermissionRead(zone,absPath, userName)
		} else if (aclString == "WRITE") {
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(), absPath, userName)
		} else if (aclString == "OWN") {
			dataObjectAO.setAccessPermissionOwn(irodsAccount.getZone(), absPath, userName)
		} else {
			log.error "invalid acl ${acl}"
			throw new JargonException("invalid ACL value:" + aclString)
		}

	}

	/**
	 * FIXME: refactor to service object
	 * @param zone
	 * @param absPath
	 * @param aclString
	 * @param userName
	 * @param collectionAO
	 * @return
	 */
	private updateACLValueForCollection(String zone, String absPath, String aclString, String userName, CollectionAO collectionAO) {
		if (aclString == "READ") {
			collectionAO.setAccessPermissionRead(zone,absPath, userName, true)
		} else if (aclString == "WRITE") {
			collectionAO.setAccessPermissionWrite(zone, absPath, userName, true)
		} else if (aclString == "OWN") {
			collectionAO.setAccessPermissionOwn(zone, absPath, userName, true)
		} else {
			log.error "invalid acl ${acl}"
			throw new JargonException("invalid ACL value:" + aclString)
		}

	}

	/**
	 * Add an ACL via the ACL dialog, then, in response, reload the ACL table
	 * TODO: may not be returning errors in a way that jquery editable can interpret
	 */
	def addAcl = {  AclCommand cmd ->
		log.info "addAcl"
		log.info "params: ${params}"
		log.info "cmd:${cmd}"

		def responseData = [:]
		def jsonData = [:]

		if (cmd.hasErrors()) {
			log.info "errors occured build error messages"
			def errorMessage = message(code:"error.data.error")
			responseData['errorMessage'] = errorMessage
			def errors = []
			def i = 0
			cmd.errors.allErrors.each() {
				log.info "error identified in validation: ${it}"
				errors.add(message(error:it))
			}
			responseData['errors'] = errors
			jsonData['response'] = responseData
			render jsonData as JSON
			return
		}

		// parameter vaues have been validated, proceed with updates

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount)
		def retObj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(cmd.absPath)
		def isDataObject = retObj instanceof DataObject
		log.info("adding ACLs for a data object")

		def theUserName = MiscIRODSUtils.getUserInUserName(cmd.userName)
		def theZone = MiscIRODSUtils.getZoneInUserName(cmd.userName)


		if (isDataObject) {
			DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount)

			if (cmd.acl == "READ") {
				dataObjectAO.setAccessPermissionRead(theZone, cmd.absPath, theUserName)
			} else if (cmd.acl == "WRITE") {
				dataObjectAO.setAccessPermissionWrite(theZone, cmd.absPath, theUserName)
			} else if (cmd.acl == "OWN") {
				dataObjectAO.setAccessPermissionOwn(theZone, cmd.absPath, theUserName)
			} else {
				log.error "invalid acl ${cmd.acl}"
				def errorMessage = message(code:"error.invalid.acl", args[cmd.acl])
				response.sendError(500,errorMessage)
				return
			}

		} else {

			log.info("setting ACLs for collection")
			CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount)

			if (cmd.acl == "READ") {
				collectionAO.setAccessPermissionRead(theZone, cmd.absPath,theUserName, true)
			} else if (cmd.acl == "WRITE") {
				collectionAO.setAccessPermissionWrite(theZone, cmd.absPath, theUserName, true)
			} else if (cmd.acl == "OWN") {
				collectionAO.setAccessPermissionOwn(theZone, cmd.absPath,theUserName, true)
			} else {
				log.error "invalid acl ${cmd.acl}"
				def errorMessage = message(code:"error.invalid.acl", args[cmd.acl])
				response.sendError(500,errorMessage)
				return
			}
		}

		log.info("acl set successfully")
		responseData['message'] = message(code:"message.update.successful")
		jsonData['response'] = responseData
		render jsonData as JSON

	}

	/**
	 * List the users in iRODS based on user name.  A 'term' parameter may be supplied, in which case, a LIKE% query will be used to find
	 * matching user names.  This method returns JSON as expected for the JQuery UI autocomplete text box
	 */
	def listUsersForAutocomplete = {

		log.info("listUsersForAutocomplete")
		def term = params['term']
		if (!term) {
			term = ""
		}
		log.info("term:${term}")

		UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount)

		def userList = userAO.findUserNameLike(term)
		def jsonBuff = []


		userList.each {
			jsonBuff.add(
					["label": it]
					)
		}

		render jsonBuff as JSON

	}

	def deleteAcl = {
		log.info("deleteAcl")
		log.info(params)
		def absPath = params['absPath']
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount)
		def retObj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(absPath)
		def isDataObject = retObj instanceof DataObject
		log.info("adding ACLs for a data object")

		def DataObjectAO dataObjectAO
		def CollectionAO collectionAO

		if (isDataObject) {
			dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount)
		} else {
			collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount)
		}

		if (!absPath) {
			log.error "no path provided"
			def errorMessage = message(code:"error.no.path.provided")
			response.sendError(500,errorMessage)
			return
		}

		def aclsToDelete = params['selectedAcl']

		// if nothing selected, just jump out and return a message
		if (!aclsToDelete) {
			log.info("no acls to delete")
			def errorMessage = message(code:"error.nothing.selected")
			response.sendError(500,errorMessage)
			return
		}

		log.info("aclsToDelete: ${aclsToDelete}")

		if (aclsToDelete instanceof Object[]) {
			log.debug "is array"
			aclsToDelete.each{
				log.info "selectedAcl: ${it}"
				if (isDataObject) {
					log.info "delete as data object"
					deleteAclForDataObject(absPath, it, dataObjectAO)
				} else {
					deleteAclForCollection(absPath, it, collectionAO)
				}
			}

		} else {
			log.debug "not array"
			log.info "deleting: ${aclsToDelete}"
			if (isDataObject) {
				log.info "delete as data object"
				deleteAclForDataObject(absPath, aclsToDelete, dataObjectAO)
			} else {
				deleteAclForCollection(absPath, aclsToDelete, collectionAO)
			}
		}

		render "OK"

	}

	private void deleteAclForDataObject(String absPath, String userName, DataObjectAO dataObjectAO) throws JargonException {

		if (!absPath) {
			throw new IllegalArgumentException("null absPath")
		}

		if (!userName) {
			throw new IllegalArgumentException("null userName")
		}

		if (!dataObjectAO) {
			throw new IllegalArgumentException("null dataObjectAO")
		}

		def theUserName = MiscIRODSUtils.getUserInUserName(userName)
		def theZone = MiscIRODSUtils.getZoneInUserName(userName)

		dataObjectAO.removeAccessPermissionsForUser(theZone, absPath, theUserName)

	}

	private void deleteAclForCollection(String absPath, String userName, CollectionAO collectionAO) throws JargonException {

		if (!absPath) {
			throw new IllegalArgumentException("null absPath")
		}

		if (!userName) {
			throw new IllegalArgumentException("null userName")
		}

		if (!collectionAO) {
			throw new IllegalArgumentException("null collectionAO")
		}

		def theUserName = MiscIRODSUtils.getUserInUserName(userName)
		def theZone = MiscIRODSUtils.getZoneInUserName(userName)

		collectionAO.removeAccessPermissionForUser(theZone, absPath, theUserName,true)

	}

}

class AclCommand {
	String absPath
	String acl
	String userName
	static constraints = {
		acl(blank:false, inList:["READ", "WRITE", "OWN"])
		userName(blank:false)
		absPath(blank:false)
	}
}

