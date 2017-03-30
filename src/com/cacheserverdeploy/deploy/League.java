//package com.cacheserverdeploy.deploy;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.cacheserverdeploy.deploy.Graph.UpdateOperator;
//
//
//public class League {
//	int client; //id
//	int demand;
//	boolean setServer;
//	int server;
//	List<Integer> nodes;
//	int[][] flow;
//	int[][] cost;
//	Map<Integer, List<TwoTuple<Integer, Integer>>> neighbors;
//	Map<Integer, List<ThreeTuple<String, Integer, Integer>>> offers;
//	
//	
//	public League(int client, int demand){
//		this.client = client;
//		this.demand = demand;
//		this.nodes = new ArrayList<>();
//		this.offers = new HashMap<>();
//		this.neighbors = new HashMap<>();
//	}
//
//	public void initOut(Graph graph){
//		for(int innerNode: nodes){
//			for(int node: graph.getNodes()){
//				if(!this.nodes.contains(node) && graph.bandWidths[innerNode][node] != 0){
//					graph.out[innerNode] += graph.bandWidths[innerNode][node];
//					int neighborID = graph.leagueID.get(node);
//					List<TwoTuple<Integer, Integer>> list = null;
//					if(neighbors.get(neighborID) == null){
//						list = new ArrayList<>();
//					}else{
//						list = neighbors.get(neighborID);
//					}
//					list.add(new TwoTuple<>(innerNode, node));
//					neighbors.put(neighborID, list);
//				}
//			}
//		}
//		graph.out[nodes.get(0)] += demand;
//	}
//	
//	
//	public List<ThreeTuple<String, Integer, Integer>> getOffers(Graph graph){
//		List<ThreeTuple<String, Integer, Integer>> offers = new ArrayList<>();
//		for(int neighborID:neighbors.keySet()){
//			League neighbor = graph.getLeague(neighborID);
//			if(!neighbor.setServer)
//				continue;
//			List<TwoTuple<Integer, Integer>> src_desList = neighbors.get(neighborID);
//			for(TwoTuple<Integer, Integer> des_src: src_desList){
//				int src = des_src.second;
//				int des = des_src.first;
//				int maxOffer = Math.min(graph.maxOffer[src], graph.bandWidths[src][des]);
//				ThreeTuple<String, Integer, Integer> pcfToDes = null;
//				
//				initSubgraph(graph);
//				List<ThreeTuple<String, Integer, Integer>> pcfsToDes = getPaths(nodes.indexOf(des), 0);
//				
//				int need = maxOffer;
//				pcfToDes = pcfsToDes.get(0);
//				boolean noPath = false;
//				for(ThreeTuple<String, Integer, Integer> pcfFromSrc: neighbor.offers.get(src)){
//					int flow = pcfFromSrc.third;
//					if(flow>need){
//						flow = need;
//						noPath = true;
//					}
//					while(flow!=0){
//						if(flow <= pcfToDes.third){
//							offers.add(new ThreeTuple<>(pcfFromSrc.first+"->"+pcfToDes.first,
//									pcfFromSrc.second + pcfToDes.second + graph.unitCosts[src][des],
//									flow));
//							need-=flow;
//							pcfToDes = new ThreeTuple<>(pcfToDes.first, pcfToDes.second, pcfToDes.third-flow);
//							break;
//						}else{
//							offers.add(new ThreeTuple<>(pcfFromSrc.first+"->"+pcfToDes.first,
//									pcfFromSrc.second + pcfToDes.second + graph.unitCosts[src][des],
//									pcfToDes.third));
//							need-=pcfToDes.third;
//							flow -= pcfToDes.third;
//							pcfsToDes.remove(0);
//							if(pcfsToDes.size()==0){
//								noPath = true;
//								break;
//							}
//							pcfToDes = pcfsToDes.get(0);
//						}
//					}
//					if(noPath)
//						break;
//				}
//			}			
//		}
//		return offers;
//	}
//	
//	
//	public TwoTuple<Boolean, Integer> getBestServer(Graph graph){
// 		int need = demand;
// 		int cost = 0;
// 		List<ThreeTuple<String, Integer, Integer>> optiPaths = new ArrayList<>();
//		while(true){
//	 		List<ThreeTuple<String, Integer, Integer>> offers = getOffers(graph);
//	 		if(offers.size()==0){
//	 			break;
//	 		}
//	 		Collections.sort(offers, new Comparator<ThreeTuple<String, Integer, Integer>>() {
//				@Override
//				public int compare(ThreeTuple<String, Integer, Integer> o1, ThreeTuple<String, Integer, Integer> o2) {
//					return o1.second-o2.second;
//				}
//			});
//			ThreeTuple<String, Integer, Integer> opti_pcf = offers.get(0);
//		
//			int real = Math.min(opti_pcf.third, need);
//			graph.updateBandWidth(opti_pcf.first.replaceFirst("->", " "), real, UpdateOperator.MINUS);
//			graph.updateMaxOffer(opti_pcf.first, opti_pcf.third);
//			need -= opti_pcf.third;
//			cost += opti_pcf.second * real;
//			optiPaths.add(new ThreeTuple<>(opti_pcf.first.replaceFirst("->", " "), opti_pcf.second, real));
//			if(need <= 0 || cost>graph.serverCost)
//				break;
//		}
//		
//		//租用流量
//		if(need<=0 && cost<graph.serverCost){
//			setServer = false;
//			for(ThreeTuple<String, Integer, Integer> optiPath: optiPaths){
//				graph.plusNodeFlow(optiPath);
//				System.out.println("path:"+optiPath.first+"->client:"+client+" cost:"+optiPath.second+" flow:"+optiPath.third);
//			}
//			offers.clear();
//			return new TwoTuple<>(false, -1);
//		}else{
//		//设立服务器
//			setServer = true;
//			int maxInnerCost = Integer.MAX_VALUE;
//			TwoTuple<Boolean, Integer> result = null;
//			for(int node:nodes){
//				result = setNodeAsServer(node, graph);
//				if(result.first && result.second-graph.nodeCost[node] < maxInnerCost){
//					server = node;
//					maxInnerCost = result.second-graph.nodeCost[node];
//				}
//			}
//			System.out.println(server+"->client:"+client+" cost: 0"+" flow："+demand);
//			return new TwoTuple<>(true, server);
//		}
//	}
//	
//	
//	public TwoTuple<Boolean, Integer> setNodeAsServer(int node, Graph graph){
//		int innerCost = 0;
//		int need = demand;
//		List<ThreeTuple<String, Integer, Integer>> pcfs = getPaths(nodes.indexOf(node), 0);
//		for(ThreeTuple<String, Integer, Integer> pcf: pcfs){
//			innerCost += pcf.second *  Math.min(pcf.third, need);
//			need -= pcf.third;
//			if(need<=0)
//				break;
//		}
//		if(need<=0){
//			return new TwoTuple<>(true, innerCost);
//		}else{
//			return new TwoTuple<>(false, innerCost);
//		}
//	}
//	
//	
//	public void initMaxoffer(Graph graph){
//		List<ThreeTuple<String, Integer, Integer>> list = null;
//		for(int innerNode: nodes){			
//			int totalOffer = 0;
//			initSubgraph(graph);
//			list = getPaths(nodes.indexOf(server), nodes.indexOf(innerNode));
//			for(ThreeTuple<String, Integer, Integer> pcf:list)
//				totalOffer += pcf.third;
//			graph.maxOffer[innerNode] = Math.min(totalOffer, graph.out[innerNode]);
//			offers.put(innerNode, list);
//		}
//	}
//	
//	
//	public void initSubgraph(Graph graph){
//		this.flow = new int[nodes.size()][nodes.size()];
//		this.cost = new int[nodes.size()][nodes.size()];
//		for(int i=0; i<nodes.size(); i++){
//			for(int j=0; j<nodes.size(); j++){
//				flow[i][j] = graph.bandWidths[nodes.get(i)][nodes.get(j)];
//				cost[i][j] = graph.unitCosts[nodes.get(i)][nodes.get(j)];
//			}
//		}
//	}
//	
//
//	
//	
//	public void updateFlow(String path, int increment, UpdateOperator operator){
//		int src, des;
//		String[] pathNodesStr = path.split(" ");
//		for(int ii=0; ii<pathNodesStr.length-1; ii++){
//			src = nodes.indexOf(Integer.parseInt(pathNodesStr[ii]));//起始节点
//			des = nodes.indexOf(Integer.parseInt(pathNodesStr[ii+1])); //终止节点
//			if(operator == UpdateOperator.MINUS){
//				flow[src][des] = flow[src][des] - increment;
//			}else{
//				flow[src][des] = flow[src][des] + increment;
//			}
//		}
//	}
//
//	
//	
//	
//	
//	public List<ThreeTuple<String, Integer, Integer>> getPaths(int src){
//		List<ThreeTuple<String, Integer, Integer>> list = new ArrayList<>();
//		while(true){
//			ThreeTuple<String, Integer, Integer> pcf = getPath(src, );
//			if(pcf.second == Graph.MAX_VALUE){
//				break;
//			}				
//			list.add(pcf);
//			if(pcf.third == Graph.MAX_VALUE)
//				break;
//			updateFlow(pcf.first, pcf.third, UpdateOperator.MINUS);
//		}
//		return list;
//	}
//	
//	private List<ThreeTuple<String, Integer, Integer>> getPath(int src, List<Integer> des){
//    	int[] costs = new int[nodes.size()];
//    	int[] flows = new int[nodes.size()];
//    	String[] shortPaths = new String[nodes.size()];
//    	//dijkstra方法计算结果是 src 到所有顶点的最短距离
//    	dijkstra(src, shortPaths, costs, flows);
//    	List<ThreeTuple<String, Integer, Integer>> list = new ArrayList<>();
//    	for(int node:des){
//    		if(costs[node] != Graph.MAX_VALUE)
//    			list.add(new ThreeTuple<String, Integer, Integer>(shortPaths[node], costs[node], flows[node]));
//    	}
//    	return list;
//	}
//	
//	
//    private void dijkstra(int src, String[] shortPaths, int[] unitCosts, int[] flows){
//    	int nodesNum = this.nodes.size();
//    	int[][] costs = new int[nodesNum][nodesNum];
//    	int[][] maxFlow = new int[nodesNum][nodesNum];
//    	//初始化图中单位租用费用信息和最大流量信息。不存在的链路单位租用费用设置为最大值，最大流量设置为0
//    	for(int i=0; i<nodesNum; i++){
//    		for(int j=0; j<nodesNum; j++){
//        		maxFlow[i][j] = flow[i][j];
//    			if(this.cost[i][j] == 0 || this.flow[i][j] == 0){//没有对应的边
//    				costs[i][j] = Graph.MAX_VALUE;
//    			}else{
//    				costs[i][j]  = this.cost[i][j];
//    			}
//    		}
//    	}
//    	
//    	boolean isVisited[] = new boolean[nodesNum];//标记节点最短距离是否求出
//    	//初始化节点 src 到其他节点的距离为无穷大
//    	for(int i=0; i<unitCosts.length; i++){
//    		unitCosts[i] = Integer.MAX_VALUE;
//    		shortPaths[i] = nodes.get(src)+" "+nodes.get(i);
//    	}
//    	
//    	isVisited[src] = true;
//    	unitCosts[src] = 0;
//    	flows[src] = Graph.MAX_VALUE;
//    	
//    	for(int count=1; count<nodesNum; count++){
//    		int minDis = Integer.MAX_VALUE;
//    		int nextNode = -1;
//    		int flow = 0;
//    		for(int i=0; i<nodesNum; i++){
//				if(! isVisited[i] && 
//						(costs[src][i] < minDis || (costs[src][i] == minDis && maxFlow[src][i] > flow))){
//					minDis = costs[src][i];
//					nextNode = i;
//					flow = maxFlow[src][i];
//				}
//    		}
//    		unitCosts[nextNode] = minDis;
//    		flows[nextNode] = flow;
//    		isVisited[nextNode] = true;
//    		//松弛操作
//    		for(int i=0; i<nodesNum; i++){
//				if(!isVisited[i] && costs[src][nextNode]+costs[nextNode][i]< costs[src][i]){
//					costs[src][i] = costs[src][nextNode] + costs[nextNode][i];
//					maxFlow[src][i] = Math.min(maxFlow[src][nextNode], maxFlow[nextNode][i]);
//					shortPaths[i] = shortPaths[nextNode] +" "+nodes.get(i);
//				}
//    		}
//    	}
//    }
//	
//}
