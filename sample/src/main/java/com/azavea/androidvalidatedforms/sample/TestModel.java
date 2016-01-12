package com.azavea.androidvalidatedforms.sample;

import java.util.Date;

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

    public Date When;
}
