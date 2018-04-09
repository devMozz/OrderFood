package com.example.milymozz.orderfood.Model;

import java.util.List;

/**
 * Created by milymozz on 2018. 2. 14..
 */

public class MyResponse  {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;

}
