/*
Script Name: Checking Comment
Author: Lucid
Version: v1.0
Version History: Initial Version
Purpose: This script is to check comment if it is rejection
*/
def commentNotFound=false;
def approverCommentMandatory=" ";
if(reject==true && (approverComment==null || approverComment.toString().isEmpty()))
{
loggerApi.info("Comment Not Found for Data Owner & Steward Rejection")
approverCommentMandatory="Comment is mandatory for Reject, please enter a comment";
commentNotFound=true
}
execution.setVariable("approverCommentMandatory",approverCommentMandatory)
loggerApi.info(approverCommentMandatory)
execution.setVariable("commentNotFound",commentNotFound)
