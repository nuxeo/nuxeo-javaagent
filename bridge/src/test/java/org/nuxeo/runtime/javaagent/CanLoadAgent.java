package org.nuxeo.runtime.javaagent;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.runtime.javaagent.LoadAgentRule.InjectSizer;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

@RunWith(FeaturesRunner.class)
@Features(RuntimeFeature.class)
public class CanLoadAgent {

    @Rule
    public LoadAgentRule agentRule = new LoadAgentRule();

    @InjectSizer
    protected ObjectSizer sizer;

    @Before
    public void tryLoad() {
        Assert.assertThat(sizer, Matchers.notNullValue());
    }

    @Test
    public void tryDeepSizeOf() {

    }
}
