package hudson.plugins.tfs;

import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Setup/teardown of the TFS server configured by the {@code tfs_server_name} property,
 * will create the necessary structure in source control.
 */
@Documented
@JenkinsRecipe(EndToEndTfs.RunnerImpl.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface EndToEndTfs {

    class RunnerImpl extends JenkinsRecipe.Runner<EndToEndTfs>  {

        @Override
        public void setup(JenkinsRule jenkinsRule, EndToEndTfs recipe) throws Exception {
        }

        @Override
        public void decorateHome(JenkinsRule jenkinsRule, File home) throws Exception {
        }

        @Override
        public void tearDown(JenkinsRule jenkinsRule, EndToEndTfs recipe) throws Exception {
        }
    }
}
