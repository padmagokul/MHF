/*
Script Name: Add Comments Script
Author: Lucid
Version: v1.0
Version History: -
Purpose: This script is used to review and add comments to the asset.
*/

import com.collibra.dgc.core.api.model.ResourceType
import com.collibra.dgc.workflow.api.exception.WorkflowException

dgAnalystComments = comments.toString()
loggerApi.info("--Comments--"+dgAnalystComments)

if(dgAnalystComments.isEmpty()){
    loggerApi.error("Please provide comments.")
    String errorMessage = translation.getMessage("Please provide comments.")
    String errorTitle = translation.getMessage("commentsNotFound");
    WorkflowException workflowException = new WorkflowException(errorMessage);
    workflowException.setTitleMessage(errorTitle);
    throw workflowException;
}
else{
    //Add Comments to asset
    dgAnalystComment = commentApi.addComment(builders.get("AddCommentRequest").baseResourceId(string2Uuid(assetId))
                        .baseResourceType(ResourceType.Asset).content(comments.toString()).build())
    loggerApi.info('--------Comments added successfully----------'+dgAnalystComment)

    //Set the acronym asset to variable
    def relatedAsset = execution.getVariable("relatedAsset")
    
    //Get Acronym Relation UUID
    loggerApi.info("----Adding LOB to BT-------")
    lobRelationId = relationTypeApi.findRelationTypes(builders.get("FindRelationTypesRequest")
                                    .sourceTypeName(lobRelationSourceType).targetTypeName(lobRelationTargetType)
                                    .role(lobRelation).build()).getResults()*.getId()
    loggerApi.info("-----Got LOB id------"+lobRelationId)
    addRelationToAsset(assetId, lobRelationId, relatedAsset)

    loggerApi.info("---Domain ID----"+domainId)

    def businessStewards = responsibleUsers(domainId, businessStewardRole)
    def stakeholders = responsibleUsers(domainId, stakeholderRole)

    //Adding the business stewards and stakeholders together
    def voters = []
    groupBusinessStewards = businessStewards as Set
    groupStakeholders = stakeholders as Set
    if(groupStakeholders == null) 
        voters = groupBusinessStewards
    else
        voters = groupBusinessStewards + groupStakeholders
    loggerApi.info("-----Voters-----"+voters)

    List<String> votersList = new ArrayList<String>();
    votersList.addAll(voters);
    loggerApi.info("----Voters List----"+votersList)
    
    execution.setVariable('votersList', votersList)
}

//User defined methods start here
//Method to add any acronym relation to asset
    def addRelationToAsset(def sourceUuid,def relationTypeUuid,def targetUuid) {
           if (targetUuid == null || targetUuid.isEmpty()){
                return;
           }
       
           relationApi.addRelation(builders.get("AddRelationRequest")
                   .sourceId(string2Uuid(sourceUuid)).targetId(targetUuid)
                   .typeId(relationTypeUuid).build())
    }

//Method to find the users(ID's) that are responsible for the target domain with specified role
    def responsibleUsers(newDomain, roleName) {

        //Add the target domain to the list to get the users responsible for that domain
        def responsibilityList = []
        responsibilityList.add(newDomain)

        //Get Role ID for the given role
        def rolesIdList = roleApi.findRoles(builders.get('FindRolesRequest').name(roleName) 
                            .build()).getResults()*.getId()

        //Fetch the responsible user Id's
        def responsibleUserIds = responsibilityApi.findResponsibilities(builders.get('FindResponsibilitiesRequest')
                                    .resourceIds(responsibilityList).roleIds(rolesIdList) 
                                    .build()).getResults()*.getOwner()*.getId()
        loggerApi.info("-------------User IDs for the responsibility on target domain------------ "+responsibleUserIds)

        if(responsibleUserIds.isEmpty())
           return 

        def uuidList =[]
        for(userIds in responsibleUserIds){
            uuidList.add(uuid2String(userIds))
        }   
        def responsibleUserNames = userApi.findUsers(builders.get('FindUsersRequest').userIds(uuidList)
                                        .build()).getResults()*.getUserName()
        loggerApi.info("-------------User names for the responsibility on target domain------------ "+responsibleUserNames)
        return responsibleUserNames
    }
