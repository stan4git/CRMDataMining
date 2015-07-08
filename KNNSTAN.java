package task11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import org.omg.CORBA.TRANSACTION_MODE;


//1,1,11,31,121,21,  0.9139784946236559
//11,1,11,31,31,111,  0.9193548387096774
//11,1,11,41,21,131,  0.9247311827956989
//11,1,11,51,21,121,  0.9301075268817204
public class KNNSTAN {
	private static double maxVacation = Integer.MIN_VALUE;
	private static double minVacation = Integer.MAX_VALUE;
	private static double maxeCredit = Integer.MIN_VALUE;
	private static double mineCredit = Integer.MAX_VALUE;
	private static double maxSalary = Integer.MIN_VALUE;
	private static double minSalary = Integer.MAX_VALUE;
	private static double maxProperty = Integer.MIN_VALUE;
	private static double minProperty = Integer.MAX_VALUE;
	private static ArrayList<Node> trainingSet;
	private static ArrayList<Node> testSet;
	private static double[] weight;
	private static double maxCorrectness;
	private final static double INCREMENT = 1;
	private final static int MAX_WEIGHT = 200;
	private final static double TARGET_CORRECTNESS = 0.95;
	private final static int FOLD = 10;
	
	
	public static void main(String[] args) throws Exception {
		trainingSet = parseData(loadData("trainProdSelection.arff", true));
		testSet = parseData(loadData("testProdSelection.arff", false));
		weight = new double[] {11, 1, 11, 51, 21, 121};
		for (Node node : testSet) {
			System.out.println(predict(node, -1, -1, weight));
		}
//		shuffle();
//		double[] currentWeight = new double[] {11, 1, 11, 51, 21, 121};
//		adjustWeight(currentWeight,0);
//		System.out.println(maxCorrectness);
//		System.out.println(weight[0] + "," + weight[1] + "," + weight[2] + "," + weight[3] + "," + weight[4] + "," + weight[5]);
	}
	
	 
	private static double calculateTrainingSetCorrectness(double[] currentWeight) throws Exception {
		int testSetSize = FOLD;
		int n = 0, start = 0, end = 0;
		double correct = 0, wrong = 0;
		double correctness = 0;
		while (end < trainingSet.size()) {
			start = n * testSetSize;
			end = (n + 1) * testSetSize;
			if (end > trainingSet.size()) {
				end = trainingSet.size();
			}
			for (int i = start; i < end; i++) {
				if (predict(trainingSet.get(i), start, end, currentWeight).equals(trainingSet.get(i).Label)) {
					correct++;
				} else {
					wrong++;
				}
			}
			n++;
		}
		correctness = correct / (wrong + correct);
		return correctness;
	}
	
	
	private static void shuffle() {
		Random rd = new Random();
		for (int i = 0; i < trainingSet.size(); i++) {
			Node temp = new Node(trainingSet.get(i));
			int target = rd.nextInt(trainingSet.size());
			trainingSet.set(i, trainingSet.get(target));
			trainingSet.set(target, temp);
		}
		return;
	}

	
	private static void adjustWeight(double[] currentWeight, int weightIndex) throws Exception {
		if (maxCorrectness > TARGET_CORRECTNESS || 
				weightIndex == currentWeight.length) {
			return;
		}
		
		double temp = currentWeight[weightIndex];
		
		while (currentWeight[weightIndex] <= MAX_WEIGHT) {
			double currentCorrectness = calculateTrainingSetCorrectness(currentWeight);
			if (currentCorrectness > maxCorrectness) {
				System.out.println(currentWeight[0] + ", " + currentWeight[1] + ", " + currentWeight[2] + ", " +
							currentWeight[3] + ", " + currentWeight[4] + ", " + currentWeight[5] +
							",  " + currentCorrectness);
				maxCorrectness = currentCorrectness;
				weight = Arrays.copyOf(currentWeight, currentWeight.length);
			}
			adjustWeight(currentWeight, weightIndex + 1);
			currentWeight[weightIndex] += INCREMENT;
		}
		currentWeight[weightIndex] = temp;
		return;
	}
	
	
	private static PriorityQueue<ResultType> sim(Node input, int testStart, int testEnd, double[] weight) throws Exception {
		PriorityQueue<ResultType> top3 = new PriorityQueue<ResultType>(trainingSet.size(), new maxComparator());
		for (int i = 0; i < trainingSet.size(); i++) {
			if (testStart <= i && i < testEnd) {
				continue;
			}
			Node base = input;
			Node current = trainingSet.get(i);
			double v1 = (base.CustomerType.equals(current.CustomerType)) ? 0 : 1;
			double v2 = (base.LifeStyle.equals(current.LifeStyle)) ? 0 : 1;
			double v3 = base.Vacation - current.Vacation;
			double v4 = base.eCredit - current.eCredit;
			double v5 = base.Salary - current.Salary;
			double v6 = base.Property - current.Property;
			ResultType node = new ResultType(1 / Math.sqrt(weight[0] * v1 + weight[1] * v2 + weight[2] * v3 * v3
					+ weight[3] * v4 * v4 + weight[4] * v5 * v5 + weight[5] * v6 * v6), current.Label);
			top3.offer(node);
		}
		return top3;
	}
	
	
	private static String predict(Node input, int testStart, int testEnd, double[] weight) throws Exception {
		PriorityQueue<ResultType> top3 = sim(input, testStart, testEnd, weight);
		ArrayList<ResultType> result = new ArrayList<ResultType>(Arrays.asList(new ResultType(0, "C1"), 
				new ResultType(0, "C2"), new ResultType(0, "C3"), new ResultType(0, "C4"), new ResultType(0, "C5")));
		for (int k = 0; k < 3; k++) {
			ResultType node = top3.poll();
			switch (node.Type) {
				case "C1" : result.get(0).Distance += node.Distance; break;
				case "C2" : result.get(1).Distance += node.Distance; break;
				case "C3" : result.get(2).Distance += node.Distance; break;
				case "C4" : result.get(3).Distance += node.Distance; break;
				case "C5" : result.get(4).Distance += node.Distance; break;
				default : throw new Exception("Error"); 
			}
		}
		Collections.sort(result, new nodeComparator());		//get the max class
		return result.get(4).Type;
	}
	
	
	private static ArrayList<String> loadData(String file, boolean isTrainingData) throws IOException {
		InputStream fileStream = new FileInputStream(file);
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(fileStream, Charset.forName("UTF-8")));
		String line = null;
		ArrayList<String> strTrainingData = new ArrayList<String>();
		boolean start = false;
		
		while ((line = buffer.readLine()) != null) {		// load data into memory and get the max/min value of each field
			if (line.equals("@data")) {
				start = true;
				continue;
			}
			if (start) {
				if (isTrainingData) {
					setMax(line);
				}
				strTrainingData.add(line);
			}
		}
		buffer.close();
		return strTrainingData;
	}
	

	private static ArrayList<Node> parseData(ArrayList<String> dataSet) {
		ArrayList<Node> targetSet = new ArrayList<Node>();
		for (String row : dataSet) {
			targetSet.add(getDataObject(row));
		}
		return targetSet;
	}

	
	private static void setMax(String line) {
		String[] split = line.split(",");
		maxVacation = Math.max(maxVacation, Double.parseDouble(split[2]));
		minVacation = Math.min(minVacation, Double.parseDouble(split[2]));
		maxeCredit = Math.max(maxeCredit, Double.parseDouble(split[3]));
		mineCredit = Math.min(mineCredit, Double.parseDouble(split[3]));
		maxSalary = Math.max(maxSalary, Double.parseDouble(split[4]));
		minSalary = Math.min(minSalary, Double.parseDouble(split[4]));
		maxProperty = Math.max(maxProperty, Double.parseDouble(split[5]));
		minProperty = Math.min(minProperty, Double.parseDouble(split[5]));
		return;
	}

	
	private static Node getDataObject(String line) {
		Node node = new Node();
		if (line == null || line.length() == 0) {
			return node;
		}
		String[] split = line.split(",");
		node.CustomerType = split[0].toLowerCase();
		node.LifeStyle = split[1].toLowerCase();
		node.Vacation = nomalize(maxVacation, minVacation, Double.parseDouble(split[2]));
		node.eCredit = nomalize(maxeCredit, mineCredit, Double.parseDouble(split[3]));
		node.Salary = nomalize(maxSalary, minSalary, Double.parseDouble(split[4]));
		node.Property = nomalize(maxProperty, minProperty, Double.parseDouble(split[5]));
		if (split.length == 7) {
			node.Label = split[6];
		}
		return node;
	}
	
	
	private static double nomalize(double max, double min, double val) {
		return (val - min) / (max - min);
	}
	
	
	private static class nodeComparator implements Comparator<ResultType> {
		@Override
		public int compare(ResultType arg0, ResultType arg1) {
			return (arg0.Distance < arg1.Distance) ? -1 : 1;
		}
	}
	
	
	private static class maxComparator implements Comparator<ResultType> {
		@Override
		public int compare(ResultType arg0, ResultType arg1) {
			return (arg0.Distance < arg1.Distance) ? 1 : -1;
		}
	}
}
