
      import com.collibra.dgc.core.api.dto.instance.asset.AddAssetRequest;
      import com.collibra.dgc.core.api.dto.instance.attribute.AddAttributeRequest;
      import com.collibra.dgc.core.api.dto.instance.relation.AddRelationRequest;

            def note = execution.getVariable("note")
            def definition = execution.getVariable("definition")


      def newAssetUuid = assetApi.addAsset(AddAssetRequest.builder()
		.name(signifier)
		.displayName(signifier)
		.typeId(conceptType)
		.domainId(string2Uuid(intakeVocabulary))
		.build()).getId()
		
		
addAttributeToAsset(newAssetUuid,definition,definitionAttributeTypeUuid)
addAttributeToAsset(newAssetUuid,note,noteAttributeTypeUuid)
addRelationsWithOneSourceAndMultipleTargetsToAsset(newAssetUuid,usesRelationTypeUuid,usesrelation)

execution.setVariable("outputCreatedTermId",uuid2String(newAssetUuid))


def addAttributeToAsset(assetUuid,attributeValue,attributeTypeUuid) {
if (attributeValue == null){
return;
}
attributeApi.addAttribute(AddAttributeRequest.builder()
		.assetId(assetUuid)
		.typeId(string2Uuid(attributeTypeUuid))
		.value(attributeValue.toString())
		.build())
}

def addRelationsWithOneSourceAndMultipleTargetsToAsset(sourceUuid,relationTypeUuid,targetUuidList) {
	def addRelationsRequests = []
	
	loggerApi.info("Source: " + sourceUuid.toString())
	loggerApi.info("Type: " + relationTypeUuid.toString())
	loggerApi.info("Target: " + targetUuidList.toString())
	loggerApi.info("Target Class" + targetUuidList.getClass().toString())
	
	targetUuidList.each{ t ->
		loggerApi.info("T Class" + t.getClass().toString())
		
		addRelationsRequests.add(AddRelationRequest.builder()
			.sourceId(sourceUuid)
			.targetId(t)
			.typeId(string2Uuid(relationTypeUuid))
			.build())
	}
	
	relationApi.addRelations(addRelationsRequests)
}