package com.cacheserverdeploy.deploy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.cacheserverdeploy.deploy.Graph;
import com.cacheserverdeploy.deploy.Graph.UpdateOperator;


public class Deploy{
	private static int lineNum = 0;//网络图读取过程中标记行数
	final  static long TIME_LIMIT = 88*1000;
	
	/**
	 * 
	 * 
	 * @param graphContent
	 * @return
	 */
	public static String[] deployServer(String[] graphContent){
        Graph graph = null;
        graph = readProblemLine(graphContent);
    	readEdges(graphContent, graph);
    	readClients(graphContent, graph);
    	if(graph.nodesNum>400){
    		StringBuffer sb = new StringBuffer();
    		int cost = graph.getServers().size() * graph.serverCost;
	    	for(CLD cld: graph.clds){
	    		sb.append("\n"+cld.linked+" "+cld.client+" "+cld.demand);
	    		System.out.println(cld.linked+" "+cld.client+" cost: 0"+" flow:"+cld.demand);
	    	}
	    	System.out.println("cost:"+cost);
	    	return new String[]{graph.clds.size()+"", sb.toString()};
    	}
    	graph.sortClients();
//    	for(int randomServer: getRandomServers(graph.nodesNum,(int)(graph.nodesNum*0.02))){
//    		graph.isServer[randomServer] = true;
//    	}
    	int pathNum = 0;
    	Map<Integer, List<PCF>> clientPaths = null;
		StringBuffer sb = new StringBuffer();
		boolean changed=false;
    	while(true){
    		clientPaths = graph.getBestServers();
	    	changed = graph.update();
	    	System.out.println(graph.printServers());
	    	if(changed){
	    		for(int client: clientPaths.keySet()){
		    		for(PCF optiPath: clientPaths.get(client)){
			    		graph.updateBandWidth(optiPath.path, optiPath.flow, UpdateOperator.PLUS);
			    	}
	    		}
	    	}else{
		    	int cost = graph.getServers().size() * graph.serverCost;
		    	for(int client: clientPaths.keySet()){
			    	for(PCF optiPath: clientPaths.get(client)){
			    		sb.append("\n"+optiPath.path+" "+client+" "+optiPath.flow);
			    		pathNum++;
			    		System.out.println(optiPath.path+" "+client+" cost:"+optiPath.cost+" flow:"+optiPath.flow);
			    		cost += optiPath.cost * optiPath.flow;
			    	}
		    	}
		    	System.out.println("cost:"+cost);
		    	break;
	    	}
    	}
    	if(!changed)
    		return new String[]{pathNum+"", sb.toString()};
    	return new String[]{"NA"};
    }
	
	
	public static Set<Integer> getRandomServers(int range, int num){
		Set<Integer> servers = new HashSet<>();
		Random random = new Random();
		while(num != 0){
			int server = random.nextInt(range);
			if(!servers.contains(server)){
				servers.add(server);
				num--;
			}
		}
		return servers;
	}
    /**
     * 读取网络节点数量、消费节点数量、服务器部署成本、网络链路数量。基于此信息初始化 Graph
     * 
     * @param graphContent
     * @return 返回初始化之后的Graph对象
     */
    public static Graph readProblemLine(String[] graphContent){
    	String problemLine = graphContent[lineNum++];
    	String[] problemLineStrs = problemLine.split(" ");
    	lineNum++; //跳过空行
    	String costLine = graphContent[lineNum++];
    	return new Graph(Integer.parseInt(problemLineStrs[0]), // 网络节点数量
    			Integer.parseInt(problemLineStrs[2]), //消费节点数量
    			Integer.parseInt(costLine), //服务器部署成本
    			Integer.parseInt(problemLineStrs[1])); //网络链路数量
    }
    
    /**
     * 逐行读取链路信息
     * 
     * @param graphContent
     * @param graph
     */
    public static void readEdges(String[] graphContent, Graph graph){
    	String line = null;
    	lineNum++;//跳过空行
    	while(! (line = graphContent[lineNum++]).equals("")){
    		String[] lineStrs = line.split(" ");
    		graph.addEdge(Integer.parseInt(lineStrs[0]),//起始节点
    				Integer.parseInt(lineStrs[1]),//终止节点
    				Integer.parseInt(lineStrs[2]), //链路总带宽
    				Integer.parseInt(lineStrs[3]));//单位带宽租用费用    		
    	}
    }
    
    /**
     * 逐行读取消费节点信息
     * 
     * @param graphContent
     * @param graph
     */
    public static void readClients(String[] graphContent, Graph graph){
    	String line = null;
    	while(lineNum < graphContent.length){
    		line = graphContent[lineNum++];
    		String[] lineStrs = line.split(" ");
    		graph.addClient(Integer.parseInt(lineStrs[0]),//消费节点
    				Integer.parseInt(lineStrs[1]), //相邻的网络节点
    				Integer.parseInt(lineStrs[2]));//带宽需求
    	}
    }
    
  
   
}
