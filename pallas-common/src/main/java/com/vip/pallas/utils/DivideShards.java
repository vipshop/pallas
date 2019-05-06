package com.vip.pallas.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DivideShards {

	private static final Logger logger = LoggerFactory.getLogger(DivideShards.class);

	public static List<HashSet<String>> divideShards2Group(Map<Integer, List<String>> shardNodesMap,
			Integer totalCopy, HashSet<String> nodes) {
		List<HashSet<String>> allGroups = new ArrayList<>();
		// sort shards & nodes, make sure it won't divide into different groups with different orders of map's key&values.
		Set<Entry<Integer, List<String>>> entrySet = shardNodesMap.entrySet();
		List<Entry<Integer, List<String>>> entryList = new ArrayList<>(entrySet);
		Collections.sort(entryList, (o1, o2) -> {
			Collections.sort(o1.getValue());
			Collections.sort(o2.getValue());
			return o1.getKey().compareTo(o2.getKey());
		});

		try {
			for (int i = 0; i < totalCopy; i++) {
				if (!nodes.isEmpty()) {
					HashSet<String> nodesInThisGroup = new HashSet<>();
					HashSet<String> nodes4nextRound = new HashSet<>();
					Iterator<Entry<Integer, List<String>>> it = entryList.iterator();
					while (it.hasNext()) {
						Entry<Integer, List<String>> entry = it.next();
						List<String> nodeList = entry.getValue();
						for (HashSet<String> group : allGroups) {
							nodeList.removeAll(group);// 已有组的node不参与这次选择
						}
						if (nodeList.isEmpty()) { // 所有节点都有组了
							return allGroups;
						}
						if (nodesInThisGroup.isEmpty()) { // 将第一个node放进去
							nodesInThisGroup.add(nodeList.get(0));
							nodes4nextRound.addAll(nodeList);
							nodes4nextRound.removeAll(nodesInThisGroup); // 剩余的node放进下个迭代
							continue;
						}
						if (!nodesInThisGroup.stream().anyMatch(c -> { return nodeList.contains(c);})) { // 如无包含则用第一个
							List<String> excludeNextRound = nodeList.stream().filter(node -> {
								return !nodes4nextRound.contains(node);
							}).collect(Collectors.toList());
							nodesInThisGroup.add(excludeNextRound.isEmpty() ? nodeList.get(0) : excludeNextRound.get(0));
						}
						nodes4nextRound.addAll(nodeList);
						nodes4nextRound.removeAll(nodesInThisGroup);
					}
					nodes.removeAll(nodesInThisGroup);// 已有组的node不参与下round选择
					allGroups.add(nodesInThisGroup);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return allGroups;
	}
}
