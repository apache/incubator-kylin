export default {
  'en': {
    title: 'Text Recognition',
    emptyText: 'The recognized columns will be displayed here',
    previous: 'Prev',
    next: 'Next',
    recognize: 'Recognize',
    errorCount: ' | {count} error | {count} errors',
    repeatCount: ' | {count} duplicate | {count} duplicates',
    selectedDimensionCount: 'Select {count} results | Select {count} result | Select {count} results',
    usedDimensionCount: '{count} already exists',
    inputPlaceholder1: 'Please enter "table.column", separated by commas in English. If you are using the computed column, enter the "fact table.computed column".',
    inputPlaceholder2: 'e.g. CUSTOMER.C_CUSTKEY1, CUSTOMER.C_CUSTKEY2',
    // inputPlaceholderTooltip1: 'Method 1: Enter the formula A1 & "," on a new column in Excel, enter and drag the bottom right corner of the cell to add in bulk;',
    // inputPlaceholderTooltip2: 'Method 2: Select all the cells that need to added in bulk, right-click and select the cell format (shortcut cmd & ctrl + 1). Select "Custom", enter English format General "," or @ "," in the type, confirm and add in bulk.',
    // inputPlaceholderTooltipTrigger: 'Not sure how to batch add characters?',
    recognizeFailed: 'Recognize failed. No result, please check and try again',
    columnDuplicate: 'Duplicate with {column}',
    columnNotInModel: 'Column {column} does not exist in the current model',
    columnNotInIncludes: 'Column {column} does not exist in include dimension',
    columnUsedInOther: 'Column {column} is used in other dimension',
    dimensionName: 'Dimension Name',
    dataType: 'Data Type',
    dexecute: 'Dexecute',
    acceleratorKey: ' ⌃/⌘ enter',
    repeatTip: 'The selectable options have been automatically deduplicated'
  }
}
