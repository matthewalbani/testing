package com.squareup.testing.methodslowteststests;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/cucumber1.feature")
public class Cucumber1Test {
}
