{
  "size": 1,
  "sort": [{
    "timestamp": {
      "order": "desc"
    }
  }],
  "query": {
    "bool": {
      "filter": [{
        "range": {
          "timestamp": {
            "gte": "${beginTime}",
            "lte": "${endTime}",
            "format": "epoch_millis"
          }
        }
      }, {
        "term": {
          "type": "${type}"
        }
      }, {
        "term": {
          "cluster_name": "${cluserName}"
        }
      }
<#if type == 'node_stats'>
      ,{
        "term": {
          "node_stats.name": "${nodeName}"
        }
      }
<#elseif type == 'index_stats'>
      ,{
        "term": {
          "index_stats.index_name": "${indexName}"
        }
      }
</#if>
    ]}
  }

}