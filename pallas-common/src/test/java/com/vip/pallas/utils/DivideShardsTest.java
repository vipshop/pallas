package com.vip.pallas.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class DivideShardsTest {

	@Test
	public void oneShard5Nodes() {
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> nodes = Arrays.asList("node1", "node2", "node3", "node4", "node5");
		List<String> newList = new ArrayList<>(nodes);
		shardNodesMap.put(0, newList);
		List<HashSet<String>> shards2Group = DivideShards.divideShards2Group(shardNodesMap, 5, new HashSet<>(nodes));
		// DivideShards.divideShards2Group.stream().map(g -> {return g.stream().forEach(c -> {return
		// "";})}).collect(Collectors.toList()));
		Map<String, String> nodesInfo = new HashMap();
		nodesInfo.put("node1", "192.168.200.210");
		nodesInfo.put("node2", "192.168.200.212");
		nodesInfo.put("node3", "192.168.200.213");
		nodesInfo.put("node4", "192.168.200.214");
		nodesInfo.put("node5", "192.168.200.215");

		List<List<String>> groupByNodeIdList = shards2Group.stream()
				.map(set -> set.stream().map(ip -> nodesInfo.get(ip)).collect(Collectors.toList()))
				.collect(Collectors.toList());

		assertThat(groupByNodeIdList.toString()).isEqualTo(
				"[[192.168.200.210], [192.168.200.212], [192.168.200.213], [192.168.200.214], [192.168.200.215]]");
	}

	@Test
	public void fourNodes4Shards2copies() {
		List<String> fakeNodes = Arrays.asList("node3", "node2", "node4", "node1");
		HashSet<String> nodes = new HashSet<>(fakeNodes);
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();

		List<String> shard2FakeList = Arrays.asList("node2", "node3");
		ArrayList<String> shard2 = new ArrayList<>(shard2FakeList);
		shardNodesMap.put(2, shard2);
		List<String> shard3FakeList = Arrays.asList("node4", "node1");
		ArrayList<String> shard3 = new ArrayList<>(shard3FakeList);
		shardNodesMap.put(3, shard3);

		List<String> shard0FakeList = Arrays.asList("node1", "node4");
		ArrayList<String> shard0 = new ArrayList<>(shard0FakeList);
		shardNodesMap.put(0, shard0);
		List<String> shard1FakeList = Arrays.asList("node3", "node2");
		ArrayList<String> shard1 = new ArrayList<>(shard1FakeList);
		shardNodesMap.put(1, shard1);

		Set<Entry<Integer, List<String>>> entrySet = shardNodesMap.entrySet();
		List<Entry<Integer, List<String>>> entryList = new ArrayList<>(entrySet);
		Collections.sort(entryList, (o1, o2) -> {
			Collections.sort(o1.getValue());
			Collections.sort(o2.getValue());
			return o1.getKey().compareTo(o2.getKey());
		});

		assertThat(entryList.toString())
				.isEqualTo("[0=[node1, node4], 1=[node2, node3], 2=[node2, node3], 3=[node1, node4]]");

		List<HashSet<String>> shards2Group = DivideShards.divideShards2Group(shardNodesMap, 2, nodes);
		Map<String, String> nodesInfo = new HashMap();
		nodesInfo.put("node1", "192.168.200.210");
		nodesInfo.put("node2", "192.168.200.212");
		nodesInfo.put("node3", "192.168.200.213");
		nodesInfo.put("node4", "192.168.200.214");
		nodesInfo.put("node5", "192.168.200.215");


		assertThat(shards2Group.stream().collect(Collectors
				.toMap(list -> list.stream().map(ip -> nodesInfo.get(ip)).collect(Collectors.joining(",")), v -> v))
				.toString())
						.isEqualTo(
								"{192.168.200.212,192.168.200.210=[node2, node1], 192.168.200.214,192.168.200.213=[node4, node3]}");
		;

	}

	@Test
	public void fourNodes4Shards3copies() {
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

		DivideShards.divideShards2Group(shardNodesMap, 3, nodes);
	}

	@Test
	public void threeNodes2Shards2copies() {
		List<String> fakeNodes = Arrays.asList("node1", "node2", "node3");
		HashSet<String> nodes = new HashSet<>(fakeNodes);
		Map<Integer, List<String>> shardNodesMap = new HashMap<>();
		List<String> shard0FakeList = Arrays.asList("node3", "node1");
		ArrayList<String> shard0 = new ArrayList<>(shard0FakeList);
		shardNodesMap.put(0, shard0);
		List<String> shard1FakeList = Arrays.asList("node2", "node3");
		ArrayList<String> shard1 = new ArrayList<>(shard1FakeList);
		shardNodesMap.put(1, shard1);

		DivideShards.divideShards2Group(shardNodesMap, 2, nodes);
	}

	@Test
	public void sixNodes3Shards2copies() {
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

		DivideShards.divideShards2Group(shardNodesMap, 2, nodes);
	}
}
