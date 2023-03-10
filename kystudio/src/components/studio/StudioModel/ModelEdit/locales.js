export default {
  'en': {
    FACT: 'Fact Table',
    LOOKUP: 'Dimension Table',
    'adddimension': 'Add Dimension',
    'addmeasure': 'Add Measure',
    'addjoin': 'Add Join Relationship',
    'editmeasure': 'Edit Measure',
    'editdimension': 'Edit Dimension',
    'editjoin': 'Edit Join',
    'showtable': 'Show Table',
    'tableaddjoin': 'Add Join',
    'modelSetting': 'Model Setting',
    'userMaintainedModel': 'User Maintained Model',
    'systemMaintainedModel': 'System Maintained Model',
    'userMaintainedTip1': 'System is not able to change the model definition: dimension, measure or join tree',
    'userMaintainedTip2': 'System can change the model index: aggregate index or table index',
    'userMaintainedTip3': 'System is not able to delete this model',
    'systemMaintainedTip1': 'System can change the model definition: dimension, measure or join tree',
    'systemMaintainedTip2': 'System can change the model index: aggregate index or table index',
    'systemMaintainedTip3': 'System can delete this model',
    'avoidSysChange': 'Avoid system change semantics',
    'allowSysChange': 'Allow system change semantics',
    'delTableTip': 'All dimensions, measures and joins using this table would be deleted. Are you sure you want to delete this table?',
    'noFactTable': 'Please select a fact table.',
    switchLookup: 'Set as Dimension Table',
    switchFact: 'Set as Fact Table',
    switchTableTypeTips: 'Continue switching will replace the current fact table, confirm to replace?',
    switchReplace: 'Switch and Replace',
    kafakFactTips: 'Kafka tables can only be used as fact tables. Please delete the current fact table or switch it to a dimension table before loading a Kafka table.',
    kafakDisableSecStorageTips: 'Can\'t use Kafka tables when the tiered storage is ON.',
    editTableAlias: 'Rename',
    deleteTable: 'Delete the table',
    noSelectJobs: 'Please check at least one item',
    add: 'Add',
    batchAdd: 'Batch Add',
    batchDel: 'Batch Delete',
    checkAll: 'Check All',
    unCheckAll: 'Uncheck All',
    delete: 'Delete',
    back: 'Back',
    requiredName: 'Please enter alias',
    modelDataNullTip: 'Can\'t find this model.',
    saveSuccessTip: 'The model has been saved successfully.',
    buildIndex: 'Build Index',
    createBaseIndexTips: 'Successfully added {createBaseNum} base index(es). ',
    updateBaseIndexTips: 'Successfully updated {updateBaseNum} base index(es).',
    addSegmentTips: 'To make it available for queries, please define the data range which this model would be served for.',
    addIndexTips: 'To improve query performance, please add and build indexes. ',
    addIndexAndBaseIndex: 'To improve query performance, more indexes could be added and built.',
    createAndBuildBaseIndexTips: '{createBaseIndexNum} base index(es) have been added successfully.',
    addIndex: 'Add Index',
    viewIndexes: 'View Index',
    addSegment: 'Add Segment',
    ignoreaddIndexTip: 'Not Now',
    noDimensionTipContent: 'No dimension has been added. If this model would be used for aggregate queries, please add some necessary dimensions for generating aggregate indexes later.',
    noDimensionAndMeasureTipContent: 'No dimension or measure has been added. If this model would be used for aggregate queries, please add some necessary dimensions and measures for generating aggregate indexes later.',
    noDimensionTipTitle: 'Add Dimension',
    noDimensionAndMeasureTipTitle: 'Add Dimension and Measure',
    noDimensionGoOnSave: 'Save',
    noDimensionBackEdit: 'Continue Editing',
    searchHistory: 'Searh History',
    searchActionSaveSuccess: '{saveObj} saved successfully',
    measure: 'Measure',
    dimension: 'Dimension',
    addTableJoinCondition: 'Add table join condition',
    editTableJoinCondition: 'Edit table join condition',
    tableJoin: 'Table join condition',
    addDimension: 'Add dimension',
    addMeasure: 'Add measure',
    editDimension: 'Edit dimension',
    editMeasure: 'Edit measure',
    searchTable: 'Search table',
    searchInputPlaceHolder: 'Search model\'s table alias, column, measure name or join relationship',
    delConnTip: 'Are you sure you want to delete this connection?',
    delConnTitle: 'Delete connection',
    brokenEditTip: 'This model is broken. Please check and adjust the join relationship, the partition column, and the filter condition.',
    noTableTip: '<p>Add Table: drag the table from the left source tree and drop it to the central zone.</p><p>Add Join: drag the column from one table and drop it on another table.</p>',
    noBrokenLink: 'No error join(s).',
    canNotRepairBrokenTip: 'Can\'t recover this model as too much metadata information is lost. Please contact technical support.',
    searchColumn: 'Search column name',
    modelChangeTips: 'You are modifying the model definition. After submitted, all indexes of this model may be rebuilt. The model will be unavailable to serve queries until the indexes are built successfully.',
    ignore: 'Ignore',
    saveAndSubmitJobSuccess: 'Successfully saved the changes, and submitted the job of loading data',
    tableHasOppositeLinks: 'A reserved join condition already exists between the tables. Please click on the join condition to modify.',
    changeTableJoinCondition: 'Modifying the table\'s type would affect the existing join condition. Please delete or modify the join condition first.',
    lockupTableToFactTableTip: 'Please add join condition from the fact table to a look up table.',
    noStarOrSnowflakeSchema: 'This join condition is not allowed in neither <a href="https://en.wikipedia.org/wiki/Star_schema" target="_blank">star</a> or <a href="https://en.wikipedia.org/wiki/Snowflake_schema" target="_blank">snowflake</a> schema. Please adjust and try again.',
    varcharSumMeasureTip: 'Can\'t save model. The following measures can\'t reference column(s) in Varchar type, as the selected function is SUM or PERCENTILE_APPROX.',
    measureRuleErrorTip: 'This measure\'s function ({type}) is incompatible with the referenced column, which is Varchar.',
    pleaseModify: 'Please modify.',
    iKnow: 'Got It',
    disabledConstantMeasureTip: 'Can\'t modify the default measure.',
    flattenLookupTableTips: 'Unable to use columns from this table for dimension and measure. Because the join relationship of this dimension table won\'t be precomputed.',
    disableDelDimTips: 'When the tiered storage is ON, the time partition column can\'t be deleted from the dimension.',
    forbidenCreateCCTip: 'Can\'t add computed column to fusion model',
    streamTips: 'For fusion model, the time partition column can\'t be deleted from the dimension.',
    rename: 'Rename Table Name',
    spreadTableColumns: 'Collapse',
    expandTableColumns: 'Expand',
    noResults: 'No result',
    searchResults: '{number} results',
    doubleClick: 'Double Click Header',
    loseFactTableAlert: 'The current model is missing a fact table, please set up a fact table.',
    discardChange: 'Discard Changes',
    continueEditing: 'Continue Editing',
    betaSearchTips: 'The current search is beta version. Having search problems? ',
    feedback: 'Go to feedback',
    introductionUrl: 'http://kyligence.io/enterprise/#analytics',
    noConnectedColumn: 'No Connected Column'
  }
}
