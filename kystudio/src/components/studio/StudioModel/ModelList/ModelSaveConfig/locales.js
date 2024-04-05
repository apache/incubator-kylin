export default {
  'en': {
    partitionSet: 'Partition Setting',
    dateFormat: 'Time Format',
    partitionDateColumn: 'Time Partition Column',
    saveModel: 'Save Model',
    setting: 'Setting',
    buildRange: 'Build Range',
    startDate: 'Start Date',
    endDate: 'End Date',
    to: 'To',
    loadData: 'Load Data',
    loadExistingData: 'Load existing data',
    loadExistingDataDesc: 'Load new records existing from the last load job.',
    customLoadRange: 'Customize Load Range',
    loadRange: 'Loaded Range',
    noPartition: 'No Partition',
    invaildDate: 'Please enter a valid date',
    detectAvailableRange: 'Detect available range',
    modelPartitionSet: 'Model Partition',
    modelSaveSet: 'Save',
    dataFilterCond: 'Data Filter Condition',
    dataFilterCondTips: 'Data filter condition is an addition data filter during data loading. E.g. you can filter out those records with null values or specific records according to your business rules',
    noColumnFund: 'Column not found',
    pleaseInputColumn: 'Please select a partition column',
    pleaseInputColumnFormat: 'Please select or enter a customized time format',
    detectFormat: 'Detect partition time format',
    errorMsg: 'Error Message:',
    filterCondTips: 'Modifying the data filter conditions will result in all indexes under this model being rebuilt. Please modify with caution.',
    filterPlaceholder: 'Please enter your filter condition and no clause "WHERE" needed. If you have several filter conditions, you should combine them with "AND" or "OR". E.g. BUYER_ID <> 0001 AND COUNT_ITEM > 1000 OR TOTAL_PRICE = 1000',
    changeSegmentTip1: 'You have modified the partition column as {tableColumn}, time format {dateType}. After saving, all segments under the model {modelName} will be purged. You need to reload the data, the model cannot serve related queries during data loading. Please confirm whether to submit?',
    changeSegmentTip2: 'You have modified as full load. After saving, all segments under the model {modelName} will be purged . The system will automatically rebuild the index and full load the data. The model cannot serve related queries during index building. Please confirm whether to submit?',
    chooseBuildType: 'Please select a load method',
    incremental: 'Incremental Load',
    fullLoad: 'Full Load',
    recommend: 'Recommend',
    isNotBatchModel: 'Can’t load the stream model in full.',
    incrementalTips: 'It will load data incrementally based on the selected partition column, which is more resource-efficient.',
    fullLoadTips: 'The system will load all data',
    changeBuildTypeTips: 'With partition setting changed, all segments and data would be deleted. The model couldn\'t serve queries. Meanwhile, the related ongoing jobs for building index would be discarded.',
    editCCBuildTipTitle: 'Will delete and generate new indexes, continue to save model?',
    editCCBuildTip: 'Query performance will degrade until the new index are built! Modification of a computed column as a dimension or metric in the current model will cause the system to delete the relevant index and generate new index. The query performance will not be restored until the build index job is done.',
    saveAndBuild: 'Save and Build',
    purgeSegmentDataTips: 'Model definition has changed. Once saving the model, all data in the segments will be deleted. As a result, this model CAN\'T be used to serve queries. We strongly recommend to reload all data (in total {storageSize}).\r\n Do you want to continue?',
    onlyAddLeftJoinTip: 'The model definition relationship has changed and the current change only affects incremental data. To overwrite the stock data, go to the Segment page to clear the old data and rebuild.',
    changeSegmentTips: 'With partition setting changed, all segments and data would be deleted. The model couldn\'t serve queries. Meanwhile, the related ongoing jobs for building index would be discarded.<br/>Do you want to continue?',
    saveAndLoad: 'Save and Build',
    partitionDateTable: 'Partition Table',
    multilevelPartition: 'Subpartition Column',
    multilevelPartitionDesc: 'A column from the selected table could be chosen. The models under this project could be partitioned by this column in addition to time partitioning. ',
    indexSetting: 'Index Setting',
    advanceSetting: 'Advanced Setting',
    addBaseAggIndexCheckBox: 'Base Aggregate Index',
    addBaseTableIndexCheckBox: 'Base Table Index',
    secStorage: 'Tiered Storage',
    secStorageDesc: 'Tiered storage can significantly improve the performance of  flexible ultra-multidimensional analysis and detailed queries. It will add base table index by default and cannot be deleted for synchronizing  data when open.',
    secStorageTips: 'With this switch OFF, the model\'s tiered storage data will be cleared。',
    openSecStorageTips: 'It\'s recommended to turn on the tiered storage, as too many dimensions are included.',
    openSecStorageTips2: 'With the tiered storage ON, the existing data needs to be loaded to tiered storage to take effect.',
    disableSecStorageActionTips: 'The tiered storage can\'t be used for fusion or streaming models at the moment.',
    disableSecStorageActionTips2: 'The tiered storage can\'t be used because no dimension or measure has been added and the base table index can\'t be added.',
    forbidenComputedColumnTips: 'The parquet files containing data prior to 1970 cannot be loaded. <a class="ky-a-like" href="" target="_blank">View the manual <i class="el-ksd-icon-spark_link_16"></i></a>',
    secondStoragePartitionTips: 'Can\'t save the model. When the model uses incremental load method and the tiered storage is ON, the time partition column must be added as a dimension.',
    streamSecStoragePartitionTips: 'Can\'t save the model. For fusion model, the time partition column must be added as a dimension.',
    baseAggIndexTips: 'The base aggregate index contains all dimensions and metrics of the model, and can override answering aggregated queries to avoid pushdown. It will automatically update as the model changes by default.',
    baseTableIndexTips: 'The base table index contains all the columns used in the dimensions and metrics of the model, and can override answering detailed queries to avoid pushdown. It will automatically update as the model changes by default.',
    notBatchModelPartitionTips: 'The time partition settings can\'t be modified after the fusion model or streaming model is saved.',
    disableChangePartitionTips: 'The time partition settings can\'t be modified for fusion model and streaming model.',
    previewFormat: 'Format preview: ',
    formatRule: 'The customized time format is supported. ',
    viewDetail: 'More info',
    rule1: 'Support using some elements of yyyy, MM, dd, HH, mm, ss, SSS in positive order',
    rule2: 'Support using - (hyphen), / (slash), : (colon), English space as separator',
    rule3: 'When using unformatted letters, use a pair of \' (single quotes) to quote, i.e. \'T\' will be recognized as T'
  }
}
