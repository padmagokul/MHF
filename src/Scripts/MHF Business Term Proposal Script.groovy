/*
Script Name: Business Term Proposal Script
Author: Lucid
Version: 1.0
Version History: -
Purpose: This script is used to get the asset attributes such as 
name, description, note etc and passes them to approval
workflow.
*/

import com.collibra.dgc.core.api.model.workflow.WorkflowBusinessItemType 
import com.collibra.dgc.core.api.model.workflow.WorkflowDefinition
import com.collibra.dgc.workflow.api.exception.WorkflowException


    //Values to be passed to user defined(createAsset) method
    def note = execution.getVariable("note")
    def definition = execution.getVariable("definition")
    def typeOfAssetId = execution.getVariable("conceptType")
    loggerApi.info("----Concept type-----:"+typeOfAssetId)
    def domainId = domainApi.findDomains(builders.get("FindDomainsRequest").name(intakeVocabulary).build()).getResults()*.getId()
        
    //Script to create a new asset
    def newAssetUuid = assetApi.addAsset(builders.get("AddAssetRequest").name(signifier).displayName(signifier)
                            .typeId(conceptType).domainId(domainId[0])
                            .build()).getId()
    loggerApi.info('------Asset Created Successfully--------')
    
    execution.setVariable("outputCreatedTermId", uuid2String(newAssetUuid))

    def definitionAttributeId = getAttributeId(definitionAttribute)
    def noteAttributeId = getAttributeId(noteAttribute)

    //Pass the attributes to user defined method(addAttributeToAsset)
    addAttributeToAsset(newAssetUuid, definition, definitionAttributeId)
    addAttributeToAsset(newAssetUuid, note, noteAttributeId)

    //Add the target domain to the list to get the users responsible for that domain
    def responsibilityList = []
    responsibilityList.add(domainId[0])

    //Get Role ID's for the given roles
    def dgAnalystId = roleApi.findRoles(builders.get("FindRolesRequest").name(dataGovernanceAnalystRole) 
                        .build()).getResults()*.getId()

    //Find the users(ID's) that are responsible for the target domain with specified role
    def responsibleUsers = responsibleUsers(responsibilityList, dgAnalystId)
    def dgAnalystUserList = []

    //Build the user expression for user task
    for(int user=0; user<responsibleUsers.size;user++){
        dgAnalystUserList.add("user("+responsibleUsers[user]+")")
    }

    //Convert the list to CSV to use in user tasks
    dgAnalystUsers = utility.toCsv(dgAnalystUserList)
    loggerApi.info("--------CSV User-------"+dgAnalystUsers)

    //Values to be passed to the approval workflow
    def formProperties =[:]
    formProperties.put("assetNote", note.toString())
    formProperties.put("assetDefinition", definition.toString())
    formProperties.put("assetName", signifier.toString())
    formProperties.put("assetId", uuid2String(newAssetUuid))
    formProperties.put('dgAnalyst', dgAnalystUsers)
    formProperties.put('assetTypeId', typeOfAssetId)
    formProperties.put('domainId', domainId[0])
    // formProperties.put('startUserId', requester)

    try{
        def workflowId = workflowDefinitionApi.getWorkflowDefinitionByProcessId(calledWorkflowName).getId()
        //Add the asset ID to the list
        def assetIdList = []
        assetIdList.add(newAssetUuid)

        //Get the user ID of the workflow Initiator
        def userId = userApi.findUsers(builders.get("FindUsersRequest")
                        .name(requester).build()).getResults()*.getId()
        
        //Pass the values to user defined method(callWorkflow)
        callWorkflow(workflowId, assetIdList, userId, formProperties)
        loggerApi.info("-----------Business Term Proposal Successful---------------")
    }
    catch(Exception e){
        loggerApi.error("Couldn't find target workflow")
	    String errorMessage = translation.getMessage("Couldn't find target workflow")
	    String errorTitle = translation.getMessage("workFlowNotFound");
	    WorkflowException workflowException = new WorkflowException(errorMessage);
	    workflowException.setTitleMessage(errorTitle);
	    throw workflowException;
    }


    //User Defined methods start here
    //Method to get attribute Id's
    def getAttributeId(attributeType) {
        attributeTypeId = attributeTypeApi.findAttributeTypes(builders.get("FindAttributeTypesRequest").name(attributeType).build())
                                        .getResults()*.getId()
        return attributeTypeId[0]
    }

    //Method to add attributes to asset
    def addAttributeToAsset(assetUuid, attributeValue, attributeTypeUuid) {
        if (attributeValue == null){
            return
        }
        attributeApi.addAttribute(builders.get("AddAttributeRequest").assetId(assetUuid).typeId(attributeTypeUuid)
                .value(attributeValue.toString()).build())
        loggerApi.info('------Attributes Added Successfully--------')
    }

    //Method to find the users(ID's) that are responsible for the target domain with specified role
    def responsibleUsers(responsibilityList, rolesList) {
        def responsibleUserIds = responsibilityApi.findResponsibilities(builders.get("FindResponsibilitiesRequest")
                                    .resourceIds(responsibilityList).roleIds(rolesList) 
                                    .build()).getResults()*.getOwner()*.getId()
        loggerApi.info("-------------User ID's for the responsibility on domain------------ "+responsibleUserIds)
        if(responsibleUserIds.isEmpty()){
            loggerApi.error("Couldn't find Users for the given role")
            String errorMessage = translation.getMessage("Couldn't find Users for the given role "+dataGovernanceAnalystRole)
            String errorTitle = translation.getMessage("usersNotFound");
            WorkflowException workflowException = new WorkflowException(errorMessage);
            workflowException.setTitleMessage(errorTitle);
            throw workflowException;
        }

        def uuidList =[]
        for(userIds in responsibleUserIds){
            uuidList.add(uuid2String(userIds))
        }   
        def responsibleUserNames = userApi.findUsers(builders.get('FindUsersRequest').userIds(uuidList)
                                        .build()).getResults()*.getUserName()
        loggerApi.info("-------------User names for the responsibility on domain------------ "+responsibleUserNames)
        return responsibleUserNames
    }

    //Method to call approval workflow using workflowInstanceApi's "startWorkflowInstances"    
    def callWorkflow(workflowId, assetIdList, userId, formProperties) {    
        workflowInstanceApi.startWorkflowInstances(builders.get("StartWorkflowInstancesRequest").workflowDefinitionId(workflowId)
            .businessItemType(WorkflowBusinessItemType.ASSET)
            .businessItemIds(assetIdList).guestUserId(userId[0])
            .formProperties(formProperties).build())

        loggerApi.info("----------------Workflow Call Successful----------------")
    }