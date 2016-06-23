package com.azavea.androidvalidatedforms.sample;

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * Test model class with some random constraints.
 *
 * Created by kathrynkillebrew on 12/28/15.
 */
public class TestModel {

    @Size(min=2, max=10)
    public String FirstName;

    @Size(min=4, max=30)
    public String LastName;

    public String FavoriteColor;

    public String Truthiness;

    public Double HowMany;

    public Date When;

    public ImageHolder Pic;

    public Set<String> Options;
}
