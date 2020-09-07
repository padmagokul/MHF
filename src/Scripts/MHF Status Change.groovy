/*
Script Name: Mark as Candidate Script
Author: Lucid
Version: v1.0
Version History: Implemented the way to get status ID by configuration variable
Purpose: This script is used to change the status of the asset
*/

import com.collibra.dgc.workflow.api.exception.WorkflowException

//Method to change the status of the asset
def changeStatus(assetId, approvedStatusId){
    def changedAsset = assetApi.changeAsset(builders.get("ChangeAssetRequest").id(string2Uuid(assetId))
        .statusId(approvedStatusId).build())
    loggerApi.info("-----Candidate Status Id "+approvedStatusId)
}

try{
    def approvedStatusId = statusApi.getStatusByName(approvedStatus).getId()
    //Method call
    changeStatus(assetId, approvedStatusId)
}
catch(Exception e){
    String errorMessage = translation.getMessage("Status: "+approvedStatus+" not found")
    String errorTitle = translation.getMessage("statusNotFound");
    WorkflowException workflowException = new WorkflowException(errorMessage);
    workflowException.setTitleMessage(errorTitle);
    throw workflowException;
}