{
  "size": 0,
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
      , {
        "term": {
            "node_stats.name": "${nodeName}"
        }
      }
<#elseif type == 'index_stats'>
      , {
        "term": {
            "index_stats.index_name": "${indexName}"
        }
      }
</#if>
    ]
    }
  },
  "aggs": {
    "aggs_2_date_histogram": {
      "date_histogram": {
        "interval": "${interval_unit}",
        "field": "timestamp",
        "min_doc_count": 0,
        "extended_bounds": {
          "min": "${beginTime}",
          "max": "${endTime}"
        },
        "format": "epoch_millis"
      },
      "aggs": {
        "aggs_1_value": {
          "max": {
            "field": "${fieldName}"
<#if type == 'indices_stats'>
             ,
             "script":{
                  "inline": "_value/60"
              }
</#if>
          }
        }
<#if isDerivative == true>
        ,
        "aggs_3_derivative": {
          "derivative": {
            "buckets_path": "aggs_1_value"
          }
        }
</#if>
      }
    }
  }

}