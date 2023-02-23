import { jsPlumbTool } from '../../../util/plumb'
import { modelRenderConfig } from '../../studio/StudioModel/ModelEdit/config'
import ModelTree from '../../studio/StudioModel/ModelEdit/layout'

let allConnectList = {}

export function initPlumb (renderDom, zoom) {
  allConnectList = {}
  const plumbs = jsPlumbTool()
  return {
    plumbInstance: plumbs.init(renderDom, zoom / 10),
    plumbTool: plumbs
  }
}

// 表连线
export function drawLines (that, plumbTool, joints) {
  plumbTool.lazyRender(() => {
    joints.forEach(v => {
      v.joins.forEach(item => {
        that.primaryKeys.push(item.primaryKey)
        that.foreignKeys.push(item.foreignKey)
        addPlumbPoints(plumbTool, v.guid)
        addPlumbPoints(plumbTool, item.guid)
        const conn = plumbTool.connect(v.guid, item.guid, () => {}, {
          joinType: v.type ?? '',
          brokenLine: false
        })
        allConnectList[`${v.guid}$${item.guid}`] = conn
      })
    })
  })
}

// 添加端点
function addPlumbPoints (plumbTool, guid) {
  const anchor = modelRenderConfig.jsPlumbAnchor
  const scope = 'showlink'
  const endPointConfig = Object.assign({}, plumbTool.endpointConfig, {
    scope: scope,
    uuid: guid
  })

  plumbTool.addEndpoint(guid, {anchor: anchor}, endPointConfig)
}

// 根据树形结构自定义表的位置
export function customCanvasPosition (renderDom, model, zoom) {
  const { tables } = model
  const canvas = {
    coordinate: {},
    zoom: zoom
  }
  const layers = autoCalcLayer(tables)
  if (layers && layers.length > 0) {
    const baseL = modelRenderConfig.baseLeft
    const baseT = modelRenderConfig.baseTop
    const renderDomBound = renderDom.getBoundingClientRect()
    const centerL = renderDomBound.width / 2 - modelRenderConfig.tableBoxWidth / 2
    const moveL = layers[0].X - centerL
    for (let k = 0; k < layers.length; k++) {
      let [currentTable] = tables.filter(item => item.guid === layers[k].guid)
      canvas.coordinate[`${currentTable.alias}`] = {
        x: baseL - moveL + layers[k].X,
        y: baseT + layers[k].Y,
        width: modelRenderConfig.tableBoxWidth,
        height: modelRenderConfig.tableBoxHeight
      }
    }
  }
  return canvas
}

// 获取树形结构
function autoCalcLayer (tables) {
  const [factTable] = tables.filter(it => it.type === 'FACT')
  if (!factTable) {
    return
  }
  const rootGuid = factTable.guid
  const tree = new ModelTree({rootGuid: rootGuid, showLinkCons: allConnectList})
  tree.positionTree()
  return tree.nodeDB.db
}
