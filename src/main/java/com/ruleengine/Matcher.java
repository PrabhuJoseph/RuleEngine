package com.ruleengine;

public interface Matcher {

    abstract public boolean match(Rule r, BidRequest request);

}
