package com.vip.pallas.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevideShards {

	private static final Logger logger = LoggerFactory.getLogger(DevideShards.class);
	public static List<HashSet<String>> devideShards2Group(Map<Integer, List<String>> shardNodesMap,
			Integer totalCopy, HashSet<String> nodes) {
		List<HashSet<String>> allGroups = new ArrayList<>();
		try {
			for (int i = 0; i < totalCopy; i++) {
				if (!nodes.isEmpty()) {
					HashSet<String> nodesInThisGroup = new HashSet<>();
					HashSet<String> nodes4nextRound = new HashSet<>();
					Iterator<Entry<Integer, List<String>>> it = shardNodesMap.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Integer, List<String>> entry = it.next();
						List<String> nodeList = entry.getValue();
						for (HashSet<String> group : allGroups) {
							nodeList.removeAll(group);// 已有组的node不参与这次选择
						}
						if (nodeList.isEmpty()) { // 所有节点都有组了
							return allGroups;
						}
						if (nodesInThisGroup.isEmpty()) {
							nodesInThisGroup.add(nodeList.get(0));
							// nodes.remove(nodeList.get(0));
							nodes4nextRound.addAll(nodeList);
							nodes4nextRound.removeAll(nodesInThisGroup);
							continue;
						}
						boolean nodeAdded = false;
						for (String node : nodeList) {
							if (nodesInThisGroup.contains(node)) { // 偏向于在已经选择node的
								nodeAdded = true;
								break;
							}
							if (!nodes4nextRound.contains(node)) {
								nodesInThisGroup.add(node);
								// nodes.remove(node);
								nodes4nextRound.addAll(nodeList);
								nodes4nextRound.removeAll(nodesInThisGroup);
								nodeAdded = true;
								break;
							}
						}
						if (!nodeAdded) { // 那就加第一个
							nodesInThisGroup.add(nodeList.get(0));
							// nodes.remove(nodeList.get(0));
						}
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

	public static void main(String[] args) {
		oneShard5Nodes();
		System.out.println("-----------------");
		fourNodes4Shards2copies();
		System.out.println("-----------------");
		fourNodes4Shards3copies();
		System.out.println("-----------------");
		threeNodes2Shards2copies();
		System.out.println("-----------------");
		sixNodes3Shards2copies();
	}

	public static void oneShard5Nodes() {
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> nodes = Arrays.asList("node1", "node2", "node3", "node4", "node5");
		List<String> newList = new ArrayList<>(nodes);
		shardNodesMap.put(0, newList);
		List<HashSet<String>> devideShards2Group = devideShards2Group(shardNodesMap, 5, new HashSet<>(nodes));
		// devideShards2Group.stream().map(g -> {return g.stream().forEach(c -> {return
		// "";})}).collect(Collectors.toList()));

		List<String> collect = Stream.iterate(1, i -> i + 1).limit(devideShards2Group.size()).map(i -> {
			return "group " + i + ": " + devideShards2Group.get(i - 1);
		}).collect(Collectors.toList());
		System.out.println(collect);
	}

	public static void fourNodes4Shards2copies() {
		List<String> fakeNodes = Arrays.asList("node1", "node2", "node3", "node4");
		HashSet<String> nodes = new HashSet<>(fakeNodes);
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> shard0FakeList = Arrays.asList("node1", "node4");
		ArrayList<String> shard0 = new ArrayList<>(shard0FakeList);
		shardNodesMap.put(0, shard0);
		List<String> shard1FakeList = Arrays.asList("node2", "node3");
		ArrayList<String> shard1 = new ArrayList<>(shard1FakeList);
		shardNodesMap.put(1, shard1);
		List<String> shard2FakeList = Arrays.asList("node2", "node3");
		ArrayList<String> shard2 = new ArrayList<>(shard2FakeList);
		shardNodesMap.put(2, shard2);
		List<String> shard3FakeList = Arrays.asList("node1", "node4");
		ArrayList<String> shard3 = new ArrayList<>(shard3FakeList);
		shardNodesMap.put(3, shard3);

		devideShards2Group(shardNodesMap, 2, nodes);
	}

	public static void fourNodes4Shards3copies() {
		List<String> fakeNodes = Arrays.asList("node1", "node2", "node3", "node4");
		HashSet<String> nodes = new HashSet<>(fakeNodes);
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> shard0FakeList = Arrays.asList("node1", "node2", "node4");
		ArrayList<String> shard0 = new ArrayList<>(shard0FakeList);
		shardNodesMap.put(0, shard0);
		List<String> shard1FakeList = Arrays.asList("node2", "node1", "node4");
		ArrayList<String> shard1 = new ArrayList<>(shard1FakeList);
		shardNodesMap.put(1, shard1);
		List<String> shard2FakeList = Arrays.asList("node2", "node1", "node3");
		ArrayList<String> shard2 = new ArrayList<>(shard2FakeList);
		shardNodesMap.put(2, shard2);
		List<String> shard3FakeList = Arrays.asList("node2", "node3", "node4");
		ArrayList<String> shard3 = new ArrayList<>(shard3FakeList);
		shardNodesMap.put(3, shard3);

		devideShards2Group(shardNodesMap, 3, nodes);
	}

	public static void threeNodes2Shards2copies() {
		List<String> fakeNodes = Arrays.asList("node1", "node2", "node3");
		HashSet<String> nodes = new HashSet<>(fakeNodes);
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> shard0FakeList = Arrays.asList("node3", "node1");
		ArrayList<String> shard0 = new ArrayList<>(shard0FakeList);
		shardNodesMap.put(0, shard0);
		List<String> shard1FakeList = Arrays.asList("node2", "node3");
		ArrayList<String> shard1 = new ArrayList<>(shard1FakeList);
		shardNodesMap.put(1, shard1);

		devideShards2Group(shardNodesMap, 2, nodes);
	}

	public static void sixNodes3Shards2copies() {
		List<String> fakeNodes = Arrays.asList("node1", "node2", "node3", "node4", "node5", "node6");
		HashSet<String> nodes = new HashSet<>(fakeNodes);
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> shard0FakeList = Arrays.asList("node1", "node4");
		ArrayList<String> shard0 = new ArrayList<>(shard0FakeList);
		shardNodesMap.put(0, shard0);
		List<String> shard1FakeList = Arrays.asList("node2", "node5");
		ArrayList<String> shard1 = new ArrayList<>(shard1FakeList);
		shardNodesMap.put(1, shard1);
		List<String> shard2FakeList = Arrays.asList("node3", "node6");
		ArrayList<String> shard2 = new ArrayList<>(shard2FakeList);
		shardNodesMap.put(2, shard2);

		devideShards2Group(shardNodesMap, 2, nodes);
	}
}
