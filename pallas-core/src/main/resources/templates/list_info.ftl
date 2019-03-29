{
	"_source": "none",
    "size": 1000,
	"query": {
		"bool": {
			"filter": [{
					"range": {
						"timestamp": {
							"lte": "${endTime}",
                            "gte": "${beginTime}",
							"format": "epoch_millis"
						}
					}
				},
				{
					"term": {
						"type": "${type}"
					}
				},
				{
					"term": {
						"cluster_name": "${clusterName}"
					}
				}
			]
		}
	},
	"aggs": {
		"aggs_max": {
			"max": {
				"field": "timestamp"
			}
		}
	},
	"collapse": {
		"field": "${fieldName}"
	}
}