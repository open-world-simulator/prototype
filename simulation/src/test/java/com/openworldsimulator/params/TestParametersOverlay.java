package com.openworldsimulator.params;

import com.openworldsimulator.demographics.DemographicParams;
import com.openworldsimulator.economics.EconomyParams;
import com.openworldsimulator.simulation.ModelParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

public class TestParametersOverlay {

    public static class TestParams extends ModelParameters {
        public String STR_1 = null;

        public double DB_1 = 80;
        public double DB_2 = 10;

    }

    @Test
    public void testLoadParameterValues() {
        TestParams params = new TestParams();

        params.loadParameterValues(
                null,
                null
        );

        Assert.assertEquals(null, params.STR_1);
        Assert.assertEquals(80, params.DB_1, 0.001);
        Assert.assertEquals(10, params.DB_2, 0.001);

        ///////////////////////////////////////////////////////////////////
        params = new TestParams();
        Properties defaults = new Properties();
        defaults.put("X", 20);
        defaults.put("STR_1", "Z");
        defaults.put("DB_1", "90");

        params.loadParameterValues(
                defaults,
                null
        );

        Assert.assertEquals("Z", params.STR_1);
        Assert.assertEquals(90, params.DB_1, 0.001);
        Assert.assertEquals(10, params.DB_2, 0.001);

        ///////////////////////////////////////////////////////////////////
        params = new TestParams();
        defaults = new Properties();
        Properties additional = new Properties();
        defaults.put("X", 20);
        defaults.put("STR_1", "Z");
        defaults.put("DB_1", "90");

        additional.put("X", 30);
        additional.put("STR_1", "ZZ");
        additional.put("DB_1", "900");

        params.loadParameterValues(
                defaults,
                additional
        );
        Assert.assertEquals("ZZ", params.STR_1);
        Assert.assertEquals(900, params.DB_1, 0.001);
        Assert.assertEquals(10, params.DB_2, 0.001);
    }

    @Test
    public void testParametersNames() {
        TestParams params = new TestParams();

        List<String> names = params.getParameterNames();

        Assert.assertEquals(3, names.size());
        Assert.assertTrue(names.contains("STR_1"));
        Assert.assertTrue(names.contains("DB_1"));
        Assert.assertTrue(names.contains("DB_2"));


        Assert.assertEquals(80D, params.getParameterValueDouble("DB_1"), 0.001);
        Assert.assertEquals(null, params.getParameterValueString("STR_1"));

        params.STR_1 = "aa";
        Assert.assertEquals("aa", params.getParameterValueString("STR_1"));
    }

    @Test
    public void testComplexParameters() {
        EconomyParams params = new EconomyParams();

        DemographicParams demographicParams = new DemographicParams();

        Properties properties = new Properties();
        properties.setProperty("INITIAL_DEMOGRAPHY_DATA_COUNTRY", "Spain");
        properties.setProperty("MATERNITY_AGE_MEAN", "99");
        properties.setProperty("TAX_ON_DISCRETIONARY_CONSUMPTION_RATE", "99");
        properties.setProperty("MINIMAL_MONTHLY_WAGE", "1000");
        properties.setProperty("MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN", "1000");

        params.loadParameterValues(
                properties, null
        );


        demographicParams.loadParameterValues(properties, null);

        Assert.assertEquals("Spain", demographicParams.INITIAL_DEMOGRAPHY_DATA_COUNTRY);
        Assert.assertEquals(1000, params.MINIMAL_MONTHLY_WAGE, 1);
        Assert.assertEquals(99, params.TAX_ON_DISCRETIONARY_CONSUMPTION_RATE, 1);
        Assert.assertEquals(1000, params.MONTHLY_NON_DISCRETIONARY_EXPENSES_MIN, 1);
    }
}
