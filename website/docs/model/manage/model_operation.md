---
title: Model Operation
language: en
sidebar_label: Model Operation
pagination_label: Model Operation
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - model management
    - model operations
draft: false
last_update:
    date: 09/13/2024
---




### <span id="operation">Model Operation</span>

You are only allowed to operate on models. You can hover on the right most column **Actions** of the model list to get the action names. Specific actions are listed below:

- **Edit**: Click on the pencil shape button, enter into the model editing page.

- **Build Index**: Loads data for models. You can choose the data range in the pop-up window.

- **Model Partition**: Set partition column for the model.

- **Export Model**: Export single model metadata.

  > **Note**: Since the locked indexes will be deleted after the new indexes have been built, the exported model metadata will not include the locked index.

- **Export TDS**: Export TDS file of the model .

- **Rename**: Renames the model.

- **Clone**: Clones an identical model. You can give a new name for this new model. The new model has the same fact table, dimension tables, join relationship, dimensions, measures, computed columns, date partition column, aggregate indexes, table indexes, etc. as the origin model. But the new model does not have data, you need to load data for this cloned model manually.

  > **Note**: Since the locked indexes will be deleted after the new indexes have been built, the cloned model will not include the locked index.

- **Change Owner**：Change model owner. Only system administrators and project administrators have the authority to modify model owner.

- **Delete**: Deletes the model, remove the loaded data at the same time.

- **Purge**: Purges all loaded data in this model.

- **Offline**: Makes a *Online / Warning* model offline. An offline model cannot answer any queries.

- **Online**: Makes a *Offline* model online. An online model should be able to answer related queries.

> **Note:** If the model is in *BROKEN* status, only the **delete** operation is allowed.
