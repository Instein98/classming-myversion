package com.classming.cluster;

import com.classming.Vector.LevenshteinDistance;

import java.util.*;

public class HACluster {

    private Set<Point> pointSet = new HashSet<>();
    private Queue<Segment> segmentPQ = new PriorityQueue<Segment>(new Comparator<Segment>() {
        @Override
        public int compare(Segment o1, Segment o2) {
            if(o1.distance > o2.distance) {
                return 1;
            }else if (o1.distance < o2.distance) {
                return -1;
            }else{
                return 0;
            }
        }
    });

    private static int IsolationDistance = 2;
    private Map<InstructionFlow, Integer> stateOrder = new HashMap<>();



    private class Point {
        List<InstructionFlow> stateCluster = new ArrayList<>();

        void addState(InstructionFlow state) {
            stateCluster.add(state);
        }

        double distanceTo(Point point) {
            double total = 0;
            for (int i = 0; i < this.stateCluster.size(); i++) {
                for (int j = 0; j < point.stateCluster.size(); j++) {
                    total += statesDistance(this.stateCluster.get(i), point.stateCluster.get(j));
                }
            }
            return total/(this.stateCluster.size() * point.stateCluster.size() * 1.0);
        }

        Point mergeTo(Point point) {
            Point newPoint = new Point();
            for (int i = 0; i < this.stateCluster.size(); i++) {
                newPoint.addState(this.stateCluster.get(i));
            }
            for (int i = 0; i < point.stateCluster.size(); i++) {
                newPoint.addState(point.stateCluster.get(i));
            }
            return newPoint;
        }
    }

    private class Segment {
        Point begin;
        Point end;
        double distance;

        Segment(Point begin, Point end, double distance) {
            this.begin = begin;
            this.end = end;
            this.distance = distance;
        }
    }




    public void testParameter (List<InstructionFlow> states, int IsolationDistance) {
        this.IsolationDistance = IsolationDistance;
        cluster(states);
    }

    public List<Double> cluster(List<InstructionFlow> states){
        initialPointSet(states);
        initialSegment();

        while(true) {
            Segment shortestSegment = segmentPQ.poll();
            if (shortestSegment != null && shortestSegment.distance < IsolationDistance){
                if(pointSet.contains(shortestSegment.begin) && pointSet.contains(shortestSegment.end)){

//                    System.out.println("shortest: " + shortestSegment.distance);

                    Point newPoint = shortestSegment.begin.mergeTo(shortestSegment.end);
                    pointSet.remove(shortestSegment.begin);
                    pointSet.remove(shortestSegment.end);
                    pointSet.add(newPoint);
                    pointToPointsDistance(newPoint);

                }
            }else {
                break;
            }
        }

        Double[] distribution = new Double[states.size()];
        double sum = 0;
        for(Point point: pointSet){
            sum += 1.0/point.stateCluster.size();
        }
        for(Point point: pointSet) {
            for (int i = 0; i < point.stateCluster.size(); i++) {
                distribution[stateOrder.get(point.stateCluster.get(i))] =
                        1.0 / point.stateCluster.size() / point.stateCluster.size() / sum;
            }
        }
//        System.out.println(Arrays.asList(distribution).toString());
        printClusterInfo();
        return Arrays.asList(distribution);
    }


    private void initialPointSet(List<InstructionFlow> states) {
        for (int i = 0; i < states.size(); i++) {
            Point point = new Point();
            point.addState(states.get(i));
            pointSet.add(point);
            stateOrder.put(states.get(i), i);
        }
    }

    private void initialSegment() {
        for(Point begin: pointSet) {
            for(Point end: pointSet){
                if(begin != end) {
                    segmentPQ.add(new Segment(begin, end, begin.distanceTo(end)));
//                    System.out.println("initial segment: " + begin.distanceTo(end));
                }
            }
        }
    }

    private void pointToPointsDistance(Point point) {
        for(Point target: pointSet) {
            if(point != target) {
                segmentPQ.add(new Segment(point, target, point.distanceTo(target)));
//                System.out.println("distance between clusters: " + point.distanceTo(target));
            }
        }
    }

    private double statesDistance(InstructionFlow aMethodLiveCode, InstructionFlow bMethodLiveCode) {
//        System.out.println("stateDistance: " + LevenshteinDistance.computeLevenshteinDistance(aMethodLiveCode, bMethodLiveCode));
        return LevenshteinDistance.computeLevenshteinDistance(aMethodLiveCode.stateInstruction, bMethodLiveCode.stateInstruction);
    }

    private void printClusterInfo() {
        System.out.println("cluster number: " + pointSet.size());
        for (Point point: pointSet) {
            System.out.print(" " + point.stateCluster.size() + " ");
        }
        System.out.println();
    }
}

