package com.ruleengine;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class RuleEngine {


    private List<Rule> rules = new ArrayList<>();
    private List<Matcher> matchers = new ArrayList<>();
    private String ruleIdPrefix = "Rule";
    private int ruleIdSeq = 0;
    private List<Worker> workers = new ArrayList<Worker>();
    private ArrayBlockingQueue<BidRequest> queue;
    private boolean isDone = false;

    static int NUM_WORKERS = 5;
    static int QUEUE_SIZE = 1000;


    public void start () {
        queue = new ArrayBlockingQueue<BidRequest>(QUEUE_SIZE);

        for (int i=0; i<NUM_WORKERS; i++) {
            matchers.add(new GenderMatcher());
            matchers.add(new GeoCountryMatcher());
            matchers.add(new AppCategoryMatcher());
            matchers.add(new DeviceModelMatcher());
            matchers.add(new HourOfDayMatcher());
            matchers.add(new LocationMatcher());

            Worker worker = new Worker(rules, matchers);
            worker.start();
            workers.add(worker);
        }
    }

    public void stop () throws InterruptedException {
        isDone = true;
        for (Worker w : workers) {
            while (w.isAlive()) {
              Thread.sleep(100);
              w.interrupt();
            }
        }
    }

    // List of Matchers
    class GenderMatcher implements Matcher {
        public boolean match(Rule r, BidRequest request) {
           return r.getUserGender().equals(request.getUserGender());
        }
    }

    class GeoCountryMatcher implements Matcher {
        public boolean match(Rule r, BidRequest request) {
            return r.getGeoCountry().contains(request.getGeoCountry());
        }
    }

    class AppCategoryMatcher implements Matcher {
        public boolean match(Rule r, BidRequest request) {
            return r.getAppCategory().contains(request.getAppCategory());
        }
    }

    class DeviceModelMatcher implements Matcher {
        public boolean match(Rule r, BidRequest request) {
            return r.getDeviceModel().contains(request.getDeviceModel());
        }
    }

    class HourOfDayMatcher implements Matcher {
        public boolean match(Rule r, BidRequest request) {
            Timestamp ts = new Timestamp(request.getEventTimestamp());
            Date date = new Date(ts.getTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            return r.getHourOfDay().contains(hourOfDay);
        }
    }

    class LocationMatcher implements Matcher {
        public boolean match(Rule r, BidRequest request) {
            // same location
            if (Double.compare(request.getLatitude(), r.getLatitude())== 0 &&
                    Double.compare(request.getLongitude(), r.getLongitude())== 0) {
                return true;
            }
            // check if requested location is within the radius of the rule
            double theta = request.getLongitude() - r.getLongitude();
            double dist = Math.sin(Math.toRadians(request.getLatitude())) * Math.sin(Math.toRadians(r.getLatitude())) +
                    Math.cos(Math.toRadians(request.getLatitude())) * Math.cos(Math.toRadians(r.getLatitude())) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1.609344;
            return Double.compare(dist, r.getRadius()) <= 0;
        }
    }

    class Worker extends Thread {

        private List<Matcher> matcherList = new ArrayList<>();
        private List<Rule> ruleSet = new ArrayList<>();

        public Worker(List<Rule> ruleSet, List<Matcher> matcherList) {
            this.ruleSet = ruleSet;
        }

        @Override
        public void run()  {
            try {
                while (!isDone) {
                    BidRequest request = queue.take();
                    for (Rule r : ruleSet) {
                        int matches = 0;
                        for (Matcher m : matcherList) {
                            if (m.match(r, request)) {
                                matches++;
                            } else {
                                break;
                            }
                        }
                        if (matches == matcherList.size()) {
                            System.out.println("BidRequest" + request.getId() + " matches " + r.getId());
                            break;
                        }
                    }
                }
            }
            catch (Exception e) {
               System.out.println("Thread "+Thread.currentThread().getId()+" stopped");
            }
        }
    };


    public void addRule(Gender userGender, Set<String> geoCountry, double lat,
                        double lon, double radius, Set<String> appCategory,
                        Set<String> deviceModel, Set<Integer> hourOfDay) {
        String ruleId = ruleIdPrefix + (ruleIdSeq++);
        Rule rule = new Rule(ruleId, userGender,  geoCountry,  lat,
                 lon, radius, appCategory, deviceModel,  hourOfDay);
        rules.add(rule);
    }

    public void bidRequest(BidRequest request) {
        queue.add(request);
    }

    // Test Engine with sample rules and sample Bid Requests
    public static void main(String[] args) throws Exception {
        RuleEngine engine = new RuleEngine();

        Set<String> geoCountry = new HashSet<>();
        geoCountry.add("JPN");
        Set<String> deviceModel = new HashSet<>();
        deviceModel.add("iPhone");
        Set<String> appCategory = new HashSet<>();
        appCategory.add("IAB3,utilities");
        Set<Integer> hourOfDay = new HashSet<>();
        hourOfDay.add(6);

        engine.addRule(Gender.valueOf("m"), geoCountry, 34.79,138.86,5,
                appCategory, deviceModel, hourOfDay);
        engine.addRule(Gender.valueOf("f"), geoCountry, 34.79,138.86,5,
                appCategory, deviceModel, hourOfDay);

        engine.start();

        BidRequest request = BidRequest.parseBidRequest(
                new File("src/resources/BidRequest_1.json"));

        engine.bidRequest(request);

        engine.stop();
    }

}
