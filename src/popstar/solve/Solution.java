package popstar.solve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import popstar.util.Util;

public class Solution {
	private int curMaxScore = 0;
	private int first3StepCount = 0;
	private Layout curMaxScoreLayout;
	private Layout rootLayout;
	private ArrayList<Node> curMaxPath = new ArrayList<>();
	private HashMap<Layout, Integer> curTotalScore = new HashMap<>();
	private HashMap<Layout, ArrayList<Node>> curTotalPath = new HashMap<>();
	private HashSet<Layout> layoutSet = new HashSet<>();
	
	public Layout getCurMaxScoreLayout(){
		return curMaxScoreLayout;
	}
	
	public Solution(Layout layout){
		rootLayout = layout;
		layoutSet.add(layout);
		curTotalScore.put(layout, 0);
		InitFirst3Step(rootLayout, 0);
		getMaxScore();
		System.out.println("score: " + curMaxScore);
		System.out.println("path: " + curMaxPath);
	}
	
	private void InitFirst3Step(Layout ancestor, int layer){
		if(layer == 3 || first3StepCount >= 1000){
			first3StepCount++;
			return;
		}
		layoutSet.remove(ancestor);
		List<ArrayList<Node>> validConnectList = ancestor.getValidConnect();
		if(validConnectList.size() == 0){
			if(curTotalScore.get(ancestor) + Util.getBonusScore(ancestor.getCurTotalSingleCount()) > curMaxScore){
				curMaxScore = curTotalScore.get(ancestor) + Util.getBonusScore(ancestor.getCurTotalSingleCount());
				curMaxPath = curTotalPath.get(ancestor);
				curMaxScoreLayout = ancestor;
			}
			return;
		}
		for(ArrayList<Node> nodeList : validConnectList){
			int[][] nextInput = Util.removeConnect(ancestor.getInput(), nodeList, ancestor.getLength(), ancestor.getWidth());
			Layout layout = new Layout(nextInput);
			layoutSet.add(layout);
			layout.setBeforeLayout(ancestor);
			curTotalScore.put(layout, curTotalScore.get(ancestor) + Util.getConnectScore(nodeList));
			ArrayList<Node> pathLayout = new ArrayList<>();
			if (curTotalPath.containsKey(ancestor)){
				pathLayout.addAll(curTotalPath.get(ancestor));
			}
			pathLayout.add(nodeList.get(0));
			curTotalPath.put(layout, pathLayout);
			InitFirst3Step(layout, layer + 1);
		}
		return;
		
	}
	
	public void getMaxScore(){
		for(Layout layout : layoutSet){
			Layout nextLayout = layout;
			List<ArrayList<Node>> validConnectList = nextLayout.getValidConnect();
			while(nextLayout.getValidConnect().size() != 0){
				double inLoopMaxLayoutScore = 0 - Double.MAX_VALUE;
				Layout inLoopMaxLayout = null;
				ArrayList<Node> inLoopConnectNodeList = null;
				ArrayList<Node> inLoopMaxPathLayout = new ArrayList<>();
				
				for(ArrayList<Node> nodeList : validConnectList){
					int[][] nextInput = Util.removeConnect(nextLayout.getInput(), nodeList, nextLayout.getLength(), nextLayout.getWidth());
					Layout inLoopNextLayout = new Layout(nextInput);
					if(inLoopNextLayout.getCurLayoutScore() > inLoopMaxLayoutScore){
						inLoopMaxLayout = inLoopNextLayout;
						inLoopMaxLayoutScore = inLoopNextLayout.getCurLayoutScore();
						inLoopConnectNodeList = nodeList;
					}
				}
				if (curTotalPath.containsKey(nextLayout)){
					inLoopMaxPathLayout.addAll(curTotalPath.get(nextLayout));
				}
				inLoopMaxPathLayout.add(inLoopConnectNodeList.get(0));
				curTotalPath.put(inLoopMaxLayout, inLoopMaxPathLayout);
				curTotalPath.remove(nextLayout);
				curTotalScore.put(inLoopMaxLayout, curTotalScore.get(nextLayout) + Util.getConnectScore(inLoopConnectNodeList));
				curTotalScore.remove(nextLayout);
				inLoopMaxLayout.setBeforeLayout(nextLayout);
				if(inLoopMaxLayoutScore == Double.MAX_VALUE && inLoopMaxLayout.getMaxConnect().size() != 0){
					ArrayList<Node> nextMaxConnectPath = new ArrayList<>();
					int[][] nextInput = Util.removeConnect(inLoopMaxLayout.getInput(), inLoopMaxLayout.getMaxConnect(), inLoopMaxLayout.getLength(), inLoopMaxLayout.getWidth());
					Layout nextMaxConnectLayout = new Layout(nextInput);
					nextMaxConnectLayout.setBeforeLayout(inLoopMaxLayout);
					nextMaxConnectPath.addAll(inLoopMaxPathLayout);
					nextMaxConnectPath.add(inLoopMaxLayout.getMaxConnect().get(0));
					curTotalPath.put(nextMaxConnectLayout, nextMaxConnectPath);
					curTotalPath.remove(inLoopMaxLayout);
					curTotalScore.put(nextMaxConnectLayout, curTotalScore.get(inLoopMaxLayout) + Util.getConnectScore(inLoopMaxLayout.getMaxConnect()));
					curTotalScore.remove(inLoopMaxLayout);
					inLoopMaxLayout = nextMaxConnectLayout;
				}
				nextLayout = inLoopMaxLayout;
				validConnectList = nextLayout.getValidConnect();
			}
			if(curTotalScore.get(nextLayout) + Util.getBonusScore(nextLayout.getCurTotalSingleCount()) > curMaxScore){
				curMaxScore = curTotalScore.get(nextLayout) + Util.getBonusScore(nextLayout.getCurTotalSingleCount());
				curMaxPath = curTotalPath.get(nextLayout);
				curMaxScoreLayout = nextLayout;
			}
		}
	}

	public static void main(String[] args){
		Random ra =new Random();
		int score = 0;
		int count = 0;
		Solution solution;
		ArrayList<Integer> scoreList = new ArrayList<>();
		int[][] input = new int[10][10];
		for(int k = 0; k < 100; k++){
			for(int i = 0; i < 10; i++){
				for(int j = 0; j < 10; j++){
					input[i][j] = ra.nextInt(5) + 1;
				}
			}
			solution = new Solution(new Layout(input));
			scoreList.add(solution.curMaxScore);
			score += solution.curMaxScore;
			count++;
		}
		Collections.sort(scoreList);
		
		System.out.println("average score: " + score / count);
	}

}
